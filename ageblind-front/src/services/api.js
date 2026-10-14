const BASE = '/api/v1'

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

async function request(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  })

  if (res.status === 204) return null

  const contentType = res.headers.get('content-type') || ''
  const body = contentType.includes('json')
    ? await res.json().catch(() => null)
    : null

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
}
