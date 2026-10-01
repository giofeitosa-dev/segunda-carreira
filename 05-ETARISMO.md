# 05 — Etarismo e Triagem Algorítmica

> Documento de apoio técnico do módulo **Segunda Carreira** (`03-IDEACAO\segunda-carreira.md`).
> Objetivo: explicar **como os robôs de triagem descobrem a idade**, o que a lei brasileira garante, **como detectar a discriminação** e **como construir um currículo e um perfil "cegos à idade"**.

**Fontes principais:** pesquisas próprias em `Publicações\empregabilidade\` (Brasil e Global) + referências externas ao final.

---

## 1. Como os "robôs" descobrem a idade — mesmo sem pedir a data

O sistema **não precisa do campo "idade"**. Ele usa **proxies** (substitutos aparentemente neutros). Este é o ponto central: **remover a data de nascimento não resolve** — é o que a doutrina chama de *discriminação indireta*: critério neutro que produz desvantagem injustificada.

### Tabela de proxies

| Proxy | O que o robô infere | Onde aparece | Força |
|---|---|---|---|
| **Ano de conclusão da graduação** | idade aproximada (±2 anos) | formação | ★★★★★ |
| **Tempo total de experiência** | "25 anos de carreira" = 50+ | resumo/linha do tempo | ★★★★★ |
| **Primeiro emprego / trajetória longa** | tempo de mercado | experiência mais antiga | ★★★★ |
| **Lacunas no histórico** | desatualização / cuidado de família | datas de vínculos | ★★★★ |
| **Tecnologias antigas** (Delphi, COBOL, XP) | geração passada | skills | ★★★ |
| **Tempo de casa muito longo** | pouca reciclagem | estabilidade | ★★★ |
| **"Nascidos antes de 1976"** | corte explícito por geração | configuração do ATS | ★★★★★ |
| **Foto, nome, endereço** | idade, classe, gênero | cabeçalho | ★★★ |
| **Idade no LinkedIn / redes** | idade direta | perfil público | ★★★★ |

### Evidências de uso real dos proxies

- **Gupy:** ex-funcionários relataram ao *Intercept* (2022) que a pontuação sobe "quanto mais novo o candidato" e "quanto mais recente a formatura".
- **Brasil:** algoritmos que excluem **nascidos antes de 1976** documentados em processos seletivos (pesquisa própria, arquivo Brasil, seção 6).
- **ATS padrão:** rejeitam automaticamente lacunas >6 meses e formação antiga (pesquisa global, seção 6).
- **Generation.org (2024):** com IA no processo, **90%** dos gestores considerariam candidatos <35 anos vs. **32–33%** para 60+.

### Casos reais de detecção

| Caso | O que aconteceu | Desfecho |
|---|---|---|
| **iTutorGroup (EUA, 2023)** | Software rejeitava automaticamente 55+/60+. A candidata **reenviou o MESMO currículo com data de nascimento mais nova** e foi chamada | Acordo de **US$ 365 mil** |
| **Mobley v. Workday** | Ação coletiva de 40+ admitida; lacunas no histórico usadas como indicador indireto; Workday processou **mais de 1 bilhão** de candidaturas | Em curso (2026) |
| **CTO brasileiro (2026)** | 30+ anos de experiência, 22 países, mentor Endeavor: 9 meses, centenas de candidaturas, **0 entrevistas**; contratado remotamente no Canadá — "país que não usou meu CPF para inferir idade" | Contratado fora |
| **Grace Ann Hansen (EUA)** | 35 anos em TI, **500 candidaturas = 0 entrevistas** | Relato público |
| **Luchetti (SC, 2022)** | E-mail: "Cancela, já passou da idade kkk" | Condenação: **R$ 5 mil** |

---

## 2. A discriminação em números

### Brasil (pesquisa própria: `Pesquisa_Empregabilidade_Brasil_Regioes.md`)

| Indicador | Valor | Fonte |
|---|---|---|
| Profissionais brasileiros que já sofreram etarismo | **41%** (global: 36%) | Michael Page Talent Trends 2025 |
| 60+ que já sofreram discriminação no trabalho | **86%** | Instituto Locomotiva/Oldiversity 2024 |
| Empresas que se consideram etaristas | **78%** | Ernst & Young/Maturi |
| Usam IA em recrutamento | **73%** | — |
| **Auditam** a IA para discriminação | apenas **12%** | — |
| Vagas fechadas para 50+ em 2024 | **160 mil** | CAGED |
| Ações judiciais de etarismo (SP) | **3 (2018) → 403 (2023)** = +13.000% | — |
| Indenizações | **R$ 4,47 mi → R$ 174,64 mi** (2018→2023) | — |
| Acreditam que 50+ têm dificuldade de adaptação | **44%** | Stato/McKinsey |
| Percebem menos oportunidades de promoção para mais velhos | **70%** | — |

**Jurisprência brasileira:** Engenheira 59 anos/TST 2025 (**R$ 440 mil**), Bradesco/TST 2025 (R$ 100 mil ao FAT), Dataprev/TRT-2 2025 (reintegração + R$ 15 mil), Usina/TRT-15 2025 (R$ 20 mil).

**Interseccionalidade:** mulheres negras — desemprego **10,1%** vs. homens não negros 4,6% (2º trim 2024); jovens mulheres negras 18-24: **16,5%**.

### Global (pesquisa própria: `Pesquisa_Empregabilidade_Global_Por_Faixa_Etaria.md`)

| Estudo | Achado |
|---|---|
| **Dinamarca 2024** (5.017 empregadores, 20.068 CVs fictícios, candidatos 45–75) | **A idade supera raça, gênero e religião** como viés de contratação |
| **Espanha 2024** (ISEAK) | 35 anos: **7,8%** de retorno · 49 anos: **3,7%** → **gap de 50%** (12,8 vs 27 currículos por retorno) |
| **Áustria** (IZA) | Remover limite de idade nos anúncios → idade média dos contratados 23→34 anos, **+18 p.p.** de contratação 35+ |
| **Generation.org 2024** | **89%** dos empregadores reconhecem que 45+ performam igual ou melhor — mesmo assim os rejeitam |
| **AARP/EUA 2025** | **64%** dos 50+ viram/sofreram etarismo; formas sutis: presumir baixa competência tecnológica **33%**, resistência à mudança **24%** |
| **EEOC** | Reclamações por idade: 11.500 (2022) → 16.223 (2024) |
| **Austrália 2025** | 1 em cada 4 RH classifica 51–55 anos como "idosos" (era 10% há 2 anos) |
| **ILO WESO 2025** | Em **62% dos países**, o desemprego de longa duração é maior para 55+ que para 25–54 |

**Tendência:** a discriminação etária começa aos **35–40 anos** em setores como tecnologia.

---

## 3. Marco legal brasileiro

| Norma | O que garante |
|---|---|
| **CF, art. 7º, XXX** | Veda critério de admissão por **idade** |
| **CF, art. 3º, V** | Veda discriminação (igualdade) |
| **Lei 9.029/1995** | Proíbe práticas discriminatórias (inclusive idade) na admissão — **ônus da prova cabe ao empregador**; sanção penal |
| **Estatuto do Idoso, art. 27** | Veda limite máximo de idade — pena de **6 meses a 1 ano de reclusão** |
| **CLT, art. 373-A** | Veda restrição de idade em anúncios de vaga |
| **Lei 14.457/2022** | Incentiva contratação de profissionais 40+ |
| **Lei 14.611/2023** | Igualdade salarial e critérios remuneratórios transparentes/auditáveis |
| **LGPD, art. 20** | Direito de exigir **revisão humana** de decisão automatizada que defina perfil profissional + informação sobre critérios |
| **PL 2.338/2023** | Regulamentação de IA no Brasil (em tramitação) |
| **Convenção 111 (OIT)** | Distinção por **requisito inerente ao cargo** não é discriminação — mas o fardo de provar a correlação é da empresa |

**Pontos-chave:**
1. **Discriminação indireta é discriminação:** excluir por ano de formatura ou tempo de experiência, sem justificativa funcional, é ilegal — mesmo sem usar a palavra "idade".
2. **Dano moral "in re ipsa"** é presumido na jurisprudência recente.
3. **O candidato tem poder:** pode exigir por escrito os critérios da decisão automatizada (LGPD art. 20) e preservar provas (prints, e-mails, anúncio da vaga).

---

## 4. Como IDENTIFICAR a discriminação

### 4.1 Teste do currículo-gêmeo *(poder feito pelo próprio candidato)*
Envie **o mesmo currículo com duas datas diferentes** (ano de graduação ou faixa etária). Se a resposta mudar, há filtro etário comprovado — exatamente o que revelou o caso iTutorGroup.
- **Na API:** endpoint `/v1/testes/gemeo` pode comparar respostas de dois envios (versão age-blind vs. versão completa).

### 4.2 Auditoria estatística de funil *(poder feito pela empresa)*
Calcule a taxa de avanço por faixa etária em cada etapa. Sinal de viés:
- Diferença grande e consistente de aprovação entre grupos com qualificações equivalentes.
- Regra prática internacional: se a razão entre grupos cai abaixo de **80%** (*adverse impact*), investigar.
- **Exemplo real:** Espanha — 7,8% vs 3,7% de retorno = razão de 47%.

### 4.3 Revisão humana (LGPD art. 20)
O candidato solicita por escrito: (a) revisão por pessoa humana, (b) informação sobre os critérios usados, (c) dados tratados. A empresa **não pode recusar** nem retaliar.

### 4.4 Auditoria da descrição da vaga
Termos que são prova documental: *"perfil jovem"*, *"energia jovem"*, *"nativo digital"*, *"recém-formado"*, *"até 35 anos"*, *"X anos de experiência no máximo"*, *"dynamism"*, *"fit cultural"* sem definição.

### 4.5 Relatório de explicabilidade
Exigir do fornecedor do ATS: quais critérios pontuam, quais variáveis sensíveis existem, como o modelo foi testado, taxa de aprovação por grupo.

**Indicadores mínimos para a empresa monitorar:** % de candidatos 40+/50+ no funil · taxa de contratação por faixa · tempo médio de avanço por grupo · nº de revisões humanas pedidas (LGPD).

---

## 5. Técnica age-blind — o currículo que não revela a idade

Baseada no exemplo real `Giovani_Feitosa_CV_ageblind.md` (15 regras aplicadas) + literatura ("botox no currículo").

### 5.1 O que REMOVER (sinais temporais diretos)

| # | Regra | Motivo |
|---|---|---|
| 1 | Sem data de nascimento, sem idade | óbvio |
| 2 | Sem foto (em envio automatizado) | inferência visual |
| 3 | **Sem ano de conclusão da graduação** | vetor mais forte de inferência |
| 4 | Sem "15+ anos de experiência" / total de anos | revela a década de início |
| 5 | Sem histórico cronológico completo — só experiência **recente** (últimos 10–15 anos) | impede contagem |
| 6 | Sem datas em certificações e idiomas | evita marcos antigos |
| 7 | Sem menções a aposentadoria, previdência, pretensão fixa | ativa estereótipo |

### 5.2 O que OCULTAR/NEUTRALIZAR (sinais indiretos)

| # | Regra | Motivo |
|---|---|---|
| 8 | Datas restantes devem ser **relativas e abertas** ("2014 – Presente" em vez de "1998–2014") | reduz precisão da inferência |
| 9 | Usar **ano recente estrategicamente** (ex.: "conclusão 2024") como sinal de **atualização** | contrabalança o viés de obsolescência |
| 10 | **Reordenar a formação:** curso mais recente **antes** dos diplomas acadêmicos | inverte a leitura cronológica |

### 5.3 O que REFORMULAR (linguagem)

| # | Regra | Motivo |
|---|---|---|
| 11 | Sem adjetivos geracionais: "veterano", "sênior", "maduro", "jovem", "experiente" | identidade ≠ geração |
| 12 | **Experiência prévia traduzida em competências transferíveis** — o passado entra como *habilidade*, não como *linha do tempo* | é o coração do módulo Segunda Carreira |
| 13 | **Números do passado viram métricas de entrega**: "45+ testes automatizados", "equipe de 20 pessoas" | tempo → valor |
| 14 | **Foco em recência tecnológica** (stack atual, CI/CD) | ataca o estereótipo "não domina tecnologia" (33% do viés sutil da AARP) |
| 15 | Idiomas por nível, sem ano | remove marco temporal |

### 5.4 Riscos da técnica (efeito boomerang)

- **Histórico vazio demais** pode parecer ocultação → manter 2–3 experiências relevantes sem datas antigas.
- **"2014 – Presente" residual** ainda permite estimar tempo de carreira.
- Títulos de cargo público ("Chefe de Divisão") podem ativar estereótipo de burocrata → traduzir para competências.
- **Nunca mentir:** ocultar ≠ falsificar. Datas omitidas são permitidas; informações falsas desclassificam.

### 5.5 Checklist "perfil interessante para o robô" (o outro lado da moeda)

O robô **pontua por palavras-chave e formato**. Para passar na triagem sem revelar idade:

- [ ] **Espelhar a linguagem da vaga** — 89% dos empregadores admitem rejeitar qualificados que não usam as mesmas palavras da descrição (Harvard).
- [ ] **Formato ATS-parseable:** colunas simples, títulos padrão, sem ícones/tabelas/imagens, PDF com texto selecionável.
- [ ] **Competências em keywords normalizadas** (termos do mercado + CBO), não só narrativa.
- [ ] **Quantificação de resultados** (%, R$, prazos) — fáceis de pontuar.
- [ ] **Resumo profissional nas 3 primeiras linhas** — é o que o score lê primeiro.
- [ ] **Certificações recentes** — sinal de reciclagem contínua.
- [ ] **Preencher pausas** com curso, freelancer ou voluntariado (evita o vermelho da "lacuna").
- [ ] **LinkedIn consistente** com o currículo (mesmas keywords), sem data de formatura visível.

---

## 6. Como isso vira funcionalidade na API

Módulo 2 da **Segunda Carreira** — detalhes completos em `03-IDEACAO\segunda-carreira.md`:

| Endpoint | Função | Base legal/técnica |
|---|---|---|
| `POST /v1/curriculo/age-blind` | Gera versão oculta de proxies de idade (seções 5.1–5.3) | técnica |
| `POST /v1/curriculo/auditoria` | Score ATS + lista de sinais que revelam idade (seção 1) | técnica |
| `POST /v1/vagas/auditoria-linguagem` | Termos discriminatórios + risco legal na descrição | CLT 373-A, Lei 9.029 |
| `GET /v1/funil/metricas` | Taxa de avanço por faixa etária + alerta de adverse impact | auditoria (seção 4.2) |
| `POST /v1/revisao/solicitacao` | Modelo de pedido de revisão humana para o candidato | **LGPD art. 20** |

**Princípio de produto:** a API protege **os dois lados** — o candidato (escudo age-blind) e a empresa (auditoria que evita passivo trabalhista: R$ 174 mi em indenizações em 2023).

---

## 7. Referências

**Pesquisas próprias** (em `C:\Users\giova\OneDrive\Área de Trabalho\ProjetosDEV\Publicações\empregabilidade\`):
- `Pesquisa_Empregabilidade_Brasil_Regioes.md` — 427 linhas, 26 fontes, comp. 18/09/2026
- `Pesquisa_Empregabilidade_Global_Por_Faixa_Etaria.md` — 410 linhas, 30 fontes, comp. 18/09/2026
- `Giovani_Feitosa_CV_ageblind.md` — exemplo real da técnica (15 regras)
- `flipchart-etarismo.mmd` / `flipchart etarismo.jpg` — narrativa visual para apresentação
- Posts LinkedIn (4 versões) — versões de divulgação dos dados

**Externas:**
- Intercept (2022) — como plataformas de IA podem discriminar no RH
- Ámbito Jurídico (2026) — IA no recrutamento pode gerar discriminação?
- BBC/G1 (2026) — "Tive que fazer um botox no meu currículo"
- RINA Advogados (2026) — IA no processo seletivo: direitos do candidato
- SpaceMoney (2026) — profissionais escondem idade para fugir de algoritmos
- Harvard Business School / EEOC / ILO WESO 2025 / OECD Employment Outlook 2025 / AARP / Eurostat

> ⚠️ **Alerta de dado:** no arquivo global, linha ~277, há trecho com caracteres corrompidos — não usar esse fragmento sem revisão.
