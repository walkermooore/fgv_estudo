import { useCallback, useEffect, useState } from 'react'
import { historyApi } from '../services/api'

const emptyPage = { items: [], totalElements: 0, totalPages: 0, page: 0, size: 20 }

export function useHistory() {
  const [data, setData] = useState(emptyPage)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async (page = 0) => {
    setLoading(true)
    setError('')
    try {
      setData(await historyApi.list(page, 20))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load(0)
  }, [load])

  return { ...data, loading, error, load, retry: () => load(data.page) }
}
