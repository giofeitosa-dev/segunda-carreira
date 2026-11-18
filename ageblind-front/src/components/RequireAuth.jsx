import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'

const CHAVE_TOKEN = 'ageblind.token'

export default function RequireAuth({ children }) {
  const location = useLocation()

  useEffect(() => {
    const token = localStorage.getItem(CHAVE_TOKEN)
    if (!token) {
      window.location.replace('/login')
    }
  }, [location])

  const token = localStorage.getItem(CHAVE_TOKEN)
  if (!token) return null

  return children
}
