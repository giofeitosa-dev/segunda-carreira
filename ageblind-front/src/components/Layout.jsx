import { NavLink, Outlet } from 'react-router-dom'

const links = [
  { to: '/', label: 'Início', end: true },
  { to: '/perfil', label: 'Perfil' },
  { to: '/vagas', label: 'Vagas' },
  { to: '/recomendadas', label: 'Recomendadas' },
  { to: '/escudo', label: 'Escudo' },
  { to: '/rh/painel', label: 'Painel RH' },
]

export default function Layout() {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-primaria text-white sticky top-0 z-10 shadow">
        <div className="mx-auto max-w-6xl px-4 py-3 flex flex-wrap items-center gap-x-6 gap-y-2">
          <NavLink to="/" className="text-lg font-semibold tracking-tight">
            Segunda Carreira
          </NavLink>
          <nav className="flex flex-wrap gap-x-4 gap-y-1 text-sm">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.end}
                className={({ isActive }) =>
                  `py-1 border-b-2 transition-colors ${
                    isActive
                      ? 'border-sucesso-claro text-white'
                      : 'border-transparent text-slate-300 hover:text-white'
                  }`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <div className="mx-auto max-w-6xl px-4 py-6">
          <Outlet />
        </div>
      </main>

      <footer className="bg-primaria text-slate-300 text-sm">
        <div className="mx-auto max-w-6xl px-4 py-4 flex flex-wrap gap-x-6 gap-y-1 justify-between">
          <span>Segunda Carreira — contratação por competência</span>
          <span>ODS 5 e ODS 10 · LGPD art. 20 — revisão de decisão automatizada</span>
        </div>
      </footer>
    </div>
  )
}
