import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  agendarLembrete,
  criarInscricao,
  getPerfilAtivaId,
  obterVaga,
} from '../services/carreiraService'
import Carregando from '../components/Carregando'
import EstadoVazio from '../components/EstadoVazio'
import Feedback from '../components/Feedback'

const brl = (v) =>
  Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

export default function VagaDetalhe() {
  const { id } = useParams()
  const [vaga, setVaga] = useState(null)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(null)
  const [inscricao, setInscricao] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [sucesso, setSucesso] = useState('')
  const [erroAcao, setErroAcao] = useState(null)
  const perfilId = getPerfilAtivaId()

  const [idAnterior, setIdAnterior] = useState(id)
  if (id !== idAnterior) {
    setIdAnterior(id)
    setCarregando(true)
    setErro(null)
  }

  useEffect(() => {
    let ativo = true
    obterVaga(id)
      .then((r) => {
        if (ativo) setVaga(r)
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
  }, [id])

  async function inscrever() {
    setEnviando(true)
    setSucesso('')
    setErroAcao(null)
    try {
      const r = await criarInscricao({
        perfilId,
        tipo: 'VAGA',
        referenciaId: String(id),
      })
      setInscricao(r)
      setSucesso(
        `Inscrição enviada com sucesso (protocolo #${r.id}). Status: ${r.status}.`,
      )
    } catch (e) {
      setErroAcao(e)
    } finally {
      setEnviando(false)
    }
  }

  async function agendar() {
    setEnviando(true)
    setErroAcao(null)
    try {
      await agendarLembrete({ inscricaoId: inscricao.id, antecedenciaHoras: 24 })
      setSucesso(
        `Inscrição #${inscricao.id} confirmada. Lembrete agendado para 24h antes do prazo.`,
      )
    } catch (e) {
      setErroAcao(e)
    } finally {
      setEnviando(false)
    }
  }

  if (carregando) return <Carregando texto="Carregando vaga..." />

  if (erro?.status === 404) {
    return (
      <EstadoVazio
        titulo="Vaga não encontrada"
        texto={`A vaga ${id} não existe ou foi removida.`}
        acaoTo="/vagas"
        acaoRotulo="Voltar para vagas"
      />
    )
  }

  if (erro) {
    return (
      <Feedback
        titulo={erro.title || 'Erro ao carregar a vaga'}
        detalhe={erro.detail}
        acao={
          <Link
            to="/vagas"
            className="mt-2 inline-block min-h-12 rounded border border-current px-4 py-1 font-medium"
          >
            Voltar para vagas
          </Link>
        }
      />
    )
  }

  if (!vaga) return null

  return (
    <section>
      <Link
        to="/vagas"
        className="text-sm font-medium text-primaria hover:underline"
      >
        ← Voltar para vagas
      </Link>

      <div className="mt-3 grid gap-6 lg:grid-cols-3">
        <article className="lg:col-span-2 rounded-lg border border-slate-200 bg-white p-5">
          <div className="flex flex-wrap items-start justify-between gap-2">
            <h1 className="text-2xl font-semibold text-primaria">
              {vaga.titulo}
            </h1>
            <span className="rounded-full bg-primaria/10 px-3 py-1 text-sm font-semibold text-primaria">
              {vaga.modelo}
            </span>
          </div>

          <p className="mt-1 text-texto-suave">
            {vaga.empresa} · {vaga.area}
          </p>

          <dl className="mt-4 grid gap-3 sm:grid-cols-2 text-sm">
            <div className="rounded-md bg-slate-50 p-3">
              <dt className="font-medium text-texto-suave">Salário</dt>
              <dd className="mt-0.5 text-lg font-semibold text-primaria">
                {brl(vaga.salario)}
              </dd>
            </div>
            <div className="rounded-md bg-slate-50 p-3">
              <dt className="font-medium text-texto-suave">
                Prazo de inscrição
              </dt>
              <dd className="mt-0.5 text-lg font-semibold text-primaria">
                {new Date(vaga.prazoInscricao).toLocaleString('pt-BR', {
                  dateStyle: 'short',
                  timeStyle: 'short',
                })}
              </dd>
            </div>
          </dl>

          <h2 className="mt-5 font-semibold text-primaria">Descrição</h2>
          <p className="mt-1 text-texto">{vaga.descricao}</p>

          {vaga.requisitos?.length > 0 && (
            <>
              <h2 className="mt-5 font-semibold text-primaria">
                Competências exigidas
              </h2>
              <ul className="mt-2 flex flex-wrap gap-2">
                {vaga.requisitos.map((r) => (
                  <li
                    key={r}
                    className="rounded bg-slate-100 px-2 py-1 text-sm text-texto-suave"
                  >
                    {r}
                  </li>
                ))}
              </ul>
            </>
          )}
        </article>

        <aside className="rounded-lg border border-slate-200 bg-white p-5 h-fit">
          <h2 className="font-semibold text-primaria">Ações do candidato</h2>

          {sucesso && <Feedback tipo="sucesso" titulo={sucesso} />}
          {erroAcao && (
            <Feedback
              titulo={erroAcao.title || 'Erro na operação'}
              detalhe={erroAcao.detail}
              campos={erroAcao.fieldErrors}
            />
          )}

          {!perfilId ? (
            <div className="mt-3">
              <p className="text-sm text-texto-suave">
                Cadastre seu perfil para se inscrever nesta vaga.
              </p>
              <Link
                to="/perfil"
                className="mt-3 inline-block min-h-12 rounded-lg bg-sucesso px-5 py-3 font-semibold text-white hover:bg-sucesso/90"
              >
                Cadastrar perfil
              </Link>
            </div>
          ) : (
            <div className="mt-3 space-y-3">
              {!inscricao ? (
                <button
                  type="button"
                  onClick={inscrever}
                  disabled={enviando}
                  className="min-h-12 w-full rounded-lg bg-sucesso px-5 py-3 font-semibold text-white hover:bg-sucesso/90 disabled:opacity-60"
                >
                  {enviando ? 'Enviando...' : 'Inscrever-se na vaga'}
                </button>
              ) : (
                <button
                  type="button"
                  onClick={agendar}
                  disabled={enviando}
                  className="min-h-12 w-full rounded-lg border-2 border-primaria px-5 py-3 font-semibold text-primaria hover:bg-primaria hover:text-white disabled:opacity-60"
                >
                  {enviando
                    ? 'Agendando...'
                    : 'Agendar lembrete (24h antes do prazo)'}
                </button>
              )}

              <Link
                to={`/escudo?vagaAlvo=${encodeURIComponent(vaga.titulo)}`}
                className="block min-h-12 rounded-lg bg-primaria px-5 py-3 text-center font-semibold text-white hover:bg-primaria/90"
              >
                Blindar meu currículo para esta vaga
              </Link>
            </div>
          )}
        </aside>
      </div>
    </section>
  )
}
