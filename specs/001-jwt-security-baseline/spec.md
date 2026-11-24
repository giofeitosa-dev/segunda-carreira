# Feature Specification: Autenticação JWT e Baseline de Segurança

**Feature Branch**: `001-jwt-security-baseline`

**Created**: 2026-11-17

**Status**: Draft

**Input**: User description: "JWT completo (candidato + RH) — cadastro/login, autorização por papel e posse, hardening baseline (headers, CORS, rate limit, secret fail-fast), contrato bearer no OpenAPI. Leitura pública mantida em vagas/trilhas/mentorias/busca externa. Papel EMPRESA fora de escopo."

**Constitution**: [`.specify/memory/constitution.md`](../../.specify/memory/constitution.md) (v1.0.0) — princípios I (Security-by-default), III (API-first), IV (Test-first), V (YAGNI).

## Contexto

Hoje a API tem 31 mappings em 9 controllers com apenas 2 regras de autorização (`SecurityConfig.java:39-40`): HTTP Basic protege `POST /vagas/auditoria-linguagem` e `/funil/**` (ROLE_RH); os demais 29 endpoints são `permitAll` (`SecurityConfig.java:44`). A única identidade é um usuário in-memory `rh` com senha `{noop}rh-demo` (`SecurityConfig.java:50-58`). Não existe entidade `Usuario`, papel CANDIDATO/EMPRESA, token, nem infraestrutura de login.

O front (`ageblind-front/`) não monta header `Authorization` em lugar nenhum (`src/services/api.js:14-20`), não tem contexto de login e `/rh/painel` é rota aberta (`App.jsx:13-29`); o "login" do RH hoje é o prompt nativo do browser (HTTP Basic). O E2E hardcódica `rh`/`rh-demo` via `httpCredentials` do Playwright (`e2e/smoke.spec.js:60,75`).

Este ciclo implementa autenticação e autorização do zero (não é "trocar Basic por JWT") e o baseline de hardening de segurança.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cadastro e login com JWT (Priority: P1)

Como **candidato** (ou RH), quero me cadastrar e me logar com email e senha para receber um token de acesso que identifica meu papel no sistema.

**Why this priority**: É a fundação de todas as demais stories — sem token não existe autorização, posse nem hardening. Entrega valor isolada: a API ganha `/auth/cadastro` e `/auth/login` funcionais com testes, sem quebrar nada existente (aditivo).

**Independent Test**: `curl -X POST /api/v1/auth/cadastro` cria a conta; `POST /api/v1/auth/login` com credenciais válidas devolve `{"token": "...", "expiresIn": ...}`; senha errada devolve 401 problem+json; senha persistida nunca aparece em texto puro (BCrypt).

**Acceptance Scenarios**:

1. **Given** usuário não existe, **When** `POST /api/v1/auth/cadastro` com email/senha/papel válidos, **Then** `201` e usuário criado com senha BCrypt.
2. **Given** usuário já existe, **When** cadastro com mesmo email, **Then** `409` problem+json.
3. **Given** usuário cadastrado, **When** `POST /api/v1/auth/login` com senha correta, **Then** `200` com access token HS256 contendo `sub` (id), `email`, `papel` e `exp`.
4. **Given** usuário cadastrado, **When** login com senha errada, **Then** `401` problem+json (sem distinguir "usuário inexistente" de "senha errada" na mensagem).
5. **Given** campos ausentes/inválidos no cadastro ou login, **When** requisição enviada, **Then** `400` problem+json com lista de violações.
6. **Given** token válido, **When** qualquer endpoint protegido chamado com `Authorization: Bearer <token>`, **Then** `200` (autorizado).
7. **Given** token expirado ou adulterado, **When** endpoint protegido chamado, **Then** `401` problem+json com `WWW-Authenticate: Bearer`.

---

### User Story 2 - Autorização por papel e posse (Priority: P1)

Como **sistema**, quero que cada endpoint exija o papel mínimo e que dados pessoais só sejam acessíveis pelo dono, para que nenhum candidato leia ou altere o perfil/currículo de outro.

**Why this priority**: É o coração do "Security-by-default" (Constituição I) e fecha os 29 endpoints abertos. Depende da US1 (token existe), mas é testável de forma independente via tokens mockados.

**Independent Test**: Com token CANDIDATO-A, `GET /perfis/{id-do-B}` devolve `403`; com token do dono devolve `200`; com token RH em `/funil/metricas` devolve `200`; sem token em endpoint de dados devolve `401`.

**Acceptance Scenarios**:

1. **Given** dois candidatos A e B, **When** A chama `GET/PUT /api/v1/perfis/{idDeB}`, **Then** `403` problem+json (posse violada).
2. **Given** token CANDIDATO, **When** chama `GET /api/v1/funil/metricas`, **Then** `403`.
3. **Given** sem token, **When** chama `POST /api/v1/curriculo/age-blind`, **Then** `401` com `WWW-Authenticate: Bearer`.
4. **Given** sem token, **When** chama `GET /api/v1/vagas` ou `/trilhas/{area}` ou `/mentorias` ou `/vagas/busca-externa`, **Then** `200` (leitura pública mantida).
5. **Given** token RH, **When** chama `POST /api/v1/vagas/auditoria-linguagem` e `GET /api/v1/funil/metricas`, **Then** `200`.
6. **Given** token CANDIDATO sem `Perfil` linkado, **When** chama recurso que exige perfil, **Then** `403` ou `404` documentado (decisão no plan).
7. **Given** webhook `POST /api/v1/webhooks/lembretes`, **When** chamado pelo dispatcher interno, **Then** segue autenticação definida no plan (serviço-to-serviço vs. papel específico) — sem ficar aberto silenciosamente.

**Matriz de acesso (contrato deste ciclo)**:

| Endpoint | Anônimo | CANDIDATO | RH |
|---|---|---|---|
| `GET /vagas`, `/vagas/{id}`, `/vagas/recomendadas`, `/vagas/busca-externa` | ✅ | ✅ | ✅ |
| `GET /trilhas/{area}`, `GET /mentorias` | ✅ | ✅ | ✅ |
| `GET /v3/api-docs`, `/swagger-ui/**` (dev) | ✅ | ✅ | ✅ |
| `GET /actuator/health` | ✅ | ✅ | ✅ |
| `POST/GET/PUT /perfis/**`, `/perfis/{id}/**` (currículo, transferíveis) | ❌ 401 | ✅ dono | ✅ |
| `POST/GET/DELETE /inscricoes/**`, `POST /webhooks/lembretes` | ❌ 401 | ✅ dono | ✅ |
| `POST /curriculo/age-blind`, `POST /curriculo/auditoria` | ❌ 401 | ✅ | ✅ |
| `POST /revisao/solicitacao` | ❌ 401 | ✅ dono (perfilId do token) | ✅ |
| `POST /vagas/auditoria-linguagem`, `GET /funil/**` | ❌ 401 | ❌ 403 | ✅ |
| `GET /auditoria/**` (se existir — ciclo 002) | — | — | — |

*Papel EMPRESA: fora de escopo (decisão 2026-11-17); a entidade `Usuario` já nasce com o enum para evolução futura sem migração.*

---

### User Story 3 - Front: login, Bearer e guarda de rota (Priority: P2)

Como **usuário do front**, quero uma tela de login para operar com meu token, e que o painel RH exija login — em vez do prompt nativo do browser.

**Why this priority**: Sem a US3, a US2 quebra a experiência do demo (o prompt Basic some e o painel RH fica inalcançável). Depende da US1 (endpoint de login), é testável isoladamente com mock do login.

**Independent Test**: `npm test` — contexto de auth guarda/expõe token; `api.js` injeta `Authorization`; rota `/rh/painel` sem token redireciona para `/login`; com token renderiza o painel. E2E: fluxo de login preenche formulário e acessa o painel.

**Acceptance Scenarios**:

1. **Given** usuário na página de login, **When** submete email/senha válidos, **Then** token é persistido e usuário redirecionado para destino original.
2. **Given** sem token, **When** acessa `/rh/painel`, **Then** redireciona para `/login` (guard de rota).
3. **Given** token armazenado, **When** qualquer chamada via `services/api.js`, **Then** header `Authorization: Bearer <token>` presente.
4. **Given** chamada que retorna `401` (token expirado), **When** interceptada, **Then** token limpo e redirect para `/login`.
5. **Given** usuário logado, **When** clica em sair, **Then** token removido e redirect para home.
6. **Given** credenciais inválidas no login, **When** submetidas, **Then** mensagem de erro exibida sem vazar detalhes internos.

---

### User Story 4 - Hardening baseline (Priority: P2)

Como **operador**, quero a baseline de hardening da Constitution I para que produção não rode com segredos fracos, superfícies de debug abertas nem tráfego sem política de CORS.

**Why this priority**: Protege tudo o que a US1–US3 entregam; independente das demais (pode ser testada sem front), mas faz mais sentido após a virada JWT para testar em conjunto.

**Independent Test**: Subir com profile `prod` sem `JWT_SECRET` → falha na startup (fail-fast); `GET /swagger-ui.html` em prod → 404; resposta em prod contém `Strict-Transport-Security` e `X-Content-Type-Options`; requisição cross-origin fora da allowlist → bloqueada; 100 logins consecutivos rápidos → `429` após o limite.

**Acceptance Scenarios**:

1. **Given** profile `prod` sem `JWT_SECRET` definido, **When** app sobe, **Then** falha rápida com mensagem clara (nenhum default fraco aceito).
2. **Given** profile `prod`, **When** acesso `/swagger-ui.html`, `/v3/api-docs`, `/h2-console`, **Then** `404` (desativados fora de dev).
3. **Given** qualquer resposta da API, **When** inspecionada, **Then** headers `X-Content-Type-Options: nosniff`, `Referrer-Policy`, `X-Frame-Options` e HSTS (quando HTTPS) presentes.
4. **Given** CORS allowlist via env (`CORS_ALLOWED_ORIGINS`), **When** origem fora da lista faz preflight, **Then** bloqueada; origem da lista é permitida.
5. **Given** endpoint `/api/v1/auth/login`, **When** excede N tentativas por janela (ex.: 10/min por IP), **Then** `429` problem+json.
6. **Given** upload `POST /perfis/{id}/curriculo/arquivo`, **When** excede limite de taxa/size, **Then** `429`/`413` tratados (size 10MB já existe — rate é o delta).

---

### User Story 5 - Contrato, testes e E2E (Priority: P2)

Como **consumidor da API e CI**, quero o contrato OpenAPI com `bearerAuth` e a suíte migrada para JWT, para que o spec, os testes e o E2E reflitam a realidade nova sem perder a trava de contrato.

**Why this priority**: Fechamento obrigatório do ciclo (Constituição III e IV) — sem ela o `ContratoOpenApiTest` e o E2E quebram a CI. Independente no sentido de ser verificável por `mvnw verify` + `npm run e2e`.

**Independent Test**: `./mvnw clean verify` verde com todos os testes usando token/jwt post-processor; `npm run e2e` verde com login via API/UI; `openapi.yaml` importa no Swagger e mostra `bearerAuth`.

**Acceptance Scenarios**:

1. **Given** `openapi.yaml`, **When** inspecionado, **Then** `components.securitySchemes.bearerAuth` (http, bearer, JWT) definido e aplicado às operações protegidas.
2. **Given** springdoc, **When** `/v3/api-docs` gerado, **Then** `/auth/**` aparece com tag `Auth` **fora** do escopo AgeBlind (trava de 19 paths do `ContratoOpenApiTest` preservada ou atualizada com justificativa).
3. **Given** suíte de testes, **When** `mvnw verify`, **Then** nenhum teste depende de HTTP Basic ou de endpoints abertos; asserts de `WWW-Authenticate: Basic` substituídos por `Bearer`.
4. **Given** E2E Playwright, **When** roda, **Then** não usa mais `httpCredentials` — login obtido via API ou formulário.
5. **Given** READMEs, **When** lidos, **Then** credenciais/curl de exemplo atualizados para o fluxo JWT.

### Edge Cases

- Token expirado no meio de uma sessão do painel RH → 401 + limpeza + redirect (US3.4).
- `JWT_SECRET` com valor default conhecido (`changeme`, etc.) em prod → rejeitado no fail-fast (US4.1).
- Usuário cadastrado como CANDIDATO tenta login e não tem `Perfil` → pode usar a API para `POST /perfis` (criar o próprio); acesso a id alheio → 403 (US2.1).
- Cadastro com papel `RH` público? → decisão de product: restringir `RH` a seed/admin no plan (recomendado: cadastro público só cria CANDIDATO; RH nasce via seed).
- Força bruta no login → rate limit (US4.5); resposta 401 idêntica para usuário inexistente (US1.4 — enumeração prevenida).
- Token adulterado/assinado com outra chave → 401 (US1.7).
- Webhook de lembretes chamado pelo job interno sem HTTP externo → mecanismo definido no plan (não deixar `permitAll` por omissão).
- Testes existentes que criam `perfilId` em sequência → fixture central de token do dono para não quebrar ordem de execução.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Sistema MUST manter leitura pública de `GET /vagas*`, `/trilhas/**`, `/mentorias` e `/vagas/busca-externa` (decisão de produto 2026-11-17).
- **FR-002**: Sistema MUST persistir `Usuario` (id, email único, senha BCrypt, papel enum `CANDIDATO|RH|EMPRESA`, opcional `perfilId` FK) via Flyway `V2__*.sql` — sem `ddl-auto` como mecanismo principal.
- **FR-003**: Sistema MUST expor `POST /api/v1/auth/cadastro` e `POST /api/v1/auth/login` devolvendo access token JWT HS256 (`sub`, `email`, `papel`, `exp`).
- **FR-004**: Sistema MUST aceitar `JWT_SECRET` e `JWT_EXPIRATION` via env; profile `prod` MUST falhar na startup sem secret não-default.
- **FR-005**: Sistema MUST exigir `Authorization: Bearer` em todos os endpoints de dados pessoais e webhook, com checagem de posse (`perfilId` do token) onde aplicável (matriz da US2).
- **FR-006**: Sistema MUST manter RH protegendo `/funil/**` e `POST /vagas/auditoria-linguagem` — papel obtido do token.
- **FR-007**: Sistema MUST remover HTTP Basic e o `InMemoryUserDetailsManager` (usuário `rh` in-memory substituído por seed no banco).
- **FR-008**: Sistema MUST devolver 401/403 em problem+json (RFC 7807) com `WWW-Authenticate: Bearer` no 401.
- **FR-009**: Front MUST injetar `Authorization` em todas as chamadas de `services/api.js`, ter tela de login, guarda de `/rh/painel` e tratamento de 401.
- **FR-010**: Sistema MUST aplicar security headers padrão, CORS allowlist via env, rate limit em `/auth/**` e desativar Swagger/H2 em prod.
- **FR-011**: `openapi.yaml` MUST declarar `bearerAuth` e refletir as operações protegidas; `ContratoOpenApiTest` MUST continuar válido (escopo AgeBlind = 19 paths ou asserção atualizada com justificativa).
- **FR-012**: Seed MUST criar usuários demo (CANDIDATO, RH) com senhas conhecidas apenas de demo, respeitando a Constituição II (não usar default fraco em prod).
- **FR-013**: Cadastro público MUST criar apenas CANDIDATO; papel RH MUST nascer de seed/admin (sem auto-cadastro de RH) [NEEDS CLARIFICATION: confirmar no plan — recomendação é esta].
- **FR-014**: Todo teste HTTP existente MUST ser migrado para jwt post-processor/`@WithMockUser` equivalente; zero testes dependendo de Basic.

### Key Entities

- **Usuario**: identidade autenticável — email (único), senha (BCrypt, nunca em resposta), papel (CANDIDATO/RH/EMPRESA), vínculo opcional com `Perfil`; criado por cadastro ou seed.
- **Perfil** (existente): dado pessoal do candidato — passa a ter dono (`usuarioId`) para checagem de posse; seed de perfis demo vincula ao usuário CANDIDATO demo.
- **Token JWT** (stateless): `sub`=usuarioId, `email`, `papel`, `exp`; sem refresh token nesta versão (YAGNI — Constituição V).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Zero endpoints de dado pessoal acessíveis sem token (matriz da US2 implementada e coberta por testes de autorização).
- **SC-002**: `./mvnw clean verify` verde — suíte migrada, nenhum teste com Basic, nenhum teste assumindo endpoint aberto indevidamente.
- **SC-003**: `npm test` e `npm run e2e` verdes — E2E loga via JWT, sem `httpCredentials`.
- **SC-004**: Em profile `prod`: Swagger/H2 retornam 404 e app sem `JWT_SECRET` não sobe.
- **SC-005**: `openapi.yaml` valida com `bearerAuth` e `ContratoOpenApiTest` verde.
- **SC-006**: Rate limit bloqueia força bruta em `/auth/login` (429 após limite configurado).
- **SC-007**: READMEs de API e front documentam o fluxo JWT com curl/exemplo de login atualizados.

## Assumptions

- Token stateless sem refresh/expiry curto (30min default) é aceitável para demo/hackathon; refresh token fica para spec futura.
- Papel EMPRESA existe no enum mas não tem endpoints neste ciclo (adiado — ROADMAP).
- Webhook de lembretes é chamado pelo job interno do mesmo processo — mecanismo de autenticação interna (ex.: header de serviço via env) definido no plan, nunca `permitAll` silencioso.
- Rate limit em memória (single-instance) é suficiente — sem Redis (Constituição V).
- O front continua sem TypeScript (stack existente JS+Vite mantida).
- Migração Flyway `V2__` parte do baseline `V1__init.sql` existente; `Perfil` ganha coluna de dono com dados seed retrovinculados.
- Swagger fica disponível em dev/teste (profile default) — apenas prod o desativa.
- Compatibilidade com dados demo existentes: seed `perfis-seed.csv` passa a vincular ao usuário CANDIDATO demo criado no `V2__`/seed runner.
