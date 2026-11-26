# Implementation Plan: Governança LGPD (003-lgpd-governance)

**Branch**: `003-lgpd-governance` | **Date**: 2026-11-22 | **Spec**: [spec.md](./spec.md)

## Summary

Ciclo 001 implementou autenticação/autorização JWT. Ciclo 002 adicionou observabilidade e auditoria. Este ciclo adiciona governança LGPD: exclusão cascata de dados pessoais (DELETE /api/v1/meus-dados), portabilidade (GET /api/v1/meus-dados/exportacao), classe de retenção de dados inativos, SECURITY.md, pr-checklist e checklist de conformidade. Não altera comportamento de negócio existente.

## Technical Context

**Language/Version**: Java 17 · Spring Boot 4.1.1 · Spring Data JPA · SpringDoc OpenAPI.

**Primary Dependencies**: 
- Nenhuma nova dependência além das já presentes (Spring Data JPA com orphanRemoval/CascadeType.ALL já mapeado em Perfil).
- Flyway `V4__lgpd_governance.sql` (nova tabela opcional de configuração de retenção ou apenas comentário).

**Storage**: H2 (dev/teste) · PostgreSQL 16 (prod) — Flyway V1__init.sql + V2__usuarios.sql + V3__auditoria_seguranca.sql existentes + **novo** V4__lgpd_governance.sql (tabela de configuração de retenção ou ajuste de índices).

**Testing**: JUnit 5 + MockMvc + `@WithMockUser` / `jwt()` post-processor · JaCoCo · Vitest (front, não tocado neste ciclo) · Playwright E2E (não tocado neste ciclo).

**Target Platform**: Linux server (Docker) · demo local Windows/macOS/Linux.

**Project Type**: Web service (monolito modular Spring Boot) + SPA React (não alterado).

**Performance Goals**: N/A para este ciclo (exclusão síncrona, exportação síncrona).

**Constraints**: 401/403 problem+json RFC 7807 · `mvnw verify` verde a cada commit · E2E verde quando toca front (não toca) · exclusão não deve falhar auditoria (usuario_id mantido) · senha nunca na exportação · sem vazar dados de outro titular.

**Scale/Scope**: 2 endpoints novos + 1 classe de retenção + 1 tabela opcional + docs (SECURITY.md, pr-checklist, checklist-conformidade) · ~12 testes novos.

## Constitution Check

| Princípio | Veredito | Evidência / Ação |
|-----------|----------|------------------|
| I. Security-by-default | ✅ PASS | Exclusão/exportação só para titular (posse via JWT); senha nunca exposta |
| II. Privacidade & LGPD | ✅ PASS | Exclusão cascata; portabilidade sem senha; auditoria imutável (usuario_id mantido); retenção definida |
| III. API-first | ✅ PASS | `openapi.yaml` ganha `DELETE /api/v1/meus-dados` e `GET /api/v1/meus-dados/exportacao` |
| IV. Test-first | ✅ PASS | Novos testes escritos antes da implementação |
| V. Simplicity / YAGNI | ✅ PASS | Sem sistema externo de governança; retenção é detecção de elegibilidade; sem novas dependências |

**Violações**: nenhuma → *Complexity Tracking* vazio.

## Project Structure

### Documentation (this feature)

```text
specs/003-lgpd-governance/
├── spec.md                # aprovado (GATE 2)
├── plan.md                # este arquivo
├── contracts/             # (opcional — endpoints LGPD)
├── checklist-conformidade.md  # NOVO — checklist de conformidade LGPD
└── tasks.md               # tarefas agrupadas por user story
```

### Source Code (repository root)

```text
ageblind-api/api/src/main/java/com/reconecta.ageblind/
├── auth/
│   ├── domain/
│   │   └── Usuario.java           # ajuste — garantir cascade para exclusão
│   └── repository/
│       └── UsuarioRepository.java # ajuste — método para exclusão/desvinculação
├── carreira/
│   ├── api/
│   │   ├── PerfilController.java  # ajuste — nenhum (já tem @Auditar)
│   │   └── TitularController.java # NOVO — DELETE /meus-dados, GET /meus-dados/exportacao
│   ├── domain/
│   │   └── Perfil.java            # ajuste — garantir orphanRemoval em todas coleções
│   └── service/
│       └── PerfilService.java     # ajuste — método excluirTitular(usuarioId)
├── shared/
│   ├── api/
│   │   └── DadosTitularResponse.java  # NOVO — DTO de exportação
│   └── config/
│       └── RetencaoConfig.java    # NOVO — classe de retenção (dias-inatividade)
└── escudo/...                     # sem alteração
```

```text
ageblind-api/api/src/main/resources/
├── db/migration/V4__lgpd_governance.sql   # NOVO — tabela config retenção (opcional)
└── application.properties                 # ajuste — app.lgpd.retention.dias-inatividade
```

```text
ageblind-api/api/src/test/java/com/reconecta.ageblind/
├── carreira/api/TitularControllerTest.java   # NOVO — exclusão/exportação (403 para outro titular)
└── shared/config/RetencaoTest.java           # NOVO — retenção (elegibilidade por inatividade)
```

```text
.github/
├── pull_request_template.md      # NOVO — pr-checklist com itens segurança/LGPD
SECURITY.md                       # NOVO — política de vulnerabilidades
```

```text
openapi.yaml                      # ajustado — DELETE /api/v1/meus-dados + GET /api/v1/meus-dados/exportacao
README.md                         # ajustado — docs LGPD
```

## Complexity Tracking

| Violação | Por que é necessária | Alternativa mais simples rejeitada porque |
|----------|----------------------|-------------------------------------------|
| — | — | — |

## Tasks

**Format**: `[ID] [P?] [Story] Description`

**Paths**: `ageblind-api/api/src/{main,test}/java/com/reconecta/ageblind/…` · `.github/` · `SECURITY.md` · `openapi.yaml` · `README.md`

**Tests**: OBRIGATÓRIOS — Constituição IV (Test-first); todos os grupos de teste abaixo são escritos **antes** da implementação do respectivo grupo.

### Phase 1: Setup (infraestrutura de retenção e DTOs)

**Purpose**: DTOs de exportação, classe de retenção, migração V4 — sem mudança de comportamento (suite continua verde)

- [ ] T001 [P] [Setup] Criar `DadosTitularResponse.java` em `shared/api/` (DTO de exportação: usuario, perfil, experiencias, formacoes, projetos, idiomas, certificacoes, competencias, inscricoes — sem senha)
- [ ] T002 [P] [Setup] Criar `RetencaoConfig.java` em `shared/config/` (classe que identifica registros inativos elegíveis para purga baseado em `app.lgpd.retention.dias-inatividade`)
- [ ] T003 [P] [Setup] Criar migração `V4__lgpd_governance.sql` em `db/migration/` (tabela de configuração de retenção ou ajuste de índices)
- [ ] T004 [Setup] Ajustar `application.properties` (adicionar `app.lgpd.retention.dias-inatividade=365`)

**Checkpoint**: suite verde; nada de LGPD exposto ainda

---

### Phase 2: User Story 1 — Exclusão cascata (Priority: P1) 🎯 MVP

**Goal**: `DELETE /api/v1/meus-dados` exclui perfil e dados vinculados, desvincula usuario, mantém auditoria imutável.

**Independent Test**: `DELETE /api/v1/meus-dados` com token CANDIDATO → perfil excluído, usuario.perfil_id null, auditoria mantida; acesso após exclusão → 404.

### Tests for US1 (write first — expect FAIL)

- [ ] T005 [US1] `TitularControllerTest.java`: exclusão cascata exclui perfil+filhas; desvincula usuario; auditoria mantida; idempotente sem perfil; 404 após exclusão

### Implementation for US1

- [ ] T006 [US1] `TitularController.java` (`DELETE /api/v1/meus-dados`): chama serviço de exclusão, só titular (usuario.id do JWT)
- [ ] T007 [US1] `PerfilService.java` (`excluirTitular(Long usuarioId)`): exclui perfil e filhas (cascade), desvincula/exclui usuario, falha-segura
- [ ] T008 [US1] `openapi.yaml`: paths `DELETE /api/v1/meus-dados` e `GET /api/v1/meus-dados/exportacao`

**Checkpoint**: exclusão testável isoladamente com MockMvc; `mvnw verify` verde

---

### Phase 3: User Story 2 — Portabilidade (Priority: P1) 🎯 MVP

**Goal**: `GET /api/v1/meus-dados/exportacao` retorna JSON com dados pessoais do titular (sem senha).

**Independent Test**: `GET /api/v1/meus-dados/exportacao` com token CANDIDATO → 200 com JSON dos dados; sem perfil → 200 com dados do usuário apenas; RH → 403.

### Tests for US2 (write first — expect FAIL)

- [ ] T009 [US2] `TitularControllerTest.java`: exportação retorna JSON sem senha; idempotente sem perfil; 403 para não-titular

### Implementation for US2

- [ ] T010 [US2] `TitularController.java` (`GET /api/v1/meus-dados/exportacao`): monta DadosTitularResponse, só titular
- [ ] T011 [US2] `PerfilService.java` (`exportarTitular(Long usuarioId)`): coleta dados do perfil e filhas, sem senha

**Checkpoint**: exportação testável isoladamente com MockMvc; `mvnw verify` verde

---

### Phase 4: User Story 3 — Retenção (Priority: P2)

**Goal**: Classe de retenção identifica registros inativos elegíveis para purga/anonymização.

**Independent Test**: Teste unitário `RetencaoTest` verifica elegibilidade baseado em `dias-inatividade`.

### Tests for US3 (write first — expect FAIL)

- [ ] T012 [US3] `RetencaoTest.java`: perfil inativo > dias é elegível; perfil ativo não é; auditoria não entra na purga

### Implementation for US3

- [ ] T013 [US3] `RetencaoConfig.java`: método `elegivelParaPurga(perfil, diasInatividade)`; auditoria excluída
- [ ] T014 [US3] `application.properties`: `app.lgpd.retention.dias-inatividade=365`

**Checkpoint**: `mvnw verify` verde

---

### Phase 5: User Story 4 — Documentação (Priority: P2)

**Goal**: SECURITY.md, pr-checklist, checklist de conformidade LGPD.

- [ ] T015 [US4] Criar `SECURITY.md` na raiz (política de divulgação de vulnerabilidades)
- [ ] T016 [US4] Criar `.github/pull_request_template.md` (checklist de PR com itens segurança/LGPD)
- [ ] T017 [US4] Criar `specs/003-lgpd-governance/checklist-conformidade.md` (checklist LGPD)

**Checkpoint**: arquivos existem; `mvnw verify` verde

---

### Phase 6: Polish & validação cruzada

- [ ] T018 [Polish] Rodar `quickstart.md` (seções 1–4) e registrar resultado
- [ ] T019 [Polish] `grep` de higiene: zero ocorrências de senha na exportação; logs sem dados sensíveis
- [ ] T020 [Polish] Atualizar `specs/ROADMAP.md` status `003` (após merge do PR — última tarefa do ciclo)

---

## Dependencies & Execution Order

- **Setup (Phase 1)**: sem dependências — inicia imediatamente
- **US1 (Phase 2)**: depende da fundação (DTOs, Perfil com cascade)
- **US2 (Phase 2)**: depende de US1 (mesmo controller, DTOs compartilhados)
- **US3 (Phase 3)**: independente (classe de retenção isolada)
- **US4 (Phase 4)**: independente (apenas arquivos de documentação)
- **Polish (Phase 6)**: após todas as stories

**Within Each User Story**: Testes escritos ANTES e devem FALHAR antes da implementação

**Parallel Opportunities**:
- T001 ∥ T002 ∥ T003 ∥ T004 (arquivos distintos)
- T005 ∥ T009 ∥ T012 (test sources distintos)
- US1 ∥ US2 ∥ US3 após Setup (arquivos não se sobrepõem)

---

## Commit Mapping (1 commit = 1 grupo = suite verde)

| Commit | Tarefas | Mensagem sugerida |
|--------|---------|-------------------|
| **C3.1** | T001–T004 | `feat(api): infra LGPD - DTO exportacao, classe retentao, migracao V4` |
| **C3.2** | T005–T008 | `feat(api): exclusao cascata DELETE /meus-dados (titular)` |
| **C3.3** | T009–T011 | `feat(api): portabilidade GET /meus-dados/exportacao (sem senha)` |
| **C3.4** | T012–T014 | `feat(api): classe de retencao de dados inativos` |
| **C3.5** | T015–T017 | `docs(security): SECURITY.md, PR template, checklist conformidade LGPD` |
| **C3.6** | T018–T020 | `docs(spec): validacao quickstart e status ciclo 003 no ROADMAP` |

---

## Notes

- [P] = arquivos distintos, sem dependências — executáveis em paralelo
- Cada commit exige `./mvnw clean verify` verde
- Exclusão não toca auditoria_seguranca (imutável, usuario_id mantido via SET NULL já na V3)
- Senha nunca entra na exportação de portabilidade
- O front não é tocado neste ciclo (sem alteração em `ageblind-front/`)