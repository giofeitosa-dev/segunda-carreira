import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  atualizarPerfil,
  competenciasTransferiveis,
  criarPerfil,
  getPerfilAtivaId,
  limparPerfilAtiva,
  obterPerfil,
  setPerfilAtivaId,
  vagasRecomendadas,
} from '../services/carreiraService'
import { blindarCurriculo } from '../services/escudoService'
import Carregando from '../components/Carregando'
import CurriculoEstruturado from '../components/CurriculoEstruturado'
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

const DISPONIBILIDADES = ['REMOTO', 'HIBRIDO', 'PRESENCIAL', 'TURNO_NOITE']

const brl = (v) =>
  Number(v).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

const FORM_INICIAL = {
  nome: '',
  email: '',
  telefone: '',
  cidade: '',
  resumo: '',
  linkedin: '',
  github: '',
  areaAtual: '',
  areaAlvo: '',
  pretensaoSalarial: '',
  disponibilidade: [],
}

export default function Perfil() {
  const [carregando, setCarregando] = useState(true)
  const [modo, setModo] = useState('criar')
  const [form, setForm] = useState(FORM_INICIAL)
  const [enviando, setEnviando] = useState(false)
  const [sucesso, setSucesso] = useState('')
  const [erro, setErro] = useState(null)
  const [transferiveis, setTransferiveis] = useState(null)
  const [mapeando, setMapeando] = useState(false)
  const [curriculoTexto, setCurriculoTexto] = useState('')
  const [recomendadas, setRecomendadas] = useState(null)
  const [carregandoRec, setCarregandoRec] = useState(false)
  const [vagaAberta, setVagaAberta] = useState(null)
  const [adaptando, setAdaptando] = useState(false)
  const [versao, setVersao] = useState(null)
  const [erroCurriculo, setErroCurriculo] = useState(null)

  useEffect(() => {
    const id = getPerfilAtivaId()
    if (!id) {
      setModo('criar')
      setCarregando(false)
      return
    }
    obterPerfil(id)
      .then((perfil) => {
        preencher(perfil)
        setModo('editar')
        return competenciasTransferiveis(perfil.id)
          .then((t) => {
            setTransferiveis(t)
            setCurriculoTexto((atual) => atual || comporCurriculo(perfil, t))
          })
          .catch(() => {
            setCurriculoTexto((atual) => atual || comporCurriculo(perfil, null))
          })
      })
      .catch((e) => {
        if (e.status === 404) {
          limparPerfilAtiva()
          setModo('criar')
        } else {
          setErro(e)
        }
      })
      .finally(() => setCarregando(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function preencher(perfil) {
    setForm({
      nome: perfil.nome || '',
      email: perfil.email || '',
      telefone: perfil.telefone || '',
      cidade: perfil.cidade || '',
      resumo: perfil.resumo || '',
      linkedin: perfil.linkedin || '',
      github: perfil.github || '',
      areaAtual: perfil.areaAtual || '',
      areaAlvo: perfil.areaAlvo || '',
      pretensaoSalarial:
        perfil.pretensaoSalarial != null ? String(perfil.pretensaoSalarial) : '',
      disponibilidade: perfil.disponibilidade || [],
    })
  }

  function alterar(campo, valor) {
    setForm((f) => ({ ...f, [campo]: valor }))
  }

  function alternarDisponibilidade(valor) {
    setForm((f) => ({
      ...f,
      disponibilidade: f.disponibilidade.includes(valor)
        ? f.disponibilidade.filter((d) => d !== valor)
        : [...f.disponibilidade, valor],
    }))
  }

  async function enviar(e) {
    e.preventDefault()
    setEnviando(true)
    setSucesso('')
    setErro(null)
    const dados = {
      nome: form.nome.trim(),
      email: form.email.trim(),
      areaAtual: form.areaAtual.trim() || null,
      areaAlvo: form.areaAlvo.trim(),
      pretensaoSalarial: form.pretensaoSalarial
        ? Number(form.pretensaoSalarial)
        : null,
      disponibilidade: form.disponibilidade,
      telefone: form.telefone.trim() || null,
      cidade: form.cidade.trim() || null,
      resumo: form.resumo.trim() || null,
      linkedin: form.linkedin.trim() || null,
      github: form.github.trim() || null,
    }
    try {
      if (modo === 'criar') {
        const criado = await criarPerfil(dados)
        setPerfilAtivaId(criado.id)
        preencher(criado)
        setModo('editar')
        setSucesso(`Perfil criado com sucesso (id ${criado.id}).`)
      } else {
        const id = getPerfilAtivaId()
        const atualizado = await atualizarPerfil(id, dados)
        preencher(atualizado)
        setSucesso('Perfil atualizado com sucesso.')
      }
    } catch (e) {
      if (e.status === 404) {
        limparPerfilAtiva()
        setModo('criar')
      }
      setErro(e)
    } finally {
      setEnviando(false)
    }
  }

  async function mapear() {
    setMapeando(true)
    setErro(null)
    setSucesso('')
    try {
      const resultado = await competenciasTransferiveis(getPerfilAtivaId())
      setTransferiveis(resultado)
    } catch (e) {
      setErro(e)
    } finally {
      setMapeando(false)
    }
  }

  function comporCurriculo(perfil, transfer) {
    const linhas = [perfil.nome, perfil.email]
    if (perfil.telefone) linhas.push(perfil.telefone)
    if (perfil.cidade) linhas.push(perfil.cidade)
    if (perfil.resumo) linhas.push('', perfil.resumo)
    if (perfil.linkedin) linhas.push(`LinkedIn: ${perfil.linkedin}`)
    if (perfil.github) linhas.push(`GitHub: ${perfil.github}`)
    linhas.push(
      '',
      `Área atual: ${perfil.areaAtual || '—'}`,
      `Área alvo: ${perfil.areaAlvo || '—'}`,
      `Pretensão salarial: ${
        perfil.pretensaoSalarial ? brl(perfil.pretensaoSalarial) : '—'
      }`,
      `Disponibilidade: ${
        (perfil.disponibilidade || []).join(', ') || '—'
      }`,
    )
    if (transfer?.competenciasTransferiveis?.length) {
      linhas.push('', 'Competências transferíveis:')
      transfer.competenciasTransferiveis.forEach((m) => {
        linhas.push(
          `- ${m.de} → ${m.para} (${Math.round(m.similaridade * 100)}%)`,
        )
      })
    }
    return linhas.join('\n')
  }

  function gerarDoPerfil() {
    setCurriculoTexto(
      comporCurriculo(
        {
          nome: form.nome,
          email: form.email,
          telefone: form.telefone,
          cidade: form.cidade,
          resumo: form.resumo,
          linkedin: form.linkedin,
          github: form.github,
          areaAtual: form.areaAtual,
          areaAlvo: form.areaAlvo,
          pretensaoSalarial: form.pretensaoSalarial
            ? Number(form.pretensaoSalarial)
            : null,
          disponibilidade: form.disponibilidade,
        },
        transferiveis,
      ),
    )
  }

  async function carregarRecomendadas() {
    setCarregandoRec(true)
    setErroCurriculo(null)
    try {
      setRecomendadas(await vagasRecomendadas(getPerfilAtivaId()))
    } catch (e) {
      setErroCurriculo(e)
    } finally {
      setCarregandoRec(false)
    }
  }

  async function adaptar(vagaTitulo) {
    setAdaptando(true)
    setErroCurriculo(null)
    setVersao(null)
    try {
      setVersao(await blindarCurriculo(curriculoTexto, vagaTitulo || null))
    } catch (e) {
      setErroCurriculo(e)
    } finally {
      setAdaptando(false)
    }
  }

  function guardarRascunho() {
    sessionStorage.setItem('segundaCarreira.rascunhoCurriculo', curriculoTexto)
  }

  if (carregando) return <Carregando texto="Carregando perfil..." />

  const campo = (nome) => (erro?.fieldErrors || []).find((f) => f.field === nome)

  return (
    <section className="max-w-2xl">
      <h1 className="text-2xl font-semibold text-primaria">Meu Perfil</h1>

      {sucesso && <Feedback tipo="sucesso" titulo={sucesso} />}
      {erro && (
        <Feedback
          titulo={erro.title || 'Erro na operação'}
          detalhe={erro.detail}
          campos={erro.fieldErrors}
        />
      )}

      <form
        onSubmit={enviar}
        className="mt-4 space-y-4 rounded-lg border border-slate-200 bg-white p-5"
      >
        <div>
          <label htmlFor="nome" className="block text-sm font-medium">
            Nome completo *
          </label>
          <input
            id="nome"
            type="text"
            required
            value={form.nome}
            onChange={(e) => alterar('nome', e.target.value)}
            className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
          />
          {campo('nome') && (
            <p className="mt-1 text-sm text-perigo">{campo('nome').message}</p>
          )}
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-medium">
            E-mail *
          </label>
          <input
            id="email"
            type="email"
            required
            value={form.email}
            onChange={(e) => alterar('email', e.target.value)}
            className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
          />
          {campo('email') && (
            <p className="mt-1 text-sm text-perigo">{campo('email').message}</p>
          )}
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="telefone" className="block text-sm font-medium">
              Telefone
            </label>
            <input
              id="telefone"
              type="tel"
              value={form.telefone}
              onChange={(e) => alterar('telefone', e.target.value)}
              placeholder="Ex.: (11) 98888-1001"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
            />
            {campo('telefone') && (
              <p className="mt-1 text-sm text-perigo">
                {campo('telefone').message}
              </p>
            )}
          </div>
          <div>
            <label htmlFor="cidade" className="block text-sm font-medium">
              Cidade
            </label>
            <input
              id="cidade"
              type="text"
              value={form.cidade}
              onChange={(e) => alterar('cidade', e.target.value)}
              placeholder="Ex.: Sao Paulo - SP"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
            />
            {campo('cidade') && (
              <p className="mt-1 text-sm text-perigo">
                {campo('cidade').message}
              </p>
            )}
          </div>
        </div>

        <div>
          <label htmlFor="resumo" className="block text-sm font-medium">
            Resumo profissional
          </label>
          <textarea
            id="resumo"
            rows={4}
            maxLength={1500}
            value={form.resumo}
            onChange={(e) => alterar('resumo', e.target.value)}
            placeholder="Conte em poucas linhas quem voce e, sua experiencia e o que busca."
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-primaria focus:outline-none"
          />
          <p className="mt-1 text-xs text-texto-suave">
            {form.resumo.length}/1500 caracteres
          </p>
          {campo('resumo') && (
            <p className="mt-1 text-sm text-perigo">{campo('resumo').message}</p>
          )}
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="linkedin" className="block text-sm font-medium">
              LinkedIn
            </label>
            <input
              id="linkedin"
              type="url"
              value={form.linkedin}
              onChange={(e) => alterar('linkedin', e.target.value)}
              placeholder="https://linkedin.com/in/seu-perfil"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
            />
            {campo('linkedin') && (
              <p className="mt-1 text-sm text-perigo">
                {campo('linkedin').message}
              </p>
            )}
          </div>
          <div>
            <label htmlFor="github" className="block text-sm font-medium">
              GitHub
            </label>
            <input
              id="github"
              type="url"
              value={form.github}
              onChange={(e) => alterar('github', e.target.value)}
              placeholder="https://github.com/seu-usuario"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
            />
            {campo('github') && (
              <p className="mt-1 text-sm text-perigo">
                {campo('github').message}
              </p>
            )}
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label htmlFor="areaAtual" className="block text-sm font-medium">
              Área atual
            </label>
            <input
              id="areaAtual"
              type="text"
              list="areas"
              value={form.areaAtual}
              onChange={(e) => alterar('areaAtual', e.target.value)}
              placeholder="Ex.: Producao"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
            />
          </div>
          <div>
            <label htmlFor="areaAlvo" className="block text-sm font-medium">
              Área alvo *
            </label>
            <input
              id="areaAlvo"
              type="text"
              list="areas"
              required
              value={form.areaAlvo}
              onChange={(e) => alterar('areaAlvo', e.target.value)}
              placeholder="Ex.: Logistica"
              className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
            />
            {campo('areaAlvo') && (
              <p className="mt-1 text-sm text-perigo">
                {campo('areaAlvo').message}
              </p>
            )}
          </div>
        </div>
        <datalist id="areas">
          {AREAS.map((a) => (
            <option key={a} value={a} />
          ))}
        </datalist>

        <div>
          <label htmlFor="pretensao" className="block text-sm font-medium">
            Pretensão salarial (R$)
          </label>
          <input
            id="pretensao"
            type="number"
            min="0"
            step="100"
            value={form.pretensaoSalarial}
            onChange={(e) => alterar('pretensaoSalarial', e.target.value)}
            className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
          />
          {campo('pretensaoSalarial') && (
            <p className="mt-1 text-sm text-perigo">
              {campo('pretensaoSalarial').message}
            </p>
          )}
        </div>

        <fieldset>
          <legend className="text-sm font-medium">Disponibilidade</legend>
          <div className="mt-2 flex flex-wrap gap-4">
            {DISPONIBILIDADES.map((d) => (
              <label
                key={d}
                className="flex min-h-12 items-center gap-2 text-sm"
              >
                <input
                  type="checkbox"
                  checked={form.disponibilidade.includes(d)}
                  onChange={() => alternarDisponibilidade(d)}
                  className="h-5 w-5 accent-[#0F2942]"
                />
                {d.replace('_', ' ')}
              </label>
            ))}
          </div>
        </fieldset>

        <button
          type="submit"
          disabled={enviando}
          className="min-h-12 w-full rounded-lg bg-sucesso px-6 py-3 font-semibold text-white hover:bg-sucesso/90 disabled:opacity-60 sm:w-auto"
        >
          {enviando
            ? 'Salvando...'
            : modo === 'criar'
              ? 'Cadastrar perfil'
              : 'Salvar alterações'}
        </button>
      </form>

      {modo === 'editar' && (
        <div className="mt-6 rounded-lg border border-slate-200 bg-white p-5">
          <h2 className="font-semibold text-primaria">
            Competências transferíveis
          </h2>
          <button
            type="button"
            onClick={mapear}
            disabled={mapeando}
            className="mt-3 min-h-12 rounded-lg border-2 border-primaria px-5 py-2 font-semibold text-primaria hover:bg-primaria hover:text-white disabled:opacity-60"
          >
            {mapeando ? 'Mapeando...' : 'Mapear competências'}
          </button>

          {transferiveis && (
            <div className="mt-4">
              <p className="text-sm font-medium text-texto">
                Área alvo: {transferiveis.areaAlvo}
              </p>
              <ul className="mt-2 space-y-1 text-sm">
                {transferiveis.competenciasTransferiveis.map((m) => (
                  <li key={`${m.de}-${m.para}`}>
                    <span className="font-medium">{m.de}</span> → {m.para}{' '}
                    <span className="text-texto-suave">
                      ({Math.round(m.similaridade * 100)}%)
                    </span>
                  </li>
                ))}
              </ul>
              {transferiveis.gaps?.length > 0 && (
                <p className="mt-2 text-sm text-perigo">
                  Gaps: {transferiveis.gaps.join(', ')}
                </p>
              )}
              {transferiveis.trilhaSugerida && (
                <p className="mt-2 text-sm text-sucesso">
                  Trilha sugerida: {transferiveis.trilhaSugerida}
                </p>
              )}
            </div>
          )}
        </div>
      )}

      {modo === 'editar' && (
        <CurriculoEstruturado
          perfilId={getPerfilAtivaId()}
          onUsarTexto={setCurriculoTexto}
        />
      )}

      {modo === 'editar' && (
        <div className="mt-6 rounded-lg border border-slate-200 bg-white p-5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-semibold text-primaria">Currículo em texto</h2>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={gerarDoPerfil}
                className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white"
              >
                Gerar do perfil
              </button>
              <button
                type="button"
                onClick={() => navigator.clipboard?.writeText(curriculoTexto)}
                className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white"
              >
                Copiar
              </button>
            </div>
          </div>

          <label
            htmlFor="curriculoTexto"
            className="mt-3 block text-sm font-medium"
          >
            Texto do currículo (editável)
          </label>
          <textarea
            id="curriculoTexto"
            rows={10}
            value={curriculoTexto}
            onChange={(e) => setCurriculoTexto(e.target.value)}
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-primaria focus:outline-none"
          />

          <button
            type="button"
            onClick={() => adaptar(null)}
            disabled={adaptando || !curriculoTexto.trim()}
            className="mt-3 min-h-12 rounded-lg bg-sucesso px-5 py-2 font-semibold text-white hover:bg-sucesso/90 disabled:opacity-60"
          >
            {adaptando ? 'Adaptando...' : 'Adaptar currículo'}
          </button>
        </div>
      )}

      {modo === 'editar' && (
        <div className="mt-6 rounded-lg border border-slate-200 bg-white p-5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-semibold text-primaria">Comparar com vagas</h2>
            <button
              type="button"
              onClick={carregarRecomendadas}
              disabled={carregandoRec}
              className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white disabled:opacity-60"
            >
              {carregandoRec
                ? 'Carregando...'
                : recomendadas
                  ? 'Atualizar lista'
                  : 'Ver vagas recomendadas'}
            </button>
          </div>

          {erroCurriculo && (
            <div className="mt-3">
              <Feedback
                titulo={erroCurriculo.title || 'Erro na operação'}
                detalhe={erroCurriculo.detail}
              />
            </div>
          )}

          {carregandoRec && (
            <div className="mt-3">
              <Carregando texto="Carregando vagas..." />
            </div>
          )}

          {!carregandoRec && recomendadas?.length === 0 && (
            <div className="mt-3">
              <EstadoVazio titulo="Nenhuma vaga recomendada para o seu perfil" />
            </div>
          )}

          {!carregandoRec &&
            recomendadas?.map((r) => (
              <article
                key={r.vaga.id}
                className="mt-3 rounded-md border border-slate-200 p-4"
              >
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div>
                    <h3 className="font-semibold text-primaria">
                      {r.vaga.titulo}
                    </h3>
                    <p className="text-sm text-texto-suave">
                      {[r.vaga.empresa, r.vaga.area, r.vaga.modelo]
                        .filter(Boolean)
                        .join(' · ')}
                      {r.vaga.salario != null && ` · ${brl(r.vaga.salario)}`}
                    </p>
                  </div>
                  <span className="rounded-full bg-primaria/10 px-3 py-1 text-sm font-semibold text-primaria">
                    {Math.round(r.score * 100)}% de aderência
                  </span>
                </div>

                <ul className="mt-2 flex flex-wrap gap-2 text-sm">
                  {r.competenciasAtendidas.map((c) => (
                    <li
                      key={`ok-${c}`}
                      className="rounded-full bg-sucesso/10 px-2 py-0.5 text-sucesso"
                    >
                      {c}
                    </li>
                  ))}
                  {r.competenciasFaltantes.map((c) => (
                    <li
                      key={`falta-${c}`}
                      className="rounded-full bg-perigo/10 px-2 py-0.5 text-perigo"
                    >
                      {c}
                    </li>
                  ))}
                </ul>

                <button
                  type="button"
                  onClick={() =>
                    setVagaAberta((atual) =>
                      atual === r.vaga.id ? null : r.vaga.id,
                    )
                  }
                  className="mt-3 min-h-12 rounded border-2 border-primaria px-4 py-1 text-sm font-semibold text-primaria"
                >
                  {vagaAberta === r.vaga.id ? 'Fechar' : 'Ver detalhes'}
                </button>

                {vagaAberta === r.vaga.id && (
                  <div className="mt-3 border-t border-slate-100 pt-3">
                    <p className="text-sm">{r.vaga.descricao}</p>
                    <p className="mt-2 text-sm font-medium">Requisitos</p>
                    <ul className="mt-1 list-disc space-y-1 pl-5 text-sm text-texto-suave">
                      {r.vaga.requisitos.map((req) => (
                        <li key={req}>{req}</li>
                      ))}
                    </ul>
                    <div className="mt-3 flex flex-wrap gap-2">
                      <button
                        type="button"
                        onClick={() => adaptar(r.vaga.titulo)}
                        disabled={adaptando || !curriculoTexto.trim()}
                        className="min-h-12 rounded-lg bg-sucesso px-4 py-2 text-sm font-semibold text-white hover:bg-sucesso/90 disabled:opacity-60"
                      >
                        {adaptando ? 'Adaptando...' : 'Adaptar currículo'}
                      </button>
                      <Link
                        to={`/escudo?vagaAlvo=${encodeURIComponent(r.vaga.titulo)}`}
                        onClick={guardarRascunho}
                        className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white"
                      >
                        Abrir no Escudo
                      </Link>
                    </div>
                  </div>
                )}
              </article>
            ))}
        </div>
      )}

      {modo === 'editar' && versao && (
        <div className="mt-6 rounded-lg border border-slate-200 bg-white p-5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h2 className="font-semibold text-primaria">
              Versão {versao.tipo} do seu currículo
            </h2>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => navigator.clipboard?.writeText(versao.conteudo)}
                className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white"
              >
                Copiar texto
              </button>
              <button
                type="button"
                onClick={() => {
                  setCurriculoTexto(versao.conteudo)
                  setVersao(null)
                }}
                className="min-h-12 rounded-lg bg-sucesso px-4 py-2 text-sm font-semibold text-white hover:bg-sucesso/90"
              >
                Usar esta versão
              </button>
            </div>
          </div>

          <p className="mt-2 text-sm text-texto-suave">
            {versao.regrasAplicadas.length} regra(s) aplicada(s) nesta versão.
          </p>

          <pre className="mt-3 whitespace-pre-wrap rounded-md bg-slate-50 p-4 text-sm text-texto">
            {versao.conteudo}
          </pre>

          {versao.regrasAplicadas.length > 0 && (
            <ul className="mt-4 space-y-1 text-sm text-texto-suave">
              {versao.regrasAplicadas.map((rg) => (
                <li key={rg.regra}>
                  <span className="font-medium text-texto">{rg.regra}</span> —{' '}
                  {rg.acao}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </section>
  )
}
