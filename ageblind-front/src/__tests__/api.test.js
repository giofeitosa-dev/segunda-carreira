import { afterEach, expect, test, vi } from 'vitest'
import { api, ApiError } from '../services/api'

afterEach(() => {
  vi.restoreAllMocks()
})

function resposta({ status, ok, contentType, body }) {
  return {
    status,
    ok,
    headers: { get: () => contentType },
    json: async () => body,
  }
}

test('get monta URL /api/v1 e lanca ApiError com problem+json', async () => {
  const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
    resposta({
      status: 400,
      ok: false,
      contentType: 'application/problem+json',
      body: {
        title: 'Dados inválidos',
        detail: 'Verifique os campos',
        fieldErrors: [{ field: 'nome', message: 'é obrigatório' }],
      },
    }),
  )

  await expect(api.get('/perfis/1')).rejects.toMatchObject({
    status: 400,
    title: 'Dados inválidos',
    fieldErrors: [{ field: 'nome', message: 'é obrigatório' }],
  })

  const [url, init] = fetchMock.mock.calls[0]
  expect(url).toBe('/api/v1/perfis/1')
  expect(init.headers['Content-Type']).toBe('application/json')
})

test('postForm nao seta Content-Type em FormData (browser gera o boundary)', async () => {
  const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
    resposta({ status: 200, ok: true, contentType: 'application/json', body: { ok: true } }),
  )

  const fd = new FormData()
  fd.append('arquivo', new Blob(['texto']), 'curriculo.txt')
  await api.postForm('/perfis/1/curriculo/arquivo', fd)

  const [, init] = fetchMock.mock.calls[0]
  expect(init.method).toBe('POST')
  expect(init.headers['Content-Type']).toBeUndefined()
})

test('204 No Content retorna null sem tentar ler JSON', async () => {
  vi.spyOn(globalThis, 'fetch').mockResolvedValue(
    resposta({ status: 204, ok: true, contentType: '', body: null }),
  )
  await expect(api.del('/inscricoes/1')).resolves.toBeNull()
})

test('erro sem problem+json vira ApiError com status e mensagem generica', async () => {
  vi.spyOn(globalThis, 'fetch').mockResolvedValue(
    resposta({ status: 502, ok: false, contentType: 'text/html', body: null }),
  )
  const erro = await api.get('/vagas/busca-externa?termo=java').catch((e) => e)
  expect(erro).toBeInstanceOf(ApiError)
  expect(erro.status).toBe(502)
})
