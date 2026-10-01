# 🗂️ Kanban — Sprint 1 (Planejamento: Arquitetura e Rotas)

> Quadro da equipe · atualizado em 30/09/2026
> **WIP limit:** 3 tarefas por coluna · **Sprint goal:** aprovar arquitetura e contrato de rotas para iniciar o desenvolvimento
> Importar para Trello/Notion: `trello-import.csv`

---

## 🏁 Sprint Goal

> *Ter a arquitetura decidida e as 29 rotas definidas e especificadas em OpenAPI, com o quadro de tarefas organizado, para que a Sprint 2 comece o scaffold sem retrabalho.*

**Critério de sucesso:** `openapi.yaml` importa sem erro em ferramenta + equipe aprova `07-ARQUITETURA.md`.

---

## 📋 Backlog (priorizado, fora da Sprint 1)

| ID | Tarefa | Labels | Pontos |
|---|---|---|---|
| S2-01 | Scaffold do projeto Spring Boot (`api\` + Maven wrapper) | backend | 3 |
| S2-02 | Camada transversal: ProblemDetail, validação, paginação, SpringDoc | backend | 3 |
| S2-03 | Seed de dados demo (relatos, vagas, paradas, trilhas) | dados | 5 |
| S2-04 | Motor de regras: `proxies-idade.json` + `termos-discriminatorios.json` | escudo | 5 |
| S2-05 | Front simples para demo (opcional) | frontend | 8 |
| S2-06 | Configurar CI (build + testes) | infra | 3 |

---

## 📝 A Fazer

| ID | Tarefa | Labels | Pontos |
|---|---|---|---|
| S1-11 | Definir dataset seed mínimo por módulo (quantos relatos, vagas, paradas) | dados | 2 |
| S1-12 | Congelar enums (categorias, status, faixas etárias) no arquivo de config compartilhado | backend | 1 |

---

## 🔄 Em Andamento

| ID | Tarefa | Responsável | Labels | Pontos |
|---|---|---|---|---|
| S1-10 | Revisão da equipe: aprovar `07-ARQUITETURA.md` (monolito modular + gates) | equipe | docs | 2 |
| S1-13 | Revisão da equipe: aprovar `08-ROTAS.md` + deltas dos rascunhos | equipe | docs | 2 |

---

## 🔍 Revisão / Bloqueadas

| ID | Tarefa | Bloqueio |
|---|---|---|
| — | — | — |

---

## ✅ Concluído

| ID | Tarefa | Entregável | Pontos |
|---|---|---|---|
| S1-01 | Design thinking completo (Empatia→Definição→Ideação) | `01`,`02`,`03` | 8 |
| S1-02 | Pesquisa/aprofundamento etarismo (base legal + 15 regras age-blind) | `05-ETARISMO.md` | 5 |
| S1-03 | Panorama de concorrência + diferenciais | `06-CONCORRENCIA.md` | 3 |
| S1-04 | Decisão de arquitetura (monolito modular, 3 contextos, gates) | decisão registrada na ata | 2 |
| S1-05 | Documento de arquitetura (stack, pacotes, dados, integrações, riscos) | `07-ARQUITETURA.md` | 5 |
| S1-06 | Catálogo oficial de rotas (29 rotas + deltas) | `08-ROTAS.md` | 3 |
| S1-07 | Especificação OpenAPI 3.0 | `openapi.yaml` | 5 |
| S1-09 | Kanban da sprint (md + CSV importável) | `kanban\` | 2 |
| S1-08 | Validar `openapi.yaml` (parse OK: 29 paths, 35 schemas) | `openapi.yaml` | 1 |

**Total Sprint 1:** 39 pontos concluídos · 4 em andamento/a fazer · WIP respeitado

---

## 📅 Previsão da Sprint 2 (setup + Módulo Relatos)

1. S2-01 Scaffold → 2. S2-02 Transversal → 3. S2-03 Seed →
4. CRUD de relatos completo (endpoints 1–8) → 5. Testes do serviço de relatos
**Definição de pronto (DoD):** `./mvnw clean verify` verde + Swagger documentado + demo do fluxo cidadão.

---

## 📏 Regras do quadro

- Máximo **3 cards** em "Em Andamento" por pessoa.
- Card só passa para "Concluído" com entregável linkado.
- Tarefa bloqueada → vai para "Revisão/Bloqueadas" com motivo.
- Tarefas novas entram sempre no "Backlog" e são puxadas por prioridade.
