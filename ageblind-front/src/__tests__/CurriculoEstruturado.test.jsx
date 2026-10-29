import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, expect, test, vi } from 'vitest'
import CurriculoEstruturado from '../components/CurriculoEstruturado'

const { obterCurriculo, salvarCurriculo } = vi.hoisted(() => ({
  obterCurriculo: vi.fn(),
  salvarCurriculo: vi.fn(),
}))

vi.mock('../services/carreiraService', () => ({
  obterCurriculo,
  salvarCurriculo,
  enviarArquivoCurriculo: vi.fn(),
}))

const vazio = () => ({
  experiencias: [],
  formacoes: [],
  idiomas: [],
  certificacoes: [],
  projetos: [],
})

beforeEach(() => {
  vi.clearAllMocks()
  obterCurriculo.mockResolvedValue(vazio())
})

test('renderiza as 5 secoes do curriculo estilo LinkedIn', async () => {
  render(<CurriculoEstruturado perfilId={1} onUsarTexto={vi.fn()} />)
  await screen.findByRole('heading', { name: 'Experiências' })
  for (const nome of [
    'Experiências',
    'Formações',
    'Idiomas',
    'Certificações',
    'Projetos',
  ]) {
    expect(screen.getByRole('heading', { name: nome })).toBeInTheDocument()
  }
  expect(
    screen.getByRole('button', { name: '+ Adicionar formação' }),
  ).toBeInTheDocument()
})

test('ano de inicio anterior a 1900 bloqueia o salvamento (validacao client)', async () => {
  const user = userEvent.setup()
  render(<CurriculoEstruturado perfilId={1} onUsarTexto={vi.fn()} />)
  await screen.findByRole('heading', { name: 'Experiências' })

  await user.click(screen.getByRole('button', { name: '+ Adicionar experiência' }))
  await user.type(screen.getByLabelText(/^Cargo/), 'Analista')
  await user.type(screen.getByLabelText(/^Empresa/), 'Industria Alfa')
  await user.type(screen.getByLabelText(/^Ano de início/), '1800')
  await user.click(screen.getByRole('button', { name: 'Concluir' }))
  await user.click(screen.getByRole('button', { name: 'Salvar currículo' }))

  expect(await screen.findByText(/mínimo 1900/)).toBeInTheDocument()
  expect(salvarCurriculo).not.toHaveBeenCalled()
})

test('salva experiencia e envia payload no formato do CurriculoRequest', async () => {
  salvarCurriculo.mockImplementation(async (_id, dados) => dados)
  const user = userEvent.setup()
  render(<CurriculoEstruturado perfilId={1} onUsarTexto={vi.fn()} />)
  await screen.findByRole('heading', { name: 'Experiências' })

  await user.click(screen.getByRole('button', { name: '+ Adicionar experiência' }))
  await user.type(screen.getByLabelText(/^Cargo/), 'Analista')
  await user.type(screen.getByLabelText(/^Empresa/), 'Industria Alfa')
  await user.type(screen.getByLabelText(/^Ano de início/), '2015')
  await user.click(screen.getByRole('button', { name: 'Concluir' }))
  await user.click(screen.getByRole('button', { name: 'Salvar currículo' }))

  await waitFor(() => expect(salvarCurriculo).toHaveBeenCalledTimes(1))
  const [perfilId, payload] = salvarCurriculo.mock.calls[0]
  expect(perfilId).toBe(1)
  expect(payload.experiencias).toEqual([
    {
      cargo: 'Analista',
      empresa: 'Industria Alfa',
      anoInicio: 2015,
      anoFim: null,
      descricao: null,
    },
  ])
  expect(payload.formacoes).toEqual([])
  expect(payload.idiomas).toEqual([])
  expect(payload.certificacoes).toEqual([])
  expect(payload.projetos).toEqual([])
  expect(await screen.findByText('Currículo salvo.')).toBeInTheDocument()
})

test('idioma sem nivel bloqueia o salvamento', async () => {
  const user = userEvent.setup()
  render(<CurriculoEstruturado perfilId={1} onUsarTexto={vi.fn()} />)
  await screen.findByRole('heading', { name: 'Experiências' })

  await user.click(screen.getByRole('button', { name: '+ Adicionar idioma' }))
  await user.type(screen.getByLabelText(/^Idioma/), 'Ingles')
  await user.click(screen.getByRole('button', { name: 'Concluir' }))
  await user.click(screen.getByRole('button', { name: 'Salvar currículo' }))

  expect(await screen.findByText(/Selecione o nível/)).toBeInTheDocument()
  expect(salvarCurriculo).not.toHaveBeenCalled()
})
