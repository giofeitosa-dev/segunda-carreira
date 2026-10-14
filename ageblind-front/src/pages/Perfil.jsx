import { useEffect, useState } from 'react'
import {
  atualizarPerfil,
  competenciasTransferiveis,
  criarPerfil,
  getPerfilAtivaId,
  limparPerfilAtiva,
  obterPerfil,
  setPerfilAtivaId,
} from '../services/carreiraService'
import Carregando from '../components/Carregando'
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

const FORM_INICIAL = {
  nome: '',
  email: '',
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
    }
    try {
      if (modo === 'criar') {
        const criado = await criarPerfil(dados)
        setPerfilAtivaId(criado.id)
        preencher(criado)
        setModo('editar')
        setSucesso(
          `Perfil criado com sucesso (id ${criado.id}). Nenhum dado de idade foi solicitado.`,
        )
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

  if (carregando) return <Carregando texto="Carregando perfil..." />

  const campo = (nome) => (erro?.fieldErrors || []).find((f) => f.field === nome)

  return (
    <section className="max-w-2xl">
      <h1 className="text-2xl font-semibold text-primaria">Meu Perfil</h1>
      <p className="mt-1 rounded-md bg-primaria/5 px-3 py-2 text-sm text-texto-suave">
        Nenhum dado de idade ou data de nascimento é solicitado ou armazenado.
      </p>

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
          <p className="mt-1 text-sm text-texto-suave">
            Mapeia sua experiência da área atual para a área alvo — sem usar
            idade.
          </p>
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
    </section>
  )
}
