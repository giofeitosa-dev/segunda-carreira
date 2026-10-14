import { api } from './api'

const CHAVE_PERFIL = 'reconecta.perfilAtiva'

export function getPerfilAtivaId() {
  const id = localStorage.getItem(CHAVE_PERFIL)
  return id ? Number(id) : null
}

export function setPerfilAtivaId(id) {
  localStorage.setItem(CHAVE_PERFIL, String(id))
}

export function limparPerfilAtiva() {
  localStorage.removeItem(CHAVE_PERFIL)
}

// --- Perfis ---
export const criarPerfil = (dados) => api.post('/perfis', dados)
export const obterPerfil = (id) => api.get(`/perfis/${id}`)
export const atualizarPerfil = (id, dados) => api.put(`/perfis/${id}`, dados)
export const competenciasTransferiveis = (id) =>
  api.post(`/perfis/${id}/competencias-transferiveis`)

// --- Vagas ---
export function listarVagas({ area, modelo, page = 0, size = 12 } = {}) {
  const params = new URLSearchParams()
  if (area) params.set('area', area)
  if (modelo) params.set('modelo', modelo)
  params.set('page', String(page))
  params.set('size', String(size))
  const query = params.toString()
  return api.get(`/vagas${query ? `?${query}` : ''}`)
}

export const obterVaga = (id) => api.get(`/vagas/${id}`)
export const vagasRecomendadas = (perfilId) =>
  api.get(`/vagas/recomendadas?perfilId=${perfilId}`)

// --- Trilhas ---
export const listarTrilhas = (area) =>
  api.get(`/trilhas/${encodeURIComponent(area)}`)

// --- Inscricoes ---
export const criarInscricao = (dados) => api.post('/inscricoes', dados)
export const listarInscricoes = (perfilId) =>
  api.get(`/inscricoes?perfilId=${perfilId}`)
export const cancelarInscricao = (id) => api.del(`/inscricoes/${id}`)
export const agendarLembrete = (dados) => api.post('/webhooks/lembretes', dados)

// --- Mentorias ---
export const listarMentorias = (perfilId) =>
  api.get(`/mentorias?perfilId=${perfilId}`)
