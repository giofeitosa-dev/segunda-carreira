# 🔹 Sprint 3 — Testes e revisão do projeto (Git/GitHub)

**Sprint goal:** bateria verde (`./mvnw clean verify`), cobertura dos 15 endpoints e PR revisado no GitHub.

---

## 1. Testes dos endpoints

### 1.1 Camadas

| Camada | Ferramenta | O que cobre |
|---|---|---|
| Unitário | JUnit 5 + Mockito | Services: transferíveis, ranking, motor de regras, adverse impact |
| Integração | `@SpringBootTest` + `MockMvc` | cada um dos 15 endpoints (status + corpo) |
| Contrato | validação do `openapi.yaml` | request/response batem com a spec |

### 1.2 Matriz mínima (1 teste verde por linha)

| # | Endpoint | Cenário principal | Cenário de erro |
|---|---|---|---|
| 1 | `POST /perfis` | cria perfil válido | `400` sem `areaAlvo` |
| 2 | `GET /perfis/{id}` | retorna perfil | `404` id inexistente |
| 3 | `POST .../competencias-transferiveis` | PCP → Planejamento de Rotas (sim. ≥ 0,8) | `404` perfil |
| 4 | `GET /vagas` | pagina + filtra por `area` | `400` página negativa |
| 5 | `GET /vagas/recomendadas` | ≥3 vagas ranqueadas **com justificativa** | `404` perfil |
| 6 | `GET /vagas/{id}` | detalhe completo | `404` |
| 7 | `GET /trilhas/{area}` | 3 trilhas com módulos | `200` + lista vazia (contrato não prevê 404) |
| 8 | `POST /inscricoes` | criação + status `ENVIADA` | `400` tipo inválido |
| 9 | `POST /webhooks/lembretes` | `202 Accepted` | `400` sem `inscricaoId` |
| 10 | `GET /mentorias` | mentor da área | `404` perfil |
| 11 | `POST /curriculo/age-blind` | regras aplicadas ≥ 10 no fixture real | `400` currículo vazio |
| 12 | `POST /curriculo/auditoria` | marca ≥ 3 sinais de idade | `400` |
| 13 | `POST /vagas/auditoria-linguagem` | detecta `PERFIL_JOVEM` + `riscoLegal: ALTO` | `400` |
| 14 | `GET /funil/metricas` | razão < 0,80 → `alerta: true` | `404` vaga |
| 15 | `POST /revisao/solicitacao` | `201` + protocolo | `400` sem motivo |

### 1.3 Testes de regressão anti-viés (assinatura do produto)

- [x] **Idade não muda ranking:** mesmo perfil, 2 idades hipotéticas → mesma ordem de vagas. (`rankingSemDadosEtariosEDeterministico`)
- [x] **Fixture age-blind:** currículo real → versão gerada **não contém** ano de graduação, nem "30 anos de experiência", nem data de nascimento (assert de substring). (`FixtureCurriculoRegressionTest`)
- [x] **Adverse impact:** dataset sintético com 40% vs 15% de aprovação → razão 0,375 (exibida 0,37) → alerta. (`adverseImpactoExato40vs15PorCentoDisparaAlerta`)
- [x] **LGPD:** resposta de decisão automatizada inclui caminho para `revisao/solicitacao`. (`LgpdRevisaoCaminhoTest` — campo `caminhoParaRevisao` em ranking, auditoria e funil)

## 2. Revisão no Git/GitHub

### 2.1 Convenção de repositório

```
main ──► feat/d-07-perfis ──► PR #1 ──► main
      └─ feat/d-14-motor   ──► PR #2 ──► main
```

- Commits: Conventional Commits — `feat(D-08): ...`, `test(D-08): ...`, `fix: ...`
- Branch por fase (B, C) ou por task grande; PR com no máx. ~400 linhas
- **Nunca** commitar `.env`, senhas ou `target/` (`.gitignore` obrigatório)

### 2.2 Checklist do PR (revisor outro que o autor)

- [x] Compila e testes verdes localmente (`./mvnw clean verify` → **58/58**)
- [x] Sem `dataNascimento`/`idade` em nenhuma entidade (`grep -ri "idade" src/` → só `Severidade`)
- [x] Endpoints batem com `openapi.yaml` (sem rota órfã) — `ContratoOpenApiTest`
- [x] Erros em RFC 7807 com `fieldErrors`
- [x] Sem segredo/credencial no diff (grep password/secret/key/token → nenhum)
- [x] Mensagem de commit explica o *porquê* (Conventional Commits)
- [x] Swagger atualizado (exemplos/summaries em todas as 15 rotas)

### 2.3 Qualidade (Definition of Done)

- [x] Cobertura ≥ 70% nos services dos módulos `carreira` e `escudo` — **94,2% linhas / 76% branches** (JaCoCo)
- [x] `GET /actuator/health` → `UP` (smoke com jar seed, porta 8099)
- [x] README com `./mvnw spring-boot:run` + 3 curl de exemplo (age-blind, auditoria, funil)
- [x] Tag `v1.0.0` no GitHub + release notes (criada em 03/10/2026 sobre o merge `fd2005d`)

## 3. Demo final (entrega) — ✔ ensaiada em 03/10/2026 (jar `--profiles active=seed`, porta 8099)

1. ✔ Perfil 1 (Ana Souza) → seed 10 perfis + 20 vagas → **6 vagas recomendadas** com `score`, `competenciasAtendidas`, `competenciasFaltantes` e `caminhoParaRevisao` (LGPD art. 20).
2. ✔ Currículo fixture → **15 sinais** (scoreAts 0) → age-blind → diff confirma remoção de 1974/idade:51/1998/"25 anos de"/aposentadoria/foto-maria **e das formas femininas "veterana"/"madura"** (fix R11) conservando nome, e-mail e formação.
3. ✔ Vaga "perfil jovem" → `PERFIL_JOVEM` + `RECEM_FORMADO` + `ANOS_MAX_EXPERIENCIA`, `riscoLegal: ALTO`, base Lei 9.029/1995.
4. ✔ `/funil/metricas?vagaId=1&periodo=2026-09` → Triagem razão **0,43** e Entrevista **0,50** → `alerta: true` + `caminhoParaRevisao`.
5. ✔ PR #1 aberto com checklist comentado (`/pull/1#issuecomment-5942304800`).

---

**✔ Sprint 3 pronta quando:** `clean verify` verde + checklist do PR completo + demo dos 5 passos gravada/ensaíada. — **ATINGIDO em 03/10/2026** (`clean verify` → BUILD SUCCESS 58/58; checklist T-07 completo; demo 5/5 ensaiada). **T-08 concluído:** tag `v1.0.0` + release notes publicadas; PR #1 merged em `main` (`fd2005d`).
