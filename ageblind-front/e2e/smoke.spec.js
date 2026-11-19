import { expect, test } from '@playwright/test'

// Smoke E2E contra o stack docker (compose: db + api + front em :3000).
// ADZUNA sem chave = 503 esperado — nenhum teste depende da busca externa.
// Autenticação: login obtido via API (request do Playwright) e injetado em
// localStorage (chave `ageblind.token`, definida em AuthContext) — zero
// httpCredentials (R11 / US5.4).

const API_BASE = 'http://localhost:8080'
const TOKEN_KEY = 'ageblind.token'

async function loginViaApi({ email, senha }) {
  const requestContext = await test.request()
  const res = await requestContext.post(`${API_BASE}/api/v1/auth/login`, {
    data: { email, senha },
  })
  expect(res.status()).toBe(200)
  const body = await res.json()
  return body.token
}

function capturarErros(page) {
  const erros = []
  page.on('console', (m) => m.type() === 'error' && erros.push(m.text()))
  page.on('response', (r) => r.status() >= 400 && erros.push(`HTTP ${r.status()} ${r.url()}`))
  return erros
}

test('home carrega com a marca e sem erros', async ({ page }) => {
  const erros = capturarErros(page)
  await page.goto('/')
  await expect(page).toHaveTitle(/Segunda Carreira/)
  await expect(page.locator('h1')).toContainText('competência')
  expect(erros).toEqual([])
})

test('vagas lista cards vindos da API (proxy nginx)', async ({ page }) => {
  const erros = capturarErros(page)
  await page.goto('/vagas')
  await expect(page.locator('article').first()).toBeVisible({ timeout: 20_000 })
  expect(await page.locator('article').count()).toBeGreaterThan(0)
  expect(erros).toEqual([])
})

test('com perfil ativo, /perfil mostra Meu currículo e as 5 seções', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('segundaCarreira.perfilAtiva', '1'))
  await page.goto('/perfil')
  await expect(page.getByRole('heading', { name: 'Meu currículo' })).toBeVisible({
    timeout: 20_000,
  })
  for (const nome of ['Experiências', 'Formações', 'Idiomas', 'Certificações', 'Projetos']) {
    await expect(page.getByRole('heading', { name: nome })).toBeVisible()
  }
})

test('escudo carrega com as abas (blindar/auditar)', async ({ page }) => {
  await page.goto('/escudo')
  await expect(page.getByRole('tablist')).toBeVisible({ timeout: 20_000 })
  await expect(page.getByRole('tab').first()).toBeVisible()
})

async function abrirFunil(page) {
  await page.getByRole('tab', { name: 'Funil de contratação' }).click()
  const select = page.locator('#vaga-funil')
  await expect(select.locator('option:not([value=""])').first()).toBeAttached({
    timeout: 20_000,
  })
  await select.selectOption({ index: 1 })
}

// Cenário: painel RH com token inválido → 401 (sem httpCredentials)
test('painel RH: token invalido recebe 401 e nao mostra funil', async ({ page }) => {
  const erros = capturarErros(page)
  await page.addInitScript((key) => {
    localStorage.setItem(key, 'token-invalido-123')
  }, TOKEN_KEY)
  await page.goto('/rh/painel')
  await expect(page.getByRole('heading', { name: 'Painel RH' })).toBeVisible({
    timeout: 20_000,
  })
  await abrirFunil(page)
  await expect(page.getByRole('alert')).toContainText(
    /autenticado|credenciais|401|Erro na operação/i,
    { timeout: 15_000 },
  )
  expect(erros.some((e) => e.includes('401'))).toBe(true)
})

// Cenário: painel RH com token valido (obtido via API) mostra métricas
test('painel RH: token valido obtido via API exibe funil de contratação', async ({ page }) => {
  const token = await loginViaApi({
    email: 'rh@demo.com',
    senha: process.env.RH_PASSWORD || 'rh-demo',
  })
  const erros = capturarErros(page)
  await page.addInitScript((key, valor) => {
    localStorage.setItem(key, valor)
  }, TOKEN_KEY, token)
  await page.goto('/rh/painel')
  await expect(page.getByRole('heading', { name: 'Painel RH' })).toBeVisible({
    timeout: 20_000,
  })
  await abrirFunil(page)
  await expect(page.getByRole('columnheader', { name: 'Etapa' })).toBeVisible({
    timeout: 20_000,
  })
  await expect(page.getByRole('alert')).toHaveCount(0)
  expect(erros).toEqual([])
})

// Cenário: login pela UI preenche formulário e acessa o painel (US3.1 ponta a ponta)
test('login pela UI: preenche formulario e acessa o painel RH', async ({ page }) => {
  const erros = capturarErros(page)
  await page.goto('/login')
  await page.getByLabel('Email').fill('rh@demo.com')
  await page.getByLabel('Senha').fill(process.env.RH_PASSWORD || 'rh-demo')
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByRole('heading', { name: 'Painel RH' })).toBeVisible({
    timeout: 20_000,
  })
  expect(erros).toEqual([])
})
