import { Search } from 'lucide-react'
import { useState } from 'react'
import ChatPanel from '../components/ChatPanel'
import ErrorState from '../components/ErrorState'
import MaterialDetail from '../components/MaterialDetail'
import MaterialList from '../components/MaterialList'
import Spinner from '../components/Spinner'
import SummaryComposer from '../components/SummaryComposer'
import UploadDropzone from '../components/UploadDropzone'
import { useMaterials } from '../hooks/useMaterials'
import { materialApi } from '../services/api'

export default function LibraryPage() {
  const materials = useMaterials()
  const [selected, setSelected] = useState(null)
  const [tab, setTab] = useState('materials')
  const [search, setSearch] = useState('')

  async function remove(item) {
    if (!confirm(`Excluir “${item.title}” e todos os chunks?`)) return
    await materialApi.remove(item.id)
    materials.load(search)
  }

  if (selected) {
    return (
      <main className="mx-auto max-w-5xl px-4 py-10">
        <MaterialDetail material={selected} onBack={() => setSelected(null)} />
      </main>
    )
  }

  return (
    <main className="mx-auto max-w-6xl px-4 py-12">
      <div className="mb-10">
        <span className="text-xs font-bold uppercase tracking-widest text-brand-500">Sua base de estudos</span>
        <h1 className="mt-3 text-4xl font-black">Biblioteca</h1>
        <p className="mt-3 max-w-2xl text-slate-500">Guarde seus materiais, consulte o conteúdo e produza resumos fundamentados nos documentos.</p>
      </div>

      <UploadDropzone onComplete={() => materials.load(search)} />

      <div className="mb-5 mt-10 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div className="flex w-fit rounded-xl bg-slate-200/70 p-1 dark:bg-slate-800">
          <button onClick={() => setTab('materials')} className={`btn ${tab === 'materials' ? 'bg-white shadow dark:bg-slate-700' : 'text-slate-500'}`}>Materiais</button>
          <button onClick={() => setTab('summaries')} className={`btn ${tab === 'summaries' ? 'bg-white shadow dark:bg-slate-700' : 'text-slate-500'}`}>Resumos</button>
          <button onClick={() => setTab('chat')} className={`btn ${tab === 'chat' ? 'bg-white shadow dark:bg-slate-700' : 'text-slate-500'}`}>Perguntas</button>
        </div>
        {tab === 'materials' && (
          <form onSubmit={event => { event.preventDefault(); materials.load(search) }} className="relative">
            <Search className="absolute left-3 top-3 text-slate-400" size={17} />
            <input className="field pl-9" placeholder="Pesquisar materiais" value={search} onChange={event => setSearch(event.target.value)} />
          </form>
        )}
      </div>

      {tab === 'chat' && <ChatPanel />}
      {tab === 'summaries' && (materials.loading ? <Spinner /> : materials.error ? <ErrorState message={materials.error} onRetry={() => materials.load(search)} /> : <SummaryComposer materials={materials.materials} />)}
      {tab === 'materials' && (materials.loading ? <Spinner /> : materials.error ? <ErrorState message={materials.error} onRetry={() => materials.load(search)} /> : <MaterialList materials={materials.materials} onSelect={setSelected} onDelete={remove} />)}
    </main>
  )
}
