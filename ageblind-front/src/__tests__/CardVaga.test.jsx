import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { expect, test } from 'vitest'
import CardVaga from '../components/CardVaga'

const vaga = {
  id: 7,
  titulo: 'Analista de Logistica',
  empresa: 'TransLivre',
  area: 'Logistica',
  descricao: 'PCP e controle de estoque',
  salario: 4800,
  modelo: 'HIBRIDO',
  prazoInscricao: '2026-10-20T18:00:00',
  requisitos: ['Power BI', 'PCP'],
}

function renderizar(ui) {
  return render(<MemoryRouter>{ui}</MemoryRouter>)
}

test('mostra titulo, empresa, salario em BRL, prazo pt-BR e link do detalhe', () => {
  renderizar(<CardVaga vaga={vaga} />)
  expect(screen.getByText('Analista de Logistica')).toBeInTheDocument()
  expect(screen.getByText('TransLivre · Logistica')).toBeInTheDocument()
  expect(screen.getByText('R$ 4.800,00')).toBeInTheDocument()
  expect(screen.getByText('20/10/2026')).toBeInTheDocument()
  expect(screen.getByText('Power BI')).toBeInTheDocument()
  expect(screen.getByRole('link').getAttribute('href')).toBe('/vagas/7')
})

test('justificativa mostra aderencia, atendidas e faltantes', () => {
  renderizar(
    <CardVaga
      vaga={vaga}
      justificativa={{
        score: 0.47,
        competenciasAtendidas: ['Logistica'],
        competenciasFaltantes: ['Power BI'],
        caminhoParaRevisao: '/api/v1/revisao/solicitacao',
      }}
    />,
  )
  expect(screen.getByText('47% de compatibilidade')).toBeInTheDocument()
  expect(screen.getByText(/Atende: Logistica/)).toBeInTheDocument()
  expect(screen.getByText(/Faltam: Power BI/)).toBeInTheDocument()
  expect(screen.getByText('/api/v1/revisao/solicitacao')).toBeInTheDocument()
})
