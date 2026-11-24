# Feature Specification: Observability, Audit Log e Supply-Chain (002-observability-audit)

**Feature Branch**: `002-observability-audit`

**Created**: 2026-11-18

**Status**: Aprovado (2026-11-22)

**Input**: User description: "Audit log de segurança, correlation-id/logging estruturado, actuator hardening, supply-chain CI (Dependabot, dependency audit, gitleaks)".

**Constitution**: [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (v1.0.0) — princípios I (Security-by-default), III (API-first), IV (Test-first), V (YAGNI).

## Contexto

O ciclo 001 implementou autenticação/autorização JWT com matriz de acesso por papel+posse, hardening básico (rate limit, CORS, headers, Swagger off em prod, fail-fast do JWT_SECRET). A API está segura por default, mas falta:

- **Audit log**: não há rastreamento de quem acessou/alterou dado pessoal (perfil, currículo, inscrição, revisão) — requisito de accountability para segurança e LGPD.
- **Correlation-id**: cada request não tem um identificador de rastreamento que permita correlacionar logs entre camadas.
- **Logging estruturado**: logs ainda são texto livre (SLF4J padrão), não facilitam análise em ferramentas de observabilidade.
- **Actuator**: só `/actuator/health` e `/info` estão expostos, sem hardening explícito; falta restrição de acesso e métricas de segurança.
- **Supply-chain CI**: não há Dependabot, dependency audit nem gitleaks configurados no CI.

Este ciclo implementa observabilidade e auditoria de segurança, sem modificar o comportamento de negócio existente.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Audit log de segurança (Priority: P1) 🎯 MVP

Como **sistema de segurança**, quero que todo acesso a dado pessoal (perfil, currículo, inscrição, revisão) seja registrado em um log de auditoria imutável, para que eu possa rastrear quem fez o quê e quando.

**Why this priority**: É requisito de accountability em sistemas que lidam com dado pessoal (LGPD + boas práticas de segurança). Depende do ciclo 001 (token JWT existe para identificar o usuário).

**Independent Test**: `POST /api/v1/perfis` com token CANDIDATO → registra entrada em `auditoria_seguranca` (usuario_id, acao, recurso, ip, data_hora); `GET /api/v1/auditoria` (só RH) → lista entradas.

**Acceptance Scenarios**:

1. **Given** usuário CANDIDATO autenticado, **When** cria/atualiza seu próprio perfil, **Then** `auditoria_seguranca` recebe registro com usuario_id, ação "PERFIL_CRIADO"/"PERFIL_ATUALIZADO", recurso "/api/v1/perfis/{id}", IP e timestamp.
2. **Given** usuário CANDIDATO autenticado, **When** acessa perfil de outro usuário, **Then** `auditoria_seguranca` registra tentativa de acesso negado (403).
3. **Given** usuário RH autenticado, **When** acessa `GET /api/v1/auditoria`, **Then** `200` com lista de entradas de auditoria.
4. **Given** usuário CANDIDATO, **When** acessa `GET /api/v1/auditoria`, **Then** `403` (apenas RH lê auditoria).
5. **Given** endpoint público (ex.: `GET /api/v1/vagas`), **When** acessado sem token, **Then** não gera registro em auditoria (sem usuario_id, ou registro com usuario_id null).

---

### User Story 2 - Correlation-id e logging estruturado (Priority: P1)

Como **operador**, quero que cada requisição tenha um correlation-id propagado e logs em JSON estruturado, para que eu possa rastrear o fluxo de uma requisição entre camadas e correlacionar com o audit log.

**Why this priority**: É fundação para observabilidade operacional; permite debugar incidentes de segurança correlacionando logs de aplicação com o audit log.

**Independent Test**: requisição com header `X-Correlation-Id` → resposta inclui o mesmo `X-Correlation-Id`; logs em JSON contêm `correlationId`, `usuarioId`, `acao`, `recurso`, `ip`, `dataHora`.

**Acceptance Scenarios**:

1. **Given** requisição com header `X-Correlation-Id: abc-123`, **When** processada, **Then** resposta tem header `X-Correlation-Id: abc-123` e o registro de auditoria usa esse ID.
2. **Given** requisição sem header `X-Correlation-Id`, **When** processada, **Then** sistema gera um UUID e o inclui na resposta.
3. **Given** qualquer requisição, **When** log é emitido, **Then** formato é JSON com campos estruturados (timestamp, nível, logger, mensagem, correlationId, usuarioId, metodo, caminho, status, tempoMs).

---

### User Story 3 - Actuator hardening (Priority: P2)

Como **operador**, quero que endpoints do actuator sejam expostos de forma controlada (sem expor métricas sensíveis sem auth), para que o monitoramento funcione sem aumentar a superfície de ataque.

**Why this priority**: O ciclo 001 desabilitou Swagger/H2 em prod; o actuator precisa do mesmo tratamento — só health/info públicos, outros endpoints requerem auth ou são desabilitados em prod.

**Independent Test**: `GET /actuator/health` → `200` público; `GET /actuator/metrics` → `401`/`403` em prod; em dev pode ser acessível.

**Acceptance Scenarios**:

1. **Given** profile `prod`, **When** `GET /actuator/metrics`, **Then** `401` ou `404` (não exposto sem auth).
2. **Given** profile `prod`, **When** `GET /actuator/health`, **Then** `200` (público, sem dados sensíveis).
3. **Given** profile `dev`/default, **When** `GET /actuator/metrics`, **Then** acessível (dev pode usar para debug).

---

### User Story 4 - Supply-chain CI (Priority: P2)

Como **mantenedor**, quero que o CI verifique dependências vulneráveis, secrets vazados e bibliotecas desatualizadas, para que eu possa detectar riscos de supply-chain antes do merge.

**Why this priority**: Protege contra ataques de supply-chain e credenciais vazadas no histórico do git.

**Independent Test**: CI roda `dependency:audit` (Maven), `gitleaks` (histórico), Dependabot (GitHub Actions) — nenhum falha na branch main.

**Acceptance Scenarios**:

1. **Given** CI na branch main, **When** `dependency:audit` é executado, **Then** não há vulnerabilidades conhecidas em dependências diretas.
2. **Given** CI na branch main, **Quando** `gitleaks` é executado, **Then** nenhum secret é encontrado no histórico.
3. **Given** repositório no GitHub, **Quando** Dependabot está ativo, **Then** PRs são criados automaticamente para dependências desatualizadas.

---

### Edge Cases

- Audit log não deve vazar senha ou dados sensíveis do corpo da requisição (só metadados: usuario_id, ação, recurso, IP, timestamp).
- Correlation-id deve ser gerado mesmo quando o cliente não enviar, para não quebrar rastreamento.
- Endpoints de auditoria só acessíveis por RH (matriz do ciclo 001 preservada).
- Se o banco de auditoria estiver indisponível, a aplicação principal não deve falhar (falha-segura: log em arquivo como fallback).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST registrar em `auditoria_seguranca` todo acesso (sucesso e falha) a endpoints que expõem dado pessoal (perfil, currículo, inscrição, revisão), com usuario_id, ação, recurso, IP, timestamp e correlation-id.
- **FR-002**: Sistema MUST aceitar header `X-Correlation-Id` nas requisições e propagá-lo na resposta e nos logs.
- **FR-003**: Sistema MUST gerar correlation-id automaticamente quando ausente (UUID v4).
- **FR-004**: Sistema MUST emitir logs em JSON estruturado contendo pelo menos: timestamp, nível, logger, mensagem, correlationId, metodo, caminho, status, tempoMs.
- **FR-005**: Sistema MUST expor `/actuator/health` publicamente em prod; outros endpoints do actuator requerem autenticação ou são desabilitados em prod.
- **FR-006**: Sistema MUST ter endpoint `GET /api/v1/auditoria` acessível apenas por RH, retornando entradas de auditoria paginadas.
- **FR-007**: Cadastro/login (`/api/v1/auth/**`) e leitura pública (vagas, trilhas, mentorias) NÃO geram registro de auditoria de dado pessoal (ou registram com usuario_id null).
- **FR-008**: CI MUST executar `dependency:audit`, `gitleaks` e Dependabot na branch main.

### Key Entities

- **AuditoriaSeguranca**: id (PK), usuario_id (FK → usuario, nullable), acao (string), recurso (string), ip (string), data_hora (timestamp), correlation_id (string). Tabela nova via Flyway `V3__auditoria_seguranca.sql`.
- **CorrelationId**: gerado por filter/filter, propagado em ThreadLocal ou MDC, incluído nos logs e no header de resposta.

### Restrições de Stack

- Java 17 · Spring Boot 4.1.1 · Spring Data JPA · SpringDoc OpenAPI · Flyway.
- Banco: H2 (dev/teste) · PostgreSQL 16 (prod).
- Logging: SLF4J já presente; adicionar Logback JSON encoder (ou `spring-boot-starter-logging` com config JSON) — dependência nova justificada por observabilidade.
- Migrações: Flyway obrigatório (`V3__auditoria_seguranca.sql`).
- Idioma de código, docs, commits: PT-BR.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `./mvnw clean verify` verde — suíte existente + novos testes de auditoria/correlation-id/actuator.
- **SC-002**: Acesso a dado pessoal gera registro em `auditoria_seguranca` (coberto por teste de integração).
- **SC-003**: Header `X-Correlation-Id` é aceito, gerado se ausente, e aparece na resposta e nos logs (teste de integração).
- **SC-004**: Em profile `prod`, `/actuator/metrics` retorna 401/404; `/actuator/health` retorna 200.
- **SC-005**: CI executa `dependency:audit`, `gitleaks` e Dependabot está ativo no repositório.
- **SC-006**: `openapi.yaml` atualizado com endpoint `GET /api/v1/auditoria` (tag `Auditoria`) e `ContratoOpenApiTest` atualizado (escopo AgeBlind = 20 paths).

## Assumptions

- Audit log armazena apenas metadados (não o corpo da requisição/resposta) para não vazar dados sensíveis.
- Rate limit do ciclo 001 já protege contra força bruta; auditoria complementa com detecção pós-fato.
- Supply-chain CI atua no GitHub Actions (`.github/workflows/ci.yml` já existe para `mvnw verify`).
- Sem dependência de sistema externo de logs (ELK, Splunk) — logs JSON em stdout, suficientes para o ciclo.

## Pendências resolvidas do spec

| Decisão | Resolução |
|---|---|
| Tabela de auditoria nova ou reutilizar existente? | Tabela nova `auditoria_seguranca` via Flyway `V3__` — não altera tabelas existentes. |
| Onde gera o correlation-id? | `OncePerRequestFilter` antes do RateLimitFilter, lendo `X-Correlation-Id` ou gerando UUID. |
| Logs JSON — qual biblioteca? | Logback com encoder JSON (já vem com Spring Boot via `spring-boot-starter-logging`); config em `logback-spring.xml`. |
| Actuator endpoints sensíveis em prod? | Desabilitados ou requerem auth; só `/actuator/health` público. |

---
**Version**: 1.0.0 | **Status**: Aprovado — pronto para implementação (GATE spec liberado)
