import ErrorState from '../components/ErrorState'
import QuizBoard from '../components/QuizBoard'
import SearchBar from '../components/SearchBar'
import Spinner from '../components/Spinner'
import { useQuiz } from '../hooks/useQuiz'

export default function QuizPage() {
  const quiz = useQuiz()

  return (
    <main>
      <section className="px-4 pb-10 pt-12 text-center sm:pt-16">
        <h1 className="text-3xl font-black tracking-tight sm:text-4xl">Crie seu simulado</h1>
        <p className="mx-auto mb-8 mt-3 max-w-lg text-slate-500">Digite um assunto e escolha quantas questões deseja responder.</p>
        <SearchBar onSearch={quiz.search} loading={quiz.loading} />
      </section>
      <section className="mx-auto max-w-4xl px-4 pb-20">
        {quiz.loading && <><div className="panel h-56 animate-pulse bg-slate-100 dark:bg-slate-900" /><Spinner label="Preparando seu simulado..." /></>}
        {quiz.error && <ErrorState message={quiz.error} onRetry={quiz.retry} />}
        {!quiz.loading && !quiz.error && <QuizBoard questions={quiz.questions} />}
      </section>
    </main>
  )
}
