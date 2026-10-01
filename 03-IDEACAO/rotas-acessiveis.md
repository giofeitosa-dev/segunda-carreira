# Solução 3 — Rotas Acessíveis

> **HMW:** *Como poderíamos fornecer em tempo real rotas acessíveis e seguras, combinando dados de transporte, acessibilidade e relatos dos próprios cidadãos?*
> **ODS:** 11 (principal) + 10 + 5 | **Stack:** Java + Spring Boot

---

## Conceito

API de **rota inteligente** que cruza três camadas de informação:

1. **Mobilidade** — dados oficiais de transporte (paradas, linhas, horários).
2. **Acessibilidade** — atributos de POIs e veículos (rampa, elevador, piso tátil).
3. **Segurança/qualidade** — **relatos em tempo real dos cidadãos** (consumindo a Solução 1).

O resultado: uma rota que prioriza acessibilidade e evita/zonas de risco, com aviso ao cidadão.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/rotas` | Calcula rota entre origem e destino |
| `GET` | `/api/v1/rotas/{id}` | Detalhe da rota calculada + justificativa das escolhas |
| `GET` | `/api/v1/linhas?lat=&lng=` | Linhas/paradas próximas |
| `GET` | `/api/v1/paradas/{id}/acessibilidade` | Acessibilidade de uma parada/veículo |
| `POST` | `/api/v1/relatos/geolocalizados` | Consumo dos relatos (integração Solução 1) |
| `GET` | `/api/v1/zonas-de-risco?bbox=` | Zonas com muitos relatos abertos |

### Exemplo de payload

```json
POST /api/v1/rotas
{
  "origem":      { "latitude": -23.5505, "longitude": -46.6333 },
  "destino":     { "latitude": -23.5610, "longitude": -46.6560 },
  "perfil":      "PCD_CADEIRANTE",   // PCDEIRA | IDOSO | VISUAL | NENHUM
  "prioridades": ["ACESSIBILIDADE", "SEGURANCA", "TEMPO"]
}
```

```json
Response 200
{
  "id": 512,
  "tempoTotalMin": 38,
  "transferencias": 1,
  "trocos": [
    {
      "modo": "A_PE",
      "duracaoMin": 8,
      "aviso": "Trecho sem calçada acessível — alternativa +4 min disponível"
    },
    {
      "modo": "ONIBUS",
      "linha": "477I",
      "acessivel": true,
      "aviso": "Veículo com rampa (100% da frota)"
    },
    {
      "modo": "METRO",
      "estacao": "Sé",
      "acessivel": true,
      "aviso": "Elevador em manutenção (relato #1042) — usar estação República"
    }
  ],
  "alertasSeguranca": [
    { "categoria": "ILUMINACAO", "distanciaM": 120, "relatoId": 1042 }
  ]
}
```

## Modelo de dados

**`rota`**
- `id`, `origemLat/Lng`, `destinoLat/Lng`, `perfil`, `prioridades`
- `tempoTotalMin`, `distanciaM`, `criadoEm`, `justificativa` (JSON com as decisões)

**`trechoRota`** — `id`, `rota`, `modo` (A_PE/ONIBUS/METRO/BICICLETA), `linha`, `duracaoMin`, `acessivel` (bool), `aviso`

**`parada`**
- `id`, `nome`, `latitude`, `longitude`, `linha`
- `rampa` (bool), `elevador` (bool), `pisoTatil` (bool), `banheiroAcessivel` (bool)

**`zonaAlerta`** (espelha os relatos)
- `id`, `relatoId`, `categoria`, `latitude`, `longitude`, `status`, `severidade`
- Sincronizado por job que consome `GET /relatos` da Solução 1.

**`linhaTransporte`** — `id`, `codigo`, `modal`, `frotaAcessivel` (%), `horarioInicio/Fim`

## Integrações

- **Transporte:** dados abertos em formato **GTFS** (SPTrans, metrô, ou GTFS sample do Brasil) → importador que popula `parada` e `linhaTransporte`.
- **Roteamento:** OSRM (grátis, self-hosted) ou OpenRouteService para o cálculo base — a API Spring apenas **enriquece** com acessibilidade e alertas.
- **Relatos (S RestClient):** consumir `GET /api/v1/relatos/mapa?bbox=` da Solução 1 em job de 5 min (`@Scheduled`) para atualizar `zonaAlerta`.
- **Geocodificação:** Nominatim/OpenStreetMap.

## Escopo MVP vs. ideal

| MVP (sprint) | Ideal (pós-evento) |
|---|---|
| Import GTFS simplificado (10 paradas demo) | GTFS completo da cidade |
| Rota com 1 parada + 1 ônibus (regra simples) | Integração OSRM real |
| Camada de alertas vinda dos relatos | Tempo real via WebSocket |
| Perfil PCD/idoso | Preferências do usuário salvas |
| Justificativa textual da rota | UI de mapa (front separado) |

## Estrutura do projeto (Spring Boot)

```
rotas-acessiveis/
├── pom.xml
└── src/main/java/com/reconecta/rotas/
    ├── RotasApplication.java
    ├── api/          → RotaController, ParadaController, ZonaAlertaController, dto/
    ├── application/  → CalculoRotaService, EnriquecedorService, SincronizaRelatosJob
    ├── domain/       → Rota, TrechoRota, Parada, LinhaTransporte, ZonaAlerta/, repository/
    └── infrastructure/ → GtfsImporter, OsrmClient, RelatosClient (Solução 1)
```

**Critério de "pronto":** a API devolve uma rota com ao menos 2 modos, indica acessibilidade e emite pelo menos 1 alerta vindo dos relatos do bairro.
