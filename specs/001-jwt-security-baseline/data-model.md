# Data Model — 001-jwt-security-baseline (Phase 1)

## Entidade nova: `Usuario`

| Campo | Tipo | Restrições | Observação |
|---|---|---|---|
| `id` | BIGINT PK | auto-increment | gerado pela DB |
| `email` | VARCHAR(250) | NOT NULL, **UNIQUE** | login; normalizado minúsculas no service |
| `senha` | VARCHAR(100) | NOT NULL | **BCrypt** (`{bcrypt}` nunca — só o hash puro, prefixo `$2a$`); nunca em resposta |
| `papel` | VARCHAR(20) | NOT NULL, CHECK em (`CANDIDATO`,`RH`,`EMPRESA`) | enum Java `Papel`; `EMPRESA` sem uso neste ciclo (adiado) |
| `perfil_id` | BIGINT | NULL, FK → `perfil(id)` ON DELETE SET NULL | vínculo 1:1 opcional (R3); fonte de verdade da posse |
| `criado_em` | TIMESTAMP | NOT NULL DEFAULT current_timestamp | auditoria básica |

**Índices**: `uk_usuario_email` (unique em `email`), `idx_usuario_perfil` (FK).

## Relacionamentos

```
Usuario (1) ────── (0..1) Perfil
   │                        │
   │ perfil_id FK           │ (tabela existente, intocada)
   └─ papel (enum)          └─ demais tabelas existentes via perfil_id
```

- **Posse**: `Usuario.perfilId == idDoRecurso` → dono; `papel == RH` → bypass (R5).
- `perfil_id` nullable: candidato recém-cadastrado ainda não tem perfil → `POST /perfis` cria e **auto-vincula** (edge case US2.6).

## Migração Flyway `V2__usuarios.sql`

```sql
CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(250) NOT NULL,
    senha       VARCHAR(100) NOT NULL,
    papel       VARCHAR(20)  NOT NULL,
    perfil_id   BIGINT,
    criado_em   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_papel CHECK (papel IN ('CANDIDATO','RH','EMPRESA')),
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (perfil_id) REFERENCES perfil (id) ON DELETE SET NULL
);
CREATE INDEX idx_usuario_perfil ON usuario (perfil_id);
```

- H2 e PostgreSQL compatíveis com o trecho acima (sem tipagens específicas de UM).
- `ddl-auto=update` do prod continua como safety legado — a tabela nasce pelo Flyway (Constituição: Flyway é o mecanismo principal).

## Sem mudanças em entidades existentes

- `Perfil`, `Inscricao`, `Currículo` etc. **não ganham colunas** — o vínculo é só de `usuario.perfil_id`.
- Seeds existentes (`perfil`, `vaga`, …) intactos; `UsuarioSeedRunner` referencia o primeiro perfil criado pelo `PerfilSeedRunner` (ordem via `@Profile("seed")` + dependência de bean ou consulta por id known-first).

## Estados do usuário

```
[cadastro] → ATIVO ──(exclusão futura, ciclo 003)──▶ (não há estado inativo/expirado neste ciclo)
```

Sem lockout permanente por força bruta (rate limit por janela é suficiente — R2); sem flag de email verificado (fora de escopo).

## Regras de validação (Bean Validation no DTO)

- `CadastroRequest`: `email` @NotBlank @Email · `senha` @NotBlank @Size(min=8, max=72) (BCrypt aceita até 72 bytes).
- `LoginRequest`: `email` @NotBlank @Email · `senha` @NotBlank.
- Papel no cadastro: ignorado do request (default CANDIDATO) ou aceito apenas `CANDIDATO` — **decisão: não vem do client** (FR-013: RH só via seed).
