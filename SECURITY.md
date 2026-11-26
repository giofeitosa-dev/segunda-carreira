# Política de Segurança — Segunda Carreira (AgeBlind API)

## Versões suportadas

| Versão | Suportada | Observação |
|--------|-----------|------------|
| v1.0.x | ✅ Sim | Última release estável (hackathon/ReCode) |
| < v1.0 | ❌ Não | Versões pré-lançamento sem suporte |

## Reportar uma vulnerabilidade

Se você encontrou um problema de segurança, reporte por e-mail para o mantenedor do projeto antes de abrir qualquer issue pública.

Não abra issues ou PRs detalhando a vulnerabilidade até que ela tenha sido avaliada e corrigida.

## Escopo

- `ageblind-api/` — API Spring Boot (endpoints, autenticação JWT, audit log, LGPD)
- `.github/` — workflows de CI/CD e templates de PR

## Política de divulgação

1. Recebemos o relatório e confirmamos o recebimento em até 5 dias úteis.
2. Avaliamos a gravidade e o impacto.
3. Fornecemos uma previsão de correção.
4. Publicamos o fix e creditamos o reporter (se desejado).

## Regras de boas práticas (contribuidores)

- Nunca commite segredos (senhas, tokens, `JWT_SECRET`, chaves de API).
- Endpoints de dados pessoais (`DELETE /api/v1/meus-dados`, `GET /api/v1/meus-dados/exportacao`) só são acessíveis pelo próprio titular (posse via `usuario.id` do JWT).
- A exportação de portabilidade NUNCA inclui senha ou hash.
- O audit log (`auditoria_seguranca`) é imutável: não é excluído nem purjado.
- Todo PR deve passar no checklist de segurança do `.github/pull_request_template.md`.
