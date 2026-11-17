# Implementation Plan: Autenticação JWT e Baseline de Segurança

**Branch**: `001-jwt-security-baseline` | **Date**: 2026-11-17 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-jwt-security-baseline/spec.md`

## Summary

A API hoje tem 29 de 31 endpoints em `permitAll` com um único usuário in-memory Basic (`rh`/`rh-demo`). Este ciclo implementa autenticação/autorização do zero: entidade `Usuario` (Flyway `V2__`), cadastro/login com JWT HS256 via Spring Security OAuth2 Resource Server, autorização por papel + posse (`usuario.perfilId` vs path), remoção do HTTP Basic, front com login/Bearer/guarda, e baseline de hardening (secret fail-fast, Swagger/H2 off em prod, headers, CORS, rate limit). Leitura pública de vagas/trilhas/mentorias/busca-externa é mantida (decisão de produto); papel EMPRESA fica só no enum.

## Technical Context

**Language/Version**: Java 17 · Spring Boot 4.1.1 (Spring Security 7)

**Primary Dependencies**: `spring-boot-starter-security` (já presente) + **novo** `spring-boot-starter-oauth2-resource-server` (Nimbus, HS256 simétrico) · `spring-boot-starter-validation` · SpringDoc 3.1.1 · Tika 3.3.3 (sem mudança)

**Storage**: H2 (dev/teste) · PostgreSQL 16 (prod) — Flyway `V1__init.sql` existente + **novo** `V2__usuarios.sql`

**Testing**: JUnit 5 + MockMvc + `spring-security-test` (`SecurityMockMvcRequestPostProcessors.jwt()`) · JaCoCo · Vitest (front) · Playwright E2E

**Target Platform**: Linux server (Docker) + browsers modernos · demo local Windows/macOS/Linux

**Project Type**: Web service (monolito modular Spring Boot) + SPA React

**Performance Goals**: N/A para este ciclo (auth stateless; rate limit in-memory)

**Constraints**: 401/401 problem+json RFC 7807 · `mvnw verify` verde a cada commit · E2E verde nos commits ⚠️ · token TTL 30min default · login ≤ 10 tentativas/min por IP

**Scale/Scope**: 31 mappings em 9 controllers afetados pela matriz · 74 testes HTTP migrados · 1 front React · demo com ~10 perfis seed

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Veredito | Evidência / Ação |
|---|---|---|
| I. Security-by-default | ✅ PASS | O ciclo **é** a implementação do princípio; matriz de acesso no spec; zero `permitAll` em dado pessoal |
| II. Privacidade & LGPD | ✅ PASS | `Usuario` minimiza PII (email+senha); senha BCrypt, nunca em resposta; nenhum dado de currículo persistido a mais |
| III. API-first | ✅ PASS | `openapi.yaml` ganha `bearerAuth` + paths `/auth` (tag `Auth`); `ContratoOpenApiTest` atualizado no mesmo ciclo; READMEs no mesmo PR |
| IV. Test-first | ✅ PASS | Migração dos 74 testes HTTP para `jwt()` é tarefa obrigatória; novos testes de auth/posse/401/403 por story |
| V. Simplicity / YAGNI | ✅ PASS | Sem refresh token, sem Redis, rate limit filter próprio em memória, front continua JS |
| Restrições de stack | ✅ PASS | Flyway `V2__` como mecanismo principal (prod mantém `ddl-auto=update` como safety legado já existente — dívida não expandida); PT-BR; segredos via env |
| Workflow & gates | ✅ PASS | Gates spec/plan seguidos; commits indivíduais com suite verde |

**Violações**: nenhuma → *Complexity Tracking* vazio.

## Project Structure

### Documentation (this feature)

```text
specs/001-jwt-security-baseline/
├── spec.md              # aprovado (GATE 2)
├── plan.md              # este arquivo
├── research.md          # Phase 0 — decisões R1..R14
├── data-model.md        # Phase 1 — entidade Usuario + migração V2
├── contracts/
│   └── auth-api.md      # Phase 1 — contratos de /auth, 401/403, securityScheme
├── quickstart.md        # Phase 1 — guia de validação end-to-end
└── tasks.md             # Phase 2 (/speckit.tasks — após GATE 3)
```

### Source Code (repository root)

```text
ageblind-api/api/src/main/java/com/reconecta/ageblind/
├── auth/                          # NOVO pacote
│   ├── api/                       # AuthController (cadastro, login)
│   ├── service/                   # AuthService (BCrypt, emissão JWT)
│   ├── domain/                    # Usuario, Papel (enum)
│   └── repository/                # UsuarioRepository
├── shared/
│   ├── config/
│   │   ├── SecurityConfig.java    # REESCRITO — JWT resource server + matriz
│   │   ├── JwtConfig.java         # NOVO — decoder, fail-fast secret, converter papel→ROLE_
│   │   ├── CorsConfig.java        # NOVO — allowlist via env
│   │   └── RateLimitConfig.java   # NOVO — filtro /auth/**
│   ├── security/                  # NOVO — UsuarioAtual (extrai sub do Jwt), PosseChecker
│   └── api/GlobalExceptionHandler.java  # ajustes pontuais se necessário
├── carreira/
│   ├── api/                       # ajustes de posse (Perfil, Inscricao)
│   └── seed/UsuarioSeedRunner.java  # NOVO — usuários demo (candidato, rh)
└── escudo/                        # só ajustes de autorização via matriz

ageblind-api/api/src/main/resources/
├── db/migration/V2__usuarios.sql  # NOVO
├── application.properties         # JWT_SECRET/JWT_EXPIRATION/CORS/RATE_LIMIT via env
└── application-prod.properties    # springdoc off + validação secret

ageblind-api/api/src/test/java/... # migração jwt() + novos AuthPosseTest, RateLimitTest...
ageblind-front/src/                # AuthContext, LoginPage, api.js Bearer, RequireAuth
ageblind-front/e2e/smoke.spec.js   # login via API em vez de httpCredentials
openapi.yaml                       # securitySchemes + paths /auth (tag Auth)
```

**Structure Decision**: pacote novo `auth/` isolado dos módulos `carreira`/`escudo` (monolito modular mantido — Constituição V); mudanças de segurança concentram-se em `shared/config` + `shared/security`; controllers existentes só ganham checagem de posse nos services (mínima dispersão).

## Complexity Tracking

*Preenchido apenas se Constitution Check tiver violações justificadas.*

| Violação | Por que é necessária | Alternativa mais simples rejeitada porque |
|---|---|---|
| — | — | — |
