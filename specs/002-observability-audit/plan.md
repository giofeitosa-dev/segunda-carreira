# Implementation Plan: Observability, Audit Log e Supply-Chain (002-observability-audit)

**Branch**: `002-observability-audit` | **Date**: 2026-11-18 | **Spec**: [spec.md](./spec.md)

## Summary

Ciclo 001 implementou autenticação/autorização JWT com hardening básico. Este ciclo adiciona observabilidade e auditoria de segurança: tabela `auditoria_seguranca` (Flyway `V3__`), correlation-id via filter, logging JSON estruturado, actuator hardening e supply-chain CI (Dependabot, dependency audit, gitleaks). Não altera comportamento de negócio existente.

## Technical Context

**Language/Version**: Java 17 · Spring Boot 4.1.1 · Spring Data JPA · SpringDoc OpenAPI.

**Primary Dependencies**: 
- `spring-boot-starter-actuator` (já presente)
- **novo**: `logback-spring.xml` config (JSON encoder, já vem com Spring Boot via `spring-boot-starter-logging`)
- Flyway `V3__auditoria_seguranca.sql` (nova tabela)

**Storage**: H2 (dev/teste) · PostgreSQL 16 (prod) — Flyway `V1__init.sql` + `V2__usuarios.sql` existentes + **novo** `V3__auditoria_seguranca.sql`

**Testing**: JUnit 5 + MockMvc + `@WithMockUser` / `jwt()` post-processor · JaCoCo · Vitest (front, não tocado neste ciclo) · Playwright E2E (não tocado neste ciclo)

**Target Platform**: Linux server (Docker) · demo local Windows/macOS/Linux

**Project Type**: Web service (monolito modular Spring Boot) + SPA React (não alterado)

**Performance Goals**: N/A para este ciclo (audit log síncrono, falha-segura com fallback em arquivo)

**Constraints**: 401/403 problem+json RFC 7807 · `mvnw verify` verde a cada commit · E2E verde quando toca front (não toca) · audit log não deve falhar a requisição principal (fallback arquivo) · sem vazar senha/dados sensíveis no audit log

**Scale/Scope**: 1 tabela nova + 1 filter + 1 controller + 1 service + 1 repository + CI workflow · ~15 testes novos

## Constitution Check

| Princípio | Veredito | Evidência / Ação |
|---|---|---|
| I. Security-by-default | ✅ PASS | Audit log registra acessos a dado pessoal; actuator sensível não exposto em prod; sem vazar segredos |
| II. Privacidade & LGPD | ✅ PASS | Audit log armazena apenas metadados (não corpo da requisição); não persiste senha; dados sensíveis não entram no log |
| III. API-first | ✅ PASS | `openapi.yaml` ganha `GET /api/v1/auditoria` (tag `Auditoria`); `ContratoOpenApiTest` atualizado (escopo 20 paths) |
| IV. Test-first | ✅ PASS | Novos testes escritos antes da implementação (auditoria, correlation-id, actuator, CI scripts) |
| V. Simplicity / YAGNI | ✅ PASS | Sem sistema externo de logs (ELK/Splunk); logs JSON em stdout; fallback arquivo simples; sem dependências novas além do logback config |

**Violações**: nenhuma → *Complexity Tracking* vazio.

## Project Structure

### Documentation (this feature)

```text
specs/002-observability-audit/
├── spec.md              # aprovado (GATE 2)
├── plan.md              # este arquivo
├── contracts/
│   └── auditoria-api.md # contrato de GET /api/v1/auditoria e schema de auditoria_seguranca
└── tasks.md             # tarefas agrupadas por user story
```

### Source Code (repository root)

```text
ageblind-api/api/src/main/java/com/reconecta/ageblind/
├── shared/
│   ├── config/
│   │   ├── CorrelationIdFilter.java   # NOVO — lê/gera X-Correlation-Id, põe no MDC e header resposta
│   │   └── SecurityConfig.java        # ajustado — actuator hardening em prod
│   ├── api/
│   │   ├── AuditoriaController.java   # NOVO — GET /api/v1/auditoria (só RH)
│   │   └── GlobalExceptionHandler.java # ajustado — nenhum ajuste necessário (já existe)
│   └── security/
│       └── AuditoriaAspect.java       # NOVO — @Around em controllers que expõem dado pessoal
├── auth/
│   └── domain/
│       └── AuditoriaSeguranca.java    # NOVO — entidade JPA (id, usuario_id, acao, recurso, ip, data_hora, correlation_id)
├── auth/repository/
│   └── AuditoriaSegurancaRepository.java  # NOVO — JPA repository
└── carreira/...                      # sem alteração

ageblind-api/api/src/main/resources/
├── db/migration/V3__auditoria_seguranca.sql  # NOVO — tabela auditoria_seguranca
├── logback-spring.xml                # NOVO — logging JSON estruturado
└── application-prod.properties       # ajustado — actuator hardening

ageblind-api/api/src/test/java/com/reconecta/ageblind/
├── shared/api/AuditoriaControllerTest.java      # NOVO — GET /auditoria (RH 200, CANDIDATO 403)
├── shared/config/CorrelationIdFilterTest.java   # NOVO — header X-Correlation-Id propagado/gerado
├── shared/api/AuditoriaAspectTest.java          # NOVO — acesso a dado pessoal gera registro
└── shared/api/ContratoOpenApiTest.java          # ajustado — escopo AgeBlind = 20 paths

.github/workflows/
├── ci.yml                      # ajustado — adiciona dependency:audit e gitleaks
└── dependabot.yml              # NOVO — Dependabot para dependências Java/Maven

openapi.yaml                    # ajustado — GET /api/v1/auditoria (tag Auditoria)
README.md                       # ajustado — docs de auditoria e actuator
```

## Complexity Tracking

| Violação | Por que é necessária | Alternativa mais simples rejeitada porque |
|---|---|---|
| — | — | — |

## Tasks

**Format**: `[ID] [P?] [Story] Description`

**Paths**: `ageblind-api/api/src/{main,test}/java/com/reconecta/ageblind/…` · `.github/workflows/` · `openapi.yaml` · `README.md`

**Tests**: OBRIGATÓRIOS — Constituição IV (Test-first); todos os grupos de teste abaixo são escritos **antes** da implementação do respectivo grupo.

### Phase 1: Setup (infraestrutura de auditoria e correlation-id)

- [ ] T001 [P] [Setup] Criar entidade `AuditoriaSeguranca` em `auth/domain/` + `AuditoriaSegurancaRepository` em `auth/repository/` (JPA, `@Table(name="auditoria_seguranca")`)
- [ ] T002 [P] [Setup] Criar migração `V3__auditoria_seguranca.sql` (tabela `auditoria_seguranca` + índices em usuario_id, data_hora, correlation_id)
- [ ] T003 [P] [Setup] Criar `CorrelationIdFilter.java` em `shared/config/` (`OncePerRequestFilter`, lê `X-Correlation-Id` ou gera UUID, põe no MDC, inclui no header de resposta)
- [ ] T004 [Setup] Criar `logback-spring.xml` em `main/resources/` (encoder JSON, campos: timestamp, nível, logger, mensagem, correlationId, usuarioId, método, caminho, status, tempoMs)

**Checkpoint**: suite verde; nada de auditoria exposto ainda

---

### Phase 2: User Story 1 — Audit log de segurança (Priority: P1) 🎯 MVP

**Goal**: todo acesso a dado pessoal gera registro em `auditoria_seguranca`; `GET /api/v1/auditoria` só RH.

**Independent Test**: `POST /api/v1/perfis` com token CANDIDATO → registro em auditoria; acesso negado (403) também registra; RH lê auditoria (200); CANDIDATO lê auditoria (403).

### Tests for US1 (write first — expect FAIL)

- [ ] T005 [US1] `AuditoriaAspectTest.java`: cenários de criação/atualização de perfil geram registro; acesso negado gera registro com status 403; endpoint público não gera registro (ou registro com usuario_id null)

### Implementation for US1

- [ ] T006 [US1] `AuditoriaAspect.java` (`@Aspect`, `@Around` em controllers de perfil/currículo/inscrição/revisão): extrai usuario_id do `SecurityContext` (sub do JWT), ação, recurso, IP, correlation-id do MDC; grava em `AuditoriaSegurancaRepository` (falha-segura: try/catch, não derruba a requisição)
- [ ] T007 [US1] `AuditoriaController.java` + DTO `AuditoriaResponse`: `GET /api/v1/auditoria` (paginado, só `hasRole("RH")`)
- [ ] T008 [US1] `openapi.yaml`: path `GET /api/v1/auditoria` (tag `Auditoria`, `security: [{bearerAuth: []}]`); `ContratoOpenApiTest` atualizado (escopo AgeBlind = 20 paths)

**Checkpoint**: audit log testável isoladamente com MockMvc; `mvnw verify` verde

---

### Phase 3: User Story 2 — Correlation-id e logging estruturado (Priority: P1)

**Goal**: header `X-Correlation-Id` aceito/gerado; logs JSON estruturados.

**Independent Test**: requisição com `X-Correlation-Id` → resposta tem mesmo header; sem header → UUID gerado; logs JSON contêm campos estruturados.

### Tests for US2 (write first — expect FAIL)

- [ ] T009 [US2] `CorrelationIdFilterTest.java`: header presente é propagado; ausente → UUID gerado no header de resposta; MDC populado com correlationId

### Implementation for US2

- [ ] T010 [US2] `CorrelationIdFilter.java`: `OncePerRequestFilter`, lê `X-Correlation-Id` do request ou gera `UUID.randomUUID()`, coloca no MDC (`MDC.put("correlationId", ...)`), adiciona header `X-Correlation-Id` na resposta, registra no `HttpServletResponse`
- [ ] T011 [US2] `logback-spring.xml`: encoder JSON (`ch.qos.logback.ext.spring.json.JsonLayout` ou similar), campos estruturados incluindo `correlationId` do MDC
- [ ] T012 [US2] registrar `CorrelationIdFilter` no `SecurityConfig` (antes do `RateLimitFilter`)

**Checkpoint**: `npm test` não afetado (front não tocado); `mvnw verify` verde

---

### Phase 4: User Story 3 — Actuator hardening (Priority: P2)

**Goal**: `/actuator/health` público em prod; outros endpoints requerem auth ou desabilitados.

**Independent Test**: `GET /actuator/health` → 200 em prod; `GET /actuator/metrics` → 401/404 em prod.

### Tests for US3 (write first — expect FAIL)

- [ ] T013 [US3] `ActuatorHardeningTest.java`: health público; metrics/sensíveis não expostos sem auth em prod

### Implementation for US3

- [ ] T014 [US3] `application-prod.properties`: `management.endpoints.web.exposure.include=health` apenas (remove `info` se houver dados sensíveis) ou `management.endpoint.metrics.enabled=false`; `management.endpoints.web.exposure.exclude` para endpoints sensíveis
- [ ] T015 [US3] `SecurityConfig.java`: ajustar `permitAll` apenas para `/actuator/health`; outros `/actuator/**` requerem `authenticated()` ou são bloqueados

**Checkpoint**: `mvnw verify` verde

---

### Phase 5: User Story 4 — Supply-chain CI (Priority: P2)

**Goal**: CI executa dependency:audit, gitleaks, Dependabot ativo.

**Independent Test**: CI na branch main — `dependency:audit` sem vulnerabilidades; `gitleaks` sem secrets; Dependabot cria PRs para dependências desatualizadas.

### Implementation for US4

- [ ] T016 [US4] `.github/workflows/ci.yml`: adicionar passo `mvnw dependency:audit` (fail build se vulnerabilidades)
- [ ] T017 [US4] `.github/workflows/ci.yml`: adicionar passo `gitleaks` (docker action) para varrer histórico em busca de secrets
- [ ] T018 [US4] `.github/dependabot.yml`: criar arquivo de config do Dependabot para ecossistema Maven (Java)

**Checkpoint**: CI verde na branch main; Dependabot ativo

---

### Phase 6: Polish & validação cruzada

- [ ] T019 [Polish] Rodar `quickstart.md` (seções 1–4 do ciclo 002) e registrar resultado
- [ ] T020 [Polish] `grep` de higiene: zero ocorrências de senha/corpo de requisição no audit log; logs JSON válidos
- [ ] T021 [Polish] Atualizar `specs/ROADMAP.md` status `002` (após merge do PR — última tarefa do ciclo)

## Dependencies & Execution Order

- **Setup (Phase 1)**: sem dependências — inicia imediatamente
- **US1 (Phases 2)**: depende da fundação (entidade + repositório)
- **US2 (Phase 3)**: independente de US1 (pode correr em paralelo após Setup)
- **US3 (Phase 4)**: independente das demais (só application-prod.properties + SecurityConfig)
- **US4 (Phase 5)**: independente (apenas arquivos de CI)
- **Polish (Phase 6)**: após todas as stories

**Within Each User Story**: Testes escritos ANTES e devem FALHAR antes da implementação

**Parallel Opportunities**:
- T001 ∥ T002 ∥ T003 ∥ T004 (arquivos distintos)
- T005 ∥ T009 ∥ T013 (test sources distintos)
- US1 ∥ US2 ∥ US3 após Setup (arquivos não se sobrepõem)

## Commit Mapping (1 commit = 1 grupo = suite verde)

| Commit | Tarefas | Mensagem sugerida |
|--------|---------|-------------------|
| **C2.1** | T001–T004 | `feat(api): infra auditoria - entidade, migracao V3, correlation-id filter e log JSON` |
| **C2.2** | T005–T008 | `feat(api): log de auditoria de seguranca + endpoint GET /auditoria (só RH)` |
| **C2.3** | T009–T012 | `feat(api): correlation-id e logging JSON estruturado` |
| **C2.4** | T013–T015 | `feat(api): actuator hardening em prod (só health publico)` |
| **C2.5** | T016–T018 | `ci(supply-chain): dependency:audit, gitleaks e Dependabot no CI` |
| **C2.6** | T019–T021 | `docs(spec): validacao quickstart e status ciclo 002 no ROADMAP` |

## Notes

- [P] = arquivos distintos, sem dependências — executáveis em paralelo
- Cada commit exige `./mvnw clean verify` verde
- O audit log usa falha-segura: se o banco estiver indisponível, a requisição principal não falha (try/catch no aspect)
- Não modificar `ContratoOpenApiTest` escopo de 19 → 20 paths sem atualizar `openapi.yaml` no mesmo commit (Constituição III)
- O front não é tocado neste ciclo (sem alteração em `ageblind-front/`)
