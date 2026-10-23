import { useEffect, useMemo, useState } from 'react'
import { buscarVagasExternas, listarVagas } from '../services/carreiraService'
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

const FORMATADOR_SALARIO = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  maximumFractionDigits: 0,
})

function formatarSalarioAnual(vaga) {
  if (!vaga.salarioMinimo && !vaga.salarioMaximo) return null
  const min = vaga.salarioMinimo
    ? FORMATADOR_SALARIO.format(vaga.salarioMinimo)
    : null
  const max = vaga.salarioMaximo
    ? FORMATADOR_SALARIO.format(vaga.salarioMaximo)
    : null
  if (min && max) return `${min} – ${max} / ano`
  return `${min || max} / ano`
}

function formatarData(iso) {
  if (!iso) return null
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return null
  return data.toLocaleDateString('pt-BR')
}

export default function VagasLista() {
  const [dados, setDados] = useState(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(null)
  const [area, setArea] = useState('')
  const [modelo, setModelo] = useState('')
  const [busca, setBusca] = useState('')
  const [pagina, setPagina] = useState(0)
  const [recarga, setRecarga] = useState(0)

  const [termoExterno, setTermoExterno] = useState('')
  const [localExterno, setLocalExterno] = useState('')
  const [paginaExterna, setPaginaExterna] = useState(1)
  const [dadosExternos, setDadosExternos] = useState(null)
  const [carregandoExterno, setCarregandoExterno] = useState(false)
  const [erroExterno, setErroExterno] = useState(null)
  const [ultimaBusca, setUltimaBusca] = useState(null)

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

  function executarBuscaExterna(termo, local, pagina) {
    setCarregandoExterno(true)
    setErroExterno(null)
    buscarVagasExternas({ termo, local, pagina })
      .then((r) => {
        setDadosExternos(r)
        setUltimaBusca({ termo, local })
      })
      .catch((e) => setErroExterno(e))
      .finally(() => setCarregandoExterno(false))
  }

  function enviarBuscaExterna(e) {
    e.preventDefault()
    const termo = termoExterno.trim()
    if (termo.length < 2) return
    setPaginaExterna(1)
    executarBuscaExterna(termo, localExterno.trim(), 1)
  }

  function trocarPaginaExterna(pagina) {
    setPaginaExterna(pagina)
    executarBuscaExterna(ultimaBusca.termo, ultimaBusca.local, pagina)
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
          <EstadoVazio titulo="Nenhuma vaga encontrada" />
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

      <div className="mt-10 rounded-lg border border-slate-200 bg-white p-4">
        <h2 className="text-lg font-semibold text-primaria">
          Buscar vagas na internet
        </h2>

        <form
          onSubmit={enviarBuscaExterna}
          className="mt-3 flex flex-wrap items-end gap-3"
        >
          <div className="flex-1 min-w-48">
            <label htmlFor="termoExterno" className="block text-sm font-medium">
              Termo
            </label>
            <input
              id="termoExterno"
              type="search"
              required
              minLength={2}
              value={termoExterno}
              onChange={(e) => setTermoExterno(e.target.value)}
              placeholder="ex.: desenvolvedor java"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
            />
          </div>

          <div className="min-w-40">
            <label htmlFor="localExterno" className="block text-sm font-medium">
              Local (opcional)
            </label>
            <input
              id="localExterno"
              type="text"
              value={localExterno}
              onChange={(e) => setLocalExterno(e.target.value)}
              placeholder="ex.: São Paulo"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
            />
          </div>

          <button
            type="submit"
            disabled={carregandoExterno}
            className="min-h-12 rounded-lg bg-sucesso px-5 py-2 font-semibold text-white hover:bg-sucesso/90 disabled:opacity-40"
          >
            {carregandoExterno ? 'Buscando...' : 'Buscar'}
          </button>
        </form>

        {erroExterno && (
          <Feedback
            titulo={erroExterno.title || 'Erro na busca externa'}
            detalhe={erroExterno.detail}
            acao={
              erroExterno.status !== 503 && ultimaBusca ? (
                <button
                  type="button"
                  onClick={() =>
                    executarBuscaExterna(
                      ultimaBusca.termo,
                      ultimaBusca.local,
                      paginaExterna,
                    )
                  }
                  className="mt-2 min-h-12 rounded border border-current px-4 py-1 font-medium"
                >
                  Tentar novamente
                </button>
              ) : null
            }
          />
        )}

        {carregandoExterno && (
          <Carregando texto="Buscando vagas na internet..." />
        )}

        {!carregandoExterno && !erroExterno && dadosExternos && (
          <>
            {dadosExternos.content.length === 0 ? (
              <div className="mt-4">
                <EstadoVazio titulo="Nenhuma vaga externa encontrada" />
              </div>
            ) : (
              <>
                <ul className="mt-4 space-y-3">
                  {dadosExternos.content.map((v) => (
                    <li
                      key={v.id}
                      className="rounded-lg border border-slate-200 p-4"
                    >
                      <div className="flex flex-wrap items-start justify-between gap-2">
                        <div>
                          <a
                            href={v.link}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="font-semibold text-primaria hover:underline"
                          >
                            {v.titulo}
                          </a>
                          <p className="text-sm text-texto-suave">
                            {[v.empresa, v.local, formatarData(v.publicadoEm)]
                              .filter(Boolean)
                              .join(' · ')}
                          </p>
                        </div>
                        <a
                          href={v.link}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria"
                        >
                          Ver vaga ↗
                        </a>
                      </div>
                      {v.descricao && (
                        <p className="mt-2 line-clamp-2 text-sm">
                          {v.descricao}
                        </p>
                      )}
                      {formatarSalarioAnual(v) && (
                        <p className="mt-2 text-sm font-medium text-sucesso">
                          {formatarSalarioAnual(v)}
                        </p>
                      )}
                    </li>
                  ))}
                </ul>

                {dadosExternos.totalElementos > 20 && (
                  <nav
                    aria-label="Paginação da busca externa"
                    className="mt-4 flex items-center justify-between"
                  >
                    <button
                      type="button"
                      disabled={paginaExterna <= 1}
                      onClick={() => trocarPaginaExterna(paginaExterna - 1)}
                      className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 font-semibold text-primaria disabled:opacity-40"
                    >
                      ← Anterior
                    </button>
                    <span className="text-sm text-texto-suave">
                      Página {paginaExterna} · {dadosExternos.totalElementos}{' '}
                      vagas encontradas
                    </span>
                    <button
                      type="button"
                      disabled={paginaExterna * 20 >= dadosExternos.totalElementos}
                      onClick={() => trocarPaginaExterna(paginaExterna + 1)}
                      className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 font-semibold text-primaria disabled:opacity-40"
                    >
                      Próxima →
                    </button>
                  </nav>
                )}

                <p className="mt-4 text-sm text-texto-suave">
                  Resultados reais via{' '}
                  <a
                    href="https://www.adzuna.com.br"
                    target="_blank"
                    rel="noopener noreferrer"
                    className="underline"
                  >
                    Adzuna
                  </a>
                </p>
              </>
            )}
          </>
        )}
      </div>
    </section>
  )
}
