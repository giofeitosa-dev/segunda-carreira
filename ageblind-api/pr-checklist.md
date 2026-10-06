## Checklist do PR — Sprint 3 (T-07)

- [x] **Compila e testes verdes localmente:** `mvnw -B test` → **58/58** (`BUILD SUCCESS`)
- [x] **Sem `dataNascimento`/`idade` em nenhuma entidade:** grep nos `domain/` → apenas falso positivo `Severidade`
- [x] **Endpoints batem com `openapi.yaml`:** `ContratoOpenApiTest` — 15 paths das tags Carreira/Escudo no spec = 15 paths em runtime (`/v3/api-docs`), sem rota órfã nem ausente
- [x] **Erros em RFC 7807 com `fieldErrors`:** `GlobalExceptionHandler` (400/404/405 + `IllegalArgumentException` → 400 para página negativa)
- [x] **Sem segredo/credencial no diff:** grep `password|secret|api_key|token` → nenhum; `.gitignore` cobre `target/` e `.env`
- [x] **Mensagens de commit explicam o porquê:** Conventional Commits (`fix`, `feat`, `test`, `chore`, `docs`)
- [x] **Swagger atualizado:** summaries/operationId em todas as 15 rotas (springdoc)
- [x] **Bônus (DoD):** cobertura JaCoCo **94,2% linhas / 76% branches** nos services `carreira`+`escudo` (meta 70%)

### Regressões anti-viés (assinatura do produto)

- [x] Idade não muda ranking — `rankingSemDadosEtariosEDeterministico`
- [x] Fixture real sai age-blind — `FixtureCurriculoRegressionTest` (sem 1974/1998/„25 anos de experiência"/aposentadoria/veterano/foto)
- [x] Adverse impact 40% vs 15% → alerta — `adverseImpactoExato40vs15PorCentoDisparaAlerta`
- [x] LGPD art. 20 — `caminhoParaRevisao` em ranking, auditoria e funil (`LgpdRevisaoCaminhoTest`)

### Correções neste lote

- `GET /vagas?page=-1` retornava 200 (clamp silencioso) → agora **400** RFC 7807
- `VagaRecomendada` sem `competenciasFaltantes` (previsto no spec) → adicionado
- Respostas de decisão automatizada sem caminho de revisão → `caminhoParaRevisao` (spec + DTOs)

*Suite: 58 testes · JaCoCo: services 94,2% · contrato: 15/15 paths.*
