# Tasks: Observability, Audit Log e Supply-Chain (002-observability-audit)

**Input**: Design documents from `/specs/002-observability-audit/`

**Prerequisites**: spec.md ✅ · plan.md ✅ · research.md (R1–R8) ✅ · data-model.md ✅ · contracts/auditoria-api.md ✅ · quickstart.md ✅

**Tests**: OBRIGATÓRIOS — Constituição IV (Test-first); todos os grupos de teste abaixo são escritos **antes** da implementação do respectivo grupo (verificar falha, depois implementar).

**Organization**: Tarefas agrupadas por user story. Convenção do ROADMAP: 1 commit = 1 grupo = `./mvnw clean verify` verde. Mapeamento de commits ao final.

**Paths**: `ageblind-api/api/src/{main,test}/java/com/reconecta/ageblind/…` · `.github/workflows/` · `openapi.yaml` · `README.md`

## Format: `[ID] [P?] [Story] Description`

---

## Phase 1: Setup (infraestrutura de auditoria e correlation-id)

**Purpose**: entidade, migração, filter e logging JSON — sem mudança de comportamento (suite continua verde)

- [ ] T001 [P] [Setup] Criar entidade `AuditoriaSeguranca` em `auth/domain/` (JPA, `@Table(name="auditoria_seguranca")`, campos: id, usuario_id, acao, recurso, ip, data_hora, correlation_id)
- [ ] T002 [P] [Setup] Criar `AuditoriaSegurancaRepository` em `auth/repository/` (JPA repository, `findAll(Pageable)`)
- [ ] T003 [P] [Setup] Criar migração `V3__auditoria_seguranca.sql` em `db/migration/` (tabela + índices em usuario_id, data_hora, correlation_id)
- [ ] T004 [P] [Setup] Criar `CorrelationIdFilter.java` em `shared/config/` (`OncePerRequestFilter`, lê `X-Correlation-Id` ou gera UUID, MDC, header resposta)
- [ ] T005 [Setup] Criar `logback-spring.xml` em `main/resources/` (encoder JSON, campos estruturados)

**Checkpoint**: suite verde; nada de auditoria exposto ainda

---

## Phase 2: User Story 1 — Audit log de segurança (Priority: P1) 🎯 MVP

**Goal**: todo acesso a dado pessoal gera registro em `auditoria_seguranca`; `GET /api/v1/auditoria` só RH.

**Independent Test**: `POST /api/v1/perfis` com token CANDIDATO → registro em auditoria; acesso negado (403) também registra; RH lê auditoria (200); CANDIDATO lê auditoria (403).

### Tests for US1 (write first — expect FAIL)

- [ ] T006 [US1] `AuditoriaAspectTest.java`: cenários de criação/atualização de perfil geram registro; acesso negado gera registro com status 403; endpoint público não gera registro (ou registro com usuario_id null)

### Implementation for US1

- [ ] T007 [US1] `AuditoriaAspect.java` (`@Aspect`, `@Around` em controllers de perfil/currículo/inscrição/revisão): extrai usuario_id do `SecurityContext`, ação, recurso, IP, correlation-id do MDC; grava em `AuditoriaSegurancaRepository` (falha-segura: try/catch)
- [ ] T008 [US1] `AuditoriaController.java` + DTO `AuditoriaResponse`: `GET /api/v1/auditoria` (paginado, só `hasRole("RH")`)
- [ ] T009 [US1] `openapi.yaml`: path `GET /api/v1/auditoria` (tag `Auditoria`, `security: [{bearerAuth: []}]`); `ContratoOpenApiTest` atualizado (escopo AgeBlind = 20 paths)

**Checkpoint**: audit log testável isoladamente com MockMvc; `mvnw verify` verde

---

## Phase 3: User Story 2 — Correlation-id e logging estruturado (Priority: P1)

**Goal**: header `X-Correlation-Id` aceito/gerado; logs JSON estruturados.

**Independent Test**: requisição com `X-Correlation-Id` → resposta tem mesmo header; sem header → UUID gerado; logs JSON contêm campos estruturados.

### Tests for US2 (write first — expect FAIL)

- [ ] T010 [US2] `CorrelationIdFilterTest.java`: header presente é propagado; ausente → UUID gerado no header de resposta; MDC populado com correlationId

### Implementation for US2

- [ ] T011 [US2] `CorrelationIdFilter.java`: `OncePerRequestFilter`, lê `X-Correlation-Id` do request ou gera `UUID.randomUUID()`, coloca no MDC, adiciona header `X-Correlation-Id` na resposta
- [ ] T012 [US2] Registrar `CorrelationIdFilter` no `SecurityConfig` (antes do `RateLimitFilter`)

**Checkpoint**: `mvnw verify` verde

---

## Phase 4: User Story 3 — Actuator hardening (Priority: P2)

**Goal**: `/actuator/health` público em prod; outros endpoints requerem auth ou desabilitados.

**Independent Test**: `GET /actuator/health` → 200 em prod; `GET /actuator/metrics` → 401/404 em prod.

### Tests for US3 (write first — expect FAIL)

- [ ] T013 [US3] `ActuatorHardeningTest.java`: health público; metrics/sensíveis não expostos sem auth em prod

### Implementation for US3

- [ ] T014 [US3] `application-prod.properties`: `management.endpoints.web.exposure.include=health` apenas
- [ ] T015 [US3] `SecurityConfig.java`: ajustar `permitAll` apenas para `/actuator/health`; outros `/actuator/**` requerem `authenticated()`

**Checkpoint**: `mvnw verify` verde

---

## Phase 5: User Story 4 — Supply-chain CI (Priority: P2)

**Goal**: CI executa dependency:audit, gitleaks, Dependabot ativo.

**Independent Test**: CI na branch main — `dependency:audit` sem vulnerabilidades; `gitleaks` sem secrets; Dependabot cria PRs para dependências desatualizadas.

### Implementation for US4

- [ ] T016 [US4] `.github/workflows/ci.yml`: adicionar passo `mvnw dependency:audit` (fail build se vulnerabilidades)
- [ ] T017 [US4] `.github/workflows/ci.yml`: adicionar passo `gitleaks` (docker action) para varrer histórico
- [ ] T018 [US4] `.github/dependabot.yml`: criar arquivo de config do Dependabot para ecossistema Maven (Java)

**Checkpoint**: CI verde na branch main; Dependabot ativo

---

## Phase 6: Polish & validação cruzada

- [ ] T019 [Polish] Rodar `quickstart.md` completo (seções 1–6) e registrar resultado
- [ ] T020 [Polish] `grep` de higiene: zero ocorrências de senha/corpo de requisição no audit log; logs JSON válidos
- [ ] T021 [Polish] Atualizar `specs/ROADMAP.md` status `002` (após merge do PR — última tarefa do ciclo)

---

## Dependencies & Execution Order

- **Setup (Phase 1)**: sem dependências — inicia imediatamente
- **US1 (Phases 2)**: depende da fundação (entidade + repositório + T004 filter para correlation-id)
- **US2 (Phase 3)**: independente de US1 (pode correr em paralelo após Setup)
- **US3 (Phase 4)**: independente das demais (só application-prod.properties + SecurityConfig)
- **US4 (Phase 5)**: independente (apenas arquivos de CI)
- **Polish (Phase 6)**: após todas as stories

**Within Each User Story**: Testes escritos ANTES e devem FALHAR antes da implementação

**Parallel Opportunities**:
- T001 ∥ T002 ∥ T003 ∥ T004 ∥ T005 (arquivos distintos)
- T006 ∥ T010 ∥ T013 (test sources distintos)
- US1 ∥ US2 ∥ US3 após Setup (arquivos não se sobrepõem)

---

## Commit Mapping (1 commit = 1 grupo = suite verde)

| Commit | Tarefas | Mensagem sugerida |
|--------|---------|-------------------|
| **C2.1** | T001–T005 | `feat(api): infra auditoria - entidade, migracao V3, correlation-id filter e log JSON` |
| **C2.2** | T006–T009 | `feat(api): log de auditoria de seguranca + endpoint GET /auditoria (só RH)` |
| **C2.3** | T010–T012 | `feat(api): correlation-id e logging JSON estruturado` |
| **C2.4** | T013–T015 | `feat(api): actuator hardening em prod (só health publico)` |
| **C2.5** | T016–T018 | `ci(supply-chain): dependency:audit, gitleaks e Dependabot no CI` |
| **C2.6** | T019–T021 | `docs(spec): validacao quickstart e status ciclo 002 no ROADMAP` |

---

## Notes

- [P] = arquivos distintos, sem dependências — executáveis em paralelo
- Cada commit exige `./mvnw clean verify` verde
- O audit log usa falha-segura: se o banco estiver indisponível, a requisição principal não falha (try/catch no aspect)
- Não modificar `ContratoOpenApiTest` escopo de 19 → 20 paths sem atualizar `openapi.yaml` no mesmo commit (Constituição III)
- O front não é tocado neste ciclo (sem alteração em `ageblind-front/`)
