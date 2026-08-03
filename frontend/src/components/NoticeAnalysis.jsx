import { ArrowLeft, Building2, CalendarDays, CheckCircle2, ClipboardCheck, Tag } from 'lucide-react'

export default function NoticeAnalysis({ notice, onBack }) {
  const analysis = notice.analysis
  if (!analysis) return null

  return (
    <div>
      <button className="btn-ghost mb-6" onClick={onBack}><ArrowLeft size={17} /> Voltar aos editais</button>

      <header className="panel p-6 sm:p-8">
        <p className="text-xs font-bold uppercase tracking-widest text-brand-500">Edital analisado</p>
        <h1 className="mt-2 text-3xl font-black">{notice.title}</h1>
        <div className="mt-5 grid gap-4 text-sm sm:grid-cols-3">
          <Info label="Órgão" value={analysis.organization} />
          <Info label="Banca" value={analysis.examiningBoard} />
          <Info label="Cargo" value={analysis.position} />
        </div>
        {analysis.summary && <p className="mt-6 whitespace-pre-wrap border-t border-slate-200 pt-5 leading-7 text-slate-600 dark:border-slate-700 dark:text-slate-300">{analysis.summary}</p>}
      </header>

      <section className="mt-8">
        <h2 className="mb-4 flex items-center gap-2 text-xl font-black"><CalendarDays className="text-brand-500" /> Datas e prazos</h2>
        {analysis.dates?.length ? (
          <div className="grid gap-4 md:grid-cols-2">
            {analysis.dates.map((item, index) => (
              <article className="panel p-5" key={`${item.label}-${item.date}-${index}`}>
                <p className="text-sm font-bold">{item.label || 'Data importante'}</p>
                <p className="mt-1 text-lg font-black text-brand-600 dark:text-brand-500">{item.date || 'Conforme o edital'}</p>
                {item.details && <p className="mt-2 text-sm text-slate-500">{item.details}</p>}
              </article>
            ))}
          </div>
        ) : <Empty text="Nenhuma data identificada nos trechos do edital." />}
      </section>

      <section className="mt-10">
        <h2 className="mb-4 flex items-center gap-2 text-xl font-black"><ClipboardCheck className="text-brand-500" /> Informações úteis</h2>
        {analysis.usefulInformation?.length ? (
          <div className="grid gap-4 md:grid-cols-2">
            {analysis.usefulInformation.map((item, index) => (
              <article className="panel p-5" key={`${item.category}-${item.title}-${index}`}>
                <p className="text-xs font-bold uppercase tracking-widest text-brand-500">{item.category || 'Informação'}</p>
                <h3 className="mt-2 font-bold">{item.title}</h3>
                <p className="mt-2 text-sm leading-6 text-slate-500">{item.details}</p>
              </article>
            ))}
          </div>
        ) : <Empty text="Nenhuma informação adicional foi identificada." />}
      </section>

      <section className="mt-10">
        <h2 className="mb-4 flex items-center gap-2 text-xl font-black"><CheckCircle2 className="text-brand-500" /> Conteúdo programático</h2>
        {analysis.contents?.length ? (
          <div className="space-y-4">
            {analysis.contents.map((topic, index) => (
              <details className="panel overflow-hidden" key={`${topic.topic}-${index}`} open={index === 0}>
                <summary className="cursor-pointer list-none p-5 text-lg font-bold sm:p-6">{topic.topic}</summary>
                <div className="space-y-5 border-t border-slate-100 p-5 dark:border-slate-800 sm:p-6">
                  {topic.subtopics?.map((subtopic, subtopicIndex) => (
                    <div key={`${subtopic.name}-${subtopicIndex}`}>
                      <h3 className="font-semibold">{subtopic.name}</h3>
                      {subtopic.keywords?.length > 0 && (
                        <div className="mt-2 flex flex-wrap gap-2">
                          {subtopic.keywords.map(keyword => (
                            <span key={keyword} className="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2.5 py-1 text-xs text-slate-600 dark:bg-slate-800 dark:text-slate-300"><Tag size={12} />{keyword}</span>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </details>
            ))}
          </div>
        ) : <Empty text="Nenhum conteúdo programático foi identificado." />}
      </section>
    </div>
  )
}

function Info({ label, value }) {
  return (
    <div className="flex items-start gap-3">
      <Building2 className="mt-0.5 shrink-0 text-slate-400" size={18} />
      <div><p className="text-xs text-slate-400">{label}</p><p className="mt-0.5 font-semibold">{value || 'Não identificado'}</p></div>
    </div>
  )
}

function Empty({ text }) {
  return <div className="panel p-6 text-sm text-slate-500">{text}</div>
}
