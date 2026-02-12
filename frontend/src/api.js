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

// GraphQL API functions
const GRAPHQL_URL = import.meta.env.VITE_GRAPHQL_URL || '/graphql';

async function graphqlRequest(query, variables = {}) {
  const res = await fetch(GRAPHQL_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ query, variables }),
  });

  const result = await res.json();

  if (result.errors) {
    throw new Error(result.errors[0].message);
  }

  return result.data;
}

export async function fetchSocialMessages(filters = {}) {
  const query = `
    query GetMessages($origin: String, $lang: String, $name: String) {
      socialMessages(origin: $origin, lang: $lang, name: $name, first: 100) {
        edges {
          node {
            id
            origin
            text
            lang
            name
            url
            createDateTime
          }
        }
      }
    }
  `;

  const variables = {
    origin: filters.origin || null,
    lang: filters.lang || null,
    name: filters.name || null,
  };

  const data = await graphqlRequest(query, variables);
  return data.socialMessages.edges.map((edge) => edge.node);
}

export async function deleteSocialMessages(ids) {
  const mutation = `
    mutation DeleteMessages($ids: [ID!]!) {
      deleteSocialMessages(ids: $ids) {
        message
        deletedCount
      }
    }
  `;

  const variables = { ids };
  const data = await graphqlRequest(mutation, variables);
  return data.deleteSocialMessages;
}
