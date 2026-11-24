# Contract — Auditoria de Segurança (002-observability-audit)

> Contrato do endpoint novo + esquema de auditoria. Espelhado no `openapi.yaml` (tag `Auditoria`) no mesmo ciclo.

## `GET /api/v1/auditoria` — só RH

**Request**
```http
GET /api/v1/auditoria?page=0&size=20&acao=PERFIL_CRIADO
```

| Parâmetro | Tipo | Regras |
|---|---|---|
| `page` | integer | default 0 |
| `size` | integer | default 20, máximo 100 |
| `acao` | string | filtra por tipo de ação (opcional) |

**Responses**

| Status | Quando | Body |
|---|---|---|
| `200` | sucesso | Página de `AuditoriaResponse` |
| `401` | sem token / token inválido | problem+json com `WWW-Authenticate: Bearer` |
| `403` | usuário não é RH | problem+json `{"title":"Acesso negado", ...}` |

**Body `AuditoriaResponse`**
```json
{
  "id": 1,
  "usuarioId": 7,
  "acao": "PERFIL_CRIADO",
  "recurso": "/api/v1/perfis",
  "ip": "192.168.1.100",
  "dataHora": "2026-11-18T14:30:00",
  "correlationId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Matriz de acesso**

| Papel | `GET /api/v1/auditoria` |
|---|---|
| Anônimo | 401 |
| CANDIDATO | 403 |
| RH | 200 |

## Tabela `auditoria_seguranca` (banco)

| Coluna | Tipo | Observação |
|---|---|---|
| `id` | BIGINT PK | auto-increment |
| `usuario_id` | BIGINT, NULL, FK → `usuario(id)` | usuário autenticado (null em endpoint público) |
| `acao` | VARCHAR(100), NOT NULL | tipo de ação |
| `recurso` | VARCHAR(500), NOT NULL | caminho da API acessado |
| `ip` | VARCHAR(45), NOT NULL | IP do cliente |
| `data_hora` | TIMESTAMP, NOT NULL | momento do evento |
| `correlation_id` | VARCHAR(64), NOT NULL | correlação com logs e resposta HTTP |

## Decisões de contrato

- **Imutável**: registros de auditoria não são editados nem excluídos neste ciclo.
- **Privacidade**: nunca armazena corpo da requisição/resposta, senha ou token (R5).
- **Falha-segura**: se a gravação em `auditoria_seguranca` falhar, a requisição principal não falha (R4).
