import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()
  const [erro, setErro] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const from = location.state?.from?.pathname || '/'

  const handleSubmit = async (event) => {
    event.preventDefault()
    setErro(null)
    setEnviando(true)
    const formData = new FormData(event.currentTarget)
    const email = formData.get('email')
    const senha = formData.get('senha')

    try {
      await login(email, senha)
      navigate(from, { replace: true })
    } catch {
      setErro('Email ou senha invalidos. Tente novamente.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="max-w-sm mx-auto py-10 px-4">
      <h2 className="text-2xl font-semibold text-primaria mb-6">Entrar</h2>

      {erro && (
        <div className="mb-4 p-3 rounded bg-alerta-claro text-alerta-escuro text-sm">
          {erro}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label htmlFor="email" className="block text-sm font-medium mb-1">
            Email
          </label>
          <input
            id="email"
            name="email"
            type="email"
            required
            className="w-full border rounded px-3 py-2 focus:border-primaria focus:outline-none"
          />
        </div>
        <div>
          <label htmlFor="senha" className="block text-sm font-medium mb-1">
            Senha
          </label>
          <input
            id="senha"
            name="senha"
            type="password"
            required
            className="w-full border rounded px-3 py-2 focus:border-primaria focus:outline-none"
          />
        </div>
        <button
          type="submit"
          disabled={enviando}
          className="w-full rounded-lg bg-primaria px-6 py-3 font-semibold text-white disabled:bg-primaria/60 hover:bg-primaria/90 transition-colors"
        >
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>
      </form>

      <p className="mt-6 text-sm text-slate-500 text-center">
        Nao tem uma conta? Cadastre-se na API via
        <code className="font-mono">POST /api/v1/auth/cadastro</code>.
      </p>
    </section>
  )
}
