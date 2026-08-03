import { BookOpenCheck, ChevronLeft, ChevronRight, History } from 'lucide-react'
import ErrorState from '../components/ErrorState'
import HistoryCard from '../components/HistoryCard'
import Spinner from '../components/Spinner'
import { useHistory } from '../hooks/useHistory'

export default function HistoryPage() {
  const history = useHistory()
  const firstItemNumber = history.page * history.size

  return (
    <main className="mx-auto max-w-4xl px-4 py-10 sm:py-14">
      <section className="mb-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="mb-2 flex items-center gap-2 text-sm font-bold text-brand-500">
            <History size={17} /> Histórico
          </p>
          <h1 className="text-3xl font-black tracking-tight sm:text-4xl">Questões respondidas</h1>
          <p className="mt-2 text-slate-500">Revise suas respostas, o gabarito e as explicações.</p>
        </div>
        {!history.loading && (
          <div className="rounded-2xl border border-slate-200 bg-white px-5 py-3 text-center dark:border-slate-800 dark:bg-slate-900">
            <strong className="block text-2xl">{history.totalElements}</strong>
            <span className="text-xs text-slate-500">respostas registradas</span>
          </div>
        )}
      </section>

      {history.loading && <Spinner label="Carregando seu histórico..." />}
      {history.error && <ErrorState message={history.error} onRetry={history.retry} />}

      {!history.loading && !history.error && history.items.length === 0 && (
        <section className="panel py-20 text-center">
          <BookOpenCheck className="mx-auto mb-4 text-slate-300" size={42} />
          <h2 className="text-lg font-bold">Nenhuma questão respondida ainda</h2>
          <p className="mt-2 text-sm text-slate-500">Suas próximas respostas aparecerão aqui automaticamente.</p>
        </section>
      )}

      {!history.loading && !history.error && history.items.length > 0 && (
        <>
          <section className="space-y-6">
            {history.items.map((item, index) => (
              <HistoryCard key={item.id} item={item} index={firstItemNumber + index} />
            ))}
          </section>

          {history.totalPages > 1 && (
            <nav className="mt-8 flex items-center justify-center gap-3" aria-label="Paginação do histórico">
              <button className="btn-ghost" disabled={history.page === 0} onClick={() => history.load(history.page - 1)}>
                <ChevronLeft size={18} /> Anterior
              </button>
              <span className="text-sm text-slate-500">Página {history.page + 1} de {history.totalPages}</span>
              <button className="btn-ghost" disabled={history.page + 1 >= history.totalPages} onClick={() => history.load(history.page + 1)}>
                Próxima <ChevronRight size={18} />
              </button>
            </nav>
          )}
        </>
      )}
    </main>
  )
}
