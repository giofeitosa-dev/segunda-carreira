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
    ├── README.md                  ← como rodar + 15 rotas + exemplos curl
    ├── pom.xml                    ← Spring Boot 4.1.1 + SpringDoc
    └── src\main\java\com\reconecta\ageblind\
        ├── carreira\  escudo\      ← módulos implementados (10 + 5 rotas)
        └── shared\                 ← GlobalExceptionHandler + OpenApiConfig
```

**Documentos da API:**
[`openapi.yaml`](../openapi.yaml) (tags `Carreira` e `Escudo Anti-Etarismo`) · [`05-ETARISMO.md`](../05-ETARISMO.md) (base das 15 regras) · arquitetura/rotas completas (`07-ARQUITETURA.md`, `08-ROTAS.md`) ficam no repo Reconecta pai

---

## Escopo da API

**15 rotas** (de 29 do Reconecta — só os módulos Carreira + Escudo):

- **Carreira (10):** perfis, competências transferíveis, vagas + recomendação, trilhas, inscrições, lembretes, mentorias
- **Escudo (5):** `/curriculo/age-blind`, `/curriculo/auditoria`, `/vagas/auditoria-linguagem`, `/funil/metricas`, `/revisao/solicitacao`

**Público:** candidato 40+ · empresa/RH · app parceiro
**Fora do escopo:** relatos de bairro e rotas (ficam no Reconecta pai).

## Os 7 diferenciais (da `06-CONCORRENCIA.md`)

1. Dois lados numa API só (candidato **e** empresa)
2. Foco total em **etarismo**
3. Detecção **+ geração** age-blind (15 regras)
4. Pronto para **LGPD/lei BR** (art. 20, CLT 373-A, Lei 9.029)
5. **API-first** (OpenAPI 3.0)
6. **PT-BR**
7. Fecha o ciclo: competências transferíveis → trilha → vaga
