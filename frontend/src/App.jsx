import { ClipboardList, History, Library, Menu, Moon, Sun, Target, X } from 'lucide-react'
import { useState } from 'react'
import { useDarkMode } from './hooks/useDarkMode'
import HistoryPage from './pages/HistoryPage'
import LibraryPage from './pages/LibraryPage'
import NoticesPage from './pages/NoticesPage'
import QuizPage from './pages/QuizPage'

const pages = {
  quiz: QuizPage,
  history: HistoryPage,
  library: LibraryPage,
  notices: NoticesPage
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

  const navItem = (key, label, Icon) => (
    <button key={key} onClick={() => go(key)} className={`btn-ghost w-full justify-start md:w-auto ${page === key ? 'bg-slate-100 dark:bg-slate-800' : ''}`}>
      {Icon && <Icon size={17} />} {label}
    </button>
  )

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-50 border-b border-slate-200/70 bg-white/95 backdrop-blur dark:border-slate-800 dark:bg-slate-950/95">
        <div className="mx-auto flex h-16 max-w-7xl items-center px-4">
          <button onClick={() => go('quiz')} className="flex items-center gap-2 text-lg font-black" aria-label="Ir para o início do Simula+">
            <span className="rounded-xl bg-brand-600 p-2 text-white"><Target size={21} /></span>
            <span>Simula<span className="text-brand-500">+</span></span>
          </button>
          <nav className="ml-auto hidden items-center gap-1 md:flex">
            {navItem('quiz', 'Simulados')}
            {navItem('history', 'Histórico', History)}
            {navItem('library', 'Biblioteca', Library)}
            {navItem('notices', 'Editais', ClipboardList)}
            <button aria-label="Alternar tema" className="btn-ghost" onClick={() => setDark(!dark)}>
              {dark ? <Sun size={18} /> : <Moon size={18} />}
            </button>
          </nav>
          <button aria-label="Abrir menu" className="btn-ghost ml-auto md:hidden" onClick={() => setMenu(!menu)}>
            {menu ? <X /> : <Menu />}
          </button>
        </div>
        {menu && (
          <nav className="space-y-2 border-t border-slate-200 p-4 dark:border-slate-800 md:hidden">
            {navItem('quiz', 'Simulados')}
            {navItem('history', 'Histórico', History)}
            {navItem('library', 'Biblioteca', Library)}
            {navItem('notices', 'Editais', ClipboardList)}
            <button className="btn-ghost w-full justify-start" onClick={() => setDark(!dark)}>Alternar tema</button>
          </nav>
        )}
      </header>
      <Page />
    </div>
  )
}
