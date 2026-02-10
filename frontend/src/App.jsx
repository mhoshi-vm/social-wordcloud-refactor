import useDashboardData from './hooks/useDashboardData';
import MapWidget from './components/MapWidget';
import CloudWidget from './components/CloudWidget';
import StockWidget from './components/StockWidget';
import './App.css';

export default function App() {
  const { data, error, loading } = useDashboardData();

  return (
    <>
      <header>
        <h1>Analytics Dashboard</h1>
        {loading && !data && <span className="status-badge">Loading...</span>}
        {error && <span className="status-badge error" role="alert">API error: {error}</span>}
        {data && !loading && !error && <span className="status-badge ok">Live</span>}
      </header>

      <main className="dashboard-grid">
        <MapWidget data={data?.map} />
        <StockWidget data={data?.stock} />
        <CloudWidget data={data?.cloud} />
      </main>
    </>
  );
}
