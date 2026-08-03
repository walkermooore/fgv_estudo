import { CheckCircle2, XCircle } from 'lucide-react'

const letters = ['A', 'B', 'C', 'D', 'E']
const dateFormatter = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short'
})

export default function HistoryCard({ item, index }) {
  return (
    <article className="panel overflow-hidden">
      <header className="border-b border-slate-100 p-5 dark:border-slate-800 sm:p-6">
        <div className="mb-3 flex flex-wrap items-center justify-between gap-2 text-xs">
          <div className="flex items-center gap-2">
            <span className="font-bold uppercase tracking-widest text-brand-500">
              Questão {index + 1}
            </span>
            <span className="rounded-full bg-slate-100 px-2.5 py-1 text-slate-600 dark:bg-slate-800 dark:text-slate-300">
              {item.topic}
            </span>
          </div>
          <time className="text-slate-400" dateTime={item.answeredAt}>
            {dateFormatter.format(new Date(item.answeredAt))}
          </time>
        </div>
        <p className="leading-7">{item.statement}</p>
      </header>

      <div className="space-y-2.5 p-5 sm:p-6">
        {item.options.map((option, optionIndex) => {
          const selected = optionIndex === item.selectedAnswer
          const correct = optionIndex === item.correctIndex
          let style = 'border-slate-200 text-slate-600 dark:border-slate-700 dark:text-slate-300'
          if (correct) style = 'border-emerald-500 bg-emerald-50 text-emerald-900 dark:bg-emerald-950/50 dark:text-emerald-100'
          else if (selected) style = 'border-rose-500 bg-rose-50 text-rose-900 dark:bg-rose-950/50 dark:text-rose-100'

          return (
            <div key={optionIndex} className={`flex items-start gap-3 rounded-xl border p-3.5 ${style}`}>
              <span className="font-bold">{letters[optionIndex]}</span>
              <span className="flex-1">{option}</span>
              {selected && <span className="text-xs font-bold uppercase">Sua resposta</span>}
            </div>
          )
        })}

        <div className={`mt-5 rounded-2xl p-4 ${item.correct ? 'bg-emerald-50 dark:bg-emerald-950/40' : 'bg-rose-50 dark:bg-rose-950/40'}`}>
          <div className="flex items-center gap-2 font-bold">
            {item.correct ? <><CheckCircle2 size={20} />Você acertou</> : <><XCircle size={20} />Você errou</>}
          </div>
          <details className="mt-3">
            <summary className="cursor-pointer text-sm font-semibold">Ver explicação completa</summary>
            <p className="mt-3 whitespace-pre-wrap text-sm leading-6">{item.explanation}</p>
          </details>
        </div>
      </div>
    </article>
  )
}
