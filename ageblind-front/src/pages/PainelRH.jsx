import { useEffect, useState } from 'react'
import { auditarLinguagemVaga, funilMetricas } from '../services/escudoService'
import { listarVagas } from '../services/carreiraService'
import Carregando from '../components/Carregando'
import EstadoVazio from '../components/EstadoVazio'
import Feedback from '../components/Feedback'

const ABAS = [
  { id: 'linguagem', rotulo: 'Auditoria de linguagem' },
  { id: 'funil', rotulo: 'Funil de contratação' },
]

const FAIXAS = [
  { codigo: 'ATE_39', rotulo: 'Até 39' },
  { codigo: '40_49', rotulo: '40–49' },
  { codigo: '50_MAIS', rotulo: '50+' },
]

const riscoCores = {
  BAIXO: 'bg-sucesso/10 text-sucesso',
  MEDIO: 'bg-orange-100 text-orange-800',
  ALTO: 'bg-perigo/10 text-perigo',
}

const percentual = (taxa) => `${Math.round((taxa || 0) * 100)}%`
const decimal = (n) => Number(n).toFixed(2).replace('.', ',')

export default function PainelRH() {
  const [aba, setAba] = useState('linguagem')
  const [titulo, setTitulo] = useState('')
  const [texto, setTexto] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState(null)
  const [auditoria, setAuditoria] = useState(null)

  const [vagas, setVagas] = useState(null)
  const [erroVagas, setErroVagas] = useState(null)
  const [vagaId, setVagaId] = useState('')
  const [metricas, setMetricas] = useState(null)
  const [carregandoFunil, setCarregandoFunil] = useState(false)

  useEffect(() => {
    let ativo = true
    listarVagas({ size: 100 })
      .then((r) => {
        if (ativo) setVagas(r.content || [])
      })
      .catch((e) => {
        if (ativo) setErroVagas(e)
      })
    return () => {
      ativo = false
    }
  }, [])

  useEffect(() => {
    if (!vagaId) {
      setMetricas(null)
      return
    }
    let ativo = true
    setCarregandoFunil(true)
    setErro(null)
    funilMetricas(vagaId)
      .then((r) => {
        if (ativo) setMetricas(r)
      })
      .catch((e) => {
        if (ativo) setErro(e)
      })
      .finally(() => {
        if (ativo) setCarregandoFunil(false)
      })
    return () => {
      ativo = false
    }
  }, [vagaId])

  function trocarAba(id) {
    setAba(id)
    setErro(null)
  }

  async function auditar(e) {
    e.preventDefault()
    setEnviando(true)
    setErro(null)
    setAuditoria(null)
    try {
      setAuditoria(await auditarLinguagemVaga(titulo.trim() || null, texto))
    } catch (ex) {
      setErro(ex)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section>
      <h1 className="text-2xl font-semibold text-primaria">Painel RH</h1>
      <p className="mt-2 max-w-3xl text-texto-suave">
        Audite a linguagem da vaga antes de publicar e acompanhe o funil de
        contratação com alerta de adverse impact (razão &lt; 0,80).
      </p>

      <div
        role="tablist"
        aria-label="Seções do painel"
        className="mt-4 flex flex-wrap gap-2 border-b border-slate-200"
      >
        {ABAS.map((t) => (
          <button
            key={t.id}
            role="tab"
            aria-selected={aba === t.id}
            onClick={() => trocarAba(t.id)}
            className={`min-h-12 px-4 py-2 -mb-px border-b-2 font-medium transition-colors ${
              aba === t.id
                ? 'border-sucesso text-primaria'
                : 'border-transparent text-texto-suave hover:text-primaria'
            }`}
          >
            {t.rotulo}
          </button>
        ))}
      </div>

      {erro && (
        <Feedback
          titulo={erro.title || 'Erro na operação'}
          detalhe={erro.detail}
          campos={erro.fieldErrors}
          acao={
            <button
              type="button"
              onClick={() => setErro(null)}
              className="mt-2 min-h-12 rounded border border-current px-4 py-1 font-medium"
            >
              Fechar aviso
            </button>
          }
        />
      )}

      {aba === 'linguagem' && (
        <>
          <p className="mt-4 max-w-3xl text-sm text-texto-suave">
            A auditoria aponta termos com viés etário, o risco legal
            identificado e uma sugestão de reescrita. Não constitui parecer
            jurídico.
          </p>

          <form
            onSubmit={auditar}
            className="mt-4 space-y-4 rounded-lg border border-slate-200 bg-white p-5"
          >
            <div>
              <label htmlFor="titulo-vaga" className="block text-sm font-medium">
                Título da vaga (opcional)
              </label>
              <input
                id="titulo-vaga"
                type="text"
                value={titulo}
                onChange={(e) => setTitulo(e.target.value)}
                placeholder="Ex.: Analista de Logística"
                className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none sm:max-w-md"
              />
            </div>

            <div>
              <label htmlFor="texto-vaga" className="block text-sm font-medium">
                Descrição da vaga *
              </label>
              <textarea
                id="texto-vaga"
                required
                rows={7}
                value={texto}
                onChange={(e) => setTexto(e.target.value)}
                placeholder="Cole aqui a descrição completa da vaga."
                className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-primaria focus:outline-none"
              />
            </div>

            <button
              type="submit"
              disabled={enviando}
              className="min-h-12 rounded-lg bg-sucesso px-6 py-3 font-semibold text-white hover:bg-sucesso/90 disabled:opacity-60"
            >
              {enviando ? 'Auditando...' : 'Auditar linguagem da vaga'}
            </button>
          </form>

          {auditoria && (
            <section className="mt-4 rounded-lg border border-slate-200 bg-white p-5">
              <div className="flex flex-wrap items-center gap-3">
                <h2 className="font-semibold text-primaria">
                  {auditoria.titulo || 'Descrição da vaga'}
                </h2>
                <span
                  className={`rounded-full px-3 py-1 text-sm font-semibold ${
                    riscoCores[auditoria.riscoLegal] ||
                    'bg-slate-200 text-texto-suave'
                  }`}
                >
                  Risco {auditoria.riscoLegal}
                </span>
              </div>

              <h3 className="mt-4 font-medium text-texto">
                Termos encontrados ({auditoria.termosEncontrados.length})
              </h3>
              {auditoria.termosEncontrados.length === 0 ? (
                <p className="mt-1 text-sm text-sucesso">
                  Nenhum termo com viés etário detectado na descrição.
                </p>
              ) : (
                <ul className="mt-2 flex flex-wrap gap-1.5">
                  {auditoria.termosEncontrados.map((t) => (
                    <li
                      key={t}
                      className="rounded bg-perigo/10 px-2 py-1 text-sm text-perigo"
                    >
                      {t}
                    </li>
                  ))}
                </ul>
              )}

              <p className="mt-4 text-sm text-texto-suave">
                <span className="font-medium text-texto">Base legal:</span>{' '}
                {auditoria.baseLegal}
              </p>

              <p className="mt-3 rounded-md bg-primaria/5 p-3 text-sm text-texto">
                <span className="font-semibold text-primaria">
                  Sugestão de reescrita:
                </span>{' '}
                {auditoria.sugestaoRedacao}
              </p>
            </section>
          )}
        </>
      )}

      {aba === 'funil' && (
        <div className="mt-4">
          <label htmlFor="vaga-funil" className="block text-sm font-medium">
            Vaga
          </label>

          {erroVagas ? (
            <Feedback
              titulo={erroVagas.title || 'Erro ao carregar vagas'}
              detalhe={erroVagas.detail}
            />
          ) : (
            <select
              id="vaga-funil"
              value={vagaId}
              onChange={(e) => setVagaId(e.target.value)}
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 sm:max-w-lg"
            >
              <option value="">Selecione uma vaga</option>
              {(vagas || []).map((v) => (
                <option key={v.id} value={v.id}>
                  #{v.id} · {v.titulo} — {v.empresa}
                </option>
              ))}
            </select>
          )}

          {carregandoFunil && <Carregando texto="Carregando métricas..." />}

          {!vagaId && !carregandoFunil && !erroVagas && (
            <div className="mt-4">
              <EstadoVazio
                titulo="Escolha uma vaga"
                texto="As métricas agregadas por faixa e o alerta de adverse impact aparecem aqui. Nenhum dado individual de idade é exibido."
              />
            </div>
          )}

          {vagaId && metricas && !carregandoFunil && (
            <section className="mt-4 space-y-4">
              <div className="rounded-lg border border-slate-200 bg-white p-5">
                <p className="font-medium text-texto">
                  {metricas.interpretacao}
                </p>
                <p className="mt-1 text-sm text-texto-suave">
                  Vaga #{metricas.vagaId} · taxas de avanço por etapa e faixa
                  agregada.
                </p>

                <div className="mt-4 overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b border-slate-200 text-left text-texto-suave">
                        <th className="py-2 pr-4 font-medium">Etapa</th>
                        {FAIXAS.map((f) => (
                          <th key={f.codigo} className="py-2 pr-4 font-medium">
                            {f.rotulo}
                          </th>
                        ))}
                        <th className="py-2 pr-4 font-medium">Razão</th>
                        <th className="py-2 font-medium">Alerta</th>
                      </tr>
                    </thead>
                    <tbody>
                      {metricas.etapas.map((etapa) => (
                        <tr
                          key={etapa.nome}
                          className="border-b border-slate-100"
                        >
                          <td className="py-3 pr-4 font-medium text-texto">
                            {etapa.nome}
                          </td>
                          {FAIXAS.map((f) => (
                            <td key={f.codigo} className="py-3 pr-4">
                              {percentual(etapa.aprovacao?.[f.codigo])}
                            </td>
                          ))}
                          <td className="py-3 pr-4">
                            {decimal(etapa.razaoAdverseImpact)}
                          </td>
                          <td className="py-3">
                            {etapa.alerta ? (
                              <span className="rounded-full bg-perigo/10 px-2 py-0.5 text-xs font-semibold text-perigo">
                                Adverse impact
                              </span>
                            ) : (
                              <span className="text-sucesso">—</span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {metricas.etapas.length === 0 && (
                  <p className="mt-3 text-sm text-texto-suave">
                    Sem métricas para esta vaga no período.
                  </p>
                )}
              </div>

              <p className="text-sm text-texto-suave">
                Caminho para revisão humana:{' '}
                <code className="rounded bg-slate-100 px-1">
                  {metricas.caminhoParaRevisao}
                </code>
              </p>
            </section>
          )}
        </div>
      )}
    </section>
  )
}
