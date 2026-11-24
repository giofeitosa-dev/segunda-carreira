const BASE = '/api/v1'
const CHAVE_TOKEN = 'ageblind.token'

export class ApiError extends Error {
  constructor(status, title, detail, fieldErrors) {
    super(detail || title || 'Erro na comunicacao com a API')
    this.name = 'ApiError'
    this.status = status
    this.title = title
    this.detail = detail
    this.fieldErrors = fieldErrors || []
  }
}

let onUnauthorized = () => {
  localStorage.removeItem(CHAVE_TOKEN)
  window.location.replace('/login')
}

export function setOnUnauthorized(handler) {
  onUnauthorized = handler
}

async function request(path, options = {}) {
  const { headers: opcoesHeaders, ...resto } = options
  const headers = { ...opcoesHeaders }
  const token = localStorage.getItem(CHAVE_TOKEN)
  if (token) headers['Authorization'] = `Bearer ${token}`
  if (!(resto.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }
  const res = await fetch(BASE + path, { ...resto, headers })

  if (res.status === 204) return null

  const contentType = res.headers.get('content-type') || ''
  const body = contentType.includes('json')
    ? await res.json().catch(() => null)
    : null

  if (res.status === 401) {
    onUnauthorized()
  }

  if (!res.ok) {
    throw new ApiError(
      res.status,
      body?.title,
      body?.detail,
      body?.fieldErrors,
    )
  }
  return body
}

export const api = {
  get: (path) => request(path),
  post: (path, data) =>
    request(path, { method: 'POST', body: JSON.stringify(data) }),
  put: (path, data) =>
    request(path, { method: 'PUT', body: JSON.stringify(data) }),
  del: (path) => request(path, { method: 'DELETE' }),
  postForm: (path, formData) => request(path, { method: 'POST', body: formData }),
  setOnUnauthorized: (handler) => {
    onUnauthorized = handler
  },
}
