let stockChart;

function initStockWidget(data) {
    const ctx = document.getElementById('stockChart').getContext('2d');
    stockChart = new Chart(ctx, {
        type: 'line',
        data: {
            datasets: [{
                label: 'Avg Price',
                data: data,
                borderColor: '#007bff',
                backgroundColor: 'rgba(0, 123, 255, 0.1)',
                fill: true,
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            parsing: { xAxisKey: 't', yAxisKey: 'y' },
            scales: { y: { ticks: { callback: (v) => '$' + v } } }
        }
    });
}

function updateStockChart(range, btn) {
    document.querySelectorAll('#stock-controls button').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    stockChart.update();
}