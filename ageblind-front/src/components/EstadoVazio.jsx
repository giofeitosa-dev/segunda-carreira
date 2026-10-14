import { Link } from 'react-router-dom'

export default function EstadoVazio({ titulo, texto, acaoTo, acaoRotulo }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-8 text-center">
      <p className="font-semibold text-primaria">{titulo}</p>
      {texto && <p className="mt-2 text-sm text-texto-suave">{texto}</p>}
      {acaoTo && (
        <Link
          to={acaoTo}
          className="mt-4 inline-block min-h-12 rounded-lg bg-sucesso px-5 py-3 font-semibold text-white hover:bg-sucesso/90"
        >
          {acaoRotulo}
        </Link>
      )}
    </div>
  )
}
