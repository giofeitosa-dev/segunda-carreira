import { useEffect, useState } from 'react'
import {
  getPerfilAtivaId,
  listarMentorias,
  listarTrilhas,
  vagasRecomendadas,
} from '../services/carreiraService'
import CardVaga from '../components/CardVaga'
import Carregando from '../components/Carregando'
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

const ABAS = [
  { id: 'recomendadas', rotulo: 'Vagas recomendadas' },
  { id: 'trilhas', rotulo: 'Trilhas' },
  { id: 'mentorias', rotulo: 'Mentorias' },
]

const brl = (v) =>
  Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

export default function RecomendadasTrilhas() {
  const perfilId = getPerfilAtivaId()
  const [aba, setAba] = useState('recomendadas')
  const [area, setArea] = useState(AREAS[0])
  const [dados, setDados] = useState(null)
  const [carregando, setCarregando] = useState(Boolean(perfilId))
  const [erro, setErro] = useState(null)
  const [recarga, setRecarga] = useState(0)

  const precisaPerfil = aba !== 'trilhas'
  const semPerfil = precisaPerfil && !perfilId

  const chave = `${aba}|${area}|${perfilId}|${precisaPerfil}|${recarga}`
  const [chaveAnterior, setChaveAnterior] = useState(chave)
  const chaveMudou = chave !== chaveAnterior
  if (chaveMudou) {
    setChaveAnterior(chave)
    setDados(null)
    setErro(null)
    setCarregando(!semPerfil)
  }
  const dadosVista = chaveMudou ? null : dados
  const erroVista = chaveMudou ? null : erro
  const carregandoVista = chaveMudou ? !semPerfil : carregando

  useEffect(() => {
    if (semPerfil) return undefined
    let ativo = true
    const chamada =
      aba === 'recomendadas'
        ? vagasRecomendadas(perfilId)
        : aba === 'trilhas'
          ? listarTrilhas(area)
          : listarMentorias(perfilId)

    chamada
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
  }, [aba, area, perfilId, precisaPerfil, recarga, semPerfil])

  return (
    <section>
      <h1 className="text-2xl font-semibold text-primaria">
        Oportunidades alinhadas ao seu perfil
      </h1>

      <div
        role="tablist"
        aria-label="Seções"
        className="mt-4 flex flex-wrap gap-2 border-b border-slate-200"
      >
        {ABAS.map((t) => (
          <button
            key={t.id}
            role="tab"
            aria-selected={aba === t.id}
            onClick={() => setAba(t.id)}
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

      {erroVista && (
        <Feedback
          titulo={erroVista.title || 'Erro ao carregar dados'}
          detalhe={erroVista.detail}
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

      {semPerfil && (
        <div className="mt-4">
          <EstadoVazio
            titulo="Cadastre seu perfil primeiro"
            acaoTo="/perfil"
            acaoRotulo="Ir para meu perfil"
          />
        </div>
      )}

      {carregandoVista && <Carregando />}

      {!carregandoVista && !erroVista && perfilId && aba === 'recomendadas' && (
        <div className="mt-4">
          {dadosVista?.length === 0 ? (
            <EstadoVazio
              titulo="Nenhuma recomendação no momento"
              acaoTo="/perfil"
              acaoRotulo="Revisar perfil"
            />
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {dadosVista?.map((item) => (
                <CardVaga
                  key={item.vaga.id}
                  vaga={item.vaga}
                  justificativa={item}
                />
              ))}
            </div>
          )}
        </div>
      )}

      {!carregandoVista && !erroVista && aba === 'trilhas' && (
        <div className="mt-4">
          <label htmlFor="area-trilha" className="block text-sm font-medium">
            Área da trilha
          </label>
          <select
            id="area-trilha"
            value={area}
            onChange={(e) => setArea(e.target.value)}
            className="mt-1 mb-4 min-h-12 rounded-lg border border-slate-300 px-3"
          >
            {AREAS.map((a) => (
              <option key={a} value={a}>
                {a}
              </option>
            ))}
          </select>

          {dadosVista?.length === 0 ? (
            <EstadoVazio titulo="Nenhuma trilha para esta área" />
          ) : (
            <ul className="space-y-3">
              {dadosVista?.map((t) => (
                <li
                  key={t.id}
                  className="rounded-lg border border-slate-200 bg-white p-4"
                >
                  <div className="flex flex-wrap items-baseline justify-between gap-2">
                    <h3 className="font-semibold text-primaria">{t.nome}</h3>
                    <span className="text-sm text-texto-suave">
                      {t.instituicao} · {t.duracaoSemanas} semanas ·{' '}
                      {t.custo > 0 ? brl(t.custo) : 'Gratuita'} ·{' '}
                      {t.certificado ? 'Com certificado' : 'Sem certificado'}
                    </span>
                  </div>
                  <details className="mt-2">
                    <summary className="min-h-12 cursor-pointer font-medium text-primaria">
                      Módulos ({t.modulos.length})
                    </summary>
                    <ul className="mt-2 space-y-1 text-sm text-texto-suave">
                      {t.modulos.map((m) => (
                        <li key={m.nome}>
                          {m.nome} — {m.cargaHoraria}h
                        </li>
                      ))}
                    </ul>
                  </details>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {!carregandoVista && !erroVista && perfilId && aba === 'mentorias' && (
        <div className="mt-4">
          {dadosVista?.length === 0 ? (
            <EstadoVazio titulo="Nenhuma mentoria disponível para sua área" />
          ) : (
            <ul className="grid gap-4 sm:grid-cols-2">
              {dadosVista?.map((m) => (
                <li
                  key={m.id}
                  className="rounded-lg border border-slate-200 bg-white p-4"
                >
                  <h3 className="font-semibold text-primaria">{m.mentor}</h3>
                  <p className="mt-1 text-sm text-texto-suave">
                    Área: {m.area}
                  </p>
                  <p className="text-sm text-texto-suave">
                    Disponibilidade: {m.disponibilidade}
                  </p>
                  <p className="mt-2 text-sm font-medium text-sucesso">
                    {m.agendamento}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </section>
  )
}
