import { createContext, useContext, useState } from 'react'
import { api } from '../services/api'

const CHAVE_TOKEN = 'ageblind.token'

const AuthContext = createContext(null)

export function AuthContextProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem(CHAVE_TOKEN))
  const [usuario, setUsuario] = useState(null)
  const [loading, setLoading] = useState(false)

  const login = async (email, senha) => {
    setLoading(true)
    try {
      const data = await api.post('/auth/login', { email, senha })
      const tokenValido = data.token
      localStorage.setItem(CHAVE_TOKEN, tokenValido)
      setToken(tokenValido)
      setUsuario(data.usuario)
      return { token: tokenValido, usuario: data.usuario }
    } finally {
      setLoading(false)
    }
  }

  const logout = () => {
    localStorage.removeItem(CHAVE_TOKEN)
    setToken(null)
    setUsuario(null)
  }

  const papel = usuario ? usuario.papel : null
  const isAuthenticated = !!token

  const valor = {
    token,
    usuario,
    papel,
    isAuthenticated,
    loading,
    login,
    logout,
  }

  return (
    <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
  )
}

export function useAuth() {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth deve ser usado dentro de AuthContextProvider')
  }
  return contexto
}
