import { api } from './api'

// Currículo sem proxies de idade (15 regras)
export const blindarCurriculo = (curriculo, vagaAlvo) =>
  api.post('/curriculo/age-blind', { curriculo, vagaAlvo })

// Pontuação anti-proxy de idade + sinais (NAO e ATS)
export const auditarCurriculo = (curriculo, vagaAlvo) =>
  api.post('/curriculo/auditoria', { curriculo, vagaAlvo })

// Termos discriminatórios + base legal na descrição da vaga
export const auditarLinguagemVaga = (titulo, texto) =>
  api.post('/vagas/auditoria-linguagem', { titulo, texto })

// Funil por faixa + adverse impact
export const funilMetricas = (vagaId) =>
  api.get(`/funil/metricas?vagaId=${vagaId}`)

// Revisão humana (LGPD art. 20) → protocolo REV-
export const solicitarRevisao = (perfilId, motivo) =>
  api.post('/revisao/solicitacao', { perfilId, motivo })
