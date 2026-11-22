# Research — 002-observability-audit (Phase 0)

> Resolve todos os NEEDS CLARIFICATION do spec e do plan. Formato: Decisão · Rationale · Alternativas.

## R1 — Tabela de auditoria: nova tabela vs. reutilizar existente

**Decision**: Criar tabela nova `auditoria_seguranca` via Flyway `V3__auditoria_seguranca.sql`, sem alterar tabelas existentes (`usuario`, `perfil`, etc.).

**Rationale**: Menor risco de regressão; a tabela de auditoria é independente do domínio de negócio; permite evoluir o schema de auditoria sem tocar em entidades de produção. A FK `usuario_id` é opcional (endpoints públicos não têm usuário autenticado).

**Alternatives considered**:
- *Adicionar colunas em tabelas existentes*: mais invasivo, risco de quebrar queries existentes, acopla auditoria ao domínio.
- *Usar tabela genérica de log (sem schema fixo)*: perde tipagem e consultas estruturadas por ação/recurso/usuário.

## R2 — Onde gera o correlation-id

**Decision**: `CorrelationIdFilter` (`OncePerRequestFilter`) registrado **antes** do `RateLimitFilter` no `SecurityConfig`. Lê header `X-Correlation-Id` do request; se ausente, gera `UUID.randomUUID()`. Coloca no MDC (`MDC.put("correlationId", ...)`) e adiciona header `X-Correlation-Id` na resposta.

**Rationale**: Filtro é o ponto mais cedo na cadeia de filtros do Spring Security; garante que todo filtro posterior (incluindo o RateLimitFilter e o filter de autenticação JWT) veja o correlation-id no MDC. Registrar antes do rate limit também garante que requisições bloqueadas por rate limit tenham correlation-id para rastreamento.

**Alternatives considered**:
- *Interceptor de controller*: muito tarde na cadeia; não cobre filtros de segurança.
- *HandlerInterceptor*: não tem acesso ao `HttpServletResponse` para adicionar header de resposta tão cedo.

## R3 — Logging estruturado: qual biblioteca

**Decision**: Logback com encoder JSON (`ch.qos.logback.ext.spring.json.JsonLayout` ou `net.logstash.logback.encoder.LogstashEncoder`), configurado via `logback-spring.xml`. Campos: `timestamp`, `level`, `logger`, `message`, `correlationId` (do MDC), `usuarioId` (opcional, do MDC), `metodo`, `caminho`, `status`, `tempoMs`.

**Rationale**: Spring Boot já inclui `spring-boot-starter-logging` (Logback) como dependência transitiva; não há dependência nova a adicionar. Configuração XML é suficiente para o ciclo.

**Alternatives considered**:
- *Logstash encoder (`net.logstash.logback`)*: dependência nova Maven; mais recursos (tags, campos customizados) mas overkill para o ciclo.
- *SLF4J puro (texto livre)*: não atende o requisito de logging estruturado (US2).

## R4 — Onde grava o audit log

**Decision**: Aspecto Spring (`@Aspect`, `@Around`) em `shared/security/AuditoriaAspect.java`, aplicado a controllers que expõem dado pessoal (`PerfilController`, `CurriculoController`, `InscricaoController`, `RevisaoController`). Extrai `usuario_id` do `SecurityContext` (claim `sub` do JWT), ação, recurso, IP, correlation-id do MDC. Grava em `AuditoriaSegurancaRepository` com **falha-segura** (try/catch; se falhar, não derruba a requisição principal — loga erro em arquivo).

**Rationale**: Aspecto centraliza a regra de auditoria sem poluir controllers; falha-segura protege a disponibilidade da API principal se o banco de auditoria estiver indisponível.

**Alternatives considered**:
- *Gravar diretamente em cada controller*: regra espalhada, fácil de esquecer em algum endpoint.
- *Interceptor de controller*: não tem acesso tão limpo ao `SecurityContext` para extrair o usuário autenticado.
- *Gravar de forma síncrona no banco sem fallback*: risco de indisponibilidade da API se o banco de auditoria cair.

## R5 — O que fica no audit log (privacidade)

**Decision**: Apenas metadados: `usuario_id`, `acao`, `recurso`, `ip`, `data_hora`, `correlation_id`. **Nunca** o corpo da requisição/resposta, senha, token ou dados pessoais do currículo/perfil.

**Rationale**: Constituição II (Privacidade & LGPD primeiro) — o audit log não deve se tornar um repositório de dados sensíveis. Se precisar de mais detalhes, o correlation-id permite correlação com logs de aplicação (que também não devem conter dados sensíveis).

**Alternatives considered**:
- *Gravar corpo da requisição*: risco de vazar senha, dados de currículo, etc. Rejeitado.
- *Gravar corpo com máscara*: complexo, fácil de escapar algum campo. Rejeitado.

## R6 — Actuator hardening em prod

**Decision**: `management.endpoints.web.exposure.include=health` apenas em `application-prod.properties`; `/actuator/health` permanece público; todos os demais endpoints do actuator (`metrics`, `info`, `beans`, etc.) não são expostos em prod. Em dev/default, mantém o comportamento atual (`health,info`).

**Rationale**: Minimiza superfície de ataque em prod sem quebrar o monitoramento básico (health). O `info` pode conter dados de build/ambiente que não devem ser públicos.

**Alternatives considered**:
- *Expor todos com auth*: mais complexo (requer configurar auth do actuator); overkill para o ciclo.
- *Desabilitar actuator completamente em prod*: perde health check para load balancer/monitoramento.

## R7 — Supply-chain CI: quais ferramentas

**Decision**: 
- `mvnw dependency:audit` (Maven) — verifica vulnerabilidades conhecidas em dependências diretas.
- `gitleaks` (GitHub Action `zricethezav/gitleaks-action`) — varre histórico do git em busca de secrets.
- Dependabot (`.github/dependabot.yml`) — ecossistema Maven (Java), cria PRs para dependências desatualizadas.

**Rationale**: Ferramentas gratuitas, nativas do ecossistema Java/GitHub, baixo custo de manutenção, atendem o requisito de supply-chain do ciclo.

**Alternatives considered**:
- *Snyk / SonarQube*: mais recursos, mas exigem conta externa/configuração mais complexa; overkill para o ciclo.
- *OWASP Dependency-Check*: poderoso, mas mais pesado de configurar no CI.

## R8 — Contrato OpenAPI vs `ContratoOpenApiTest`

**Decision**: Novo path `GET /api/v1/auditoria` com tag `Auditoria` e `security: [{bearerAuth: []}]` no `openapi.yaml`. Teste atualizado: `escopoAgeBlind == 20` (era 19); `specPaths.containsAll(runtimePaths)` preservado; `/auth/**` continua fora do escopo AgeBlind.

**Rationale**: Preserve a trava de negócio (contagem exata de paths AgeBlind) e ao mesmo tempo não tem rota órfã. O YAML é atualizado no mesmo commit do teste.

**Alternatives considered**:
- *Não expor /auditoria no springdoc*: esconde endpoint legítimo da spec.
- *Mudar escopo para 21 ou mais*: quebra o contrato documentado sem justificativa.

## Pendências resolvidas do spec

| NEEDS CLARIFICATION / edge case | Resolução |
|---|---|
| Onde grava o audit log? | R4 — Aspecto Spring com falha-segura |
| O que fica no audit log? | R5 — apenas metadados, nunca corpo/senha/token |
| Actuator endpoints em prod? | R6 — só health público; outros desabilitados |
| Logging JSON — qual biblioteca? | R3 — Logback (já presente no Spring Boot) |
| Trava de 19 paths | R8 — passa para 20 paths no openapi.yaml + teste atualizado |
| Tabela nova ou reutilizar? | R1 — tabela nova `auditoria_seguranca` via `V3__` |
