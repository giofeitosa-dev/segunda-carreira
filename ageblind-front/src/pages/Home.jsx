import { Link } from 'react-router-dom'

export default function Home() {
  return (
    <section className="py-10 text-center max-w-3xl mx-auto">
      <p className="text-sm font-semibold text-sucesso uppercase tracking-wide">
        ODS 5 · ODS 10
      </p>
      <h1 className="mt-3 text-3xl sm:text-4xl font-semibold text-primaria leading-tight">
        A API que contrata por competência, não por ano de nascimento.
      </h1>
      <p className="mt-4 text-lg text-texto-suave">
        Segunda Carreira conecta profissionais em recolocação a vagas, trilhas e
        mentorias — e dá ao RH um Escudo Anti-Etarismo para auditar currículos e
        vagas sem viés de idade.
      </p>

      <div className="mt-8 flex flex-col sm:flex-row gap-4 justify-center">
        <Link
          to="/perfil"
          className="rounded-lg bg-sucesso px-6 py-3 min-h-12 font-semibold text-white hover:bg-sucesso/90 transition-colors"
        >
          Sou profissional em recolocação
        </Link>
        <Link
          to="/rh/painel"
          className="rounded-lg border-2 border-primaria px-6 py-3 min-h-12 font-semibold text-primaria hover:bg-primaria hover:text-white transition-colors"
        >
          Sou recrutador / RH
        </Link>
      </div>

      <div className="mt-10 grid gap-4 sm:grid-cols-3 text-left">
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <h2 className="font-semibold text-primaria">Vagas por competências</h2>
          <p className="mt-1 text-sm text-texto-suave">
            Recomendação ranqueada só por habilidades — idade fica fora do
            ranking.
          </p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <h2 className="font-semibold text-primaria">Escudo Anti-Etarismo</h2>
          <p className="mt-1 text-sm text-texto-suave">
            Currículo cego a proxies de idade e auditoria de linguagem de vagas
            com base legal.
          </p>
        </div>
        <div className="rounded-lg border border-slate-200 bg-white p-4">
          <h2 className="font-semibold text-primaria">Decisão revisável</h2>
          <p className="mt-1 text-sm text-texto-suave">
            Pedido de revisão humana com protocolo, conforme o art. 20 da LGPD.
          </p>
        </div>
      </div>
    </section>
  )
}
