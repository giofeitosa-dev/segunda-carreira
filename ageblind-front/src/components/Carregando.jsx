export default function Carregando({ texto = 'Carregando...' }) {
  return (
    <div role="status" className="flex items-center gap-3 py-8 text-texto-suave">
      <span
        className="h-5 w-5 animate-spin rounded-full border-2 border-slate-300 border-t-primaria"
        aria-hidden="true"
      />
      <span>{texto}</span>
    </div>
  )
}
