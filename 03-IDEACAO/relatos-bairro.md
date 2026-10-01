# Solução 1 — Relatos do Bairro

> **HMW:** *Como poderíamos dar um canal único para cidadãos reportarem e acompanharem problemas do bairro, cruzando com a localização de serviços públicos próximos?*
> **ODS:** 11 (principal) + 10 | **Stack:** Java + Spring Boot

---

## Conceito

App/API de **participação cívica**: o cidadão registra um problema (buraco, iluminação quebrada, coleta atrasada, ponto de ônibus degradado), acompanha o status público e vê serviços públicos (UBS, escola, CRAS) perto de onde mora.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/relatos` | Cria relato (anonimizável), com geo, categoria e foto |
| `GET` | `/api/v1/relatos?bairro=&categoria=&status=` | Lista/filtra relatos |
| `GET` | `/api/v1/relatos/{id}` | Detalhe do relato + histórico de status |
| `PATCH` | `/api/v1/relatos/{id}/status` | Atualiza status (em análise → resolvido) |
| `GET` | `/api/v1/relatos/mapa?bbox=` | Relatos dentro de uma área (para o mapa) |
| `GET` | `/api/v1/servicos?lat=&lng=&tipo=` | Serviços públicos próximos |
| `POST` | `/api/v1/relatos/{id}/apoios` | Cidadão apoia/confirma o mesmo problema |

### Exemplo de payload

```json
POST /api/v1/relatos
{
  "titulo": "Poste quebrado na praça",
  "categoria": "ILUMINACAO",
  "descricao": "Escuro total desde segunda, perigoso à noite",
  "latitude": -23.5505,
  "longitude": -46.6333,
  "bairro": "Vila Maria",
  "anonimo": true
}
```

```json
Response 201
{
  "id": 1042,
  "status": "RECEBIDO",
  "codigoAcompanhamento": "RLT-8F3K2",
  "criadoEm": "2026-09-29T21:10:00"
}
```

## Modelo de dados

**`relato`**
- `id` (Long, PK)
- `titulo`, `descricao` (String)
- `categoria` (enum: ILUMINACAO, VIAS, COLETA, SANEAMENTO, TRANSPORTE, SEGURANCA, OUTROS)
- `status` (enum: RECEBIDO, EM_ANALISE, EM_ANDAMENTO, RESOLVIDO, REJEITADO)
- `latitude`, `longitude` (Double) + `bairro` (String)
- `anonimo` (Boolean), `codigoAcompanhamento` (String, UUID curto)
- `criadoEm`, `atualizadoEm` (Timestamp)
- Relação: `1..N` **`comentario`** (histórico público do status)

**`servicoPublico`**
- `id`, `nome`, `tipo` (enum: SAUDE, EDUCACAO, ASSISTENCIA, TRANSPORTE, LAZER)
- `endereco`, `latitude`, `longitude`, `bairro`
- `acessivelPcd` (Boolean), `horario` (String)

## Integrações

- **Geocodificação:** Nominatim/OpenStreetMap (grátis) para converter endereço → coordenada.
- **Base de serviços públicos:** CSV/GeoJSON de equipamentos abertos da prefeitura (ou IBGE localidades) importado via `data/import.sql`.
- **Alertas (opcional MVP+):** webhook/e-mail quando o status muda — usar `codigoAcompanhamento` no lugar de login.

## Escopo MVP vs. ideal

| MVP (sprint) | Ideal (pós-evento) |
|---|---|
| CRUD de relatos + status + código de acompanhamento | Login cidadão (Gov.br/CPF) |
| Lista e mapa por bbox | Painel do gestor com métricas |
| Import de serviços públicos (CSV) | Integração com 156/SMS oficial |
| Categorias fixas | Upload de fotos (S3) + upvotes |

## Estrutura do projeto (Spring Boot)

```
relatos-bairro/
├── pom.xml
└── src/main/java/com/reconecta/relatos/
    ├── RelatosApplication.java
    ├── api/         → RelatoController, ServicoController, dto/
    ├── application/ → RelatoService, AcompanhamentoService
    ├── domain/      → Relato, Comentario, ServicoPublico, enums/, repository/
    └── infrastructure/ → GeocodingClient, ImportJob
```

**Critério de "pronto":** cidadão consegue criar um relato, ver o status mudar e achar o serviço público mais próximo — tudo via Swagger.
