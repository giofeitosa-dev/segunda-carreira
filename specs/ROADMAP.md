# ROADMAP — Ciclos Spec-Driven (Segurança · Governança · LGPD)

> Índice mestre dos ciclos SDD. Atualizar o status a cada merge de ciclo.
> Constitution que rege todos os ciclos: [`.specify/memory/constitution.md`](../.specify/memory/constitution.md)

## Estado geral

| Ciclo | Feature | Escopo | Branch | Status |
|---|---|---|---|---|
| 0 | Governança base | Constitution v1.0.0 + este roadmap | `spec/governance-base` | 🔄 em execução |
| 001 | `001-jwt-security-baseline` | JWT completo (cadastro/login/roles), autorização com posse, hardening baseline (headers, CORS, rate limit, secret fail-fast), contrato bearer | `001-jwt-security-baseline` | ✅ concluído (commits C1.1–C1.8) |
| 002 | `002-observability-audit` | Audit log de segurança, correlation-id/logging estruturado, actuator hardening, supply-chain CI (Dependabot, dependency audit, gitleaks) | `002-observability-audit` | ✅ concluído (commits C2.1–C2.6) |
| 003 | `003-lgpd-governance` | Direitos do titular (portabilidade + exclusão cascata), política de retenção, SECURITY.md + pr-checklist, checklist de conformidade | `003-lgpd-governance` | ✅ concluído (commits C3.1–C3.6) |

## Decisões de escopo travadas (2026-11-17)

- **Leitura pública mantida**: `GET /vagas`, `/vagas/{id}`, `/trilhas/**`, `/mentorias`, `/vagas/busca-externa` permanecem sem autenticação.
- **Papel EMPRESA adiado**: fora do escopo do ciclo 001 (não há endpoint de publicação de vaga hoje). Definir em spec futura.
- **Auth**: JWT HS256 (env `JWT_SECRET`, `JWT_EXPIRATION`), roles `CANDIDATO` | `RH` | `EMPRESA`(futura), BCrypt, remove HTTP Basic.
- **Ciclos incrementais**: 1 branch + 1 PR por ciclo; commits granulares internos (ver `tasks.md` de cada ciclo).

## Padrão de execução de cada ciclo

```
create-new-feature.ps1 → spec.md ──GATE──▶ plan.md ──GATE──▶ tasks.md → commits C*.n → PR → merge
```

1. **GATE spec**: usuário aprova `specs/<ciclo>/spec.md`.
2. **GATE plan**: usuário aprova `specs/<ciclo>/plan.md` (+ research, data-model, contracts).
3. **Implementação**: commits indivíduais, cada um com `./mvnw clean verify` verde (+ `npm test`/`npm run e2e` quando toca front).
4. **PR**: CI verde + checklist → merge → atualizar status nesta tabela.

## Registro de conclusões

| Data | Ciclo | Commit de fechamento | Notas |
|---|---|---|---|
| 2026-11-18 | 001 | d5c6865 | C1.1–C1.8 implementados: JWT cadastro/login, autorização por papel+posse, hardening (rate limit, CORS, headers, swagger off em prod), front login/Bearer/guarda, E2E sem httpCredentials, READMEs JWT. 117/117 testes API verdes, 22/22 front verdes. |
| 2026-11-22 | 002 | 1bcd1a3 | C2.1–C2.6 implementados: auditoria de segurança (entidade, migração V3, interceptor, endpoint /auditoria só RH), correlation-id + logging JSON, actuator hardening (só health público em prod), supply-chain CI (dependency:audit, gitleaks, Dependabot). 127 testes API verdes. |
| 2026-11-26 | 003 | 2bdabfe | C3.1–C3.6 implementados: infra LGPD (DTO exportacao, classe retentao, migracao V4), exclusao cascata DELETE /meus-dados, portabilidade GET /meus-dados/exportacao, SECURITY.md, PR template, checklist conformidade LGPD. Suite API verde. |
