import { useState, useEffect, useRef, useCallback } from 'react';
import { fetchAll } from '../api';

const POLL_MS = 30_000;

export default function useDashboardData() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const timerRef = useRef(null);

  const load = useCallback(async (signal) => {
    try {
      const result = await fetchAll(signal);
      setData(result);
      setError(null);
    } catch (err) {
      if (err.name !== 'AbortError') setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const ctrl = new AbortController();
    let cancelled = false;

    async function poll() {
      await load(ctrl.signal);
      if (!cancelled) {
        timerRef.current = setTimeout(poll, POLL_MS);
      }
    }

    poll();

    return () => {
      cancelled = true;
      ctrl.abort();
      clearTimeout(timerRef.current);
    };
  }, [load]);

  return { data, error, loading };
}
