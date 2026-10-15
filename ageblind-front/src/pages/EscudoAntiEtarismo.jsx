import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import {
  auditarCurriculo,
  blindarCurriculo,
  solicitarRevisao,
} from '../services/escudoService'
import { getPerfilAtivaId } from '../services/carreiraService'
import EstadoVazio from '../components/EstadoVazio'
import Feedback from '../components/Feedback'

const ABAS = [
  { id: 'blindar', rotulo: 'Blindar currículo' },
  { id: 'auditar', rotulo: 'Auditar currículo' },
  { id: 'revisao', rotulo: 'Revisão (LGPD art. 20)' },
]

const severidadeCores = {
  ALTA: 'bg-perigo/10 text-perigo',
  MEDIA: 'bg-orange-100 text-orange-800',
  BAIXA: 'bg-slate-200 text-texto-suave',
}

export default function EscudoAntiEtarismo() {
  const [params] = useSearchParams()
  const [aba, setAba] = useState('blindar')
  const [curriculo, setCurriculo] = useState('')
  const [vagaAlvo, setVagaAlvo] = useState(params.get('vagaAlvo') || '')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState(null)
  const [versao, setVersao] = useState(null)
  const [auditoria, setAuditoria] = useState(null)
  const [motivo, setMotivo] = useState('')
  const [revisao, setRevisao] = useState(null)
  const perfilId = getPerfilAtivaId()

  function trocarAba(id) {
    setAba(id)
    setErro(null)
  }

  async function blindar(e) {
    e.preventDefault()
    setEnviando(true)
    setErro(null)
    setVersao(null)
    try {
      setVersao(await blindarCurriculo(curriculo, vagaAlvo || null))
    } catch (ex) {
      setErro(ex)
    } finally {
      setEnviando(false)
    }
  }

  async function auditar(e) {
    e.preventDefault()
    setEnviando(true)
    setErro(null)
    setAuditoria(null)
    try {
      setAuditoria(await auditarCurriculo(curriculo, vagaAlvo || null))
    } catch (ex) {
      setErro(ex)
    } finally {
      setEnviando(false)
    }
  }

  async function pedirRevisao(e) {
    e.preventDefault()
    setEnviando(true)
    setErro(null)
    setRevisao(null)
    try {
      setRevisao(await solicitarRevisao(perfilId, motivo.trim()))
    } catch (ex) {
      setErro(ex)
    } finally {
      setEnviando(false)
    }
  }

  const acaoErro = (
    <button
      type="button"
      onClick={() => setErro(null)}
      className="mt-2 min-h-12 rounded border border-current px-4 py-1 font-medium"
    >
      Fechar aviso
    </button>
  )

  return (
    <section>
      <h1 className="text-2xl font-semibold text-primaria">
        Escudo Anti-Etarismo
      </h1>
      <p className="mt-2 max-w-3xl text-texto-suave">
        Remova proxies de idade do seu currículo (15 regras), veja a pontuação
        anti-proxy de idade (não é ATS) e peça revisão humana quando quiser —
        conforme o art. 20 da LGPD.
      </p>

      <div
        role="tablist"
        aria-label="Ações do escudo"
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
          acao={acaoErro}
        />
      )}

      {aba !== 'revisao' && (
        <form
          onSubmit={aba === 'blindar' ? blindar : auditar}
          className="mt-4 space-y-4 rounded-lg border border-slate-200 bg-white p-5"
        >
          <div>
            <label htmlFor="curriculo" className="block text-sm font-medium">
              Seu currículo (texto) *
            </label>
            <textarea
              id="curriculo"
              required
              rows={8}
              value={curriculo}
              onChange={(e) => setCurriculo(e.target.value)}
              placeholder="Cole aqui o texto do currículo — sem dados de idade, se puder."
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-primaria focus:outline-none"
            />
          </div>

          <div>
            <label htmlFor="vaga-alvo" className="block text-sm font-medium">
              Vaga alvo (opcional)
            </label>
            <input
              id="vaga-alvo"
              type="text"
              value={vagaAlvo}
              onChange={(e) => setVagaAlvo(e.target.value)}
              placeholder="Ex.: Analista Logístico Pleno"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none sm:max-w-md"
            />
          </div>

          <button
            type="submit"
            disabled={enviando}
            className="min-h-12 rounded-lg bg-sucesso px-6 py-3 font-semibold text-white hover:bg-sucesso/90 disabled:opacity-60"
          >
            {enviando
              ? 'Processando...'
              : aba === 'blindar'
                ? 'Gerar versão sem proxies de idade'
                : 'Auditar currículo'}
          </button>
        </form>
      )}

      {aba === 'blindar' && versao && (
        <section className="mt-4 rounded-lg border border-slate-200 bg-white p-5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-semibold text-primaria">
              Versão {versao.tipo} do seu currículo
            </h2>
            <button
              type="button"
              onClick={() => navigator.clipboard?.writeText(versao.conteudo)}
              className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white"
            >
              Copiar texto
            </button>
          </div>

          <p className="mt-2 text-sm text-texto-suave">
            {versao.regrasAplicadas.length} regra(s) aplicada(s) nesta versão.
          </p>

          <pre className="mt-3 whitespace-pre-wrap rounded-md bg-slate-50 p-4 text-sm text-texto">
            {versao.conteudo}
          </pre>

          {versao.regrasAplicadas.length > 0 && (
            <ul className="mt-4 space-y-1 text-sm text-texto-suave">
              {versao.regrasAplicadas.map((r) => (
                <li key={r.regra}>
                  <span className="font-medium text-texto">{r.regra}</span> —{' '}
                  {r.acao}
                </li>
              ))}
            </ul>
          )}
        </section>
      )}

      {aba === 'auditar' && auditoria && (
        <section className="mt-4 space-y-4">
          <div className="rounded-lg border border-slate-200 bg-white p-5">
            <h2 className="font-semibold text-primaria">
              Pontuação anti-proxy de idade (não é ATS)
            </h2>
            <p className="mt-2 text-3xl font-semibold text-primaria">
              {auditoria.scoreAts}
              <span className="text-base font-normal text-texto-suave">
                {' '}
                / 100 — quanto maior, menos proxies de idade o texto contém
              </span>
            </p>

            <h3 className="mt-4 font-medium text-texto">
              Sinais que revelam idade ({auditoria.sinaisQueRevelamIdade.length}
              )
            </h3>
            {auditoria.sinaisQueRevelamIdade.length === 0 ? (
              <p className="mt-1 text-sm text-sucesso">
                Nenhum sinal de proxy de idade encontrado.
              </p>
            ) : (
              <ul className="mt-2 space-y-2 text-sm">
                {auditoria.sinaisQueRevelamIdade.map((s) => (
                  <li
                    key={`${s.sinal}-${s.localizacao}`}
                    className="rounded-md bg-slate-50 p-3"
                  >
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                        severidadeCores[s.severidade] ||
                        'bg-slate-200 text-texto-suave'
                      }`}
                    >
                      {s.severidade}
                    </span>{' '}
                    <span className="font-medium">{s.sinal}</span>
                    <span className="text-texto-suave"> · {s.localizacao}</span>
                    <p className="mt-1 text-texto-suave">Ação: {s.acao}</p>
                  </li>
                ))}
              </ul>
            )}

            {auditoria.sugestoes.length > 0 && (
              <>
                <h3 className="mt-4 font-medium text-texto">Sugestões</h3>
                <ul className="mt-1 list-disc space-y-1 pl-5 text-sm text-texto-suave">
                  {auditoria.sugestoes.map((s) => (
                    <li key={s}>{s}</li>
                  ))}
                </ul>
              </>
            )}
          </div>

          <div className="rounded-lg border border-slate-200 bg-white p-5">
            <p className="text-sm text-texto-suave">
              Caminho para revisão humana:{' '}
              <code className="rounded bg-slate-100 px-1">
                {auditoria.caminhoParaRevisao}
              </code>
            </p>
            <button
              type="button"
              onClick={() => trocarAba('revisao')}
              className="mt-3 min-h-12 rounded-lg border-2 border-primaria px-5 py-2 font-semibold text-primaria hover:bg-primaria hover:text-white"
            >
              Solicitar revisão humana
            </button>
          </div>
        </section>
      )}

      {aba === 'revisao' && (
        <>
          <p className="mt-4 max-w-3xl text-sm text-texto-suave">
            A LGPD garante o direito de pedir revisão de decisão automatizada.
            Descreva o que você quer revisar — você receberá um protocolo de
            acompanhamento.
          </p>

          {!perfilId ? (
            <div className="mt-4">
              <EstadoVazio
                titulo="Cadastre seu perfil primeiro"
                texto="O pedido de revisão precisa do seu perfil para rastrear o protocolo."
                acaoTo="/perfil"
                acaoRotulo="Ir para meu perfil"
              />
            </div>
          ) : (
            <form
              onSubmit={pedirRevisao}
              className="mt-4 space-y-4 rounded-lg border border-slate-200 bg-white p-5"
            >
              <div>
                <label htmlFor="motivo" className="block text-sm font-medium">
                  Motivo da revisão *
                </label>
                <textarea
                  id="motivo"
                  required
                  maxLength={1000}
                  rows={4}
                  value={motivo}
                  onChange={(e) => setMotivo(e.target.value)}
                  placeholder="Ex.: fui desclassificado(a) e gostaria de revisão humana do resultado."
                  className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-primaria focus:outline-none"
                />
                <p className="mt-1 text-xs text-texto-suave">
                  {motivo.length}/1000 caracteres
                </p>
              </div>

              <button
                type="submit"
                disabled={enviando}
                className="min-h-12 rounded-lg bg-sucesso px-6 py-3 font-semibold text-white hover:bg-sucesso/90 disabled:opacity-60"
              >
                {enviando ? 'Enviando...' : 'Solicitar revisão'}
              </button>
            </form>
          )}

          {revisao && (
            <div className="mt-4">
              <Feedback
                tipo="sucesso"
                titulo={`Protocolo ${revisao.protocolo} registrado.`}
                detalhe={`Status: ${revisao.status} · Solicitação #${
                  revisao.id
                } criada em ${new Date(revisao.criadoEm).toLocaleString('pt-BR')}.`}
              />
            </div>
          )}
        </>
      )}

      <p className="mt-6 text-sm text-texto-suave">
        Nenhum dado de idade é solicitado ou armazenado ·{' '}
        <Link to="/rh/painel" className="font-medium text-primaria underline">
          Sou RH: auditar vaga e ver o funil
        </Link>
      </p>
    </section>
  )
}
