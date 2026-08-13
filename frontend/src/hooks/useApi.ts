import { useState, useCallback } from 'react';

interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
}

export function useApi<T>() {
  const [state, setState] = useState<UseApiState<T>>({ data: null, loading: false, error: null });

  const execute = useCallback(async (apiFn: () => Promise<T>) => {
    setState({ data: null, loading: true, error: null });
    try {
      const data = await apiFn();
      setState({ data, loading: false, error: null });
      return data;
    } catch (err) {
      const error = err instanceof Error ? err.message : 'Unknown error';
      setState({ data: null, loading: false, error });
      return null;
    }
  }, []);

  return { ...state, execute };
}
