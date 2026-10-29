import { render, screen } from '@testing-library/react'
import { expect, test } from 'vitest'
import Feedback from '../components/Feedback'

test('erro de validação RFC 7807 vira alert com title e detail', () => {
  render(<Feedback titulo="Dados inválidos" detalhe="Verifique os campos" />)
  const alerta = screen.getByRole('alert')
  expect(alerta).toHaveTextContent('Dados inválidos')
  expect(alerta).toHaveTextContent('Verifique os campos')
})

test('lista fieldErrors e renderiza acao (ex.: Tentar novamente)', () => {
  render(
    <Feedback
      titulo="Falha ao salvar"
      campos={[
        { field: 'nome', message: 'obrigatório' },
        { field: 'email', message: 'inválido' },
      ]}
      acao={<button type="button">Tentar novamente</button>}
    />,
  )
  expect(screen.getByText('nome')).toBeInTheDocument()
  expect(screen.getByText(/obrigatório/)).toBeInTheDocument()
  expect(screen.getByText('email')).toBeInTheDocument()
  expect(screen.getByText(/inválido/)).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Tentar novamente' })).toBeInTheDocument()
})

test('sucesso usa role status (não alert)', () => {
  render(<Feedback tipo="sucesso" titulo="Perfil criado com sucesso (id 1)." />)
  expect(screen.getByRole('status')).toHaveTextContent('Perfil criado')
})
