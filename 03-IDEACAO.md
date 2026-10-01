# Fase 3 — Ideação

> Objetivo: gerar conceitos de API para os 3 HMWs aprovados na fase de Definição, cada um com endpoints, modelo de dados, integrações e escopo mínimo (MVP).

---

## As 3 soluções

| # | Solução | HMW | ODS | Arquivo |
|---|---|---|---|---|
| 1 | **Relatos do Bairro** | Canal único para reportar e acompanhar problemas do bairro | 11 + 10 | `03-IDEACAO\relatos-bairro.md` |
| 2 | **Segunda Carreira** | Reinserção e transição de profissionais 45+ | 10 + 5 | `03-IDEACAO\segunda-carreira.md` |
| 3 | **Rotas Acessíveis** | Rotas em tempo real com acessibilidade e segurança | 11 + 10 + 5 | `03-IDEACAO\rotas-acessiveis.md` |

---

## Sinergia entre as soluções

```
┌─────────────────────────────────────────────────────────────────┐
│                     CIDADÃO (usuário comum)                      │
└──────────┬──────────────────────────┬───────────────────────────┘
           │                          │
           ▼                          ▼
┌─────────────────────┐    ┌──────────────────────────┐
│ RELATOS DO BAIRRO   │    │ SEGUNDA CARREIRA         │
│ POST /relatos       │    │ GET /vagas/recomendadas  │
│ GET /status/{id}    │    │ GET /trilhas/{area}      │
└──────────┬──────────┘    └──────────────────────────┘
           │ relatos com geo + categoria
           ▼
┌─────────────────────────────────────────────────────────────────┐
│              ROTAS ACESSÍVEIS (camada de tempo real)             │
│  rota oficial (GTFS) + acessibilidade (POIs) + risco (relatos)  │
└─────────────────────────────────────────────────────────────────┘
```

**Ideia central:** uma plataforma de **mobilidade e participação cidadã** onde os relatos de quem vive a cidade melhoram a rota de quem vai usá-la.

---

## Padrão técnico comum (Spring Boot)

As 3 APIs seguem o mesmo esqueleto:

```
src/main/java/com/reconecta/<modulo>/
├── api/            → Controllers (REST)
├── application/    → Services (regras de negócio)
├── domain/         → Entidades + repositórios (JPA)
└── infrastructure/ → Config, clients externos, jobs
```

- Java 17+, Spring Boot 4.x, Spring Web + Spring Data JPA
- Banco: H2 para demo / PostgreSQL se houver tempo
- Docs: Springdoc OpenAPI (Swagger UI) automático — ótimo para a banca
- Validação: `jakarta.validation` nos DTOs

---

## Ordem de construção sugerida

1. **Relatos do Bairro** — valida o stack e entrega funcionalidade end-to-end mais rápido.
2. **Segunda Carreira** — recomendação por competências + trilhas.
3. **Rotas Acessíveis** — integra dados abertos + consome os relatos.

Detalhes e critérios de "pronto" → `04-ROADMAP.md`.
