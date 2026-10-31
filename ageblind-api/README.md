# AgeBlind API — *Segunda Carreira*

> **API anti-etarismo para empregabilidade:** detecção **+ geração** de currículos age-blind, auditoria de vagas, métricas de funil e recomendação de vagas por competências — **sem usar idade**.
> **Tagline:** *"A API que contrata por competência, não por ano de nascimento."*
> ODS 10 + ODS 5 · módulo extraído do projeto Reconecta (ReCode)

---

## As 3 sprints

| Sprint | Fase | Entregável | Status |
|---|---|---|---|
| 🔹 **Sprint 1** | Planejamento | Arquitetura + definição das rotas + quadro Kanban | ✅ 30/09/2026 (aguarda aprovação) |
| 🔹 **Sprint 2** | Desenvolvimento | Mão na massa — **Java 17 + Spring Boot 4.1.1**: 15/15 rotas, seed demo, suite 50/50 | ✅ 30/09/2026 (PR #1 aberto) |
| 🔹 **Sprint 3** | Testes e revisão | Testes dos endpoints + revisão no Git/GitHub | ⬜ próxima |

## Estrutura desta pasta

```
ageblind-api\
├── README.md                      ← este arquivo
├── SPRINT-1-PLANEJAMENTO.md       ← arquitetura + as 15 rotas + decisões
├── SPRINT-2-DESENVOLVIMENTO.md    ← backlog de código (setup → módulos → integração)
├── SPRINT-3-TESTES-REVISAO.md     ← matriz de testes + checklist Git/GitHub
├── kanban.md                      ← quadro único com as 3 sprints
└── api\                           ← ⭐ projeto Spring Boot (Sprint 2 concluída ✅)
    ├── README.md                  ← como rodar + 22 endpoints + exemplos curl
    ├── pom.xml                    ← Spring Boot 4.1.1 + SpringDoc
    └── src\main\java\com\reconecta\ageblind\
        ├── carreira\  escudo\      ← módulos implementados (17 + 5 endpoints)
        └── shared\                 ← GlobalExceptionHandler + OpenApiConfig
```

**Documentos da API:**
[`openapi.yaml`](../openapi.yaml) (tags `Carreira` e `Escudo Anti-Etarismo`) · [`registros/05-ETARISMO.md`](../registros/05-ETARISMO.md) (base das 15 regras) · arquitetura/rotas completas (`registros/07-ARQUITETURA.md`, `registros/08-ROTAS.md`) ficam em `registros/` (fora do git)

---

## Escopo da API

**22 endpoints** (19 paths — de 30 do Reconecta: só os módulos Carreira + Escudo):

- **Carreira (17):** perfis (consulta/edição), competências transferíveis, currículo estruturado (consultar/substituir/upload PDF-DOCX-TXT), vagas + recomendação, busca externa (Adzuna), trilhas, inscrições (criar/listar/cancelar), lembretes, mentorias
- **Escudo (5):** `/curriculo/age-blind`, `/curriculo/auditoria`, `/vagas/auditoria-linguagem`, `/funil/metricas`, `/revisao/solicitacao`

**Público:** candidato 40+ · empresa/RH · app parceiro
**Fora do escopo:** relatos de bairro e rotas (ficam no Reconecta pai).

## Perfis de execução (banco de dados)

| Profile | Banco | Uso |
|---|---|---|
| *(nenhum — padrão)* | H2 em memória | Desenvolvimento local e testes (`mvnw verify` → 90/90) |
| `seed` | H2 em memória | Demo local com dados (`mvnw spring-boot:run -Dspring-boot.run.profiles=seed`) |
| `prod` | **PostgreSQL** | Container/docker-compose e qualquer ambiente com banco real |
| `prod,seed` | PostgreSQL | Demo em Postgres com dados (default do `docker-compose.yml`) |

- **Variáveis do `prod`:** `DB_URL` · `DB_USER` · `DB_PASSWORD` (default `jdbc:postgresql://localhost:5432/segunda_carreira` / `reconecta` / `reconecta`). Console H2 desabilitado.
- **Docker:** `docker compose up -d --build` na raiz do repo sobe Postgres 16 + API (ver README raiz). Sem Flyway por ora — `ddl-auto=update` cria o schema (migração versionada é evolução).

## Os 7 diferenciais (da `06-CONCORRENCIA.md`)

1. Dois lados numa API só (candidato **e** empresa)
2. Foco total em **etarismo**
3. Detecção **+ geração** age-blind (15 regras)
4. Pronto para **LGPD/lei BR** (art. 20, CLT 373-A, Lei 9.029)
5. **API-first** (OpenAPI 3.0)
6. **PT-BR**
7. Fecha o ciclo: competências transferíveis → trilha → vaga
