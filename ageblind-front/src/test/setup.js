import '@testing-library/jest-dom/vitest'
import { randomUUID } from 'node:crypto'

// jsdom nem sempre expoe crypto.randomUUID (usado nas chaves do curriculo)
if (typeof globalThis.crypto?.randomUUID !== 'function') {
  globalThis.crypto ??= {}
  globalThis.crypto.randomUUID = randomUUID
}
