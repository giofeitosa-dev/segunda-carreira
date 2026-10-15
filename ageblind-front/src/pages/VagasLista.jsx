import { useEffect, useMemo, useState } from 'react'
import { listarVagas } from '../services/carreiraService'
import Carregando from '../components/Carregando'
import CardVaga from '../components/CardVaga'
import EstadoVazio from '../components/EstadoVazio'
import Feedback from '../components/Feedback'

const AREAS = [
  'Logistica',
  'Administracao',
  'TI',
  'Atendimento',
  'Vendas',
  'Producao',
]

const MODELOS = ['PRESENCIAL', 'HIBRIDO', 'REMOTO']

export default function VagasLista() {
  const [dados, setDados] = useState(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(null)
  const [area, setArea] = useState('')
  const [modelo, setModelo] = useState('')
  const [busca, setBusca] = useState('')
  const [pagina, setPagina] = useState(0)
  const [recarga, setRecarga] = useState(0)

  const chave = `${area}|${modelo}|${pagina}|${recarga}`
  const [chaveAnterior, setChaveAnterior] = useState(chave)
  if (chave !== chaveAnterior) {
    setChaveAnterior(chave)
    setCarregando(true)
    setErro(null)
  }

  useEffect(() => {
    let ativo = true
    listarVagas({ area, modelo, page: pagina, size: 12 })
      .then((r) => {
        if (ativo) setDados(r)
      })
      .catch((e) => {
        if (ativo) setErro(e)
      })
      .finally(() => {
        if (ativo) setCarregando(false)
      })
    return () => {
      ativo = false
    }
  }, [area, modelo, pagina, recarga])

  const itens = useMemo(() => {
    if (!dados?.content) return []
    const termo = busca.trim().toLowerCase()
    if (!termo) return dados.content
    return dados.content.filter((v) =>
      [v.titulo, v.empresa, v.descricao]
        .filter(Boolean)
        .some((campo) => campo.toLowerCase().includes(termo)),
    )
  }, [dados, busca])

  function trocarFiltro(setter) {
    return (e) => {
      setter(e.target.value)
      setPagina(0)
    }
  }

  return (
    <section>
      <h1 className="text-2xl font-semibold text-primaria">Explorar Vagas</h1>

      <div className="mt-4 flex flex-wrap items-end gap-3 rounded-lg border border-slate-200 bg-white p-4">
        <div>
          <label htmlFor="area" className="block text-sm font-medium">
            Área
          </label>
          <select
            id="area"
            value={area}
            onChange={trocarFiltro(setArea)}
            className="mt-1 min-h-12 rounded-lg border border-slate-300 px-3"
          >
            <option value="">Todas</option>
            {AREAS.map((a) => (
              <option key={a} value={a}>
                {a}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="modelo" className="block text-sm font-medium">
            Modelo
          </label>
          <select
            id="modelo"
            value={modelo}
            onChange={trocarFiltro(setModelo)}
            className="mt-1 min-h-12 rounded-lg border border-slate-300 px-3"
          >
            <option value="">Todos</option>
            {MODELOS.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
        </div>

        <div className="flex-1 min-w-48">
          <label htmlFor="busca" className="block text-sm font-medium">
            Busca (nesta página)
          </label>
          <input
            id="busca"
            type="search"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Título, empresa ou descrição"
            className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
          />
        </div>
      </div>

      {erro && (
        <Feedback
          titulo={erro.title || 'Erro ao carregar vagas'}
          detalhe={erro.detail}
          acao={
            <button
              type="button"
              onClick={() => setRecarga((r) => r + 1)}
              className="mt-2 min-h-12 rounded border border-current px-4 py-1 font-medium"
            >
              Tentar novamente
            </button>
          }
        />
      )}

      {carregando && <Carregando texto="Carregando vagas..." />}

      {!carregando && !erro && itens.length === 0 && (
        <div className="mt-4">
          <EstadoVazio
            titulo="Nenhuma vaga encontrada"
            texto="Ajuste os filtros ou limpe a busca para ver mais resultados."
          />
        </div>
      )}

      {!carregando && !erro && itens.length > 0 && (
        <>
          <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {itens.map((v) => (
              <CardVaga key={v.id} vaga={v} />
            ))}
          </div>

          <nav
            aria-label="Paginação"
            className="mt-6 flex items-center justify-between"
          >
            <button
              type="button"
              disabled={pagina === 0}
              onClick={() => setPagina((p) => p - 1)}
              className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 font-semibold text-primaria disabled:opacity-40"
            >
              ← Anterior
            </button>
            <span className="text-sm text-texto-suave">
              Página {(dados?.page ?? 0) + 1} de {dados?.totalPages || 1} ·{' '}
              {dados?.totalElements ?? 0} vagas
            </span>
            <button
              type="button"
              disabled={pagina >= (dados?.totalPages ?? 1) - 1}
              onClick={() => setPagina((p) => p + 1)}
              className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 font-semibold text-primaria disabled:opacity-40"
            >
              Próxima →
            </button>
          </nav>
        </>
      )}
    </section>
  )
}
