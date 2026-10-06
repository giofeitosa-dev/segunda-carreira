# AgeBlind API

> *"A API que contrata por competência, não por ano de nascimento."*

API REST do módulo **Reconecta** que combina **empregabilidade 40+** (Carreira: vagas,
trilhas, mentorias) com o **Escudo Anti-Etarismo** (currículos e vagas livres de
viés etário). Hackathon ReCode — ODS 10 (redução das desigualdades) + ODS 5.

**Stack:** Java 17 (Zulu) · Spring Boot **4.1.1** · Maven · Spring Data JPA · H2 ·
Actuator · SpringDoc 3.1.1

---

## Rodar

```bash
# build + testes (50/50)
mvnw.cmd clean package            # Linux/macOS: ./mvnw clean package

# API em http://localhost:8080 (H2 em memória, sem seed)
mvnw.cmd spring-boot:run

# com o SEED DEMO (10 perfis, 20 vagas, 5 trilhas)
mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=seed"

# jar
java -jar target\ageblind-api-0.0.1-SNAPSHOT.jar --spring.profiles.active=seed
```

> **Nesta máquina (Windows):** `JAVA_HOME=C:\Program Files\Java\zulu17.36.17-ca-jdk17.0.4.1-win_x64`
> — o `java.exe` do PATH é shim da Oracle e trava. Caminhos com espaço/aceite
> (ex.: `Área de Trabalho`) precisam de aspas no argumento `-jar`.

### Endereços

| URL | O que é |
|---|---|
| http://localhost:8080/swagger-ui.html | Swagger UI (**AgeBlind API**) |
| http://localhost:8080/v3/api-docs | JSON OpenAPI (spec completa em [`../openapi.yaml`](../openapi.yaml)) |
| http://localhost:8080/actuator/health | Health (`UP`) |
| http://localhost:8080/h2-console | H2 (JDBC: `jdbc:h2:mem:ageblind`) |

---

## Endpoints (15 rotas AgeBlind)

### Módulo Carreira — ODS 10+5

| # | Método | Rota | Descrição |
|---|---|---|---|
| 1 | POST | `/api/v1/perfis` | Cadastra perfil (**sem data de nascimento**) → 201 |
| 2 | GET | `/api/v1/perfis/{id}` | Consulta perfil → 200 / 404 |
| 3 | POST | `/api/v1/perfis/{id}/competencias-transferiveis` | Experiência antiga → competências da área alvo (R12) |
| 4 | GET | `/api/v1/vagas` | Explora vagas (`?area=&modelo=&page=&size=`) |
| 5 | GET | `/api/v1/vagas/{id}` | Detalhe da vaga |
| 6 | GET | `/api/v1/vagas/recomendadas?perfilId=` | Ranking **só por competências** (sem idade) |
| 7 | GET | `/api/v1/trilhas/{area}` | Trilhas da área (modulos ordenados) |
| 8 | POST | `/api/v1/inscricoes` | Inscrição em vaga/trilha → 201 |
| 9 | POST | `/api/v1/webhooks/lembretes` | Agenda lembrete de prazo → 202 |
| 10 | GET | `/api/v1/mentorias` | Encaixe de mentoria (`?perfilId=&area=`) |

### Módulo Escudo Anti-Etarismo ⭐ — ODS 10+5

| # | Método | Rota | Descrição |
|---|---|---|---|
| 11 | POST | `/api/v1/curriculo/age-blind` | Aplica 15 regras etaristas (10 automáticas) |
| 12 | POST | `/api/v1/curriculo/auditoria` | Score ATS + sinais de viés + severidade |
| 13 | POST | `/api/v1/vagas/auditoria-linguagem` | Termos discriminatorios + risco legal (CLT 373-A, Lei 9.029) |
| 14 | GET | `/api/v1/funil/metricas?vagaId=` | Funil por faixa + `razaoAdverseImpact < 0,80` |
| 15 | POST | `/api/v1/revisao/solicitacao` | Revisão de decisão automatizada (LGPD art. 20) → protocolo `REV-` |

Erros em **RFC 7807** (`application/problem+json`): 400 validação, 404 recurso,
405 método, 500 interno.

---

## Exemplos curl

Base: `http://localhost:8080`. Com o profile `seed`, existem **perfil id=1
(Ana Souza)** e **vaga id=1 (Analista de Logística)** prontos.

```bash
# 1) criar perfil (sem idade — regra de ouro)
curl -X POST http://localhost:8080/api/v1/perfis \
  -H "Content-Type: application/json" \
  -d '{"nome":"Joana Prado","email":"joana@recode.demo","areaAtual":"Producao","areaAlvo":"Logistica","pretensaoSalarial":5000,"disponibilidade":["REMOTO"]}'

# 2) consultar perfil
curl http://localhost:8080/api/v1/perfis/1

# 3) competências transferíveis (regra R12)
curl -X POST http://localhost:8080/api/v1/perfis/1/competencias-transferiveis

# 4) explorar vagas (paginação + filtro)
curl "http://localhost:8080/api/v1/vagas?area=Logistica&modelo=REMOTO&page=0&size=5"

# 5) detalhe da vaga
curl http://localhost:8080/api/v1/vagas/1

# 6) recomendações — ranking só por competências
curl "http://localhost:8080/api/v1/vagas/recomendadas?perfilId=1"

# 7) trilhas da área
curl http://localhost:8080/api/v1/trilhas/LOGISTICA

# 8) inscrição → 201
curl -X POST http://localhost:8080/api/v1/inscricoes \
  -H "Content-Type: application/json" \
  -d '{"perfilId":1,"tipo":"VAGA","referenciaId":"1"}'

# 9) lembrete de prazo → 202
curl -X POST http://localhost:8080/api/v1/webhooks/lembretes \
  -H "Content-Type: application/json" \
  -d '{"inscricaoId":1,"antecedenciaHoras":24}'

# 10) mentorias (filtra por areaAlvo do perfil)
curl "http://localhost:8080/api/v1/mentorias?perfilId=1"

# 11) curriculo age-blind — aplica as regras etaristas
curl -X POST http://localhost:8080/api/v1/curriculo/age-blind \
  -H "Content-Type: application/json" \
  -d '{"curriculo":"Maria, 51 anos. Graduação em Administração concluída em 1998. 25 anos de experiência. Profissional madura e experiente.","vagaAlvo":"Coordenadora de Logistica"}'

# 12) auditoria de currículo (score ATS + sinais)
curl -X POST http://localhost:8080/api/v1/curriculo/auditoria \
  -H "Content-Type: application/json" \
  -d '{"curriculo":"Carlos, jovem nativo digital, 10 anos de experiência em vendas.","vagaAlvo":"Vendedor"}'

# 13) auditoria de linguagem da vaga
curl -X POST http://localhost:8080/api/v1/vagas/auditoria-linguagem \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Estágio","texto":"Buscamos profissional jovem, dinâmico e com no máximo 5 anos de experiência."}'

# 14) métricas do funil (adverse impact por faixa)
curl "http://localhost:8080/api/v1/funil/metricas?vagaId=1"

# 15) solicitação de revisão → protocolo REV-
curl -X POST http://localhost:8080/api/v1/revisao/solicitacao \
  -H "Content-Type: application/json" \
  -d '{"perfilId":1,"motivo":"Candidatura rejeitada por automação; solicito reanálise humana."}'
```

---

## Seed demo (profile `seed`)

| Arquivo | Conteúdo |
|---|---|
| `src/main/resources/data/vagas-seed.csv` | 20 vagas em 6 áreas, prazos e competências |
| `src/main/resources/data/perfis-seed.csv` | 10 perfis com níveis de competência |
| `src/main/resources/data/trilhas-seed.csv` | 5 trilhas com módulos e cargas horárias |
| `src/main/resources/data/curriculos/sinalizado.txt` | Currículo com sinais etaristas (fixture) |
| `src/main/resources/data/curriculos/otimizado.txt` | Versão age-blind do mesmo currículo |
| `src/main/resources/escudo/regras-idade.json` | 15 regras (regex tolerantes a acento) |
| `src/main/resources/escudo/termos-discriminatorios.json` | 6 termos + base legal |
| `src/main/resources/carreira/sinonimos.json` | 10 grupos de sinônimos (transferíveis) |

Runners (`VagaSeedRunner`, `TrilhaSeedRunner`, `PerfilSeedRunner`) são
idempotentes (pulam se já houver dados) e só rodam com `--spring.profiles.active=seed`.

---

## Regras de ouro

1. **Nenhuma entidade tem `dataNascimento` ou `idade`** (guarda por teste: `PerfilRepositoryTest`).
2. Ranking de vagas: só competências + área — nunca idade.
3. `FaixaEtaria` (`ATE_39`/`40_49`/`50_MAIS`) existe **apenas** em agregados
   anônimos (`MetricaFunil`) para medir adverse impact.
4. Módulos conversam por `Gateway` (`VagaGateway`, `PerfilGateway`), nunca por
   repositório cruzado.
5. Erros RFC 7807 · rotas `/api/v1` · Conventional Commits (`feat(D-08): ...`).

## Testes

```bash
mvnw.cmd test          # 50/50 verdes
```

Cobrem validações, anti-viés (idade fora do ranking), motor de regras,
auditorias, funil/adverse impact, seeds e o job de lembretes.

## Documentação da Sprint

- Planejamento: [`../SPRINT-1-PLANEJAMENTO.md`](../SPRINT-1-PLANEJAMENTO.md)
- Desenvolvimento: [`../SPRINT-2-DESENVOLVIMENTO.md`](../SPRINT-2-DESENVOLVIMENTO.md)
- Testes/Revisão: [`../SPRINT-3-TESTES-REVISAO.md`](../SPRINT-3-TESTES-REVISAO.md)
- Kanban: [`../kanban.md`](../kanban.md)
