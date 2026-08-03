import { useCallback, useEffect, useState } from 'react'
import { noticeApi } from '../services/api'

export function useNotices() {
  const [notices, setNotices] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async (silent = false) => {
    if (!silent) setLoading(true)
    setError('')
    try {
      setNotices(await noticeApi.list())
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      if (!silent) setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    if (!notices.some(notice => notice.status === 'PROCESSING')) return undefined
    const timer = window.setTimeout(() => load(true), 5000)
    return () => window.clearTimeout(timer)
  }, [notices, load])

  async function upload(file, title, onProgress) {
    const created = await noticeApi.upload(file, title, onProgress)
    setNotices(current => [created, ...current])
    return created
  }

  async function remove(id) {
    await noticeApi.remove(id)
    setNotices(current => current.filter(notice => notice.id !== id))
  }

  async function retry(id) {
    const updated = await noticeApi.retry(id)
    setNotices(current => current.map(notice => notice.id === id ? updated : notice))
  }

  return { notices, loading, error, load, upload, remove, retry }
}
