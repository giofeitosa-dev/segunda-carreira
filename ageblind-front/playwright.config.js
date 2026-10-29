import { defineConfig } from '@playwright/test'

// Local: usa o Edge instalado (channel msedge, sem download de browser).
// CI: instala o Chromium via `npx playwright install --with-deps chromium`.
export default defineConfig({
  testDir: './e2e',
  timeout: 60_000,
  fullyParallel: true,
  // Edge paralelo + Docker Desktop nesta máquina gera abort/hang aleatório —
  // local roda serial (retries=1); CI sobe compose dedicado e usa 2 workers.
  workers: process.env.CI ? 2 : 1,
  retries: process.env.CI ? 2 : 1,
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:3000',
    viewport: { width: 1440, height: 900 },
    ...(process.env.CI ? {} : { channel: 'msedge' }),
  },
})
