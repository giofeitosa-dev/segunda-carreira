# Pull Request — AgeBlind API

## Resumo da mudança

Descreva o que este PR implementa/altera.

## Checklist de segurança e LGPD

- [ ] Nenhum segredo (senha, token, `JWT_SECRET`, chave API) foi commiteado
- [ ] Endpoints de dados pessoais respeitam posse via `usuario.id` do JWT (só o próprio titular)
- [ ] Exportação de portabilidade (`GET /api/v1/meus-dados/exportacao`) NUNCA inclui senha ou hash
- [ ] Audit log (`auditoria_seguranca`) não foi alterado para permitir exclusão/purga de registros
- [ ] Novos endpoints de dados pessoais foram adicionados ao `openapi.yaml`
- [ ] `./mvnw clean verify` executado localmente e verde
- [ ] Testes escritos antes da implementação (test-first, Constituição IV)

## Checklist de PR

- [ ] Conforme o escopo do ciclo/milestone vinculado
- [ ] Commits granulares, cada um com suite verde
- [ ] Documentação atualizada (README, specs, checklist de conformidade quando aplicável)
- [ ] Nenhuma alteração inesperada no front-end (este ciclo não toca `ageblind-front/`)

## Issue relacionada

Closes #
