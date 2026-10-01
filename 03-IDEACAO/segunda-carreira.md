# Solução 2 — Segunda Carreira

> **HMW:** *Como poderíamos ajudar pessoas com 45+ (ou em transição de área) a descobrirem suas competências transferíveis, encontrarem vagas e formações que as valorizem e se inscreverem com curadoria humana?*
> **ODS:** 10 (principal) + 5 | **Stack:** Java + Spring Boot

---

## Conceito

API de **recomendação de vagas + trilhas de transição** para profissionais que buscam reinserção ou troca de área. Diferencial anti-vies: **a recomendação não recebe idade** — rankeia por competências, reduzindo a discriminação etária.

A solução tem **2 módulos** (base teórica completa em `05-ETARISMO.md`; análise de mercado e diferenciais em `06-CONCORRENCIA.md`):

1. **Módulo 1 — Carreira:** perfis, competências transferíveis, vagas, trilhas, mentorias.
2. **Módulo 2 — Escudo Anti-Etarismo:** currículo cego à idade, auditoria de currículo (score ATS), auditoria de linguagem de vagas e métricas de funil para detectar discriminação.

## Endpoints — Módulo 1: Carreira

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/perfis` | Cadastra perfil: experiências, competências, objetivo |
| `GET` | `/api/v1/perfis/{id}` | Consulta perfil |
| `POST` | `/api/v1/perfis/{id}/competencias-transferiveis` | Mapeia experiência antiga → competências da área alvo |
| `GET` | `/api/v1/vagas/recomendadas?perfilId=` | Vagas compatíveis (sem filtro por idade) |
| `GET` | `/api/v1/trilhas/{area}` | Capacitações curtas/certificadas para a área |
| `POST` | `/api/v1/inscricoes` | Inscrição simplificada em vaga/curso |
| `POST` | `/api/v1/webhooks/lembretes` | Alerta de prazo de inscrição |
| `GET` | `/api/v1/mentorias?perfilId=&area=` | Encaixe com mentores/pares da área |

### Exemplo de payload

```json
POST /api/v1/perfis
{
  "nome": "João",
  "areaAtual": "OPERARIO_INDUSTRIAL",
  "areaAlvo": "LOGISTICA",
  "anosExperiencia": 25,
  "competencias": ["GestaoDeEquipe", "PCP", "ControleDeQualidade", "SegurancaDoTrabalho"],
  "pretensaoSalarial": 4500,
  "disponibilidade": ["TURNO_NOITE", "REMOTO", "HIBRIDO"]
}
```

```json
POST /api/v1/perfis/7/competencias-transferiveis
Response 200
{
  "areaAlvo": "LOGISTICA",
  "competenciasTransferiveis": [
    { "de": "PCP", "para": "Planejamento de Rotas", "similaridade": 0.92 },
    { "de": "ControleDeQualidade", "para": "Recebimento/Inventário", "similaridade": 0.88 }
  ],
  "gaps": ["Sistemas WMS"],
  "trilhaSugerida": "TRIL-LOG-01"
}
```

## Endpoints — Módulo 2: Escudo Anti-Etarismo

| Método | Rota | Descrição | Base |
|---|---|---|---|
| `POST` | `/api/v1/curriculo/age-blind` | Gera versão do currículo **sem proxies de idade** (15 regras: sem ano de graduação, sem total de anos, experiência só recente, formação reordenada...) | técnica |
| `POST` | `/api/v1/curriculo/auditoria` | **Score ATS** (0–100) + lista de sinais que revelam idade + correções sugeridas | técnica |
| `POST` | `/api/v1/vagas/auditoria-linguagem` | Detecta termos discriminatórios ("perfil jovem", "até 35 anos", "nativo digital", "recém-formado") e aponta o **risco legal** | CLT 373-A, Lei 9.029/95 |
| `GET` | `/api/v1/funil/metricas?vagaId=` | **Taxa de avanço por faixa etária** em cada etapa + alerta de *adverse impact* (razão < 80%) | auditoria estatística |
| `POST` | `/api/v1/revisao/solicitacao` | Gera modelo de pedido de **revisão humana** da decisão automatizada | **LGPD art. 20** |

### Exemplo de payload — auditoria de currículo

```json
POST /api/v1/curriculo/auditoria
{ "curriculo": "...texto ou JSON..." }

Response 200
{
  "scoreAts": 72,
  "sinaisQueRevelamIdade": [
    { "sinal": "ANO_GRADUACAO", "localizacao": "secao.formacao[0]", "severidade": "ALTA",
      "acao": "remover ano de conclusão" },
    { "sinal": "TOTAL_ANOS_EXPERIENCIA", "localizacao": "resumo", "severidade": "ALTA",
      "acao": "traduzir para competências e métricas" },
    { "sinal": "TECNOLOGIA_OBSOLETA", "localizacao": "skills", "severidade": "MEDIA",
      "acao": "manter só se a vaga pedir" }
  ],
  "sugestoes": ["adicionar certificação 2025/2026", "espelhar keywords da vaga"]
}
```

### Exemplo de payload — métricas de funil

```json
GET /api/v1/funil/metricas?vagaId=42

Response 200
{
  "etapas": [
    { "nome": "Triagem",
      "aprovacao": { "ATE_39": 0.42, "40_49": 0.31, "50_MAIS": 0.18 },
      "razaoAdverseImpact": 0.43,
      "alerta": true }
  ],
  "interpretacao": "Razão 0,43 < 0,80 → possível viés etário; investigar critérios da triagem."
}
```

> **Princípio de produto:** a API protege **os dois lados** — o candidato (escudo age-blind) e a empresa (auditoria que evita passivo trabalhista; foram **R$ 174,64 mi** em indenizações por etarismo no Brasil em 2023).

## Modelo de dados

**Módulo 1:**

**`perfil`**
- `id`, `nome`, `email`, `areaAtual` (enum CBO simplificado), `areaAlvo`
- `anosExperiencia`, `pretensaoSalarial`, `disponibilidade` (enum set)
- ⚠️ **Sem campo de data de nascimento** na entidade de recomendação (anti-vies)

**`competencia`** (catálogo) — `id`, `nome`, `area`

**`perfilCompetencia`** — `perfil` ↔ `competencia` + nível (0–5)

**`vaga`**
- `id`, `titulo`, `empresa`, `area`, `descricao`, `salario`, `modelo` (PRESENCIAL/HIBRIDO/REMOTO)
- `requisitos` (enum set de competências), `prazoInscricao`, `fonte`

**`trilha`**
- `id`, `area`, `nome`, `duracaoSemanas`, `custo`, `instituicao`, `certificado` (bool)
- Relação: `1..N` **`moduloTrilha`** (nome, cargaHoraria, ordem)

**`inscricao`** — `id`, `perfil`, `vagaOuTrilha`, `status`, `criadoEm`, `lembreteAgendado`

**`mentoria`** — `id`, `mentor` (perfil), `area`, `disponibilidade`, `agendamento`

**Módulo 2 (Escudo Anti-Etarismo):**

**`versaoCurriculo`**
- `id`, `perfil`, `tipo` (enum: COMPLETO, AGE_BLIND)
- `conteudo` (texto/JSON), `geradoEm`
- Relação: `1..N` **`sinalIdade`** (`codigo`, `localizacao`, `severidade`, `acao`, `corrigido` bool)

**`auditoriaCurriculo`** — `id`, `versaoCurriculo`, `scoreAts` (0–100), `nSinaisAltos`, `sugestoes` (JSON), `criadoEm`

**`auditoriaVaga`** — `id`, `vaga`, `termosEncontrados` (enum set: PERFIL_JOVEM, LIMITE_ETARIO, NATIVO_DIGITAL, RECEM_FORMADO, ANOS_MAX_EXPERIENCIA, FIT_CULTURAL), `riscoLegal` (BAIXO/MEDIO/ALTO), `sugestaoRedacao`, `criadoEm`

**`metricaFunil`** — `id`, `vaga`, `etapa`, `faixaEtaria` (ATE_39/40_49/50_MAIS), `candidatos`, `aprovados`, `taxaAprovacao`, `periodo`

**`solicitacaoRevisao`** — `id`, `perfil`, `vaga`, `motivo`, `status` (PENDENTE/EM_REVISAO/DECIDIDA), `respuestaHumana`, `criadoEm` *(registro do direito da LGPD art. 20)*

## Integrações

- **Vagas:** feed público de vagas (API da programathor/empregos.br, Vagas.com ou CSV aberto) + vagas demo embarcadas.
- **Classificação CBO:** mapear `areaAtual`/`areaAlvo` com códigos CBO/IBGE (arquivo JSON estático).
- **Trilhas:** catálogo SENAI/Coursera/Alura aberto, importado como CSV.
- **Curadoria humana (diferencial):** fila de perfis para revisão por um mentor antes de recomendar — modelar como status `EM_CURADORIA` no perfil.
- **Lembretes:** `@Scheduled` job do Spring verificando prazos → e-mail/console.
- **Módulo 2 — regras:** catálogo de **proxies de idade** e **termos discriminatórios** em JSON versionado (fonte: `05-ETARISMO.md`).
- **Módulo 2 — parse de currículo:** Apache Tika (extrai texto de PDF/DOCX) antes da auditoria.
- **Módulo 2 — métricas de funil:** eventos de etapa (`candidato_avancou`, `candidato_reprovado`) alimentados por webhook do ATS da empresa ou lançamento manual — agregação por faixa etária no endpoint de métricas.

## Escopo MVP vs. ideal

| MVP (sprint) | Ideal (pós-evento) |
|---|---|
| CRUD de perfis e competências | Login/CPF + CV upload |
| Regra de competências transferíveis (similaridade por keyword) | LLM para tradução de experiência |
| Recomendação de vagas por requisitos | Scores de fit + histórico de candidaturas |
| 3 trilhas demo + inscrição | Integração real com instituições |
| Lembretes de prazo (console) | SMS/WhatsApp |
| **Age-blind + auditoria de currículo (regras por keyword)** | Geração por LLM com revisão humana |
| **Auditoria de linguagem de vaga** | Integração direta com publicadores de vagas |
| **Funil com dados demo + cálculo de adverse impact** | Webhook real do ATS |
| **Template de pedido de revisão (LGPD)** | Envio automatizado à empresa |

## Estrutura do projeto (Spring Boot)

```
segunda-carreira/
├── pom.xml
└── src/main/java/com/reconecta/carreira/
    ├── SegundaCarreiraApplication.java
    ├── api/          → PerfilController, VagaController, TrilhaController,
    │                   CurriculoController, AuditoriaController, FunilController, dto/
    ├── application/  → RecomendacaoService, TransferiveisService, LembreteJob,
    │                   AgeBlindService, AuditoriaCurriculoService,
    │                   AuditoriaVagaService, FunilMetricasService
    ├── domain/       → Perfil, Competencia, Vaga, Trilha, Inscricao,
    │                   VersaoCurriculo, AuditoriaCurriculo, AuditoriaVaga,
    │                   MetricaFunil, SolicitacaoRevisao, enums/, repository/
    └── infrastructure/ → VagasClient, CboLoader, TrilhasCsvImporter,
                          TikaParser, regras/ (proxies-idade.json, termos-discriminatorios.json)
```

**Critério de "pronto":**
- *Módulo 1:* a partir de um perfil de operário 45+, a API devolve competências transferíveis, 3 vagas compatíveis e 1 trilha — sem nunca usar idade no ranking.
- *Módulo 2:* a auditoria marca ≥3 sinais de idade num currículo de exemplo, gera a versão age-blind, aponta "perfil jovem" numa vaga de teste e calcula o *adverse impact* de um funil demo.
