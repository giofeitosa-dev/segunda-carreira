# Feature Specification: Governança LGPD (003-lgpd-governance)

**Feature Branch**: `003-lgpd-governance`

**Created**: 2026-11-22

**Status**: Aprovado (2026-11-22)

**Input**: User description: "Direitos do titular (portabilidade + exclusão cascata), política de retenção, SECURITY.md + pr-checklist, checklist de conformidade".

**Constitution**: [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (v1.0.0) — princípios I (Security-by-default), II (Privacidade & LGPD), III (API-first), IV (Test-first), V (YAGNI).

## Contexto

O ciclo 001 implementou autenticação/autorização JWT com matriz de acesso por papel+posse. O ciclo 002 adicionou audit log, correlation-id, actuator hardening e supply-chain CI. A API já registra acessos a dado pessoal, mas falta:

- **Exclusão cascata** (LGPD art. 18): não há como o titular solicitar exclusão de seus dados pessoais com propagação para todas as tabelas vinculadas (perfil, experiências, formações, projetos, idiomas, certificações, competências, inscrições, etc.), mantendo o audit log imutável com usuario_id null.
- **Portabilidade** (LGPD art. 20): não há endpoint para exportar todos os dados do titular em formato estruturado.
- **Política de retenção**: não há definição nem mecanismo automático de retenção/purga de dados inativos.
- **Documentação de segurança**: não há SECURITY.md, pr-checklist nem checklist de conformidade LGPD.

Este ciclo implementa governança LGPD, sem modificar o comportamento de negócio existente.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Exclusão cascata de dados pessoais (Priority: P1) 🎯 MVP

Como **titular de dados**, quero solicitar a exclusão completa dos meus dados pessoais, para que eu possa exercer meu direito de exclusão (LGPD art. 18).

**Why this priority**: É requisito legal da LGPD e o ciclo 002 já provê o audit log para accountability.

**Independent Test**: `DELETE /api/v1/meus-dados` com token CANDIDATO → exclui perfil e dados vinculados, desvincula usuario (perfil_id null), mantém auditoria com usuario_id null; tentativa de acesso após exclusão → 404/403.

**Acceptance Scenarios**:

1. **Given** usuário CANDIDATO autenticado com perfil, **When** chama `DELETE /api/v1/meus-dados`, **Then** perfil e todas as tabelas filhas (experiencia, formacao, projeto, idioma, certificacao, perfil_competencia, inscricao) são excluídas; usuario.perfil_id fica null; auditoria mantém registros com usuario_id preenchido (imutável).
2. **Given** usuário CANDIDATO autenticado sem perfil, **When** chama `DELETE /api/v1/meus-dados`, **Then** 200 sem erro (idempotente).
3. **Given** usuário CANDIDATO autenticado, **When** tenta acessar `GET /api/v1/perfis/{id}` após exclusão, **Then** 404.

### User Story 2 - Portabilidade de dados (Priority: P1) 🎯 MVP

Como **titular de dados**, quero exportar todos os meus dados pessoais em formato estruturado, para que eu possa exercer meu direito de portabilidade (LGPD art. 20).

**Why this priority**: Complementa a exclusão; permite ao titular levar seus dados para outro serviço.

**Independent Test**: `GET /api/v1/meus-dados/exportacao` com token CANDIDATO → 200 com JSON contendo perfil, experiências, formações, projetos, idiomas, certificações, competências, inscrições e dados de usuário.

**Acceptance Scenarios**:

1. **Given** usuário CANDIDATO autenticado com perfil, **When** chama `GET /api/v1/meus-dados/exportacao`, **Then** 200 com JSON contendo todos os dados pessoais do titular (sem senha, sem hash).
2. **Given** usuário CANDIDATO autenticado sem perfil, **When** chama `GET /api/v1/meus-dados/exportacao`, **Then** 200 com JSON contendo apenas dados do usuário (email, papel, criado_em).
3. **Given** usuário RH (não é o titular), **When** chama `GET /api/v1/meus-dados/exportacao`, **Then** 403 (só o titular pode exportar seus próprios dados).

### User Story 3 - Política de retenção (Priority: P2)

Como **operador**, quero que dados inativos sejam retidos por período definido e depois purgados/anonimizados, para que a aplicação não mantenha dados pessoais além do necessário (LGPD art. 16).

**Why this priority**: Reduz risco de vazamento e custos de armazenamento; requisito de minimização de dados.

**Independent Test**: Teste unitário da classe de retenção verifica quais registros estão elegíveis para purga baseado em data de inatividade.

**Acceptance Scenarios**:

1. **Given** perfil inativo há mais de 365 dias, **When** job de retenção roda, **Then** perfil é anonimizado (dados pessoais substituídos por placeholders) ou excluído.
2. **Given** auditoria de segurança, **When** job de retenção roda, **Then** registros de auditoria são mantidos (imutáveis, não entram na purga).
3. **Given** configuração de retenção, **When** propriedade `app.lgpd.retention.dias-inatividade` é alterada, **Then** o job usa o novo valor.

### User Story 4 - Documentação de segurança e conformidade (Priority: P2)

Como **mantenedor**, quero SECURITY.md, pr-checklist e checklist de conformidade LGPD, para que o projeto tenha documentação oficial de segurança e governança.

**Why this priority**: Transparência para contributors e auditores; exige boa prática de open source.

**Independent Test**: Verificação de existência dos arquivos `SECURITY.md`, `.github/PULL_REQUEST_TEMPLATE.md` (ou `pr-checklist.md`) e `specs/003-lgpd-governance/checklist-conformidade.md`.

**Acceptance Scenarios**:

1. **Given** repositório, **When** verifica existência de `SECURITY.md`, **Then** arquivo existe com política de vulnerabilidades.
2. **Given** repositório, **When** verifica existência de checklist de PR, **Then** `.github/pull_request_template.md` existe com itens de segurança/LGPD.
3. **Given** repositório, **When** verifica checklist de conformidade LGPD, **Then** `specs/003-lgpd-governance/checklist-conformidade.md` existe com itens da LGPD.

---

## Edge Cases

- Exclusão não deve falhar se o titular não tiver perfil (idempotente).
- Audit log é imutável: registros de auditoria mantêm usuario_id mesmo após exclusão do usuário/perfil (não fazer ON DELETE CASCADE na FK de auditoria_seguranca — já é SET NULL na V3).
- Senha nunca aparece na exportação de portabilidade.
- Exportação só para o próprio titular (posse via usuario.id do JWT).
- Políticas de retenção não excluem auditoria de segurança.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST expor `DELETE /api/v1/meus-dados` para exclusão cascata dos dados pessoais do titular autenticado (perfil + filhas + desvinculação do usuario).
- **FR-002**: Sistema MUST expor `GET /api/v1/meus-dados/exportacao` para exportação de dados pessoais do titular autenticado (portabilidade), sem senha/hash.
- **FR-003**: Exclusão e exportação só acessíveis pelo próprio titular (posse via usuario.id do JWT) — papel RH não pode acessar dados de outro titular por esses endpoints.
- **FR-004**: Audit log (`auditoria_seguranca`) é imutável: registros mantêm usuario_id mesmo após exclusão (FK com ON DELETE SET NULL já definida na V3).
- **FR-005**: Sistema MUST ter classe de retenção que identifica registros inativos elegíveis para purga/anonymização baseado em `app.lgpd.retention.dias-inatividade`.
- **FR-006**: Tabelas de auditoria (`auditoria_seguranca`) são excluídas da purga de retenção (imutáveis).
- **FR-007**: Repositório MUST conter `SECURITY.md` com política de divulgação de vulnerabilidades.
- **FR-008**: Repositório MUST conter `.github/pull_request_template.md` com itens de segurança e LGPD.
- **FR-009**: Repositório MUST conter `specs/003-lgpd-governance/checklist-conformidade.md` com checklist de conformidade LGPD.

### Key Entities

- **Usuario**: id, email, senha (BCrypt), papel, perfil_id (nullable), criado_em. Exclusão desvincula perfil (perfil_id=null) ou exclui o usuario se não houver dependências críticas.
- **Perfil**: id, nome, email, telefone, cidade, resumo, linkedin, github, criado_em, + coleções filhas (experiencias, formacoes, projetos, idiomas, certificacoes, competencias). Exclusão em cascata via JPA orphanRemoval + @OneToMany(CascadeType.ALL).
- **Inscricao**: vinculada ao perfil; excluída em cascata na exclusão do titular.
- **AuditoriaSeguranca**: imutável, usuario_id nullable (SET NULL), mantém registros após exclusão.

### Restrições de Stack

- Java 17 · Spring Boot 4.1.1 · Spring Data JPA · SpringDoc OpenAPI · Flyway.
- Banco: H2 (dev/teste) · PostgreSQL 16 (prod).
- Migrações: Flyway obrigatório (`V4__lgpd_governance.sql` ou similar).
- Idioma de código, docs, commits: PT-BR.
- Não adicionar dependências novas além do já presente no ciclo 002.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `./mvnw clean verify` verde — suíte existente + novos testes de exclusão/exportação/retenção.
- **SC-002**: `DELETE /api/v1/meus-dados` exclui perfil e dados vinculados, desvincula usuario, mantém auditoria (coberto por teste de integração).
- **SC-003**: `GET /api/v1/meus-dados/exportacao` retorna JSON com dados pessoais do titular (sem senha) (coberto por teste de integração).
- **SC-004**: Acesso a dados de outro titular por esses endpoints retorna 403 (coberto por teste de integração).
- **SC-005**: Classe de retenção identifica registros inativos elegíveis para purga (coberto por teste unitário).
- **SC-006**: `SECURITY.md`, `.github/pull_request_template.md`, `specs/003-lgpd-governance/checklist-conformidade.md` existem.
- **SC-007**: `openapi.yaml` atualizado com endpoints `DELETE /api/v1/meus-dados` e `GET /api/v1/meus-dados/exportacao`.

## Assumptions

- Exclusão do titular não exclui registros de auditoria (imutabilidade para accountability).
- Exportação inclui apenas dados pessoais do titular, não dados de outros usuários.
- Política de retenção é identificação de elegibilidade; a purga efetiva pode ser job futuro (escopo deste ciclo: detecção + documentação).
- Sem sistema externo de governança de dados — mecanismos implementados na própria aplicação.

## Pendências resolvidas do spec

| Decisão | Resolução |
|---------|-----------|
| Exclusão exclui o usuario ou só desvincula o perfil? | Desvincula (perfil_id=null) se não houver outras dependências críticas; se for o único dono, exclui o usuario também. |
| Exportação inclui senha? | Não — nunca (FR-002). |
| RH pode exportar dados de outro titular? | Não — só o próprio titular (FR-003). |
| Onde fica o checklist de conformidade? | `specs/003-lgpd-governance/checklist-conformidade.md` (docs do ciclo, não código). |

---
**Version**: 1.0.0 | **Status**: Draft — aguardando aprovação do usuário (GATE spec)