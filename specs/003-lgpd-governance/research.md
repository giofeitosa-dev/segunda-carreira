# Research: Governança LGPD (003-lgpd-governance)

**Date**: 2026-11-22 | **Cycle**: 003-lgpd-governance

## R1 — LGPD art. 18 (direito à exclusão)

O titular de dados pessoais tem direito à eliminação dos dados tratados com seu consentimento, exceto nos casos de obrigação legal de retenção (ex.: auditoria de segurança, registros de acesso). A exclusão deve ser cascata: perfil e todos os dados vinculados (experiência, formação, projeto, idioma, certificação, competência, inscrição) devem ser removidos, enquanto o audit log é mantido (imutável, usuario_id via SET NULL).

## R2 — LGPD art. 20 (direito à portabilidade)

O titular tem direito de receber seus dados pessoais em formato estruturado, de uso comum e leitura por máquina, podendo transferi-los a outro fornecedor. A exportação deve incluir todos os dados do titular (perfil, experiências, formações, projetos, idiomas, certificações, competências, inscrições) mas NUNCA a senha.

## R3 — Posse de dados (ciclo 001)

A posse é resolvida comparando `usuario.id` (sub do JWT) com o id do recurso. Endpoints de exclusão/portabilidade só são acessíveis pelo próprio titular (`usuario.id` do token == dono dos dados). Papel RH não pode acessar dados de outro titular por esses endpoints.

## R4 — Auditoria imutável (ciclo 002)

A tabela `auditoria_seguranca` (V3) tem FK `usuario_id` com `ON DELETE SET NULL`. Registros de auditoria são imutáveis: mesmo após exclusão do titular, os registros mantêm `usuario_id` preenchido (não são excluídos nem anonimizados pela retenção).

## R5 — Política de retenção

LGPD art. 16 (minimização): dados pessoais não devem ser mantidos além do necessário. Classe de retenção identifica registros inativos (baseado em `data_criacao` ou `data_atualizacao` do perfil) elegíveis para purga/anonymização, usando propriedade `app.lgpd.retention.dias-inatividade`. Tabelas de auditoria são excluídas da purga.

## R6 — Documentação de segurança

Open source best practice: repositório deve conter `SECURITY.md` (política de divulgação de vulnerabilidades), template de PR com itens de segurança/LGPD, e checklist de conformidade LGPD. Esses arquivos são documentação do ciclo, não código de produção.

## Decisões de escopo

| Decisão | Resolução |
|---------|-----------|
| Exclusão exclui o usuario ou só desvincula o perfil? | Desvincula (perfil_id=null); se o usuario não tiver outros perfis vinculados, pode ser excluído também. |
| Exportação inclui senha? | Nunca (FR-002). |
| RH pode exportar dados de outro titular? | Não — só o próprio titular (FR-003). |
| Onde fica o checklist de conformidade? | `specs/003-lgpd-governance/checklist-conformidade.md` (docs do ciclo). |
| Novas dependências? | Nenhuma — usa Spring Data JPA cascade/orphanRemoval já presente. |