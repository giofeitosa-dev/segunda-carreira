# AgeBlind / Segunda Carreira Constitution

## Core Principles

### I. Security-by-default (NON-NEGOTIABLE)

Nenhum endpoint nasce `permitAll` sem justificativa registrada na spec. Regras obrigatórias:

- Todo recurso que expõe dado pessoal (perfil, currículo, inscrição, revisão) exige autenticação e checagem de posse (o `perfilId` do token deve ser o dono do recurso).
- Credenciais nunca em default de produção: `JWT_SECRET`, `DB_PASSWORD` e similares devem falhar (fail-fast) se ausentes no profile `prod`.
- Princípios de mínimo-privilégio e falha-segura: papel mínimo necessário por rota; erro de auth responde problem+json (RFC 7807) sem vazar detalhes internos.
- Endpoints de leitura pública (vagas, trilhas, mentorias, busca externa) são permitidos por decisão explícita de produto — documentar sempre na matriz de acesso da spec.

### II. Privacidade & LGPD primeiro

- Dado pessoal minimizado: não persistir texto completo de currículo no módulo Escudo (convenção existente — mantida).
- Toda decisão automatizada que afeta o titular expõe `caminhoParaRevisao` (LGPD art. 20) — verificado por teste (`LgpdRevisaoCaminhoTest`).
- Direitos do titular (LGPD art. 18) devem ser implementáveis: portabilidade (`GET /meus-dados`) e exclusão/anonimização (`DELETE /minha-conta`).
- Seeds e dados de demo nunca contêm dados pessoais reais.

### III. API-first — contrato antes do código

- `openapi.yaml` é a fonte de verdade dos contratos; mudança de contrato passa por spec + atualização do YAML + `ContratoOpenApiTest` verde.
- Tags do springdoc e escopo do contrato AgeBlind (contagem de paths) devem permanecer sincronizados — mudanças na superfície de API atualizam ambos no mesmo commit.
- Documentação de uso (READMEs com curl) é atualizada no mesmo PR da mudança de contrato.

### IV. Test-first

- `./mvnw clean verify` verde é pré-condição de merge (suite atual: 93+ testes, H2 em memória).
- Regressão anti-viés (Escudo) e testes de contrato nunca são removidos para "passar no build" — só reescritos com justificativa na spec.
- Front: `npm test` (Vitest) e `npm run e2e` (Playwright, stack docker) verdes quando `ageblind-front/` é tocado.
- Commit indivisível = suite verde: quebras de contrato (ex.: troca Basic→JWT) e seus testes no mesmo commit.

### V. Simplicity / YAGNI

- Monolito modular Spring Boot mantido (`carreira`, `escudo`, `shared`, `auth`); separação em serviços novos só com justificativa na tabela *Complexity Tracking* do plan.md.
- Começar simples: sem refresh token, sem OAuth, sem fila, sem cache — a menos que uma spec explicitamente exija.
- Dependências novas precisam de justificativa (peso, manutenção, segurança).

## Restrições de Stack

- Java 17 · Spring Boot 4.1.1 · Maven (wrapper) · Spring Data JPA · SpringDoc OpenAPI.
- Banco: H2 em memória (dev/teste) · PostgreSQL 16 (prod, docker-compose).
- Migrações: Flyway obrigatório para qualquer mudança de schema (`db/migration/V<n>__*.sql`). `ddl-auto=update` não é aceito como mecanismo principal em feature nova — apenas como safety legado do profile prod até migração completa.
- Idioma de código, docs, mensagens de erro e commits: PT-BR (nomes de pacote/classe em inglês já estabelecidos — manter o padrão existente por arquivo).
- Segredos sempre via env (`${VAR:default-somente-demo}`); defaults fracos só em profile demo/teste.

## Development Workflow & Quality Gates

- Ciclo Spec-Driven por feature: `create-new-feature.ps1` → `/speckit.specify` → **gate: usuário aprova spec** → `/speckit.plan` → **gate: usuário aprova plan** → `/speckit.tasks` → `/speckit.implement`.
- Trabalho incremental e contínuo: 1 commit = 1 tarefa = suite verde; Commits Conventional (`feat(api):`, `test(front):`, `docs(spec):`, `ci:`).
- 1 branch por ciclo SDD + 1 PR por ciclo; merge só com gates do item anterior + CI verde (`.github/workflows/ci.yml`).
- Checklist de PR (`ageblind-api/pr-checklist.md`) revisado em todo PR; itens de segurança/LGPD entram no checklist a partir do ciclo 003.
- Dívida técnica conhecida é anotada (como `ddl-auto=update`) e nunca silenciosamente expandida.

## Governance

- Esta Constitution supersede qualquer prática conflitante em specs, plans ou código.
- Emendas: requerem PR dedicado, atualização de versão (`MAJOR` quebra princípio · `MINOR` adiciona · `PATCH` esclarece), data de ratificação e aprovação do usuário.
- Todo `plan.md` passa pelo *Constitution Check* antes do Phase 0 research — violações devem ser justificadas na tabela *Complexity Tracking* ou o plano é refeito.
- Checklists de conformidade (`/speckit.checklist`) são gerados por feature de risco (segurança, LGPD) e revisados pelo usuário antes do merge.

**Version**: 1.0.0 | **Ratified**: 2026-11-17 | **Last Amended**: 2026-11-17
