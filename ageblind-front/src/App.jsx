import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import Perfil from './pages/Perfil'
import VagasLista from './pages/VagasLista'
import VagaDetalhe from './pages/VagaDetalhe'
import RecomendadasTrilhas from './pages/RecomendadasTrilhas'
import EscudoAntiEtarismo from './pages/EscudoAntiEtarismo'
import PainelRH from './pages/PainelRH'
import Login from './pages/Login'
import RequireAuth from './components/RequireAuth'
import { AuthContextProvider } from './context/AuthContext'

export default function App() {
  return (
    <AuthContextProvider>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/perfil" element={<Perfil />} />
          <Route path="/vagas" element={<VagasLista />} />
          <Route path="/vagas/:id" element={<VagaDetalhe />} />
          <Route path="/recomendadas" element={<RecomendadasTrilhas />} />
          <Route path="/escudo" element={<EscudoAntiEtarismo />} />
          <Route
            path="/rh/painel"
            element={
              <RequireAuth>
                <PainelRH />
              </RequireAuth>
            }
          />
          <Route
            path="*"
            element={
              <p className="text-texto-suave">Página não encontrada.</p>
            }
          />
        </Route>
      </Routes>
    </AuthContextProvider>
  )
}
