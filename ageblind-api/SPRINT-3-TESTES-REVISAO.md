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

- [ ] **Idade não muda ranking:** mesmo perfil, 2 idades hipotéticas → mesma ordem de vagas.
- [ ] **Fixture age-blind:** currículo real → versão gerada **não contém** ano de graduação, nem "30 anos de experiência", nem data de nascimento (assert de substring).
- [ ] **Adverse impact:** dataset sintético com 40% vs 15% de aprovação → razão 0,375 → alerta.
- [ ] **LGPD:** resposta de decisão automatizada inclui caminho para `revisao/solicitacao`.

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

- [ ] Compila e testes verdes localmente (`./mvnw clean verify`)
- [ ] Sem `dataNascimento`/`idade` em nenhuma entidade (`grep -ri "idade" src/`)
- [ ] Endpoints batem com `openapi.yaml` (sem rota órfã)
- [ ] Erros em RFC 7807 com `fieldErrors`
- [ ] Sem segredo/credencial no diff
- [ ] Mensagem de commit explica o *porquê*
- [ ] Swagger atualizado (exemplos)

### 2.3 Qualidade (Definition of Done)

- [ ] Cobertura ≥ 70% nos services dos módulos `carreira` e `escudo`
- [ ] `GET /actuator/health` → `UP`
- [ ] README com `./mvnw spring-boot:run` + 3 curl de exemplo (age-blind, auditoria, funil)
- [ ] Tag `v1.0.0` no GitHub + release notes

## 3. Demo final (entrega)

1. Swagger → criar perfil → ver 3 vagas recomendadas (justificativa visível).
2. Enviar currículo fixture → **auditoria marca sinais** → **gerar age-blind** → diff antes/depois.
3. Enviar vaga com "perfil jovem" → auditoria responde risco legal.
4. `/funil/metricas` → alerta de adverse impact.
5. Abrir PR no GitHub e mostrar a revisão.

---

**✔ Sprint 3 pronta quando:** `clean verify` verde + checklist do PR completo + demo dos 5 passos gravada/ensaíada.
