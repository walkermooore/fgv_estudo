import { BookOpenCheck, History, Library, Menu, Moon, Sun, X } from 'lucide-react'
import { useState } from 'react'
import { useDarkMode } from './hooks/useDarkMode'
import HistoryPage from './pages/HistoryPage'
import LibraryPage from './pages/LibraryPage'
import QuizPage from './pages/QuizPage'

const pages = {
  quiz: QuizPage,
  history: HistoryPage,
  library: LibraryPage
}

export default function App() {
  const [page, setPage] = useState('quiz')
  const [dark, setDark] = useDarkMode()
  const [menu, setMenu] = useState(false)
  const Page = pages[page]

  const go = nextPage => {
    setPage(nextPage)
    setMenu(false)
  }

  const navigation = (
    <>
      <button onClick={() => go('quiz')} className={`btn-ghost ${page === 'quiz' ? 'bg-slate-100 dark:bg-slate-800' : ''}`}>
        Simulados
      </button>
      <button onClick={() => go('history')} className={`btn-ghost ${page === 'history' ? 'bg-slate-100 dark:bg-slate-800' : ''}`}>
        <History size={17} /> Histórico
      </button>
      <button onClick={() => go('library')} className={`btn-ghost ${page === 'library' ? 'bg-slate-100 dark:bg-slate-800' : ''}`}>
        <Library size={17} /> Biblioteca
      </button>
    </>
  )

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-50 border-b border-slate-200/70 bg-white/90 backdrop-blur dark:border-slate-800 dark:bg-slate-950/90">
        <div className="mx-auto flex h-16 max-w-7xl items-center px-4">
          <button onClick={() => go('quiz')} className="flex items-center gap-2 font-black">
            <span className="rounded-xl bg-brand-600 p-2 text-white"><BookOpenCheck size={20} /></span>
            FGV <span className="text-brand-500">Study Hub</span>
          </button>
          <nav className="ml-auto hidden items-center gap-1 sm:flex">
            {navigation}
            <button aria-label="Alternar tema" className="btn-ghost" onClick={() => setDark(!dark)}>
              {dark ? <Sun size={18} /> : <Moon size={18} />}
            </button>
          </nav>
          <button aria-label="Abrir menu" className="btn-ghost ml-auto sm:hidden" onClick={() => setMenu(!menu)}>
            {menu ? <X /> : <Menu />}
          </button>
        </div>
        {menu && (
          <nav className="space-y-2 border-t border-slate-200 p-4 dark:border-slate-800 sm:hidden">
            {navigation}
            <button className="btn-ghost w-full" onClick={() => setDark(!dark)}>Alternar tema</button>
          </nav>
        )}
      </header>
      <Page />
    </div>
  )
}
