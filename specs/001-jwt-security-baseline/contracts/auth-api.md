# Contract — Auth API (001-jwt-security-baseline)

> Contrato dos endpoints novos + esquema de segurança. Espelhado no `openapi.yaml` (tag `Auth`) no mesmo ciclo (R10).

## Security Scheme (openapi.yaml)

```yaml
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

- Aplicado por **default nas operações protegidas**; endpoints públicos (login, cadastro, leitura de vagas…) declaram `security: []`.
- Header: `Authorization: Bearer <token>`.

## `POST /api/v1/auth/cadastro` — público

**Request**
```json
{ "email": "candidato@exemplo.com", "senha": "senha-forte-123" }
```

| Campo | Tipo | Regras |
|---|---|---|
| email | string | @NotBlank @Email · único (409 se duplicado) |
| senha | string | @Size(min=8, max=72) · BCrypt no storage |

**Responses**

| Status | Quando | Body |
|---|---|---|
| `201` | criado | `{ "id": 1, "email": "candidato@exemplo.com" }` (sem senha, sem papel explícito — default CANDIDATO) |
| `400` | validação | problem+json com violações |
| `409` | email já existe | problem+json `{ "title": "Conflito" }` |
| `429` | rate limit | problem+json `{ "title": "Muitas requisições" }` |

*Papel nunca vem do request (FR-013).*

## `POST /api/v1/auth/login` — público

**Request**
```json
{ "email": "candidato@exemplo.com", "senha": "senha-forte-123" }
```

**Response `200`**
```json
{
  "token": "<jwt-hs256>",
  "tokenType": "Bearer",
  "expiresIn": 1800,
  "usuario": { "id": 1, "email": "candidato@exemplo.com", "papel": "CANDIDATO" }
}
```

| Status | Quando |
|---|---|
| `401` | credenciais inválidas — **mensagem idêntica** para usuário inexistente e senha errada (prevenção de enumeração) |
| `400` | validação |
| `429` | rate limit (10/min por IP default) |

## Claims do JWT (HS256)

| Claim | Valor |
|---|---|
| `sub` | `usuarioId` (string) |
| `email` | email do usuário |
| `papel` | `CANDIDATO` \| `RH` \| `EMPRESA` |
| `iat` / `exp` | emitted / expiry (TTL `JWT_EXPIRATION`, default 1800s) |

Assinatura: chave simétrica `JWT_SECRET` (≥32 chars, obrigatória em prod — R7).

## Erros de autorização (todos os endpoints protegidos)

**`401` — não autenticado / token inválido / expirado**
```http
HTTP/1.1 401
WWW-Authenticate: Bearer realm="Segunda Carreira", error="invalid_token"
Content-Type: application/problem+json

{ "type": "about:blank", "title": "Não autenticado", "status": 401,
  "detail": "Token ausente, expirado ou inválido" }
```

**`403` — papel insuficiente ou posse violada**
```http
HTTP/1.1 403
Content-Type: application/problem+json

{ "type": "about:blank", "title": "Acesso negado", "status": 403,
  "detail": "Sem permissão para este recurso" }
```

*403 de posse e de papel usam o mesmo body (não revelar existência do recurso alheio).*

## Matriz de acesso (contrato — espelha spec US2)

| Grupo de endpoints | Anônimo | CANDIDATO | RH |
|---|---|---|---|
| `GET /vagas*` (exceto internos), `GET /trilhas/**`, `GET /mentorias` | ✅ | ✅ | ✅ |
| `GET /actuator/health` | ✅ | ✅ | ✅ |
| `POST /auth/cadastro`, `POST /auth/login` | ✅ | ✅ | ✅ |
| `/perfis/**` (CRUD + currículo + transferíveis) | 401 | ✅ dono | ✅ |
| `/inscricoes/**`, `/webhooks/lembretes` | 401 | ✅ dono | ✅ |
| `POST /curriculo/age-blind`, `POST /curriculo/auditoria` | 401 | ✅ | ✅ |
| `POST /revisao/solicitacao` | 401 | ✅ dono (`perfilId` do body == dono) | ✅ |
| `GET /funil/**`, `POST /vagas/auditoria-linguagem` | 401 | 403 | ✅ |

## Decisões de contrato

- **Auto-vínculo**: `POST /perfis` com CANDIDATO sem `perfil_id` vincula o novo perfil ao usuário (US2.6).
- **Webhook** exige inscrição pertencente ao dono (R4).
- Rate limit: somente `/auth/**`; headers `X-RateLimit-Remaining` (opcional, nice-to-have).
