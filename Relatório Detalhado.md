# Relatório Técnico e Estratégico — Segunda Carreira (AgeBlind)

**Escopo:** Documentação completa de concepção, origem criativa, arquitetura, módulos, front-end e integração
**ODS 5:** Gênero · **ODS 10:** Redução de Desigualdades
**Stack:** Java 17 · Spring Boot 4.1.1 · React 19 · PostgreSQL 16 · Docker

---

## 1. Origem Criativa e Contextualização Pessoal

A criação da aplicação Segunda Carreira (ou AgeBlind) surgiu a partir de questionamentos e vivências pessoais diante de barreiras em processos seletivos — marcados por silêncios corporativos ou respostas negativas genéricas, mesmo diante de qualificação técnica comprovada. Ao constatar que tal sentimento era compartilhado por outros profissionais que também duvidavam do peso de sua faixa etária, iniciou-se um levantamento aprofundado com dados oficiais sobre etarismo e empregabilidade. Essa investigação inicial resultou em um artigo no LinkedIn e despertou a curiosidade para desenvolver testes práticos de blindagem de currículo. O engajamento no programa Reconecta-Recode despertou a visão de que a dor não era isolada, e impulsionou a expansão da ideia para uma API de impacto sistêmico — capaz de proteger o candidato **e** a empresa.

## 2. Alinhamento com as ODS e Design Thinking

O projeto articula diretamente os Objetivos de Desenvolvimento Sustentável da ONU:

- **ODS 5 & ODS 10:** combate intersecional a vieses de gênero e etarismo algorítmico, promovendo inclusão e justiça econômica intergeracional.
- **O Problema (POV):** candidatos maduros são filtrados por robôs de triagem corporativa (ATS) que interpretam proxies temporais (ano de graduação, tempo total de carreira) como obsolescência.
- **A Pergunta Norteadora (HMW):** como ajudar profissionais 40+ a descobrirem competências transferíveis, apresentarem perfis invisíveis à triagem etária e fornecerem auditorias estatísticas e jurídicas para vagas e funis?

## 3. Análise de Concorrência e Diferenciais (Benchmark)

Uma varredura no ecossistema de soluções de RH revelou uma lacuna crítica: plataformas globais de otimização de currículo focam em palavras-chave genéricas, enquanto agências voltadas ao público 40+ operam prioritariamente como consultorias manuais ou portais de vagas sem tecnologia de blindagem algorítmica.

| Capacidade | Checkers de ATS (Jobscan/Enhancv) | Softwares de RH / ATS (Gupy/Workday) | Nossa API (Segunda Carreira) |
|---|---|---|---|
| Detecção de proxies etários | Parcial (foco isolado em data/idade) | Inexistente (caixa-preta de IA) | Completa (15 vetores: formatura, lacunas, cronologia) |
| Geração de currículo blindado | Apenas aponta o erro no painel | Não possui (foco na empresa) | Automatizada (gera versão age-blind estruturada) |
| Auditoria de linguagem de vaga | Inexistente | Parcial (foco em gênero/diversidade genérica) | Nativa PT-BR ("perfil jovem", CLT 373-A) |
| Métricas de funil (adverse impact) | Inexistente | Básico (sem recorte etário) | Embebida (razão < 0,80 por faixa etária) |
| Compliance LGPD (art. 20) | Inexistente | Variável | Nativo (protocolo de revisão humana) |

**Posicionamento estratégico (moat):** enquanto concorrentes cobram por otimização de palavras-chave, nossa API atua simultaneamente protegendo o candidato (Módulo Escudo) e resguardando a empresa de passivos trabalhistas decorrentes de triagem discriminatória — fechando o ciclo com recomendação direta de competências transferíveis e trilhas de requalificação.

## 4. Arquitetura da Solução (Monolito Modular)

Optou-se por um monolito modular em Spring Boot 4.1.1, com os módulos implementados e delimitados a seguir, desacoplados entre si por interfaces de serviço (*Gateways*) — o que garante a velocidade de entrega exigida em hackathons e facilita a eventual extração para microsserviços:

- **`carreira`** — perfis, competências transferíveis, currículo estruturado (consulta/edição/upload), vagas, recomendação, busca externa (Adzuna), trilhas, inscrições, lembretes e mentorias (17 endpoints);
- **`escudo`** — motor de 15 regras etaristas, auditoria de currículo e de linguagem de vaga, métricas de funil com adverse impact e revisão humana LGPD (5 endpoints);
- **`shared`** — tratamento de erros RFC 7807 (`GlobalExceptionHandler`), segurança (HTTP Basic stateless), configuração OpenAPI e caminhos LGPD.

> As soluções "Relatos do Bairro" e "Rotas Acessíveis" do projeto Reconecta pai permanecem em estágio de concepção documental e **estão fora do escopo da API entregue** — a especificação `openapi.yaml` mantém seus contratos apenas como catálogo de referência.

## 5. Modelo de Dados Consolidado (JPA / Hibernate · Flyway `V1__init.sql`)

O banco foi modelado para evitar qualquer armazenamento de dados sensíveis desnecessários: **nenhuma entidade possui data de nascimento ou idade** — guarda assegurada por teste automatizado (`PerfilRepositoryTest`). Faixas etárias existem **apenas** em agregados anônimos de métrica, para medir adverse impact.

| Módulo | Entidades principais | Campos-chave / regras de domínio |
|---|---|---|
| Carreira | `Perfil` | `nome`, `email`, `areaAtual`, `areaAlvo`, `pretensaoSalarial`, `telefone`, `cidade`, `resumo` (máx. 1500) — **sem data de nascimento**; ligado a competências e currículo |
| Carreira | `Experiencia`, `Formacao`, `Projeto`, `Idioma`, `Certificacao` | Currículo estruturado; upload PDF/DOCX/TXT é parseado (Apache Tika) e a seção de idade é descartada |
| Carreira | `Vaga` + `Competencia` (N-N) | Título, empresa, salário, área, prazo; exigências por competência |
| Carreira | `Inscricao` | Perfil + vaga/trilha, tipo (VAGA/TRILHA), lembrete de prazo agendado por job |
| Carreira | `Trilha` + `ModuloTrilha`, `Mentoria` | Trilhas por área com módulos e carga horária; encaixe de mentoria |
| Escudo | `MetricaFunil` | Etapa, `razaoAdverseImpact` (< 0,80 gera alerta) e faixa etária **anônima** (ATE_39 / 40_49 / 50_MAIS) |
| Escudo | `RegraIdade`, `SinalIdade` | 15 regras em JSON (regex tolerantes a acento) e sinais capturados na auditoria |
| Escudo | `SolicitacaoRevisao` | Protocolo `REV-` de exigência de revisão humana (LGPD art. 20) |

Schema consolidado em migração versionada **Flyway** (`V1__init.sql`, 17 tabelas); o profile `prod` (PostgreSQL) mantém `ddl-auto=update` apenas como safety net.

## 6. Stack Tecnológica e Padrões Transversais

- **Back-end:** Java 17 LTS, Spring Boot 4.1.1, Spring Data JPA + Hibernate, Flyway, Actuator, SpringDoc OpenAPI 3.
- **Bancos:** H2 em memória (dev/testes) · PostgreSQL 16 (prod, via Docker Compose).
- **Tratamento de erros:** RFC 7807 (`application/problem+json`) em todas as exceções de negócio e validação, com `title`, `detail` e `fieldErrors`.
- **Segurança:** Spring Security com HTTP Basic stateless; endpoints do painel RH exigem `ROLE_RH` (credenciais demo configuráveis via `RH_USER`/`RH_PASSWORD`).
- **Processamento de arquivos:** Apache Tika (PDF/DOCX) para extração de texto bruto de currículos antes do motor de regras.
- **Front-end:** React 19 + Vite + Tailwind CSS 4 (detalhe na seção 7).
- **Qualidade:** JUnit 5 + MockMvc + Mockito (API), Vitest + Testing Library (componentes), Playwright (E2E), JaCoCo (cobertura), oxlint (front), GitHub Actions (CI com 4 jobs: api, front, e2e e docker).

## 7. Front-end e Integração com o Back-end

O front-end em React 19 (`ageblind-front/`) traduz a jornada completa candidato × RH em 7 rotas, com dados **100% vindos da API** — nenhuma tela usa mock ou conteúdo estático:

| Rota | Tela | Integração principal |
|---|---|---|
| `/perfil` | Cadastro/edição de perfil, competências transferíveis, currículo estruturado (formulário + upload PDF/DOCX/TXT) | `POST/GET/PUT /perfis`, `POST .../competencias-transferiveis`, `GET/PUT .../curriculo`, `POST .../curriculo/arquivo` |
| `/vagas` e `/vagas/:id` | Exploração com filtros e paginação; detalhe com inscrição, lembrete e busca externa | `GET /vagas`, `GET /vagas/{id}`, `POST /inscricoes`, `POST /webhooks/lembretes`, `GET /vagas/busca-externa` |
| `/recomendadas` | Ranking só por competências, trilhas e mentorias | `GET /vagas/recomendadas`, `GET /trilhas/{area}`, `GET /mentorias` |
| `/escudo` | Blindagem (15 regras), auditoria e pedido de revisão LGPD | `POST /curriculo/age-blind`, `POST /curriculo/auditoria`, `POST /revisao/solicitacao` |
| `/rh/painel` | Auditoria de linguagem de vaga + funil com adverse impact (HTTP Basic) | `POST /vagas/auditoria-linguagem`, `GET /funil/metricas` |

**Organização do código:** componentização em `components/` (CardVaga, CurriculoEstruturado, Feedback, Layout…), uma página por rota em `pages/` e camada de serviços isolada (`services/api.js` com fetch + problem+json; `carreiraService.js`; `escudoService.js`). Todo erro da API é convertido em `ApiError` e exibido ao usuário com `title`, `detail` e erros por campo (RFC 7807). Operações de cadastro, consulta, atualização e exclusão (cancelamento de inscrição) estão operacionais nos fluxos principais.

## 8. Catálogo de Rotas da API (`/api/v1`)

A API expõe **22 endpoints em 19 paths** (17 do módulo Carreira + 5 do Escudo); o contrato completo e máquina-legível está no [`openapi.yaml`](openapi.yaml). Principais rotas:

| Módulo | Método e rota | Descrição |
|---|---|---|
| Carreira | `POST /api/v1/perfis` | Cadastra perfil profissional sem proxy etário (201) |
| Carreira | `POST /api/v1/perfis/{id}/competencias-transferiveis` | Mapeia experiência anterior para competências da área alvo |
| Carreira | `GET /api/v1/vagas/recomendadas?perfilId=` | Ranking de vagas estritamente por aderência de competências |
| Carreira | `PUT /api/v1/perfis/{id}/curriculo` | Substitui o currículo estruturado (sem idade) |
| Carreira | `DELETE /api/v1/inscricoes/{id}` | Cancela inscrição (204) |
| Escudo | `POST /api/v1/curriculo/age-blind` | Aplica as 15 regras de blindagem e retorna o conteúdo adaptado |
| Escudo | `POST /api/v1/curriculo/auditoria` | Pontuação anti-proxy de idade (0–100) e sinais de viés |
| Escudo | `POST /api/v1/vagas/auditoria-linguagem` | Termos discriminatórios + risco legal (CLT 373-A, Lei 9.029) |
| Escudo | `GET /api/v1/funil/metricas?vagaId=` | Avanço por faixa etária anônima e alerta de adverse impact |
| Escudo | `POST /api/v1/revisao/solicitacao` | Protocolo de revisão humana (LGPD art. 20) |

## 9. Validação, Conclusão e Próximos Passos

**Validação (executada em 31/10/2026):** suíte da API **93/93 testes verdes** (`mvnw clean verify`, inclui regressão anti-viés, contrato, LGPD e segurança); front **13/13** (Vitest + Testing Library) e **5/5 E2E** (Playwright contra o stack Docker de pé); CI no GitHub Actions roda API, lint+testes do front, E2E e build das imagens a cada push/PR. Demo com seed: 10 perfis, 20 vagas e 5 trilhas.

O projeto consolida-se como ferramenta de impacto social que une conformidade legal estrita (LGPD art. 20, Lei 9.029, CLT 373-A), zero coleta de idade e um ciclo completo: competências transferíveis → trilha → vaga → auditoria → revisão humana.

**Próximos passos (v1.1):** deploy em ambiente público (Vercel/Render + Neon/RDS); avaliação de usabilidade com usuários reais 40+ e times de RH; notificações push de lembrete além do e-mail/log; expansão do seed de trilhas e mentorias; painel RH com histórico de auditorias; e estudo de extração do módulo Escudo como serviço independente.

---

*Segunda Carreira — Relatório Técnico Detalhado · ReCode · outubro de 2026*
