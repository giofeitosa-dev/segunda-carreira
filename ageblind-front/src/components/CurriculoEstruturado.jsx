import { useEffect, useState } from 'react'
import {
  enviarArquivoCurriculo,
  obterCurriculo,
  salvarCurriculo,
} from '../services/carreiraService'
import Carregando from './Carregando'
import Feedback from './Feedback'

const uid = () => crypto.randomUUID()

const SECOES = [
  {
    chave: 'experiencias',
    titulo: 'Experiências',
    adicionar: 'Adicionar experiência',
    novo: () => ({
      uid: uid(),
      cargo: '',
      empresa: '',
      anoInicio: '',
      anoFim: '',
      descricao: '',
    }),
    campos: [
      { campo: 'cargo', rotulo: 'Cargo', obrigatorio: true },
      { campo: 'empresa', rotulo: 'Empresa', obrigatorio: true },
      {
        campo: 'anoInicio',
        rotulo: 'Ano de início',
        tipo: 'number',
        min: 1900,
        obrigatorio: true,
      },
      {
        campo: 'anoFim',
        rotulo: 'Ano de fim (vazio = em andamento)',
        tipo: 'number',
      },
      {
        campo: 'descricao',
        rotulo: 'Descrição',
        tipo: 'textarea',
        largo: true,
      },
    ],
  },
  {
    chave: 'formacoes',
    titulo: 'Formações',
    adicionar: 'Adicionar formação',
    novo: () => ({
      uid: uid(),
      curso: '',
      instituicao: '',
      anoConclusao: '',
      descricao: '',
    }),
    campos: [
      { campo: 'curso', rotulo: 'Curso', obrigatorio: true },
      { campo: 'instituicao', rotulo: 'Instituição', obrigatorio: true },
      {
        campo: 'anoConclusao',
        rotulo: 'Ano de conclusão (vazio = em andamento)',
        tipo: 'number',
      },
      {
        campo: 'descricao',
        rotulo: 'Descrição',
        tipo: 'textarea',
        largo: true,
      },
    ],
  },
  {
    chave: 'projetos',
    titulo: 'Projetos',
    adicionar: 'Adicionar projeto',
    novo: () => ({ uid: uid(), nome: '', descricao: '', link: '' }),
    campos: [
      { campo: 'nome', rotulo: 'Nome', obrigatorio: true },
      { campo: 'link', rotulo: 'Link' },
      {
        campo: 'descricao',
        rotulo: 'Descrição',
        tipo: 'textarea',
        largo: true,
      },
    ],
  },
]

function paraForm(c) {
  return {
    experiencias: (c.experiencias || []).map((e) => ({
      uid: uid(),
      cargo: e.cargo || '',
      empresa: e.empresa || '',
      anoInicio: e.anoInicio != null ? String(e.anoInicio) : '',
      anoFim: e.anoFim != null ? String(e.anoFim) : '',
      descricao: e.descricao || '',
    })),
    formacoes: (c.formacoes || []).map((f) => ({
      uid: uid(),
      curso: f.curso || '',
      instituicao: f.instituicao || '',
      anoConclusao: f.anoConclusao != null ? String(f.anoConclusao) : '',
      descricao: f.descricao || '',
    })),
    projetos: (c.projetos || []).map((p) => ({
      uid: uid(),
      nome: p.nome || '',
      descricao: p.descricao || '',
      link: p.link || '',
    })),
  }
}

function paraRequest(f) {
  const num = (v) => (v === '' || v == null ? null : Number(v))
  return {
    experiencias: f.experiencias
      .filter((e) => e.cargo.trim() || e.empresa.trim() || e.anoInicio !== '')
      .map((e) => ({
        cargo: e.cargo.trim(),
        empresa: e.empresa.trim(),
        anoInicio: num(e.anoInicio),
        anoFim: num(e.anoFim),
        descricao: e.descricao.trim() || null,
      })),
    formacoes: f.formacoes
      .filter(
        (x) => x.curso.trim() || x.instituicao.trim() || x.anoConclusao !== '',
      )
      .map((x) => ({
        curso: x.curso.trim(),
        instituicao: x.instituicao.trim(),
        anoConclusao: num(x.anoConclusao),
        descricao: x.descricao.trim() || null,
      })),
    projetos: f.projetos
      .filter((p) => p.nome.trim() || p.descricao.trim() || p.link.trim())
      .map((p) => ({
        nome: p.nome.trim(),
        descricao: p.descricao.trim() || null,
        link: p.link.trim() || null,
      })),
  }
}

function validar(f) {
  const expInvalida = f.experiencias.some(
    (e) =>
      (e.cargo.trim() || e.empresa.trim() || e.anoFim !== '') &&
      (!e.anoInicio.trim() || Number(e.anoInicio) < 1900),
  )
  if (expInvalida) {
    return 'Informe o ano de início (mínimo 1900) em todas as experiências.'
  }
  return null
}

function comporTexto(f) {
  const linhas = []
  const exps = f.experiencias.filter((e) => e.cargo.trim())
  if (exps.length) {
    linhas.push('EXPERIÊNCIAS')
    exps.forEach((e) => {
      const periodo = e.anoInicio
        ? e.anoFim
          ? `${e.anoInicio}–${e.anoFim}`
          : `${e.anoInicio} – atual`
        : ''
      linhas.push(
        `- ${e.cargo} · ${e.empresa}${periodo ? ` (${periodo})` : ''}`,
      )
      if (e.descricao.trim()) linhas.push(`  ${e.descricao.trim()}`)
    })
    linhas.push('')
  }
  const forms = f.formacoes.filter((x) => x.curso.trim())
  if (forms.length) {
    linhas.push('FORMAÇÕES')
    forms.forEach((x) => {
      const ano = x.anoConclusao ? ` (${x.anoConclusao})` : ''
      linhas.push(`- ${x.curso} · ${x.instituicao}${ano}`)
      if (x.descricao.trim()) linhas.push(`  ${x.descricao.trim()}`)
    })
    linhas.push('')
  }
  const projs = f.projetos.filter((p) => p.nome.trim())
  if (projs.length) {
    linhas.push('PROJETOS')
    projs.forEach((p) => {
      linhas.push(`- ${p.nome}${p.link ? ` — ${p.link}` : ''}`)
      if (p.descricao.trim()) linhas.push(`  ${p.descricao.trim()}`)
    })
  }
  return linhas.join('\n').trim()
}

function SecaoCurriculo({
  secao,
  itens,
  aoAlterar,
  aoAdicionar,
  aoRemover,
}) {
  return (
    <fieldset className="mt-5">
      <legend className="font-semibold text-primaria">{secao.titulo}</legend>

      {itens.length === 0 && (
        <p className="mt-2 text-sm text-texto-suave">Nenhum item.</p>
      )}

      {itens.map((item) => (
        <div
          key={item.uid}
          className="mt-3 rounded-md border border-slate-200 p-4"
        >
          <div className="grid gap-3 sm:grid-cols-2">
            {secao.campos.map((c) => (
              <div key={c.campo} className={c.largo ? 'sm:col-span-2' : ''}>
                <label
                  htmlFor={`${item.uid}-${c.campo}`}
                  className="block text-sm font-medium"
                >
                  {c.rotulo}
                  {c.obrigatorio ? ' *' : ''}
                </label>
                {c.tipo === 'textarea' ? (
                  <textarea
                    id={`${item.uid}-${c.campo}`}
                    rows={2}
                    value={item[c.campo]}
                    onChange={(e) =>
                      aoAlterar(item.uid, c.campo, e.target.value)
                    }
                    className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 focus:border-primaria focus:outline-none"
                  />
                ) : (
                  <input
                    id={`${item.uid}-${c.campo}`}
                    type={c.tipo || 'text'}
                    required={c.obrigatorio}
                    min={c.min}
                    value={item[c.campo]}
                    onChange={(e) =>
                      aoAlterar(item.uid, c.campo, e.target.value)
                    }
                    className="mt-1 min-h-12 w-full rounded-lg border border-slate-300 px-3 focus:border-primaria focus:outline-none"
                  />
                )}
              </div>
            ))}
          </div>
          <button
            type="button"
            onClick={() => aoRemover(item.uid)}
            className="mt-3 min-h-12 rounded-lg border-2 border-perigo px-4 py-2 text-sm font-semibold text-perigo hover:bg-perigo hover:text-white"
          >
            Remover
          </button>
        </div>
      ))}

      <button
        type="button"
        onClick={aoAdicionar}
        className="mt-3 min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white"
      >
        {secao.adicionar}
      </button>
    </fieldset>
  )
}

export default function CurriculoEstruturado({ perfilId, onUsarTexto }) {
  const [curriculo, setCurriculo] = useState(null)
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [sucesso, setSucesso] = useState('')
  const [erro, setErro] = useState(null)

  useEffect(() => {
    let ativo = true
    obterCurriculo(perfilId)
      .then((c) => {
        if (ativo) setCurriculo(paraForm(c))
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
  }, [perfilId])

  function alterarItem(chave, itemUid, campo, valor) {
    setCurriculo((c) => ({
      ...c,
      [chave]: c[chave].map((it) =>
        it.uid === itemUid ? { ...it, [campo]: valor } : it,
      ),
    }))
  }

  function adicionarItem(secao) {
    setCurriculo((c) => ({ ...c, [secao.chave]: [...c[secao.chave], secao.novo()] }))
  }

  function removerItem(chave, itemUid) {
    setCurriculo((c) => ({
      ...c,
      [chave]: c[chave].filter((it) => it.uid !== itemUid),
    }))
  }

  async function salvar() {
    const aviso = validar(curriculo)
    if (aviso) {
      setSucesso('')
      setErro({ title: aviso })
      return
    }
    setSalvando(true)
    setSucesso('')
    setErro(null)
    try {
      const resp = await salvarCurriculo(perfilId, paraRequest(curriculo))
      setCurriculo(paraForm(resp))
      setSucesso('Currículo estruturado salvo.')
    } catch (e) {
      setErro(e)
    } finally {
      setSalvando(false)
    }
  }

  async function enviarArquivo(e) {
    const arquivo = e.target.files?.[0]
    e.target.value = ''
    if (!arquivo) return
    setEnviando(true)
    setSucesso('')
    setErro(null)
    try {
      const fd = new FormData()
      fd.append('arquivo', arquivo)
      const resp = await enviarArquivoCurriculo(perfilId, fd)
      setCurriculo(paraForm(resp))
      setSucesso(`Currículo importado de "${arquivo.name}".`)
    } catch (err) {
      setErro(err)
    } finally {
      setEnviando(false)
    }
  }

  if (carregando) {
    return (
      <div className="mt-6 rounded-lg border border-slate-200 bg-white p-5">
        <Carregando texto="Carregando currículo..." />
      </div>
    )
  }

  const texto = curriculo ? comporTexto(curriculo) : ''

  return (
    <div className="mt-6 rounded-lg border border-slate-200 bg-white p-5">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="font-semibold text-primaria">Currículo estruturado</h2>
        <div className="flex flex-wrap gap-2">
          <label className="inline-flex min-h-12 cursor-pointer items-center rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white">
            {enviando ? 'Enviando...' : 'Enviar arquivo'}
            <input
              type="file"
              accept=".pdf,.docx,.txt"
              className="sr-only"
              onChange={enviarArquivo}
              disabled={enviando}
            />
          </label>
          <button
            type="button"
            onClick={salvar}
            disabled={salvando || enviando}
            className="min-h-12 rounded-lg bg-sucesso px-5 py-2 text-sm font-semibold text-white hover:bg-sucesso/90 disabled:opacity-60"
          >
            {salvando ? 'Salvando...' : 'Salvar currículo'}
          </button>
        </div>
      </div>

      {sucesso && <Feedback tipo="sucesso" titulo={sucesso} />}
      {erro && (
        <Feedback
          titulo={erro.title || 'Erro na operação'}
          detalhe={erro.detail}
          campos={erro.fieldErrors}
        />
      )}

      {curriculo &&
        SECOES.map((secao) => (
          <SecaoCurriculo
            key={secao.chave}
            secao={secao}
            itens={curriculo[secao.chave]}
            aoAlterar={(itemUid, campo, valor) =>
              alterarItem(secao.chave, itemUid, campo, valor)
            }
            aoAdicionar={() => adicionarItem(secao)}
            aoRemover={(itemUid) => removerItem(secao.chave, itemUid)}
          />
        ))}

      {texto && (
        <div className="mt-5 border-t border-slate-100 pt-4">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <h3 className="text-sm font-semibold text-texto">Prévia do texto</h3>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => navigator.clipboard?.writeText(texto)}
                className="min-h-12 rounded-lg border-2 border-primaria px-4 py-2 text-sm font-semibold text-primaria hover:bg-primaria hover:text-white"
              >
                Copiar
              </button>
              <button
                type="button"
                onClick={() => onUsarTexto?.(texto)}
                className="min-h-12 rounded-lg bg-sucesso px-4 py-2 text-sm font-semibold text-white hover:bg-sucesso/90"
              >
                Usar no texto do currículo
              </button>
            </div>
          </div>
          <pre className="mt-3 whitespace-pre-wrap rounded-md bg-slate-50 p-4 text-sm text-texto">
            {texto}
          </pre>
        </div>
      )}
    </div>
  )
}
