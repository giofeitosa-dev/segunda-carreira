# 🔹 Sprint 1 — Planejamento: Arquitetura e definição das rotas

**Status:** ✅ concluída em 30/09/2026 · pendente aprovação da equipe (cards S1-10, S1-13)
**Ferramentas usadas:** docs Markdown + `kanban.md` (equivalente a Notion/Trello — `../kanban\trello-import.csv` importa no Trello)

---

## 1. Decisões de arquitetura

| # | Decisão | Rationale |
|---|---|---|
| 1 | **Monolito modular** — 1 app Spring Boot, módulos `carreira` e `escudo` | hackathon: velocidade; módulos isolados facilitam extrair microserviço depois |
| 2 | Prefixo `/api/v1` + erros **RFC 7807** + paginação padrão | contrato único e previsível |
| 3 | Comunicação interna só via **interfaces `Gateway`** | trocar implementação por REST depois sem quebrar nada |
| 4 | Faixa etária **nunca como idade exata** (`ATE_39 / 40_49 / 50_MAIS`) | privacy by design |
| 5 | Stack **Java 17 + Spring Boot 4.1.1 + Maven + JPA + SpringDoc** | stack aprovada (Boot 3 → 4 por disponibilidade no Initializr; API inalterada) |
| 6 | Dados demo em **H2**, ideal **PostgreSQL** | sprint sem Docker |
| 7 | Idade **nunca entra no ranking de vagas** — só competências | é o princípio do produto |

**Detalhamento completo:** [`../07-ARQUITETURA.md`](../07-ARQUITETURA.md) (pacotes, modelo de dados, riscos)

## 2. Definição das rotas (15)

### Tag `Carreira` — 10 rotas

| # | Método | Rota | Descrição |
|---|---|---|---|
| 1 | `POST` | `/api/v1/perfis` | Cadastra perfil (sem data de nascimento) |
| 2 | `GET` | `/api/v1/perfis/{id}` | Consulta perfil |
| 3 | `POST` | `/api/v1/perfis/{id}/competencias-transferiveis` | Experiência antiga → competências da área alvo |
| 4 | `GET` | `/api/v1/vagas` | Explora vagas (`area`, `modelo`, paginação) |
| 5 | `GET` | `/api/v1/vagas/recomendadas` | Ranking **por competências, sem idade** |
| 6 | `GET` | `/api/v1/vagas/{id}` | Detalhe da vaga |
| 7 | `GET` | `/api/v1/trilhas/{area}` | Capacitações curtas |
| 8 | `POST` | `/api/v1/inscricoes` | Inscrição simplificada |
| 9 | `POST` | `/api/v1/webhooks/lembretes` | Alerta de prazo |
| 10 | `GET` | `/api/v1/mentorias` | Encaixe com mentores/pares |

### Tag `Escudo Anti-Etarismo` — 5 rotas

| # | Método | Rota | Descrição |
|---|---|---|---|
| 11 | `POST` | `/api/v1/curriculo/age-blind` | **Gera** versão sem proxies de idade (15 regras) |
| 12 | `POST` | `/api/v1/curriculo/auditoria` | Score ATS + sinais que revelam idade |
| 13 | `POST` | `/api/v1/vagas/auditoria-linguagem` | Termos discriminatórios + risco legal |
| 14 | `GET` | `/api/v1/funil/metricas` | Aprovação por faixa + adverse impact (razão < 0,80) |
| 15 | `POST` | `/api/v1/revisao/solicitacao` | Revisão humana (LGPD art. 20) |

**Exemplos de request/response e deltas:** [`../08-ROTAS.md`](../08-ROTAS.md) (seções B e C)
**Especificação máquina:** [`../openapi.yaml`](../openapi.yaml) — filtrar pelas tags `Carreira` e `Escudo Anti-Etarismo`

## 3. Entregáveis da Sprint 1

- [x] Arquitetura documentada (`07-ARQUITETURA.md`)
- [x] Rotas definidas (`08-ROTAS.md`)
- [x] OpenAPI 3.0 validado (15 das 29 rotas pertencem ao AgeBlind)
- [x] Kanban (`kanban.md` + CSV importável no Trello/Notion)
- [ ] Aprovação da equipe → libera a Sprint 2

## 4. Critério para sair da Sprint 1

> Equipe aprova arquitetura + rotas; `openapi.yaml` importa sem erro; backlog da Sprint 2 refinado com estimativas.
