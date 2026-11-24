# Tasks: Autenticação JWT e Baseline de Segurança (001-jwt-security-baseline)

**Input**: Design documents from `/specs/001-jwt-security-baseline/`

**Prerequisites**: plan.md ✅ · spec.md ✅ · research.md (R1–R14) ✅ · data-model.md ✅ · contracts/auth-api.md ✅ · quickstart.md ✅

**Tests**: OBRIGATÓRIOS — Constituição IV (Test-first) e FR-014 exigem; todos os grupos de teste abaixo são escritos **antes** da implementação do respectivo grupo (verificar falha, depois implementar).

**Organization**: Tarefas agrupadas por user story. Convenção do ROADMAP: 1 commit = 1 grupo = `./mvnw clean verify` verde (+ `npm test`/`npm run e2e` quando toca front). Mapeamento de commits ao final.

**Paths**: `ageblind-api/api/src/{main,test}/java/com/reconecta/ageblind/…` · `ageblind-front/src/…`

## Format: `[ID] [P?] [Story] Description`

---

## Phase 1: Setup (infraestrutura compartilhada)

**Purpose**: dependência, migração e entidade — sem mudança de comportamento (suite continua verde)

- [ ] T001 [P] [Setup] Adicionar `spring-boot-starter-oauth2-resource-server` em `ageblind-api/api/pom.xml`
- [ ] T002 [P] [Setup] Criar migração `ageblind-api/api/src/main/resources/db/migration/V2__usuarios.sql` (tabela `usuario` + FK `perfil_id` + índices — data-model.md)
- [ ] T003 [P] [Setup] Criar enum `Papel` (`CANDIDATO|RH|EMPRESA`) e entidade `Usuario` em `auth/domain/` + `UsuarioRepository` em `auth/repository/` (JPA, `@Table(name="usuario")`, unique em email)
- [ ] T004 [Setup] Criar `shared/config/JwtConfig.java`: `JwtDecoder`/`JwtEncoder` HS256 via `app.jwt.secret`/`app.jwt.expiration` (R1), `JwtAuthenticationConverter` mapeando claim `papel` → `ROLE_*`, validação fail-fast (R7: profile `prod` exige secret ≥32 chars não-default)
- [ ] T005 [Setup] Em `ageblind-api/api/src/main/resources/application.properties`: adicionar `app.jwt.secret` (default demo), `app.jwt.expiration=1800000`, `app.cors.allowed-origins`, `app.ratelimit.auth.limite/janela-segundos` via env; **manter** `app.rh.*` por enquanto (sai no C1.4)

**Checkpoint**: suite verde; nada de auth novo exposto ainda

---

## Phase 2: Foundational (bloqueante para todas as stories)

**Purpose**: nada mais avança sem a base da Phase 1 completa (T001–T005)

**⚠️ CRITICAL**: T001–T005 são pré-requisito de US1–US5

**Checkpoint**: fundação pronta — implementação das stories pode começar

---

## Phase 3: User Story 1 — Cadastro e login com JWT (Priority: P1) 🎯 MVP

**Goal**: `POST /auth/cadastro` e `POST /auth/login` funcionais com BCrypt + JWT HS256 (`sub`, `email`, `papel`, `exp`)

**Independent Test**: curl cadastro → 201; login → 200 `{token, expiresIn}`; senha errada → 401 problem+json idêntico p/ usuário inexistente; senha nunca em texto puro

### Tests for US1 (write first — expect FAIL)

- [ ] T006 [US1] `auth/api/AuthControllerTest.java`: cenários 1–7 da US1 (201 · 409 duplicado · 400 validação · 200 com claims decodificados · 401 mensagem única · 401 token adulterado/expirado com `WWW-Authenticate: Bearer`)

### Implementation for US1

- [ ] T007 [US1] `auth/service/AuthService.java`: cadastro (normaliza email, BCrypt, papel fixo CANDIDATO — FR-013) + login (verifica senha, emite JWT via `JwtEncoder`); erro 401 único p/ user inexistente/senha errada
- [ ] T008 [US1] `auth/api/AuthController.java` + DTOs (`CadastroRequest`, `LoginRequest`, `LoginResponse`) com Bean Validation; mapear email duplicado → 409 problem+json (ajuste pontual em `shared/api/GlobalExceptionHandler.java`)
- [ ] T009 [US1] `SecurityConfig.java`: adicionar `/api/v1/auth/**` ao `permitAll` (**aditivo** — Basic ainda presente, sai no C1.4)
- [ ] T010 [US1] `openapi.yaml`: `components.securitySchemes.bearerAuth` + paths `/api/v1/auth/cadastro` e `/api/v1/auth/login` (tag `Auth`, `security: []`, schemas conforme contracts/auth-api.md)
- [ ] T011 [US1] `shared/api/ContratoOpenApiTest.java`: ajustar R10 — `escopoAgeBlind == runtimePaths − {paths com tag Auth}`; manter `specPaths.containsAll(runtimePaths)` (YAML atualizado no mesmo commit)

**Checkpoint**: US1 testável isoladamente com curl; `mvnw verify` verde; contrato sem rota órfã

---

## Phase 4: User Story 1 (cont.) — Seed de usuários

- [ ] T012 [US1] `carreira/seed/UsuarioSeedRunner.java` (`@Profile("seed")`): cria `candidato@demo.com` (senha `SEED_CANDIDATO_SENHA` default `demo123`, vinculado ao primeiro perfil do seed) e `rh@demo.com` (senha `RH_PASSWORD`) — R12
- [ ] T013 [US1] Estender `carreira/seed/SeedDemoTest.java`: asserta que usuários seed existem com papel correto e perfil vinculado

**Checkpoint**: demo tem contas reais no banco; login manual funciona

---

## Phase 5: User Story 2 — Autorização por papel e posse (Priority: P1)

**Goal**: fecha os 29 endpoints abertos — matriz da spec implementada; posse via `Usuario.perfilId` (R3/R5)

**Independent Test**: token CANDIDATO-A em `GET /perfis/{idDeB}` → 403; dono → 200; CANDIDATO em `/funil/metricas` → 403; sem token em endpoint de dados → 401 `WWW-Authenticate: Bearer`; anônimo em `GET /vagas` → 200

### Tests for US2 (write first — expect FAIL)

- [ ] T014 [P] [US2] `shared/security/JwtTestSupport.java` (test sources): `jwtCandidato(perfilId)` e `jwtRh()` via `SecurityMockMvcRequestPostProcessors.jwt()` com claims `sub`/`papel` (R6)
- [ ] T015 [US2] `shared/api/AutorizacaoMatrizTest.java`: matriz completa da spec (401 anônimo em dados · 200 leitura pública · 403 papel · 403 posse A→B · RH ✅ · webhook dono/RH · CANDIDATO sem perfil → 403 documentado)
- [ ] T016 [US2] Reescrever `shared/api/RhProtegidoTest.java`: remover `httpBasic` → tokens `jwtRh()`; asserts `WWW-Authenticate: Bearer`; senha errada no login → 401 (cobre cenário antigo de credencial errada)

### Implementation for US2

- [ ] T017 [US2] `shared/security/UsuarioAtual.java` (extrai `sub` do `Jwt` autenticado, expõe `usuarioId`/`papel`) + `shared/security/PosseChecker.java` (`donoOuRh(perfilIdRecurso)` — R5)
- [ ] T018 [US2] Reescrever `SecurityConfig.java`: remover HTTP Basic + `InMemoryUserDetailsManager` (FR-007); `oauth2ResourceServer(jwt)`; matriz: `permitAll` (GET `vagas*`, `trilhas/**`, `mentorias`, `auth/**`, health/swagger/error-dev) · `hasRole("RH")` em `/api/v1/funil/**` + `POST /vagas/auditoria-linguagem` · `authenticated()` no resto · 401/403 problem+json com `WWW-Authenticate: Bearer realm="Segunda Carreira", error="invalid_token"` (FR-008)
- [ ] T019 [US2] Posse em `carreira/service/PerfilService.java` (+controller): `GET/PUT/DELETE /perfis/{id}`, currículo e transferíveis exigem dono ou RH; `POST /perfis` com CANDIDATO sem vínculo **auto-vincula** o perfil criado (US2.6/R12)
- [ ] T020 [US2] Posse em `carreira/service/InscricaoService.java`: `POST/GET/DELETE /inscricoes/**` (body/path `perfilId` == dono ou RH)
- [ ] T021 [US2] `POST /api/v1/webhooks/lembretes`: autenticado + inscrição pertence ao dono ou RH (R4) — nunca `permitAll`
- [ ] T022 [US2] Posse em `escudo` — `POST /revisao/solicitacao`: `perfilId` do body == dono ou RH
- [ ] T023 [US2] Migrar demais testes HTTP que passam a exigir token (fixtures com `jwtCandidato(...)`; `@WithMockUser(roles="RH")` continua válido onde só importa papel — revisar `LgpdRevisaoCaminhoTest`, `FunilMetricasControllerTest`, `VagaAuditoriaControllerTest` e trocar por `jwtRh()` para consistência) — FR-014
- [ ] T024 [US2] Remover de `application.properties` `app.rh.usuario`/`app.rh.senha` e de `docker-compose.yml` `RH_USER`/`RH_PASSWORD` (substituir por `JWT_SECRET` obrigatório em prod + vars de seed)

**Checkpoint**: SC-001/SC-002 verificados — zero endpoints de dado pessoal abertos; suíte migrada, verde

---

## Phase 6: User Story 3 — Front: login, Bearer e guarda de rota (Priority: P2)

**Goal**: tela de login, token persistido, header Bearer em todo request, `/rh/painel` protegido, 401 → redirect

**Independent Test**: `npm test` — contexto guarda/expõe token · `api.js` injeta `Authorization` · rota protegida sem token redireciona · E2E loga

### Tests for US3 (write first — expect FAIL)

- [ ] T025 [P] [US3] `ageblind-front/src/__tests__/AuthContext.test.jsx`: login persiste token em `localStorage` (`ageblind.token`), expõe estado, logout remove
- [ ] T026 [P] [US3] Atualizar `ageblind-front/src/__tests__/api.test.js`: header `Authorization: Bearer` presente quando há token; resposta 401 limpa token e redireciona `/login`
- [ ] T027 [US3] `ageblind-front/src/__tests__/RequireAuth.test.jsx`: sem token → redirect `/login`; com token → renderiza children

### Implementation for US3

- [ ] T028 [US3] `ageblind-front/src/context/AuthContext.jsx`: `login(email, senha)` → `POST /api/v1/auth/login`, persiste token (R14), `logout()`, estado global via Provider
- [ ] T029 [US3] `ageblind-front/src/services/api.js`: injeta `Authorization` no `request()` (linhas 14–20) + interceptor 401 → limpar token e redirect `/login` (US3.4)
- [ ] T030 [US3] `ageblind-front/src/pages/Login.jsx`: formulário email/senha, erro genérico (sem vazar detalhes — US3.6), redirect p/ destino original
- [ ] T031 [US3] `ageblind-front/src/components/RequireAuth.jsx` + rotas em `ageblind-front/src/App.jsx`: `/login` pública, `/rh/painel` envolvida em `RequireAuth` (US3.2)
- [ ] T032 [US3] Botão "Sair" no `ageblind-front/src/components/Layout.jsx` → `logout()` + redirect home (US3.5)

**Checkpoint**: painel RH alcançável só com login JWT; prompt nativo Basic some

---

## Phase 7: User Story 4 — Hardening baseline (Priority: P2)

**Goal**: fail-fast de secret, Swagger/H2 off em prod, headers, CORS allowlist, rate limit (US4.1–4.5)

**Independent Test**: prod sem `JWT_SECRET` → falha no boot · `/swagger-ui.html` em prod → 404 · headers presentes · origem fora da allowlist bloqueada · 11º login → 429

### Tests for US4 (write first — expect FAIL)

- [ ] T033 [P] [US4] `shared/config/RateLimitFilterTest.java`: 10 requisições passam, 11ª → 429 problem+json; contador zera após janela
- [ ] T034 [P] [US4] `shared/config/JwtConfigFailFastTest.java`: contexto com profile `prod` sem/secret-default → `IllegalStateException` na startup
- [ ] T035 [US4] `shared/config/CorsConfigTest.java`: preflight de origem allowlist → OK; origem fora → bloqueada

### Implementation for US4

- [ ] T036 [US4] `shared/config/RateLimitFilter.java` (`OncePerRequestFilter`, janela fixa em `ConcurrentHashMap`, só `/api/v1/auth/**`, 429 problem+json — R2) + registrar no `SecurityConfig`
- [ ] T037 [US4] `shared/config/CorsConfig.java`: `CorsConfigurationSource` com allowlist de `app.cors.allowed-origins` (R9)
- [ ] T038 [US4] Security headers em `SecurityConfig` (`headers`): `X-Content-Type-Options: nosniff`, `Referrer-Policy`, `X-Frame-Options`, HSTS quando HTTPS (US4.3)
- [ ] T039 [US4] `application-prod.properties`: `springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false` (R8; H2 já desligado)
- [ ] T040 [US4] `docker-compose.yml`: trocar `RH_USER`/`RH_PASSWORD` por `JWT_SECRET` (exigido), `SEED_*` — compose de demo sobe com secret gerado/documentado

**Checkpoint**: SC-004/SC-006 verificados

---

## Phase 8: User Story 5 — Contrato, testes e E2E (Priority: P2)

**Goal**: E2E sem `httpCredentials`, READMEs no fluxo JWT, validação final (Constituição III/IV)

**Independent Test**: `./mvnw clean verify` + `npm test` + `npm run e2e` verdes · `openapi.yaml` importa no Swagger Editor com `bearerAuth`

### Tests for US5

- [ ] T041 [US5] Reescrever `ageblind-front/e2e/smoke.spec.js`: login via `request` da API (token → `localStorage` antes de navegar), cenário RH autenticado, cenário 401/token errado; **remover todos** os `httpCredentials` (linhas 60,75 — R11)

### Implementation for US5

- [ ] T042 [US5] Cenário E2E de login pela UI (`/login` preenche e acessa painel) — US3.1 coberta ponta a ponta
- [ ] T043 [US5] Atualizar READMEs: `README.md` (linha 132 — credenciais Basic → fluxo JWT), `ageblind-api/api/README.md` (curls `-u rh:rh-demo` → `Bearer $TOKEN`, linhas 53–60/188–193), `ageblind-front/README.md` (linhas 41/60 — prompt Basic → tela de login) — SC-007
- [ ] T044 [US5] Validar `openapi.yaml` no Swagger Editor (bearerAuth + /auth) e revisar `ContratoOpenApiTest` — SC-005

**Checkpoint**: SC-001…SC-007 todos verificáveis

---

## Phase 9: Polish & validação cruzada

- [ ] T045 [Polish] Rodar `quickstart.md` completo (seções 1–6) e registrar resultado
- [ ] T046 [Polish] `grep` de higiene: zero ocorrências de `httpBasic`, `InMemoryUserDetailsManager`, `httpCredentials`, `app.rh.*` em `ageblind-api/` e `ageblind-front/` (docs do ciclo atualizados; `Relatório Detalhado.md` é artefato histórico — não alterar)
- [ ] T047 [Polish] Atualizar `specs/ROADMAP.md` status `001` (após merge do PR — última tarefa do ciclo)

---

## Dependencies & Execution Order

### Phase Dependencies
- **Setup (Phase 1)**: sem dependências — inicia imediatamente
- **Foundational (Phase 2)**: depends on Phase 1 — BLOQUEIA todas as stories
- **US1 (Phases 3–4)**: depende da fundação; seed (T012–T013) após T007–T008
- **US2 (Phase 5)**: depende de US1 (token existe); T014 (helper) pode correr em paralelo com T006–T013
- **US3 (Phase 6)**: depende de US1 (endpoint de login); independente de US2 para testes (mock)
- **US4 (Phase 7)**: independente das demais após fundação (rate limit usa `/auth` de US1)
- **US5 (Phase 8)**: depende de US1–US4 (E2E exercita o fluxo completo)
- **Polish (Phase 9)**: após todas as stories desejadas

### Within Each User Story
- Testes (T006, T015, T025–T027, T033–T035) escritos ANTES e devem FALHAR antes da implementação
- Modelos/infra antes de services; services antes de controllers; testes antes do flip de segurança

### Parallel Opportunities
- T001 ∥ T002 ∥ T003 ∥ T004 (arquivos distintos)
- T006 ∥ T014 (test sources distintos)
- T025 ∥ T026 (front tests)
- T033 ∥ T034 ∥ T035 (test sources distintos)
- US3 ∥ US4 após US2 (frontend e hardening não se sobrepõem em arquivos)

---

## Commit Mapping (1 commit = 1 grupo = suite verde)

| Commit | Tarefas | Mensagem sugerida |
|---|---|---|
| **C1.1** | T001–T005 | `feat(api): infra JWT - resource server, entidade Usuario e migracao V2` |
| **C1.2** | T006–T011 | `feat(api): endpoints de cadastro e login com JWT HS256 + contrato bearer` |
| **C1.3** | T012–T013 | `feat(api): seed de usuarios demo candidato e RH` |
| **C1.4** | T014–T024 | `feat(api): matriz de autorizacao JWT com posse - remove HTTP Basic` ⚠️ maior commit |
| **C1.5** | T025–T032 | `feat(front): login JWT, header Bearer e guarda de rota` |
| **C1.6** | T033–T040 | `feat(api): hardening - rate limit, CORS, headers e swagger off em prod` |
| **C1.7** | T041–T044 | `test(e2e): login via JWT sem httpCredentials + docs JWT nos READMEs` |
| **C1.8** | T045–T046 | `chore: validacao quickstart e higiene de referencias Basic` |
| pós-merge | T047 | `docs(spec): status do ciclo 001 no ROADMAP` |

---

## Notes

- [P] = arquivos distintos, sem dependências — executáveis em paralelo
- Cada commit exige `./mvnw clean verify` verde (e `npm test` em C1.5, `npm run e2e` em C1.7)
- C1.4 é o maior commit (flip de segurança + migração da suíte) — mantê-lo atômico evita meio-estado inseguro no histórico
- US3 pode avançar em paralelo a US2 (mock do login nos testes de front)
- Parar em qualquer checkpoint para validar a story isoladamente
- Evitar: tarefas vagas, conflitos no mesmo arquivo entre stories paralelas, dependências cross-story que quebrem a independência
