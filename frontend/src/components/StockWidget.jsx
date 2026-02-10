import { useRef, useEffect, useState, useMemo } from 'react';
import { Chart, registerables } from 'chart.js';
import { Line } from 'react-chartjs-2';

Chart.register(...registerables);

const RANGES = [
  { label: '1W', days: 7 },
  { label: '1M', days: 30 },
  { label: '1Y', days: 365 },
];

function filterByRange(data, days) {
  if (!data || data.length === 0) return [];
  const cutoff = new Date();
  cutoff.setDate(cutoff.getDate() - days);
  const cutoffStr = cutoff.toISOString().split('T')[0];
  return data.filter((d) => d.t >= cutoffStr);
}

export default function StockWidget({ data }) {
  const [range, setRange] = useState('1Y');

  const days = RANGES.find((r) => r.label === range)?.days ?? 365;
  const filtered = useMemo(() => filterByRange(data, days), [data, days]);

  const chartData = {
    labels: filtered.map((d) => d.t),
    datasets: [
      {
        label: 'Avg Price',
        data: filtered.map((d) => d.y),
        borderColor: '#007bff',
        backgroundColor: 'rgba(0, 123, 255, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: filtered.length > 60 ? 0 : 3,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    scales: {
      y: {
        ticks: {
          callback: (v) => `$${Number(v).toLocaleString()}`,
        },
      },
    },
  };

  const empty = !filtered || filtered.length === 0;

  return (
    <section className="card">
      <div className="card-header">
        <div>
          <h2>AVGO Stock</h2>
          <small>Broadcom Inc. &bull; Daily Metrics</small>
        </div>
        <div className="controls" role="group" aria-label="Date range">
          {RANGES.map((r) => (
            <button
              key={r.label}
              className={range === r.label ? 'active' : ''}
              onClick={() => setRange(r.label)}
              aria-pressed={range === r.label}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>
      <div className="chart-container">
        {empty ? (
          <div className="empty-state">No stock data available</div>
        ) : (
          <Line data={chartData} options={options} />
        )}
      </div>
    </section>
  );
}
