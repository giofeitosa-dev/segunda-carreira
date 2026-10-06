# 🔹 Sprint 1 — Planejamento: Arquitetura e definição das rotas

**Status:** ✅ concluída em 30/09/2026 · **aprovada pela equipe em 03/10/2026** (cards S1-10 e S1-13 fechados)
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

 **Detalhamento completo:** `07-ARQUITETURA.md` (pacotes, modelo de dados, riscos — no repo Reconecta pai)

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

 **Exemplos de request/response e deltas:** `08-ROTAS.md` (seções B e C — no repo Reconecta pai)
**Especificação máquina:** [`../openapi.yaml`](../openapi.yaml) — filtrar pelas tags `Carreira` e `Escudo Anti-Etarismo`

## 3. Entregáveis da Sprint 1

- [x] Arquitetura documentada (`07-ARQUITETURA.md`)
- [x] Rotas definidas (`08-ROTAS.md`)
- [x] OpenAPI 3.0 validado (15 das 29 rotas pertencem ao AgeBlind)
- [x] Kanban (`kanban.md` + CSV importável no Trello/Notion)
- [x] Aprovação da equipe → libera a Sprint 2 (aprovada em 03/10/2026, ver §5)

## 4. Critério para sair da Sprint 1

> Equipe aprova arquitetura + rotas; `openapi.yaml` importa sem erro; backlog da Sprint 2 refinado com estimativas.

## 5. Registro de aprovação (03/10/2026) — S1-10 e S1-13

Revisão cruzada: docs de planejamento × código entregue (Sprint 2/3) × `openapi.yaml`.

**S1-10 — Aprovar arquitetura (`07-ARQUITETURA.md`): ✅ APROVADA**

- ✔ Monolito modular com fronteira por pacote e portas `Gateway` (`PerfilGateway`, `VagaGateway`) — regra "nenhum módulo acessa o banco do outro" respeitada.
- ✔ Modelo de dados Carreira confere com as entidades; enum de faixa `ATE_39|40_49|50_MAIS` nunca armazena idade exata.
- Delta 1 (stack): Boot **4.1.1** (doc: 3.x) e SpringDoc **3.1.1** (doc: 2.x) — Boot 4 é a versão estável atual; API compatível.
- Delta 2 (banco): H2 **mem** com `DB_CLOSE_DELAY=-1` em vez de file mode (demo efêmera; file mode segue como evolução junto do PostgreSQL ideal).
- Delta 3 (estrutura): código em `ageblind-api/api\`, pacote `com.reconecta.ageblind`, camadas `service`/`gateway` (doc: `application`/`infrastructure`) — mesma intenção, nomes mais enxutos.
- Delta 4 (escopo): entregues os módulos **Carreira + Escudo + shared**; Relatos e Rotas são módulos irmãos do Reconecta, fora desta API.
- Delta 5 (persistência Escudo): `versao_curriculo` e `auditoria_*` **não persistidas** — coerente com a convenção LGPD do próprio doc §5 ("não persistir texto completo no MVP"); persistem apenas `metrica_funil` e `solicitacao_revisao`.
- Delta 6 (profiles/integrações): profile `seed` (doc: `demo`); integrações externas resolvidas com CSV seed (20 vagas e 5 trilhas, acima dos 10/3 previstos).

**S1-13 — Aprovar rotas (`08-ROTAS.md` + deltas): ✅ APROVADA**

- ✔ 15 rotas AgeBlind (10 Carreira + 5 Escudo) = `openapi.yaml` = runtime, garantido por `ContratoOpenApiTest`.
- Delta 1: `GET /vagas` → **400 RFC 7807** em `page` negativo (doc não especificava).
- Delta 2: `GET /funil/metricas` ganhou o parâmetro **`periodo`**.
- Delta 3: respostas de decisão acrescentadas de `competenciasFaltantes` e **`caminhoParaRevisao`** (LGPD art. 20), campo novo no doc.
- Delta 4: `GET /trilhas/{area}` responde `200` + lista vazia quando não há resultado (sem 404), conforme spec.
- ✔ Sucessos conforme catálogo: `POST /revisao/solicitacao` → 201, `POST /webhooks/lembretes` → 202.
- Rotas Relatos (8) + Rotas (6) confirmadas como **fora do escopo desta API** (outros módulos do Reconecta).
