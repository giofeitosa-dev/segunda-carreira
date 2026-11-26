# Quickstart: Governança LGPD (003-lgpd-governance)

**Cycle**: 003-lgpd-governance | **Date**: 2026-11-22

## Seção 1 — Rodar a API localmente

```bash
cd ageblind-api/api
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
# → Health:   http://localhost:8080/actuator/health
# → Swagger:  http://localhost:8080/swagger-ui.html (desabilitado em prod)
```

## Seção 2 — Rodar os testes

```bash
./mvnw clean verify
```

Todos os testes devem passar (suite existente + novos testes de exclusão/exportação/retenção).

## Seção 3 — Verificar endpoints LGPD

### Exclusão cascata (DELETE /api/v1/meus-dados)

```bash
# Token do titular (obtido via POST /api/v1/auth/login)
curl -X DELETE http://localhost:8080/api/v1/meus-dados \
  -H "Authorization: Bearer <TOKEN>" \
  -H "X-Correlation-Id: teste-exclusao-001"
# → 200 (exclusão bem-sucedida) ou 200 (idempotente, sem perfil)

# Tentar acessar perfil após exclusão → 404
curl http://localhost:8080/api/v1/perfis/<ID> \
  -H "Authorization: Bearer <TOKEN>"
# → 404
```

### Portabilidade (GET /api/v1/meus-dados/exportacao)

```bash
curl http://localhost:8080/api/v1/meus-dados/exportacao \
  -H "Authorization: Bearer <TOKEN>" \
  -H "X-Correlation-Id: teste-exportacao-001"
# → 200 com JSON dos dados pessoais (sem senha)
```

### Acesso não-titular → 403

```bash
# Token de outro usuário (ex.: RH tentando acessar dados de candidato)
curl -X DELETE http://localhost:8080/api/v1/meus-dados \
  -H "Authorization: Bearer <TOKEN_OUTRO_USUARIO>"
# → 403 (só o titular pode excluir/exportar seus próprios dados)
```

## Seção 4 — Verificar retenção

```bash
# Teste unitário da classe de retenção (coberto por ./mvnw verify)
# RetencaoConfig.elegivelParaPurga(perfil, diasInatividade)
```

## Seção 5 — Verificar documentação

```bash
# Arquivos devem existir na raiz do repositório
ls SECURITY.md
ls .github/pull_request_template.md
ls specs/003-lgpd-governance/checklist-conformidade.md
```

## Seção 6 — CI

Push na branch `003-lgpd-governance` dispara o CI (GitHub Actions):
- API (Spring Boot): `./mvnw -B -ntp verify`
- Dependency audit: `./mvnw -B -ntp dependency:audit`
- Gitleaks: secret scan
- Front (Vite): lint + testes + build
- E2E (Playwright): testes E2E
- Docker: build da imagem

## Checklist de conclusão

- [ ] `./mvnw clean verify` verde
- [ ] `DELETE /api/v1/meus-dados` retorna 200 e exclui perfil+filhas
- [ ] `GET /api/v1/meus-dados/exportacao` retorna JSON sem senha
- [ ] Acesso de não-titular retorna 403
- [ ] `SECURITY.md`, `pull_request_template.md`, `checklist-conformidade.md` existem
- [ ] `openapi.yaml` atualizado com os 2 endpoints LGPD
- [ ] `specs/ROADMAP.md` status 003 atualizado (após merge)