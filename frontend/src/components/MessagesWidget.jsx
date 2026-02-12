import { useState, useEffect } from 'react';
import { fetchSocialMessages, deleteSocialMessages } from '../api';

export default function MessagesWidget() {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [filters, setFilters] = useState({
    origin: '',
    lang: '',
    name: ''
  });

  useEffect(() => {
    loadMessages();
  }, []);

  async function loadMessages() {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchSocialMessages(filters);
      setMessages(data);
      setSelectedIds(new Set());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function handleFilterChange(field, value) {
    setFilters(prev => ({ ...prev, [field]: value }));
  }

  function toggleSelect(id) {
    setSelectedIds(prev => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  }

  function toggleSelectAll(checked) {
    if (checked) {
      setSelectedIds(new Set(messages.map(m => m.id)));
    } else {
      setSelectedIds(new Set());
    }
  }

  async function handleDelete() {
    if (selectedIds.size === 0) return;

    if (!confirm(`Delete ${selectedIds.size} message(s)?`)) {
      return;
    }

    try {
      await deleteSocialMessages(Array.from(selectedIds));
      await loadMessages();
    } catch (err) {
      setError('Delete failed: ' + err.message);
    }
  }

  if (loading) {
    return (
      <div className="messages-container">
        <div className="empty-state">Loading messages...</div>
      </div>
    );
  }

  return (
    <div className="messages-container">
      <div className="messages-controls">
        <div className="filter-group">
          <label htmlFor="origin-filter">Origin:</label>
          <select
            id="origin-filter"
            value={filters.origin}
            onChange={(e) => handleFilterChange('origin', e.target.value)}
          >
            <option value="">All Sources</option>
            <option value="MASTODON">Mastodon</option>
            <option value="LINKEDIN">LinkedIn</option>
            <option value="NEWS">News</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="lang-filter">Language:</label>
          <select
            id="lang-filter"
            value={filters.lang}
            onChange={(e) => handleFilterChange('lang', e.target.value)}
          >
            <option value="">All Languages</option>
            <option value="en">English</option>
            <option value="ja">Japanese</option>
            <option value="es">Spanish</option>
            <option value="fr">French</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="name-filter">Name:</label>
          <input
            id="name-filter"
            type="text"
            value={filters.name}
            onChange={(e) => handleFilterChange('name', e.target.value)}
            placeholder="Search by name..."
          />
        </div>

        <button className="btn-primary" onClick={loadMessages}>
          🔍 Search
        </button>

        <button
          className="btn-danger"
          onClick={handleDelete}
          disabled={selectedIds.size === 0}
        >
          🗑️ Delete Selected ({selectedIds.size})
        </button>
      </div>

      {error && (
        <div className="error-banner">
          <strong>Error:</strong> {error}
        </div>
      )}

      {messages.length === 0 ? (
        <div className="empty-state">
          <div style={{ fontSize: '48px', marginBottom: '16px' }}>📭</div>
          <h3>No messages found</h3>
          <p>Try adjusting your filters or check back later.</p>
        </div>
      ) : (
        <>
          <div className="messages-stats">
            <span>Total: {messages.length}</span>
            <span>Selected: {selectedIds.size}</span>
          </div>

          <div className="messages-table-container">
            <table className="messages-table">
              <thead>
                <tr>
                  <th style={{ width: '40px' }}>
                    <input
                      type="checkbox"
                      checked={selectedIds.size === messages.length && messages.length > 0}
                      onChange={(e) => toggleSelectAll(e.target.checked)}
                    />
                  </th>
                  <th>Origin</th>
                  <th>Name</th>
                  <th>Message</th>
                  <th>Language</th>
                  <th>Date</th>
                  <th>Link</th>
                </tr>
              </thead>
              <tbody>
                {messages.map((msg) => (
                  <tr key={msg.id}>
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedIds.has(msg.id)}
                        onChange={() => toggleSelect(msg.id)}
                      />
                    </td>
                    <td>
                      <span className="badge badge-origin">{msg.origin || 'N/A'}</span>
                    </td>
                    <td><strong>{msg.name || 'Unknown'}</strong></td>
                    <td>
                      <div className="message-text" title={msg.text || ''}>
                        {msg.text || 'No text'}
                      </div>
                    </td>
                    <td>
                      <span className="badge badge-lang">{msg.lang || 'N/A'}</span>
                    </td>
                    <td>{formatDate(msg.createDateTime)}</td>
                    <td>
                      {msg.url ? (
                        <a href={msg.url} target="_blank" rel="noopener noreferrer">
                          🔗
                        </a>
                      ) : (
                        '-'
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

function formatDate(dateStr) {
  if (!dateStr) return 'N/A';
  const date = new Date(dateStr);
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}
