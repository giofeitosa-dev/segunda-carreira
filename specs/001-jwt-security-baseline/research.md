# Research — 001-jwt-security-baseline (Phase 0)

> Resolve todos os NEEDS CLARIFICATION do spec e do plan. Formato: Decisão · Rationale · Alternativas.

## R1 — Biblioteca/mecanismo JWT

**Decision**: `spring-boot-starter-oauth2-resource-server` (Spring Security OAuth2 Resource Server + Nimbus JOSE JWT) com chave simétrica HS256 (`spring.security.oauth2.resourceserver.jwt.secret`).

**Rationale**: Já usamos `spring-boot-starter-security`; o resource server é o caminho suportado oficialmente, integra com `authorizeHttpRequests`, `JwtAuthenticationConverter` (papel→`ROLE_*`) e tem post-processor `jwt()` no `spring-security-test` (já no pom) que acelera a migração dos 74 testes. Zero dependência de terceiros nova além do starter.

**Alternatives considered**:
- *jjwt (io.jsonwebtoken)*: mais código manual (parser + filter custom), mesmo resultado; rejeitado por duplicar o que o resource server já faz.
- *auth0 java-jwt*: idem, código manual.
- *Session + form login*: stateless viola o contrato de API; front precisaria de cookie/CSRF.

## R2 — Rate limiting

**Decision**: `OncePerRequestFilter` próprio (`RateLimitFilter`) aplicado só a `/api/v1/auth/**`, contador janela fixa em memória (`ConcurrentHashMap<String, janela>`), limite via `app.ratelimit.auth.limite` (default 10) e janela `app.ratelimit.auth.janela-segundos` (default 60), resposta `429` problem+json.

**Rationale**: single-instance (demo/docker), Constituição V (YAGNI) — sem Redis, sem dependência nova. Escopo restrito a `/auth` atende o US4.5 (força bruta no login).

**Alternatives considered**:
- *Bucket4j*: poderoso, mas exige config/plano de liberação; overkill p/ 1 filtro.
- *resilience4j*: foco em circuit-breaker/bulkhead; rate limit é tangencial.
- *Spring Security built-in*: não tem rate limit nativo.

## R3 — Vínculo Usuario ↔ Perfil

**Decision**: coluna **`usuario.perfil_id` FK nullable** (1:1 opcional) em `V2__usuarios.sql`. O token carrega apenas `sub` (usuarioId), `email`, `papel`; a posse é resolvida carregando `Usuario` do banco (`perfilId`) — fonte de verdade sempre atual, sem claim obsoleta no token.

**Rationale**: spec diz "vínculo opcional com Perfil"; FK em `usuario` evita tocar na tabela `perfil` existente (menor diff de migração); resolver posse via banco evita problema de token antigo apontando para perfil trocado.

**Alternatives considered**:
- *claim `perfilId` no JWT*: token desatualizado se o vínculo mudar; rejeitado.
- *`perfil.usuario_id`*: exigiria ALTER na tabela core + backfill dos seeds; mais risco, mesmo resultado.

## R4 — Autorização do webhook `/webhooks/lembretes`

**Decision**: exige **CANDIDATO dono** (a inscrição referenciada deve pertencer ao `perfilId` do usuário logado) ou RH. O `LembreteJob` NÃO passa por HTTP (roda in-processo via `@Scheduled`, `LembreteJob.java:33-71`), então não precisa de credencial de serviço — só o endpoint HTTP é protegido.

**Rationale**: o endpoint é chamado pelo front (`carreiraService.js:63`), não pelo job; é dado pessoal (agenda lembrete de inscrição); nunca fica `permitAll` silencioso (spec US2.7).

**Alternatives considered**: *service token interno* — só seria necessário se o job fizesse chamada HTTP; não faz.

## R5 — Onde vive a checagem de posse

**Decision**: componente `PosseChecker` em `shared/security` com métodos `donoOuRh(Long perfilIdDoRecurso)` (lê `UsuarioAtual` do `SecurityContext` → carrega `Usuario` → compara `perfilId`) chamado **nos services** (`PerfilService`, `InscricaoService`, `CurriculoService`, `RevisaoSolicitacaoService`) antes de ler/escrever. Papel RH bypassa (regra centralizada, não espalhada).

**Rationale**: services já são o centro de regra de negócio; teste unitário do checker é barato; evita anotação custom + AOP (mais infra). `UsuarioAtual` extrai `sub` do `Jwt` autenticado.

**Alternatives considered**:
- *`@PreAuthorize("hasRole...")` por endpoint*: cobre papel, mas não posse (depende de dado da URL/body) — precisaria de método custom de qualquer forma.
- *Interceptor/filter por path*: regra por URL não conhece o dono do recurso.

## R6 — Migração dos 74 testes HTTP

**Decision**: helper de teste `JwtTestSupport` (test sources) expondo `jwtCandidato(perfilId)` e `jwtRh()` que devolvem `RequestPostProcessor` via `SecurityMockMvcRequestPostProcessors.jwt()` com claims `sub`/`papel`. Testes de posse usam fixture que cria `Usuario`+`Perfil` vinculados. Os 58 testes abertos ganham `.with(jwtCandidato(...))` adequado; `RhProtegidoTest` é reescrito (senhas → tokens, asserts `WWW-Authenticate: Bearer`).

**Rationale**: `spring-security-test` já está no pom; post-processor simula o filtro real sem HMAC manual; migração mecânica e verificável por arquivo.

**Alternatives considered**: *tokens HS256 reais gerados no teste* — valida mais o decoder, mas acopla teste a secret/config; pode ser adicionado pontualmente em um teste de integração do decoder (1 teste), não em 74.

## R7 — Fail-fast do `JWT_SECRET` em prod

**Decision**: `JwtConfig` valida no startup (`@PostConstruct`/validação de bean): profile `prod` ativo exige `app.jwt.secret` definido, com tamanho mínimo (≥32 chars) e **diferente dos defaults demo**; caso contrário `IllegalStateException` com mensagem clara. Profile demo/teste aceita default conhecido.

**Rationale**: Constituição I (falha-segura); validação no boot é cedo o bastante para não subir vulnerável.

**Alternatives considered**: *validação em CI* — não protege deploys manuais; *EnvironmentPostProcessor* — mais complexo sem ganho.

## R8 — Swagger/H2 em prod

**Decision**: `application-prod.properties`: `springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false` (H2 já está `console.enabled=false` — `application-prod.properties:12`). SecurityConfig mantém `permitAll` nos paths de docs (inócuos quando desabilitados → 404).

**Rationale**: superfície de debug fechada fora de dev sem mexer em regras de auth; mudança só de propriedade.

**Alternatives**: *negar no SecurityConfig por profile* — mesmo efeito, mais código; propriedade é o mecanismo nativo do springdoc.

## R9 — CORS

**Decision**: bean `CorsConfigurationSource` único: allowlist de `app.cors.allowed-origins` (env `CORS_ALLOWED_ORIGINS`, default `http://localhost:3000,http://localhost:5173` = front nginx e Vite dev), methods `GET,POST,PUT,DELETE,OPTIONS`, headers `Authorization,Content-Type`, `maxAge=3600`.

**Rationale**: allowlist explícita via env (Constituição I); dev local e compose funcionam sem mudança; origem fora da lista não passa.

**Alternatives**: *`allowedOriginPatterns(*)`* — perigoso p/ demo exposto; *sem CORS* — o proxy `/api` do nginx/vite já oculta em demo, mas chamadas diretas ao :8080 ficariam inconsistentes e o spec pede (US4.4).

## R10 — Contrato OpenAPI vs `ContratoOpenApiTest`

**Decision**: dois paths novos (`POST /api/v1/auth/cadastro`, `POST /api/v1/auth/login`) no `openapi.yaml` com **tag `Auth`** (fora de Carreira/Escudo) e `security: []` (públicos). Teste atualizado: asserção de 19 paths do escopo AgeBlind mantida; `specPaths.containsAll(runtimePaths)` passa a incluir `/auth` (por isso o YAML é atualizado no mesmo commit); `escopoAgeBlind == runtimePaths` vira `escopoAgeBlind == runtimePaths − {paths com tag Auth}` (runtime filtrado por paths que não são `/auth`).

**Rationale**: preserva a trava de negócio (19 paths Carreira/Escudo) e ao mesmo tempo não tem rota órfã — exatamente o que o teste garante (linhas 86-92).

**Alternatives**: *não expor /auth no springdoc* — esconde endpoint legítimo da spec; *mudar escopo para 21* — quebra o contrato documentado "19 endpoints AgeBlind".

## R11 — E2E sem `httpCredentials`

**Decision**: `smoke.spec.js` obtém token via `request` do Playwright (`POST {base}/api/v1/auth/login` com credenciais seed) e o injeta em `localStorage` (chave definida pelo `AuthContext`, ex. `ageblind.token`) antes de navegar; cenário de senha errada usa a API diretamente. Login via UI pode vir como cenário extra (não bloqueante).

**Rationale**: E2E valida o fluxo real de token; não depende de digitação em formulário para todos os testes; remove o acoplamento `httpCredentials` (spec US5.4).

**Alternatives**: *login por formulário em todo teste* — mais frágil/slow; manter `httpCredentials` — incompatível com JWT.

## R12 — Seed de usuários

**Decision**: `UsuarioSeedRunner` (`@Profile("seed")`) cria: `candidato@demo.com` / senha demo (env `SEED_CANDIDATO_SENHA`, default `demo123`) papel CANDIDATO **vinculado ao primeiro perfil do seed**; `rh@demo.com` papel RH (senha via `RH_PASSWORD` existente). Cadastro público só cria CANDIDATO (FR-013, aprovado no GATE 2). Migração `V2__` cria a tabela; dados são do runner (consistente com padrão `PerfilSeedRunner`).

**Rationale**: mantém demo funcional; RH in-memory sai de cena; senhas de demo nunca em prod (fail-fast R7 cobre secret, senha seed só roda com profile `seed`).

**Alternatives**: *usuarios no CSV* — menos flexível p/ BCrypt/vínculo; *seed só na migração* — Flyway não deve conter hashes variáveis por env.

## R13 — Expiração/refresh

**Decision**: access token TTL 30min (`JWT_EXPIRATION=1800000` ms via env), **sem refresh token** (YAGNI — Constituição V; front trata 401 → redirect login, US3.4).

**Alternatives**: *refresh token rotativo* — necessidade real só com sessões longas em produção multi-usuário; fica para spec futura.

## R14 — Armazenamento do token no front

**Decision**: `localStorage` (chave `ageblind.token`) + injeção no `request()` de `api.js` + interceptor de 401 → limpar e redirecionar `/login`.

**Rationale**: persistência entre refreshes sem backend de sessão; XSS é risco aceito e mitigado por escopo de dados (demo); spec US3 pede persistência implícita (redirect após reload).

**Alternatives**: *sessionStorage* — perde login ao fechar aba (pior UX, mesmo risco XSS); *cookie httpOnly* — exige endpoint de cookie/CSRF, mais backend (fora do escopo YAGNI deste ciclo).

## Pendências resolvidas do spec

| NEEDS CLARIFICATION / edge case | Resolução |
|---|---|
| FR-013 cadastro público só CANDIDATO | ✅ aprovado no GATE 2; RH só via seed (R12) |
| US2.6 usuário sem Perfil linkado | `POST /perfis` por CANDIDATO sem vínculo **vincula o perfil criado** ao usuário (auto-vínculo no `PerfilService.criar`); acesso a perfil alheio → 403 |
| US2.7 webhook | R4 — CANDIDATO dono ou RH |
| Ordem de migração dos testes | R6 — helper `JwtTestSupport`, commit atômico C1.4 |
| Trava de 19 paths | R10 — tag `Auth` + ajuste do teste |
