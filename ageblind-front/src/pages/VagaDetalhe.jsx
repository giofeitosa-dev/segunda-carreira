import { useParams } from 'react-router-dom'

export default function VagaDetalhe() {
  const { id } = useParams()
  return (
    <section>
      <h1 className="text-2xl font-semibold text-primaria">Vaga {id}</h1>
      <p className="mt-2 text-texto-suave">
        Detalhe, inscrição e lembrete — em construção (Fase 3).
      </p>
    </section>
  )
}
