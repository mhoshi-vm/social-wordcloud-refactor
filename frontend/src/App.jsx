import { useState } from 'react';
import useDashboardData from './hooks/useDashboardData';
import MapWidget from './components/MapWidget';
import CloudWidget from './components/CloudWidget';
import StockWidget from './components/StockWidget';
import MessagesWidget from './components/MessagesWidget';
import './App.css';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const { data, error, loading } = useDashboardData();

  return (
    <>
      <header>
        <div className="header-content">
          <h1>Analytics Dashboard</h1>
          {activeTab === 'dashboard' && (
            <>
              {loading && !data && <span className="status-badge">Loading...</span>}
              {error && <span className="status-badge error" role="alert">API error: {error}</span>}
              {data && !loading && !error && <span className="status-badge ok">Live</span>}
            </>
          )}
        </div>

        <nav className="nav-tabs">
          <button
            className={activeTab === 'dashboard' ? 'nav-tab active' : 'nav-tab'}
            onClick={() => setActiveTab('dashboard')}
          >
            📊 Dashboard
          </button>
          <button
            className={activeTab === 'messages' ? 'nav-tab active' : 'nav-tab'}
            onClick={() => setActiveTab('messages')}
          >
            💬 Messages
          </button>
        </nav>
      </header>

      <main>
        {activeTab === 'dashboard' && (
          <div className="dashboard-grid">
            <MapWidget data={data?.map} />
            <StockWidget data={data?.stock} />
            <CloudWidget data={data?.cloud} />
          </div>
        )}

        {activeTab === 'messages' && <MessagesWidget />}
      </main>
    </>
  );
}
