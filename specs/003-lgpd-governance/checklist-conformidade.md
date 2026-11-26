# Checklist de Conformidade LGPD (003-lgpd-governance)

**Cycle**: 003-lgpd-governance | **Date**: 2026-11-22

## Direitos do titular (LGPD art. 18 a 22)

| Item | Descrição | Status |
|------|-----------|--------|
| LGPD-01 | Direito à exclusão (art. 18, VI): endpoint `DELETE /api/v1/meus-dados` implementado | ⬜ |
| LGPD-02 | Exclusão cascata: perfil + todas as tabelas filhas excluídas | ⬜ |
| LGPD-03 | Usuário desvinculado (perfil_id=null) após exclusão do perfil | ⬜ |
| LGPD-04 | Idempotência: exclusão sem perfil retorna 200 sem erro | ⬜ |
| LGPD-05 | Direito à portabilidade (art. 20): endpoint `GET /api/v1/meus-dados/exportacao` implementado | ⬜ |
| LGPD-06 | Exportação em formato estruturado (JSON) | ⬜ |
| LGPD-07 | Exportação NUNCA inclui senha ou hash | ⬜ |
| LGPD-08 | Exportação só para o próprio titular (posse via JWT) | ⬜ |
| LGPD-09 | Acesso de não-titular retorna 403 | ⬜ |

## Auditoria e accountability (LGPD + ciclo 002)

| Item | Descrição | Status |
|------|-----------|--------|
| AUD-01 | Audit log (`auditoria_seguranca`) imutável após exclusão do titular | ⬜ |
| AUD-02 | usuario_id mantido em registros de auditoria (SET NULL na V3) | ⬜ |
| AUD-03 | Exclusão não toca tabela de auditoria | ⬜ |

## Política de retenção (LGPD art. 16)

| Item | Descrição | Status |
|------|-----------|--------|
| RET-01 | Classe de retenção identifica registros inativos elegíveis para purga | ⬜ |
| RET-02 | Propriedade `app.lgpd.retention.dias-inatividade` configurável | ⬜ |
| RET-03 | Tabelas de auditoria excluídas da purga (imutáveis) | ⬜ |

## Documentação de segurança

| Item | Descrição | Status |
|------|-----------|--------|
| SEC-01 | `SECURITY.md` existe na raiz com política de vulnerabilidades | ⬜ |
| SEC-02 | `.github/pull_request_template.md` existe com itens segurança/LGPD | ⬜ |
| SEC-03 | `specs/003-lgpd-governance/checklist-conformidade.md` existe | ⬜ |
| SEC-04 | `openapi.yaml` atualizado com endpoints LGPD | ⬜ |

## Testes

| Item | Descrição | Status |
|------|-----------|--------|
| TST-01 | `./mvnw clean verify` verde (suite existente + novos testes) | ⬜ |
| TST-02 | Teste de exclusão cascata (integração) | ⬜ |
| TST-03 | Teste de exportação/portabilidade (integração) | ⬜ |
| TST-04 | Teste de 403 para não-titular (integração) | ⬜ |
| TST-05 | Teste de retenção (unitário) | ⬜ |

---
**Como usar**: Marcar cada item como ✅ após implementação e verificação. Este checklist é revisado antes de cada PR do ciclo 003.