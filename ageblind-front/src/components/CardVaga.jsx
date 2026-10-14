import { Link } from 'react-router-dom'

const brl = (v) =>
  Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

const data = (v) => new Date(v).toLocaleDateString('pt-BR')

const modeloCores = {
  REMOTO: 'bg-sucesso-claro/15 text-sucesso',
  HIBRIDO: 'bg-primaria/10 text-primaria',
  PRESENCIAL: 'bg-slate-200 text-texto-suave',
}

export default function CardVaga({ vaga, justificativa }) {
  return (
    <article className="flex flex-col rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-semibold text-primaria">{vaga.titulo}</h3>
        <span
          className={`shrink-0 rounded-full px-2 py-0.5 text-xs font-semibold ${
            modeloCores[vaga.modelo] || 'bg-slate-200 text-texto-suave'
          }`}
        >
          {vaga.modelo}
        </span>
      </div>

      <p className="mt-1 text-sm text-texto-suave">
        {vaga.empresa} · {vaga.area}
      </p>

      <p className="mt-2 text-sm text-texto">{vaga.descricao}</p>

      <dl className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-sm text-texto-suave">
        <div>
          <dt className="inline font-medium">Salário:</dt>{' '}
          <dd className="inline">{brl(vaga.salario)}</dd>
        </div>
        <div>
          <dt className="inline font-medium">Prazo:</dt>{' '}
          <dd className="inline">{data(vaga.prazoInscricao)}</dd>
        </div>
      </dl>

      {vaga.requisitos?.length > 0 && (
        <ul className="mt-2 flex flex-wrap gap-1.5">
          {vaga.requisitos.map((r) => (
            <li
              key={r}
              className="rounded bg-slate-100 px-2 py-0.5 text-xs text-texto-suave"
            >
              {r}
            </li>
          ))}
        </ul>
      )}

      {justificativa && (
        <div className="mt-3 rounded-md bg-primaria/5 p-3 text-sm">
          <p className="font-semibold text-primaria">
            {Math.round(justificativa.score * 100)}% de compatibilidade
          </p>
          {justificativa.competenciasAtendidas?.length > 0 && (
            <p className="mt-1 text-sucesso">
              ✓ Atende: {justificativa.competenciasAtendidas.join(', ')}
            </p>
          )}
          {justificativa.competenciasFaltantes?.length > 0 && (
            <p className="mt-0.5 text-texto-suave">
              ○ Faltam: {justificativa.competenciasFaltantes.join(', ')}
            </p>
          )}
          {justificativa.caminhoParaRevisao && (
            <p className="mt-1 text-xs text-texto-suave">
              {justificativa.caminhoParaRevisao}
            </p>
          )}
        </div>
      )}

      <Link
        to={`/vagas/${vaga.id}`}
        className="mt-4 min-h-12 rounded-lg border-2 border-primaria py-2 text-center font-semibold text-primaria hover:bg-primaria hover:text-white transition-colors"
      >
        Ver detalhes da vaga
      </Link>
    </article>
  )
}
