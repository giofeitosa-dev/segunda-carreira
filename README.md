# Reconecta — Segunda Carreira · AgeBlind API

> **"A API que contrata por competência, não por ano de nascimento."**
> Projeto de hackathon/ReCode — monolito modular Spring Boot construído com Design Thinking (Empatia → Definição → Ideação).

**Status (06/10/2026): ✅ PROJETO CONCLUÍDO** — Sprint 1 (planejamento) · Sprint 2 (desenvolvimento, D-01…D-22) · Sprint 3 (testes/revisão, T-01…T-09) · PR #1 merged · **release [`v1.0.0`](https://github.com/giofeitosa-dev/segunda-carreira/releases/tag/v1.0.0)** · 58/58 testes verdes.

---

## ODS abordadas

| ODS | Tema | Foco no projeto |
|---|---|---|
| ♀️ **ODS 5** | Igualdade de Gênero | Segurança da mulher, oportunidades profissionais e redes de apoio |
| 🤝 **ODS 10** | Redução das Desigualdades | Acessibilidade, inclusão social, empregabilidade (core da AgeBlind) |
| 🏙️ **ODS 11** | Cidades e Comunidades Sustentáveis | Mobilidade, segurança, serviços públicos, participação cidadã |

## As 3 soluções do Reconecta

1. **Relatos do Bairro** — canal único para reportar e acompanhar problemas do bairro *(ODS 11 + 10)* — docs de concepção; API fora do escopo desta entrega.
2. **⭐ Segunda Carreira + Escudo Anti-Etarismo** — reinserção de profissionais 40+ com recomendação de vagas por competências e escudo contra viés etário — **é o que está implementado em [`ageblind-api/`](ageblind-api/)** *(ODS 10 + 5)*.
3. **Rotas Acessíveis** — rotas com camadas de acessibilidade e segurança *(ODS 11 + 10 + 5)* — docs de concepção; API fora do escopo desta entrega.

**Stack:** Java 17 · Spring Boot 4.1.1 · Maven · Spring Data JPA + H2 (demo) / PostgreSQL (ideal) · SpringDoc OpenAPI · JUnit 5 + MockMvc + JaCoCo.

---

## 🗺️ Esquema do repositório

📦 = versionado no GitHub (`giofeitosa-dev/segunda-carreira`) · 🖥️ = apenas local (recorte do repositório: docs base ficam na máquina da equipe)

```
reconecta-recode\
│
├── 📦 README.md                      ← este arquivo (sumário do projeto)
├── 📦 .gitignore
│
│   ── Documentação base (Design Thinking, Sprint 0) ──
├── 🖥️ 01-EMPATIA.md                  ← temas, personas e roteiro de entrevistas
├── 🖥️ 02-DEFINICAO.md                ← POV, HMW e priorização de soluções
├── 📦 03-IDEACAO.md                  ← índice das 3 soluções e sinergia
├── 📦 03-IDEACAO\                    ← detalhe de cada solução
│   ├── segunda-carreira.md           ←    (a que virou a API)
│   ├── relatos-bairro.md
│   └── rotas-acessiveis.md
├── 🖥️ 04-ROADMAP.md                  ← ordem de construção das sprints
├── 📦 05-ETARISMO.md                 ← base técnica/legal do anti-etarismo (15 regras, Lei 9.029)
├── 🖥️ 06-CONCORRENCIA.md             ← o que já existe, lacunas e diferenciais
├── 🖥️ 07-ARQUITETURA.md              ← Sprint 1: decisão de arquitetura, pacotes, dados, riscos (aprovado)
├── 🖥️ 08-ROTAS.md                    ← Sprint 1: catálogo oficial das 29 rotas (aprovado)
│
│   ── Especificação ──
├── 📦 openapi.yaml                   ← OpenAPI 3.0 — 29 rotas (15 são do AgeBlind; filtros: tags
│                                       "Carreira" e "Escudo Anti-Etarismo")
│
│   ── Gestão ──
├── 📦 kanban\
│   ├── SPRINT-1.md                   ← quadro original (colunas, WIP, critérios)
│   └── trello-import.csv             ← import para Trello/Notion
├── 🖥️ registros\
│   ├── 2026-09-29-sessao-01.md       ← atas das sessões
│   ├── 2026-09-30-sessao-02.md
│   └── apresentacao.md               ← template para a banca
│
│   ── ⭐ Entrega final ──
└── 📦 ageblind-api\                  ← A API (Sprints 1, 2 e 3)
    ├── README.md                     ← visão geral da entrega + as 3 sprints
    ├── kanban.md                     ← quadro único do projeto (WIP 0 — tudo concluído)
    ├── pr-checklist.md               ← checklist de revisão usado no PR #1
    ├── SPRINT-1-PLANEJAMENTO.md      ← arquitetura, 15 rotas, registro de aprovação (§5)
    ├── SPRINT-2-DESENVOLVIMENTO.md   ← backlog de código D-01…D-22
    ├── SPRINT-3-TESTES-REVISAO.md    ← matriz de testes, DoD e demo final
    └── api\                          ← ⭐⭐ código-fonte Spring Boot
        ├── README.md                 ← as 15 rotas + 15 exemplos de curl
        ├── pom.xml · mvnw · mvnw.cmd ← Maven wrapper (Java 17)
        └── src\
            ├── main\java\com\reconecta\ageblind\
            │   ├── carreira\          ← api · service · domain · repository · gateway · seed
            │   ├── escudo\            ← api · service · dominio · dto · seed
            │   └── shared\            ← GlobalExceptionHandler (RFC 7807) · CaminhosLgpd · config
            ├── main\resources\       ← application.properties · seeds (CSV) · regras-idade.json
            └── test\java\            ← 58 testes (MockMvc, regressão anti-viés, contrato, LGPD)
```

## 📇 Sumário — o que há em cada arquivo

| Arquivo / pasta | Conteúdo | Quando abrir |
|---|---|---|
| `01-EMPATIA.md` | Pesquisa com personas, temas e perguntas de entrevista | Onboarding no processo de design |
| `02-DEFINICAO.md` | Pontos de vista, "Como podemos…", priorização | Entender *por que* esta solução |
| `03-IDEACAO.md` + `03-IDEACAO\` | Conceito das 3 soluções e sinergia entre elas | Ver a ideia original da Segunda Carreira |
| `04-ROADMAP.md` | Ordem de construção planejada para as sprints | Ver o planejamento de prazo |
| `05-ETARISMO.md` | O que é etarismo, 15 regras de detecção, base legal (Lei 9.029/95, CLT 373-A) | Entender o Escudo Anti-Etarismo |
| `06-CONCORRENCIA.md` | Mercado, concorrentes e diferenciais | Argumentar na apresentação |
| `07-ARQUITETURA.md` | Decisão monolito modular, pacotes, modelo de dados, riscos | Revisar arquitetura (aprovado em 06/10) |
| `08-ROTAS.md` | Catálogo das 29 rotas (15 do AgeBlind) + changelog de rotas | Consultar contratos (aprovado em 06/10) |
| `openapi.yaml` | Especificação máquina OpenAPI 3.0 | Importar no Swagger/Postman |
| `kanban\` | Quadro das sprints + CSV para Trello/Notion | Gestão da equipe |
| `registros\` | Atas das sessões e template de apresentação | Preparar a banca |
| `ageblind-api\README.md` | Visão geral da entrega AgeBlind | Primeiro contato com a API |
| `ageblind-api\kanban.md` | Quadro único (Sprints 1–3, todos os cards) | Status do projeto |
| `ageblind-api\SPRINT-*.md` | Detalhe de cada sprint (planejamento, código, testes) | Aprofundar em uma sprint |
| `ageblind-api\api\README.md` | As 15 rotas documentadas + exemplos de curl | Usar/testar a API |
| `ageblind-api\api\src\main\` | Código de produção (carreira, escudo, shared) | Desenvolver |
| `ageblind-api\api\src\test\` | 58 testes (unit, integração, regressão anti-viés) | Rodar `mvnw clean verify` |

---

## ▶️ Rodar a API

```bash
cd ageblind-api/api
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed   # Windows: .\mvnw.cmd ...
# → Swagger:  http://localhost:8080/swagger-ui.html
# → Health:   http://localhost:8080/actuator/health
# → Seed:     10 perfis · 20 vagas · 5 trilhas · métricas de funil
```

Testes: `./mvnw clean verify` → **58/58 verdes** (cobertura dos services: 94,2% linhas).

**Rotas principais:** `POST /api/v1/curriculo/age-blind` · `POST /api/v1/curriculo/auditoria` · `GET /api/v1/vagas/recomendadas` · `GET /api/v1/funil/metricas` · `POST /api/v1/revisao/solicitacao` — lista completa no [`ageblind-api/api/README.md`](ageblind-api/api/README.md).

## 🧭 Como navegar

- **Novo na equipe:** `01-EMPATIA` → `02-DEFINICAO` → `ageblind-api/README.md`.
- **Quer entender a solução:** `03-IDEACAO/segunda-carreira.md` → `05-ETARISMO.md`.
- **Vai programar:** `07-ARQUITETURA` → `08-ROTAS` → `openapi.yaml` → `ageblind-api/api/README.md`.
- **Vai apresentar:** `registros/apresentacao.md` + `ageblind-api/kanban.md` (status).

## 🔗 Links

- Repositório: https://github.com/giofeitosa-dev/segunda-carreira
- Release v1.0.0: https://github.com/giofeitosa-dev/segunda-carreira/releases/tag/v1.0.0
- PR #1 (feat → main): https://github.com/giofeitosa-dev/segunda-carreira/pull/1
