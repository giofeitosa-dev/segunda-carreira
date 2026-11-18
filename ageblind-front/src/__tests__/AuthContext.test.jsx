import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { AuthContextProvider, useAuth } from '../context/AuthContext'

const CHAVE_TOKEN = 'ageblind.token'

let storage = {}

function mockLocalStorage() {
  const store = storage
  return {
    getItem: (key) => (key in store ? String(store[key]) : null),
    setItem: (key, value) => {
      store[key] = String(value)
    },
    removeItem: (key) => {
      delete store[key]
    },
    clear: () => {
      storage = {}
    },
  }
}

function loginResposta(body, status = 200, ok = status >= 200 && status < 300) {
  return {
    status,
    ok,
    headers: { get: () => 'application/json' },
    json: async () => body,
  }
}

beforeEach(() => {
  storage = {}
  vi.spyOn(globalThis, 'localStorage', 'get').mockReturnValue(mockLocalStorage())
  vi.restoreAllMocks()
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('AuthContext', () => {
  test('login persiste token em localStorage e expõe estado autenticado', async () => {
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(
        loginResposta({
          token: 'jwt-teste-123',
          tokenType: 'Bearer',
          expiresIn: 1800,
          usuario: { id: 7, email: 'cand@demo.com', papel: 'CANDIDATO' },
        }),
      )

    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthContextProvider,
    })

    await act(async () => {
      const res = await result.current.login('cand@demo.com', 'senha123')
      expect(res).toMatchObject({
        token: 'jwt-teste-123',
        usuario: { id: 7, email: 'cand@demo.com', papel: 'CANDIDATO' },
      })
    })

    expect(result.current.token).toBe('jwt-teste-123')
    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.usuario.email).toBe('cand@demo.com')
    expect(result.current.papel).toBe('CANDIDATO')
    expect(globalThis.localStorage.getItem(CHAVE_TOKEN)).toBe('jwt-teste-123')

    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/v1/auth/login')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body)).toEqual({ email: 'cand@demo.com', senha: 'senha123' })
  })

  test('logout remove token do localStorage e limpa estado', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      loginResposta({
        token: 'jwt-teste-456',
        tokenType: 'Bearer',
        expiresIn: 1800,
        usuario: { id: 3, email: 'rh@demo.com', papel: 'RH' },
      }),
    )

    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthContextProvider,
    })

    await act(async () => {
      await result.current.login('rh@demo.com', 'senha123')
    })
    expect(result.current.isAuthenticated).toBe(true)
    expect(globalThis.localStorage.getItem(CHAVE_TOKEN)).toBe('jwt-teste-456')

    await act(async () => {
      result.current.logout()
    })

    expect(result.current.token).toBeNull()
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.usuario).toBeNull()
    expect(globalThis.localStorage.getItem(CHAVE_TOKEN)).toBeNull()
  })

  test('login com credenciais invalidas retorna 401 e nao persiste token', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      status: 401,
      ok: false,
      headers: { get: () => 'application/problem+json' },
      json: async () => ({
        title: 'Nao autenticado',
        status: 401,
        detail: 'Token ausente, expirado ou invalido',
      }),
    })

    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthContextProvider,
    })

    await act(async () => {
      await expect(
        result.current.login('cand@demo.com', 'senha_errada'),
      ).rejects.toMatchObject({ status: 401 })
    })

    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.token).toBeNull()
    expect(globalThis.localStorage.getItem(CHAVE_TOKEN)).toBeNull()
  })

  test('estado inicial sem token nao e autenticado', () => {
    const { result } = renderHook(() => useAuth(), {
      wrapper: AuthContextProvider,
    })

    expect(result.current.token).toBeNull()
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.usuario).toBeNull()
  })
})
