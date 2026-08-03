import { FileSearch, FileText, Trash2, UploadCloud } from 'lucide-react'
import { useRef, useState } from 'react'
import ErrorState from '../components/ErrorState'
import NoticeAnalysis from '../components/NoticeAnalysis'
import Spinner from '../components/Spinner'
import { useNotices } from '../hooks/useNotices'

const statusLabel = { PROCESSING: 'Analisando', READY: 'Pronto', FAILED: 'Falhou' }

export default function NoticesPage() {
  const data = useNotices()
  const input = useRef(null)
  const [selected, setSelected] = useState(null)
  const [title, setTitle] = useState('')
  const [uploading, setUploading] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [error, setError] = useState('')
  const selectedNotice = selected ? data.notices.find(notice => notice.id === selected) : null

  async function upload(file) {
    if (!file || uploading) return
    setUploading(true)
    setError('')
    setUploadProgress(0)
    try {
      await data.upload(file, title, event => setUploadProgress(event.total ? Math.round(event.loaded * 100 / event.total) : 0))
      setTitle('')
      if (input.current) input.current.value = ''
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUploading(false)
    }
  }

  async function remove(notice) {
    if (!confirm(`Excluir a análise e o material “${notice.title}”?`)) return
    try {
      await data.remove(notice.id)
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  if (selectedNotice?.status === 'READY') {
    return <main className="mx-auto max-w-6xl px-4 py-10"><NoticeAnalysis notice={selectedNotice} onBack={() => setSelected(null)} /></main>
  }

  return (
    <main className="mx-auto max-w-6xl px-4 py-12">
      <header className="mb-8">
        <p className="text-sm font-bold text-brand-500">Editais</p>
        <h1 className="mt-2 text-3xl font-black sm:text-4xl">Organize seu edital</h1>
        <p className="mt-2 max-w-2xl text-slate-500">Envie o arquivo para separar datas, informações importantes, conteúdos, subconteúdos e palavras-chave.</p>
      </header>

      <section className="panel grid gap-5 p-5 sm:p-6 lg:grid-cols-[1fr_auto] lg:items-end">
        <div>
          <label className="text-sm font-bold" htmlFor="notice-title">Nome do edital (opcional)</label>
          <input id="notice-title" className="field mt-2" placeholder="Ex.: Concurso SEFAZ 2026" value={title} onChange={event => setTitle(event.target.value)} />
        </div>
        <div>
          <input ref={input} hidden type="file" accept=".pdf,.docx,.txt,.md,.markdown,.html,.htm" onChange={event => upload(event.target.files?.[0])} />
          <button className="btn-primary w-full lg:w-auto" disabled={uploading} onClick={() => input.current?.click()}>
            <UploadCloud size={18} /> {uploading ? `Enviando ${uploadProgress}%` : 'Enviar edital'}
          </button>
        </div>
      </section>

      <p className="mt-3 text-xs text-slate-500">O processamento acontece em segundo plano. Editais extensos podem levar vários minutos na CPU local.</p>
      {error && <div className="mt-5"><ErrorState message={error} onRetry={() => setError('')} /></div>}

      <section className="mt-10">
        <h2 className="mb-4 text-xl font-black">Seus editais</h2>
        {data.loading ? <Spinner label="Carregando editais..." /> : data.error ? <ErrorState message={data.error} onRetry={() => data.load()} /> : data.notices.length === 0 ? (
          <div className="panel py-16 text-center">
            <FileSearch className="mx-auto mb-4 text-slate-300" size={42} />
            <h3 className="font-bold">Nenhum edital enviado</h3>
            <p className="mt-2 text-sm text-slate-500">A primeira análise aparecerá aqui.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {data.notices.map(notice => (
              <article className="panel flex flex-col gap-4 p-5 sm:flex-row sm:items-center" key={notice.id}>
                <div className="rounded-xl bg-slate-100 p-3 text-brand-500 dark:bg-slate-800"><FileText /></div>
                <button className="min-w-0 flex-1 text-left" disabled={notice.status !== 'READY'} onClick={() => setSelected(notice.id)}>
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="truncate font-bold">{notice.title}</h3>
                    <span className={`rounded-full px-2.5 py-1 text-[10px] font-bold ${notice.status === 'READY' ? 'bg-emerald-100 text-emerald-700' : notice.status === 'FAILED' ? 'bg-rose-100 text-rose-700' : 'bg-amber-100 text-amber-700'}`}>{statusLabel[notice.status]}</span>
                  </div>
                  <p className="mt-1 text-xs text-slate-500">{new Date(notice.createdAt).toLocaleString('pt-BR')}</p>
                  {notice.status === 'PROCESSING' && (
                    <div className="mt-3 max-w-md">
                      <div className="h-2 overflow-hidden rounded-full bg-slate-200 dark:bg-slate-700"><div className="h-full bg-brand-500 transition-all" style={{ width: `${Math.max(3, notice.progressPercentage)}%` }} /></div>
                      <p className="mt-1 text-xs text-slate-400">{notice.processedBatches} de {notice.totalBatches || '?'} etapas · {notice.progressPercentage}%</p>
                    </div>
                  )}
                  {notice.status === 'FAILED' && <p className="mt-2 text-sm text-rose-500">{notice.failureReason}</p>}
                </button>
                {notice.status !== 'PROCESSING' && <button aria-label="Excluir edital" className="btn-ghost text-rose-500" onClick={() => remove(notice)}><Trash2 size={17} /></button>}
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}
