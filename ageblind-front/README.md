# Reconecta — Front-end (Segunda Carreira + Escudo Anti-Etarismo)

> UI de demo para a API [`ageblind-api`](../ageblind-api) — "a API que contrata por
> competência, não por ano de nascimento". Público: profissionais 40+ em
> recolocação e RH que audita vagas.

**Stack:** React 19 · Vite · Tailwind CSS 4 · react-router-dom 7 · JavaScript (sem TS) · oxlint.

---

## Rodando a demo

```bash
# 1. API em :8080 (com seed de dados, incluindo mentorias)
cd ../ageblind-api/api
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed

# 2. Front em :5173 (proxy /api → :8080, ver vite.config.js)
cd ../../ageblind-front
npm install
npm run dev
```

| Script | O que faz |
|---|---|
| `npm run dev` | dev server com HMR em `http://localhost:5173` |
| `npm run build` | build de produção em `dist/` |
| `npm run lint` | oxlint (meta: 0 warnings) |
| `npm run preview` | serve o build de produção |

## Rotas

| Rota | Tela | Endpoints usados |
|---|---|---|
| `/` | Home (jornada candidato × RH) | — |
| `/perfil` | Cadastro/edição de perfil + competências transferíveis | `POST/GET/PUT /perfis`, `POST /perfis/{id}/competencias-transferiveis` |
| `/vagas` | Exploração com filtros (área/modelo) e paginação | `GET /vagas` |
| `/vagas/:id` | Detalhe + inscrição + lembrete + atalho para o escudo | `GET /vagas/{id}`, `POST /inscricoes`, `POST /webhooks/lembretes` |
| `/recomendadas` | Abas: recomendadas (ranking só por competências), trilhas, mentorias | `GET /vagas/recomendadas`, `GET /trilhas/{area}`, `GET /mentorias` |
| `/escudo` | Blindar currículo (15 regras), auditar, pedido de revisão LGPD art. 20 | `POST /curriculo/age-blind`, `POST /curriculo/auditoria`, `POST /revisao/solicitacao` |
| `/rh/painel` | Auditoria de linguagem de vaga + funil com alerta de adverse impact | `POST /vagas/auditoria-linguagem`, `GET /funil/metricas` |

## Estrutura

```
src/
├── components/    CardVaga, Carregando, EstadoVazio, Feedback, Layout
├── pages/         uma página por rota (acima)
├── services/      api.js (fetch + problem+json), carreiraService.js, escudoService.js
├── App.jsx        rotas
└── index.css      tema Tailwind (@theme): primaria, sucesso, perigo, texto…
```

## Regras do projeto (respeitadas no código)

1. **Nenhum campo de idade / data de nascimento** — nem formulário, nem filtro, nem entidade.
2. "Pontuação anti-proxy de idade (**não é ATS**)" — o JSON `scoreAts` mantém o nome (zero breaking), mas a UI nunca chama de ATS.
3. Auditoria de vaga exibe termos + base legal (Lei 9.029) e **não promete conformidade**.
4. Todo dado vem da API — zero mock/estático.
5. Erros vêm do `application/problem+json` (RFC 7807: `title`, `detail`, `fieldErrors`), nunca mensagem genérica.
6. Acessibilidade 40+: contraste AA, alvos de clique ≥ 48px (`min-h-12`), labels sempre visíveis.
