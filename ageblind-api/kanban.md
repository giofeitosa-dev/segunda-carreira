# 🗂️ Kanban — AgeBlind API (3 sprints)

> Quadro único do projeto · atualizado em 30/09/2026
> **WIP:** 2 cards em "Em Andamento" (S1-10, S1-13 — aprovações da equipe) · import para Trello/Notion: [`../kanban/trello-import.csv`](../kanban/trello-import.csv) (sprint 1) ou copie as listas abaixo

---

## ✅ Concluído — Sprint 1 (Planejamento)

| ID | Card | Entregável |
|---|---|---|
| S1-01 | Design thinking completo | `01`,`02`,`03` (raiz) |
| S1-02 | Pesquisa etarismo + 15 regras | `05-ETARISMO.md` |
| S1-03 | Concorrência + diferenciais | `06-CONCORRENCIA.md` |
| S1-04 | Decisão: monolito modular | `07-ARQUITETURA.md` |
| S1-05 | Arquitetura (pacotes, dados, riscos) | `07-ARQUITETURA.md` |
| S1-06 | Catálogo de rotas (29 / 15 do AgeBlind) | `08-ROTAS.md` |
| S1-07 | OpenAPI 3.0 validado | `openapi.yaml` |
| S1-09 | Kanban das sprints | este arquivo + CSV |
| S1-11 | Dataset seed mínimo (10 perfis, 20 vagas, 5 trilhas, fixtures) | entregue como **D-20** |

## 🔄 Em Andamento

| ID | Card | Responsável | h |
|---|---|---|---|
| S1-10 | Aprovar arquitetura (`07-ARQUITETURA.md`) | equipe | 2 |
| S1-13 | Aprovar rotas (`08-ROTAS.md` + deltas) | equipe | 2 |

## ✅ Sprint 2 — CONCLUÍDA em 30/09/2026 (D-01…D-22)

| ID | Card | Evidência |
|---|---|---|
| D-01 | Spring Initializr + Maven wrapper | `mvnw clean package` → **BUILD SUCCESS**, 6 testes verdes |
| D-02 | Pacotes `carreira`, `escudo`, `shared` | árvore em `api\src\main\java` |
| D-03 | Transversal: ProblemDetail + validação + **paginação** | `GlobalExceptionHandler` (RFC 7807, +404/400 params) + `PaginaResponse` `{content,page,size,totalElements,totalPages}` |
| D-04 | SpringDoc + título "AgeBlind API" | `/swagger-ui.html` 200 · `/v3/api-docs` → `AgeBlind API 1.0.0` |
| D-05 | Enums compartilhados | 11 enums + `FaixaEtariaTest` (códigos oficiais `ATE_39/40_49/50_MAIS`) |
| D-06 | Entidades Carreira | 8 entidades + 6 repositórios + `PerfilRepositoryTest` (inclui guarda "sem campos de idade") |
| D-07 | `POST/GET /perfis` | 201+Location, 400+fieldErrors, 404 ProblemDetail — 4 testes MockMvc |
| D-14 | Motor de regras age-blind | `regras-idade.json` (15 regras: 10 automáticas + 5 manuais) + `MotorRegrasIdade` (aplicar/detectar) |
| D-15 | `POST /curriculo/age-blind` | remove nascimento, ano de graduação, total de anos, certificados, adjetivos — regex tolerantes a sem acento |
| D-16 | `POST /curriculo/auditoria` | score ATS (100 − penalidades por severidade) + sinais com `localizacao`/`severidade`/`acao` + sugestões — 3 testes |
| D-17 | `POST /vagas/auditoria-linguagem` | `termos-discriminatorios.json` (6 termos × padrões regex) → risco BAIXO/MEDIO/ALTO + baseLegal (CLT 373-A, Lei 9.029) + reescrita — 3 testes |
| D-18 | `GET /funil/metricas` | `MetricaFunil` (agregado anônimo por faixa) + `VagaGateway` (fronteira de módulo) + razão < 0,80 → alerta — 4 testes |
| D-19 | `POST /revisao/solicitacao` | LGPD art. 20: protocolo `REV-AAAA-XXXXXX`, status `PENDENTE`, `PerfilGateway` valida titular — 3 testes |
| D-08 | `POST /perfis/{id}/competencias-transferiveis` | `sinonimos.json` (10 grupos) + Jaccard com piso 0,90 por sinônimo → `de`/`para`/`similaridade` + `gaps` + `trilhaSugerida` — 2 testes |
| D-09 | `GET /vagas` + `GET /vagas/{id}` + seed CSV | `VagaService` com filtros area/modelo + paginação custom + `vagas-seed.csv` (20 vagas, 6 áreas, profile `seed`) — 5 testes |
| D-10 | `GET /vagas/recomendadas` ⭐ | ranking **só por competências** (`Σnível / 5×requisitos`, desempate por id) + teste anti-viés (JSON sem termos etários + determinístico) — 3 testes |
| D-11 | `GET /trilhas/{area}` | `TrilhaService` (id string conforme contrato, módulos ordenados) + `trilhas-seed.csv` (3 trilhas) — 2 testes |
| D-12 | `POST /inscricoes` + `POST /webhooks/lembretes` | 201/202 + validação perfil/vaga/trilha (404) + `LembreteJob` `@Scheduled` (dispara prazo dentro da antecedência) — 3 testes |
| D-13 | `GET /mentorias` | `MentoriaService`: filtro `area` (case-insensitive) ou `perfilId`→`areaAlvo`, ordenado por id; 404 perfil inexistente — 3 testes |
| D-20 | Seed demo completo | 10 perfis + competências (`PerfilSeedRunner`), 5 trilhas (2 novas), currículos fixture (`curriculos/sinalizado.txt`/`otimizado.txt`) + `SeedDemoTest` (DB isolado) — bônus: 405/400 RFC 7807 no handler |
| D-21 | README da API | `api/README.md` — 15 rotas, como rodar (dev/seed/jar), 15 exemplos curl, seed, regras de ouro |
| D-22 | Branch `feat/*` + PR inicial | Repo `giofeitosa-dev/segunda-carreira` (15 commits Conventional) + PR #1 `feat/ageblind-api` → `main` |

> **Suite atual: 50/50 testes verdes** (`mvnw test` → BUILD SUCCESS) · **Sprint 2 CONCLUÍDA (D-01…D-22)** · smoke com jar + `--spring.profiles.active=seed`: 10 perfis, 20 vagas, 5 trilhas, recomendadas OK · **D-22:** repo GitHub `giofeitosa-dev/segunda-carreira` (privado), `main` = docs base, `feat/ageblind-api` = 11 commits da Sprint 2, PR #1 aberto → passa para Sprint 3

---

## 📋 Backlog — Sprint 2 (Desenvolvimento) · 58h · ✅ CONCLUÍDO

*Detalhes e dependências em [`SPRINT-2-DESENVOLVIMENTO.md`](SPRINT-2-DESENVOLVIMENTO.md)*

**Fase A — Setup** ✅ (D-01…D-05)
| ID | Card | h |
|---|---|---|
| D-01 | Spring Initializr + Maven wrapper | 1 |
| D-02 | Pacotes `carreira`, `escudo`, `shared` | 1 |
| D-03 | Transversal: ProblemDetail, validação, paginação | 3 |
| D-04 | SpringDoc + título "AgeBlind API" | 1 |
| D-05 | Enums compartilhados | 2 |

**Fase B — Carreira**
| ID | Card | h |
|---|---|---|
| D-06 | Entidades + enums CBO | 4 |
| D-07 | `POST/GET /perfis` | 2 |
| D-08 | Competências transferíveis | 4 |
| D-09 | `/vagas` list/detalhe + seed | 3 |
| D-10 | Recomendação sem idade ⭐ | 4 |
| D-11 | `/trilhas/{area}` | 2 |
| D-12 | Inscrições + lembretes `@Scheduled` | 3 |
| D-13 | `/mentorias` | 2 |

**Fase C — Escudo ⭐**
| ID | Card | h |
|---|---|---|
| D-14 | Motor de regras (JSON proxies + termos) | 4 |
| D-15 | `POST /curriculo/age-blind` (15 regras) | 4 |
| D-16 | `POST /curriculo/auditoria` | 4 |
| D-17 | `POST /vagas/auditoria-linguagem` | 3 |
| D-18 | `GET /funil/metricas` (adverse impact) | 4 |
| D-19 | `POST /revisao/solicitacao` (LGPD) | 2 |

**Fase D — Fechamento**
| ID | Card | h |
|---|---|---|
| D-20 | Seed demo completo | 3 |
| D-21 | README + curl examples | 2 |
| D-22 | Branch/PR inicial → Sprint 3 | 1 |

---

## 📋 Backlog — Sprint 3 (Testes e revisão)

*Detalhes em [`SPRINT-3-TESTES-REVISAO.md`](SPRINT-3-TESTES-REVISAO.md)*

| ID | Card | Tipo |
|---|---|---|
| T-01 | Testes unitários dos services (transferíveis, ranking, motor, adverse impact) | teste |
| T-02 | Testes de integração MockMvc — matriz dos 15 endpoints | teste |
| T-03 | Testes de regressão anti-viés (4 asserções) | teste |
| T-04 | Validação contrato `openapi.yaml` vs código | teste |
| T-05 | Cobertura ≥ 70% | métrica |
| T-06 | `.gitignore`, Conventional Commits, branch `feat/*` | git |
| T-07 | PRs revisados com checklist (revisor ≠ autor) | git |
| T-08 | README final + tag `v1.0.0` no GitHub | git |
| T-09 | Ensaio da demo (5 passos) | demo |

---

## 🚦 Fluxo do quadro

```
Backlog → A Fazer → Em Andamento → Revisão → Concluído
   ▲                                            │
   └────────── blockers voltam p/ Revisão ◄─────┘
```

**Regras:** máx. 3 em "Em Andamento" · card só fecha com entregável linkado · tarefa bloqueada vai para "Revisão" com motivo.
