export default function Feedback({
  tipo = 'erro',
  titulo,
  detalhe,
  campos = [],
  acao,
}) {
  const ehErro = tipo === 'erro'
  return (
    <div
      role={ehErro ? 'alert' : 'status'}
      className={`mt-4 rounded-lg border p-4 text-sm ${
        ehErro
          ? 'border-perigo bg-perigo/5 text-perigo'
          : 'border-sucesso bg-sucesso/5 text-sucesso'
      }`}
    >
      <p className="font-semibold">{titulo}</p>
      {detalhe && <p className="mt-1">{detalhe}</p>}
      {campos.length > 0 && (
        <ul className="mt-2 list-disc pl-5">
          {campos.map((c) => (
            <li key={c.field}>
              <span className="font-medium">{c.field}</span>: {c.message}
            </li>
          ))}
        </ul>
      )}
      {acao}
    </div>
  )
}
