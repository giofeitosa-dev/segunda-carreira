# Quickstart — validação do 002-observability-audit

> Guia de validação end-to-end. Não contém implementação — só como provar que funciona.

## Pré-requisitos

- Java 17 + Maven wrapper (`ageblind-api/api/mvnw`)
- Node 22 (se tocar front — não é o caso deste ciclo)

## 1. Suíte da API (obrigatória a cada commit)

```bash
cd ageblind-api/api
./mvnw clean verify        # Windows: .\mvnw.cmd clean verify
```

**Esperado**: todos os 117+ testes verdes, incluindo novos `AuditoriaControllerTest`, `CorrelationIdFilterTest`, `AuditoriaAspectTest`, `ActuatorHardeningTest` e `ContratoOpenApiTest` (escopo AgeBlind = 20 paths).

## 2. Smoke manual da auditoria (profile seed)

```bash
cd ageblind-api/api
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

```bash
# 2a. login como candidato para obter token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"candidato@demo.com","senha":"demo123"}' | jq -r .token)

# 2b. criar perfil (gera registro de auditoria com usuario_id)
curl -s -X POST http://localhost:8080/api/v1/perfis \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Teste","email":"ana.teste@demo.com","areaAtual":"Producao","areaAlvo":"Logistica"}'   # → 201

# 2c. tentar acessar perfil alheio como candidato → 403 + registro de auditoria
curl -s -o /dev/null -w '%{http_code}\n' -X GET http://localhost:8080/api/v1/perfis/1 \
  -H "Authorization: Bearer $TOKEN"   # → 403 (se o perfil 1 não for do candidato)

# 2d. login como RH
RH_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"rh@demo.com","senha":"rh-demo"}' | jq -r .token)

# 2e. ler auditoria como RH → 200 com registros
curl -s -X GET "http://localhost:8080/api/v1/auditoria?page=0&size=10" \
  -H "Authorization: Bearer $RH_TOKEN"   # → 200 com lista

# 2f. ler auditoria como candidato → 403
curl -s -o /dev/null -w '%{http_code}\n' -X GET "http://localhost:8080/api/v1/auditoria" \
  -H "Authorization: Bearer $TOKEN"   # → 403
```

## 3. Correlation-id e logging estruturado

```bash
# 3a. requisição com header X-Correlation-Id
curl -s -D - -o /dev/null -X GET http://localhost:8080/api/v1/vagas \
  -H 'X-Correlation-Id: meu-id-123'   # → resposta deve conter X-Correlation-Id: meu-id-123

# 3b. requisição sem header → UUID gerado
curl -s -D - -o /dev/null -X GET http://localhost:8080/api/v1/vagas   # → X-Correlation-Id presente com UUID

# 3c. logs em stdout devem conter JSON com correlationId
# (observar no console da aplicação ao fazer requisições acima)
```

## 4. Actuator hardening (profile prod)

```bash
# 4a. em prod, /actuator/health é público
JWT_SECRET=<chave-aleatoria-32+> SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/actuator/health   # → 200

# 4b. em prod, /actuator/metrics não exposto sem auth
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/actuator/metrics   # → 401 ou 404
```

## 5. Supply-chain CI

O CI (`.github/workflows/ci.yml`) executa:
- `./mvnw dependency:audit` — falha se houver vulnerabilidades conhecidas
- `gitleaks` — varre histórico em busca de secrets
- Dependabot — cria PRs para dependências desatualizadas (ecossistema Maven)

## 6. Contrato

- `openapi.yaml` valida (importar no Swagger Editor): `GET /api/v1/auditoria` com tag `Auditoria` e `security: [{bearerAuth: []}]`.
- `ContratoOpenApiTest` verde: escopo AgeBlind = 20 paths (era 19 no ciclo 001).
