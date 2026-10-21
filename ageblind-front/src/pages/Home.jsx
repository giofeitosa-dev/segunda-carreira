import { Link } from 'react-router-dom'

export default function Home() {
  return (
    <section className="py-10 text-center max-w-3xl mx-auto">
      <h1 className="text-3xl sm:text-4xl font-semibold text-primaria leading-tight">
        A API que contrata por competência, não por ano de nascimento.
      </h1>

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
    </section>
  )
}
