import { expect, test } from '@playwright/test'

// Smoke E2E contra o stack docker (compose: db + api + front em :3000).
// ADZUNA sem chave = 503 esperado — nenhum teste depende da busca externa.

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

// Nota: sem nenhuma credencial o Chromium segura o fetch no diálogo HTTP Basic
// (pendurado) — por isso o "negativo" usa usuário/senha errados, como no
// RhProtegidoTest do backend (rh/errada → 401 problem+json "Não autenticado").
test('painel RH: funil com credenciais erradas recebe 401; rh/rh-demo vê as métricas', async ({ browser }) => {
  const semAuth = await browser.newContext({
    httpCredentials: { username: 'rh', password: 'errada' },
  })
  const pageSem = await semAuth.newPage()
  await pageSem.goto('/rh/painel')
  await expect(pageSem.getByRole('heading', { name: 'Painel RH' })).toBeVisible({
    timeout: 20_000,
  })
  await abrirFunil(pageSem)
  await expect(pageSem.getByRole('alert')).toContainText(
    /autenticado|credenciais|401|Erro na operação/i,
    { timeout: 15_000 },
  )
  await semAuth.close()

  const comAuth = await browser.newContext({
    httpCredentials: { username: 'rh', password: 'rh-demo' },
  })
  const pageCom = await comAuth.newPage()
  await pageCom.goto('/rh/painel')
  await expect(pageCom.getByRole('heading', { name: 'Painel RH' })).toBeVisible({
    timeout: 20_000,
  })
  await abrirFunil(pageCom)
  await expect(pageCom.getByRole('columnheader', { name: 'Etapa' })).toBeVisible({
    timeout: 20_000,
  })
  await expect(pageCom.getByRole('alert')).toHaveCount(0)
  await comAuth.close()
})
