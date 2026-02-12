const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function fetchJson(path, signal) {
  const res = await fetch(`${API_BASE_URL}${path}`, { signal });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return res.json();
}

export async function fetchStockData(signal) {
  const data = await fetchJson('/stocks', signal);
  return data
    .map((d) => ({
      t: d.bucket?.split(/[T\s]/)[0], // Handle both 'T' and space separators
      y: Number(d.avgPrice)
    }))
    .filter((d) => d.t && !Number.isNaN(d.y))
    .sort((a, b) => new Date(a.t) - new Date(b.t));
}

export async function fetchTermData(duration, signal) {
  return fetchJson(`/term/${duration}`, signal);
}

export async function fetchMapData(signal) {
  return fetchJson('/messages/analysis', signal);
}

export async function fetchAll(signal) {
  const [stock, day, week, month, map] = await Promise.all([
    fetchStockData(signal),
    fetchTermData('DAY', signal),
    fetchTermData('WEEK', signal),
    fetchTermData('MONTH', signal),
    fetchMapData(signal),
  ]);
  return {
    stock,
    cloud: { daily: day, weekly: week, monthly: month },
    map,
  };
}
