# 🔹 Sprint 2 — Desenvolvimento: mão na massa

**Stack definida:** Java 17 (Zulu) · Spring Boot **4.1.1** · Maven · Spring Data JPA · SpringDoc 3.1.1 · H2 (demo) — Boot 4 em vez do 3 porque o Initializr só oferece 4.x (API equivalente); Node/Python ficam para o front/demo opcional.


**Sprint goal:** API respondendo com todos os 15 endpoints documentados no Swagger.

---

## Ordem de execução (backlog)

### Fase A — Setup (dia 1) — ✅ concluída em 30/09/2026

| ID | Tarefa | Estimativa | Pronto quando |
|---|---|---|---|
| D-01 | Spring Initializr: Web, JPA, Validation, H2, Actuator + Maven wrapper | 1h | ✅ `./mvnw spring-boot:run` sobe (build + teste verde) |
| D-02 | Estrutura de pacotes `com.reconecta.ageblind.{carreira,escudo,shared}` | 1h | ✅ árvore igual a `../07-ARQUITETURA.md` |
| D-03 | Camada transversal: ProblemDetail RFC 7807, validação, paginação | 3h | ✅ ProblemDetail + validação + `PaginaResponse` custom (fechado em D-09) |
| D-04 | SpringDoc + título **AgeBlind API** | 1h | ✅ `/swagger-ui.html` responde 200 com `info.title=AgeBlind API` |
| D-05 | Enums e config compartilhados (faixas, status, CBO) — card S1-12 | 2h | ✅ 11 enums + `FaixaEtariaTest` |

### Fase B — Módulo Carreira (dias 2–3) — D-06…D-13 ✅ em 30/09/2026

| ID | Tarefa | Estimativa | Depende de |
|---|---|---|---|
| D-06 | Entidades: `Perfil`, `Competencia`, `PerfilCompetencia`, `Vaga`, `Trilha`, `ModuloTrilha`, `Inscricao`, `Mentoria` + repos | 4h | ✅ concluído (6 testes verdes) |
| D-07 | `POST/GET /perfis` | 2h | ✅ DTOs validados + `PerfilControllerTest` (201/400/404) |
| D-08 | `CompetenciasTransferiveisService` (similaridade keyword/peso) | 4h | ✅ Jaccard + `sinonimos.json` (piso 0,90) + gaps + trilha — 2 testes |
| D-09 | `GET /vagas` + `GET /vagas/{id}` + CSV seed de vagas | 3h | ✅ `VagaService` + `PaginaResponse` + `vagas-seed.csv` (20 vagas, profile `seed`) — 5 testes |
| D-10 | Recomendação `GET /vagas/recomendadas` — **ranking só de competências** (teste: mesmo perfil com/sem idade não muda ordem) | 4h | ✅ `RecomendacaoVagaService` + regressão anti-viés — 3 testes |
| D-11 | `GET /trilhas/{area}` + seed de trilhas | 2h | ✅ `TrilhaController` + `trilhas-seed.csv` (3 trilhas + módulos) — 2 testes |
| D-12 | `POST /inscricoes` + `POST /webhooks/lembretes` (`@Scheduled`) | 3h | ✅ `InscricaoService` + `LembreteJob` + `SchedulingConfig` — 3 testes |
| D-13 | `GET /mentorias` | 2h | ✅ `MentoriaService` + `MentoriaController` — 3 testes |

### Fase C — Módulo Escudo (dias 4–5) ⭐ diferencial — ✅ concluída em 30/09/2026

| ID | Tarefa | Estimativa | Fonte |
|---|---|---|---|
| D-14 | Motor de regras: `regras-idade.json` (15 regras) | 4h | ✅ `05-ETARISMO.md` §5 + `MotorRegrasIdade` |
| D-15 | `POST /curriculo/age-blind` — 15 regras aplicadas | 4h | ✅ 10 automáticas + 5 manuais, 7 testes |
| D-16 | `POST /curriculo/auditoria` — score ATS + sinais + severidade | 4h | ✅ `AuditoriaCurriculoService` (score 100−20/10/5) + 3 testes |
| D-17 | `POST /vagas/auditoria-linguagem` — termos + risco legal (CLT 373-A, Lei 9.029) | 3h | ✅ `termos-discriminatorios.json` (6 termos) + 3 testes |
| D-18 | `GET /funil/metricas` — aprovação por faixa + `razaoAdverseImpact < 0,80` | 4h | ✅ `FunilMetricasService` + `VagaGateway` + 4 testes (404/400/razão 0,43/0,80) |
| D-19 | `POST /revisao/solicitacao` — protocolo LGPD art. 20 | 2h | ✅ `RevisaoController` + protocolo `REV-` + 404 via `PerfilGateway` — 3 testes |

### Fase D — Fechamento (dia 6)

| ID | Tarefa | Estimativa |
|---|---|---|
| D-20 | Seed completo demo (10 perfis, 20 vagas, 5 trilhas, currículos fixture) | 3h | ✅ `perfis-seed.csv` + `PerfilSeedRunner` + `curriculos/` (sinalizado/otimizado) + `SeedDemoTest` + bônus: handler 405/400 |
| D-21 | README da API: como rodar (`./mvnw spring-boot:run`) + exemplos curl | 2h | ✅ `api/README.md` — 15 rotas, seed, regras de ouro, 15 curls validados |
| D-22 | Preparar branch `feat/*` e PR inicial → passa para Sprint 3 | 1h | ✅ Repo `giofeitosa-dev/segunda-carreira` (recorte: API+ideação+kanban+etarismo), 15 commits Conventional, PR #1 `feat/ageblind-api` → `main` |

**Total estimado:** ~58h · WIP: máx. 3 cards em andamento

## Regras de ouro do desenvolvimento

1. **Nenhuma entidade tem `dataNascimento` ou `idade`** — só `faixaEtaria` quando necessário para métricas.
2. Ranking de vagas: entrada = competências + área; idade fora da fórmula.
3. Toda lista aceita `?page=&size=`; todo erro sai em RFC 7807.
4. Módulos falam entre si por `Gateway`, nunca por `@Autowired` do outro módulo direto no Repository.
5. Commit pequeno: 1 task = 1 commit, mensagem `feat(D-08): ...`.

**✔ Sprint 2 pronta quando:** os 15 endpoints funcionam no Swagger com exemplo de request/response e o seed demo carrega sozinho.
