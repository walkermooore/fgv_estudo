import { BookOpen, Check, FileText } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import ReactMarkdown from 'react-markdown'
import { materialApi } from '../services/api'
import ErrorState from './ErrorState'
import Spinner from './Spinner'

const summaryTypes = [
  ['SHORT', 'Curto'],
  ['COMPLETE', 'Completo'],
  ['TECHNICAL', 'Técnico'],
  ['BEGINNER', 'Para iniciantes'],
  ['ADVANCED', 'Avançado'],
  ['MAP', 'Mapa em tópicos'],
  ['CHECKLIST', 'Checklist'],
  ['TABLE', 'Tabela'],
  ['COMPARISON', 'Comparativo']
]

export default function SummaryComposer({ materials }) {
  const readyMaterials = useMemo(() => materials.filter(material => material.status === 'READY'), [materials])
  const [selectedIds, setSelectedIds] = useState([])
  const [type, setType] = useState('COMPLETE')
  const [request, setRequest] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (selectedIds.length === 0 && readyMaterials.length > 0) {
      setSelectedIds([readyMaterials[0].id])
    }
  }, [readyMaterials, selectedIds.length])

  function toggleMaterial(id) {
    setSelectedIds(current => {
      if (current.includes(id)) return current.filter(item => item !== id)
      return current.length < 10 ? [...current, id] : current
    })
  }

  async function submit(event) {
    event?.preventDefault()
    if (selectedIds.length === 0 || loading) return
    setLoading(true)
    setError('')
    setResult(null)
    try {
      setResult(await materialApi.action('summarize', {
        materialIds: selectedIds,
        type,
        request: request.trim() || null
      }))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }

  const usedSources = result?.sources?.reduce((sources, source) => {
    const key = `${source.materialTitle}::${source.chapter || ''}`
    if (!sources.has(key)) sources.set(key, source)
    return sources
  }, new Map())

  if (readyMaterials.length === 0) {
    return (
      <section className="panel py-16 text-center">
        <FileText className="mx-auto mb-4 text-slate-300" size={40} />
        <h2 className="font-bold">Nenhum material pronto para resumir</h2>
        <p className="mt-2 text-sm text-slate-500">Envie um documento e aguarde o processamento.</p>
      </section>
    )
  }

  return (
    <div className="grid items-start gap-6 lg:grid-cols-[340px_1fr]">
      <form onSubmit={submit} className="panel p-5 sm:p-6">
        <h2 className="flex items-center gap-2 text-lg font-bold"><BookOpen size={20} /> Novo resumo</h2>
        <p className="mt-1 text-sm text-slate-500">Escolha as fontes e diga o que deseja extrair delas.</p>

        <fieldset className="mt-6">
          <legend className="mb-2 flex w-full justify-between text-sm font-bold">
            <span>Documentos</span><span className="font-normal text-slate-400">{selectedIds.length}/10</span>
          </legend>
          <div className="max-h-56 space-y-2 overflow-y-auto pr-1">
            {readyMaterials.map(material => {
              const selected = selectedIds.includes(material.id)
              return (
                <label key={material.id} className={`flex cursor-pointer items-start gap-3 rounded-xl border p-3 transition ${selected ? 'border-brand-500 bg-brand-50 dark:bg-brand-950/40' : 'border-slate-200 dark:border-slate-700'}`}>
                  <input className="sr-only" type="checkbox" checked={selected} onChange={() => toggleMaterial(material.id)} />
                  <span className={`mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded border ${selected ? 'border-brand-600 bg-brand-600 text-white' : 'border-slate-300 dark:border-slate-600'}`}>
                    {selected && <Check size={14} />}
                  </span>
                  <span className="min-w-0">
                    <span className="block truncate text-sm font-semibold">{material.title}</span>
                    <span className="text-xs text-slate-500">{material.type} · {material.chunkCount} trechos</span>
                  </span>
                </label>
              )
            })}
          </div>
        </fieldset>

        <label className="mt-5 block text-sm font-bold" htmlFor="summary-type">Formato</label>
        <select id="summary-type" className="field mt-2" value={type} onChange={event => setType(event.target.value)}>
          {summaryTypes.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>

        <label className="mt-5 block text-sm font-bold" htmlFor="summary-request">O que você quer no resumo?</label>
        <textarea
          id="summary-request"
          className="field mt-2 min-h-32 resize-y"
          maxLength={2000}
          placeholder="Ex.: Resuma apenas os pontos sobre JWT, destaque diferenças importantes e liste pegadinhas de prova."
          value={request}
          onChange={event => setRequest(event.target.value)}
        />
        <div className="mt-1 flex justify-between text-xs text-slate-400">
          <span>Se deixar vazio, será feito um resumo geral.</span>
          <span>{request.length}/2000</span>
        </div>

        <button className="btn-primary mt-5 w-full" disabled={loading || selectedIds.length === 0}>
          Gerar resumo
        </button>
      </form>

      <section className="min-w-0">
        {loading && <div className="panel"><Spinner label="Lendo os documentos e preparando o resumo..." /></div>}
        {error && <ErrorState message={error} onRetry={submit} />}
        {!loading && !error && !result && (
          <div className="panel py-20 text-center text-slate-500">
            <BookOpen className="mx-auto mb-4 text-slate-300" size={42} />
            <p>O resumo aparecerá aqui.</p>
          </div>
        )}
        {result && (
          <article className="panel p-6 sm:p-8">
            <div className="prose prose-slate max-w-none dark:prose-invert">
              <ReactMarkdown>{result.content}</ReactMarkdown>
            </div>
            {usedSources?.size > 0 && (
              <details className="mt-8 border-t border-slate-200 pt-5 text-sm dark:border-slate-700">
                <summary className="cursor-pointer font-bold">Documentos e capítulos utilizados</summary>
                <ul className="mt-3 space-y-1 text-slate-500">
                  {[...usedSources.values()].map(source => (
                    <li key={`${source.materialTitle}-${source.chapter || source.chunkId}`}>
                      {source.materialTitle}{source.chapter ? ` — ${source.chapter}` : ''}
                    </li>
                  ))}
                </ul>
              </details>
            )}
          </article>
        )}
      </section>
    </div>
  )
}
