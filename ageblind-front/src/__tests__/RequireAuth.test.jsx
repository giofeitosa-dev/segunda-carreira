import { afterEach, beforeEach, expect, test, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import RequireAuth from '../components/RequireAuth'

const CHAVE_TOKEN = 'ageblind.token'

let storage = {}
let replaceCalls = []

function criarLocalStorageMock() {
  return {
    getItem: (key) => (key in storage ? String(storage[key]) : null),
    setItem: (key, value) => {
      storage[key] = String(value)
    },
    removeItem: (key) => {
      delete storage[key]
    },
    clear: () => {
      Object.keys(storage).forEach((k) => delete storage[k])
    },
  }
}

function mockLocation() {
  replaceCalls = []
  Object.defineProperty(window, 'location', {
    configurable: true,
    writable: true,
    value: {
      replace: (url) => {
        replaceCalls.push(url)
      },
      href: 'http://localhost/',
      assign: () => {},
      reload: () => {},
    },
  })
}

beforeEach(() => {
  storage = {}
  mockLocation()
  vi.stubGlobal('localStorage', criarLocalStorageMock())
  vi.restoreAllMocks()
})

afterEach(() => {
  vi.restoreAllMocks()
  vi.unstubAllGlobals()
})

function renderRequireAuth({ token } = {}) {
  storage = {}
  if (token) storage[CHAVE_TOKEN] = token

  return render(
    <MemoryRouter initialEntries={['/rh/painel']}>
      <RequireAuth>
        <div data-testid="painel">Painel RH</div>
      </RequireAuth>
    </MemoryRouter>,
  )
}

test('sem token nao renderiza children e redireciona para /login', () => {
  renderRequireAuth({})
  expect(screen.queryByTestId('painel')).not.toBeInTheDocument()
  expect(replaceCalls).toContain('/login')
})

test('com token renderiza os children', () => {
  renderRequireAuth({ token: 'jwt-valido' })
  expect(screen.getByTestId('painel')).toBeInTheDocument()
  expect(replaceCalls).not.toContain('/login')
})
