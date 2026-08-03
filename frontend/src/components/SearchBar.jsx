import { Search } from 'lucide-react'
import { useState } from 'react'

export default function SearchBar({ onSearch, loading, suggestedTopics = [] }) {
  const [topic, setTopic] = useState('')
  const [amount, setAmount] = useState(5)

  const go = event => {
    event.preventDefault()
    if (topic.trim()) onSearch(topic, amount)
  }

  return (
    <form onSubmit={go} className="mx-auto max-w-4xl">
      <div className="flex items-center gap-2 rounded-2xl border border-slate-200 bg-white p-2 shadow-soft focus-within:border-brand-500 dark:border-slate-700 dark:bg-slate-900">
        <Search className="ml-3 shrink-0 text-slate-400" />
        <input autoFocus className="min-w-0 flex-1 bg-transparent px-2 py-3 text-lg" placeholder="Digite um assunto..." value={topic} onChange={event => setTopic(event.target.value)} />
        <select aria-label="Quantidade" className="rounded-lg bg-slate-100 px-2 py-3 text-sm dark:bg-slate-800" value={amount} onChange={event => setAmount(Number(event.target.value))}>
          <option>5</option>
          <option>10</option>
          <option>20</option>
        </select>
        <button disabled={loading || !topic.trim()} className="btn-primary px-5">Gerar</button>
      </div>

      {suggestedTopics.length > 0 ? (
        <div className="mt-6 text-left">
          <p className="mb-3 text-sm font-semibold text-slate-600 dark:text-slate-300">Conteúdos do Perfil 3 — Desenvolvimento de Software</p>
          <div className="flex max-h-44 flex-wrap gap-2 overflow-y-auto pr-1">
            {suggestedTopics.map(suggestion => (
              <button
                key={suggestion}
                type="button"
                onClick={() => setTopic(suggestion)}
                className={`rounded-full border px-3 py-1.5 text-sm transition ${topic === suggestion ? 'border-brand-500 bg-brand-50 text-brand-700 dark:bg-brand-950/40 dark:text-brand-300' : 'border-slate-200 bg-white text-slate-600 hover:border-brand-400 hover:text-brand-600 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-300'}`}
              >
                {suggestion}
              </button>
            ))}
          </div>
        </div>
      ) : <p className="mt-4 text-center text-sm text-slate-500">Envie um edital para carregar os tópicos do Perfil 3.</p>}
    </form>
  )
}
