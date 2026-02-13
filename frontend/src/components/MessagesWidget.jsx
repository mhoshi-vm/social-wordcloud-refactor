import { useState, useEffect } from 'react';
import { fetchSocialMessages, deleteSocialMessages } from '../api';

export default function MessagesWidget() {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [pageInfo, setPageInfo] = useState(null);
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [jumpToPage, setJumpToPage] = useState('');
  const [currentCursor, setCurrentCursor] = useState(null);
  const [cursorStack, setCursorStack] = useState([]);
  const [filters, setFilters] = useState({
    origin: '',
    lang: '',
    name: '',
    first: 50,
  });

  useEffect(() => {
    loadMessages();
  }, []);

  async function loadMessages(cursor = null, pageNumber = null) {
    setLoading(true);
    setError(null);
    try {
      const { messages: data, pageInfo: info, totalCount: total } = await fetchSocialMessages({
        ...filters,
        after: cursor,
      });
      setMessages(data);
      setPageInfo(info);
      setTotalCount(total || 0);
      setCurrentCursor(cursor);
      setSelectedIds(new Set());

      if (pageNumber !== null) {
        setCurrentPage(pageNumber);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function handleFilterChange(field, value) {
    setFilters(prev => ({ ...prev, [field]: value }));
  }

  function handleSearch() {
    setCursorStack([]);
    setCurrentCursor(null);
    setCurrentPage(1);
    loadMessages(null, 1);
  }

  function handleNextPage() {
    if (pageInfo?.hasNextPage && pageInfo?.endCursor) {
      setCursorStack(prev => [...prev, currentCursor]);
      loadMessages(pageInfo.endCursor, currentPage + 1);
    }
  }

  function handlePreviousPage() {
    if (cursorStack.length > 0) {
      const previousCursor = cursorStack[cursorStack.length - 1];
      setCursorStack(prev => prev.slice(0, -1));
      loadMessages(previousCursor, currentPage - 1);
    }
  }

  async function handleJumpToPage() {
    const pageNum = parseInt(jumpToPage, 10);
    const totalPages = Math.ceil(totalCount / filters.first);

    if (!pageNum || pageNum < 1 || pageNum > totalPages) {
      setError(`Please enter a page number between 1 and ${totalPages}`);
      return;
    }

    if (pageNum === currentPage) {
      setJumpToPage('');
      return;
    }

    setJumpToPage('');
    setError(null);

    // Calculate offset for the target page
    const targetOffset = (pageNum - 1) * filters.first;

    setLoading(true);
    try {
      const { messages: data, pageInfo: info, totalCount: total } = await fetchSocialMessages({
        ...filters,
        offset: targetOffset,
        after: null, // Clear cursor when using offset
      });

      setMessages(data);
      setPageInfo(info);
      setTotalCount(total || 0);
      setCurrentPage(pageNum);
      setSelectedIds(new Set());

      // Reset cursor stack when jumping
      setCursorStack([]);
      setCurrentCursor(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
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

        <button className="btn-primary" onClick={handleSearch}>
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

          {pageInfo && (
            <div className="pagination-controls">
              <button
                className="btn-primary"
                onClick={handlePreviousPage}
                disabled={cursorStack.length === 0}
              >
                ← Previous
              </button>

              <div className="pagination-info-group">
                <span className="pagination-info">
                  Page {currentPage} • Showing {((currentPage - 1) * filters.first) + 1}-{Math.min(currentPage * filters.first, totalCount)} of {totalCount} messages
                </span>

                <div className="jump-to-page">
                  <label htmlFor="jump-page">Jump to:</label>
                  <input
                    id="jump-page"
                    type="number"
                    min="1"
                    max={Math.ceil(totalCount / filters.first)}
                    value={jumpToPage}
                    onChange={(e) => setJumpToPage(e.target.value)}
                    placeholder={`1-${Math.ceil(totalCount / filters.first)}`}
                  />
                  <button
                    className="btn-primary btn-sm"
                    onClick={handleJumpToPage}
                    disabled={!jumpToPage}
                  >
                    Go
                  </button>
                </div>
              </div>

              <button
                className="btn-primary"
                onClick={handleNextPage}
                disabled={!pageInfo.hasNextPage}
              >
                Next →
              </button>
            </div>
          )}
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
