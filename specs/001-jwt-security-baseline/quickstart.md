# Quickstart — validação do 001-jwt-security-baseline

> Guia de validação end-to-end. Não contém implementação — só como provar que funciona.

## Pré-requisitos

- Java 17 + Maven wrapper (`ageblind-api/api/mvnw`)
- Node 22 (front/E2E)
- Docker (para E2E com stack completo)

## 1. Suíte da API (obrigatória a cada commit)

```bash
cd ageblind-api/api
./mvnw clean verify        # Windows: .\mvnw.cmd clean verify
```
**Esperado**: todos os testes verdes, incluindo migrados para JWT (`jwt()` post-processor), novos `AuthControllerTest`, `PosseCheckerTest`, `RateLimitFilterTest` e `ContratoOpenApiTest` (escopo AgeBlind = 19 paths, `/auth` fora).

## 2. Smoke manual da auth (profile seed)

```bash
cd ageblind-api/api
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

```bash
# 2a. cadastro
curl -s -X POST http://localhost:8080/api/v1/auth/cadastro \
  -H 'Content-Type: application/json' \
  -d '{"email":"novo@exemplo.com","senha":"senha-forte-123"}'   # → 201

# 2b. login
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"candidato@demo.com","senha":"demo123"}' | jq -r .token)
# → 200 { token, expiresIn, usuario }

# 2c. endpoint público sem token
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/v1/vagas   # → 200

# 2d. endpoint de dados SEM token
curl -s -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/api/v1/curriculo/age-blind \
  -H 'Content-Type: application/json' -d '{"curriculo":"x"}'                  # → 401 + WWW-Authenticate: Bearer

# 2e. COM token
curl -s -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/api/v1/curriculo/age-blind \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"curriculo":"Analista com 20 anos de experiencia"}'                     # → 200

# 2f. posse: logar como candidato A e pedir perfil de B → 403
# 2g. RH: login rh@demo.com → GET /api/v1/funil/metricas com token → 200; token CANDIDATO → 403
```

## 3. Fail-fast e hardening (profile prod)

```bash
# 3a. SEM JWT_SECRET → app não sobe (mensagem clara de erro)
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run    # → IllegalStateException no boot

# 3b. Com secret: Swagger/H2 devem ser 404
JWT_SECRET=<chave-aleatoria-32+> SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/swagger-ui.html  # → 404
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/h2-console       # → 404

# 3c. rate limit: 11 logins seguidos → último 429
for i in $(seq 1 11); do
  curl -s -o /dev/null -w '%{http_code}\n' -X POST http://localhost:8080/api/v1/auth/login \
    -H 'Content-Type: application/json' -d '{"email":"x@y.com","senha":"errada123"}'
done   # → 401 x10, 429 no 11º
```

## 4. Front (Vitest)

```bash
cd ageblind-front
npm test
```
**Esperado**: testes do `AuthContext` (login/logout/401), do `RequireAuth` (redirect) e do `api.js` (header Bearer) verdes; suíte antiga preservada.

## 5. E2E (stack docker)

```bash
docker compose up -d --build
cd ageblind-front && npm run e2e
```
**Esperado**: login via API (token em localStorage) no `smoke.spec.js`; cenário de painel RH autenticado verde; **zero** `httpCredentials`.

## 6. Contrato

- `openapi.yaml` valida (importar no Swagger Editor): `bearerAuth` presente, paths `/auth/cadastro` e `/auth/login` com tag `Auth` e `security: []`.
- `GET /v3/api-docs` em dev mostra os 2 endpoints novos.
- READMEs (`README.md` raiz, `ageblind-api/api/README.md`, front) documentam login e curl com Bearer.
