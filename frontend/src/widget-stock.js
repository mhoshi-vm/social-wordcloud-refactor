// src/widget-stock.js
let stockChart;
let currentRange = '1Y'; // Track current UI state

function initStockWidget(data) {
    // If we call init again (rare), destroy old chart
    if (stockChart) stockChart.destroy();

    const ctx = document.getElementById('stockChart').getContext('2d');
    stockChart = new Chart(ctx, {
        type: 'line',
        data: {
            datasets: [{
                label: 'Avg Price',
                data: data, // Initial data
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
            scales: { y: { ticks: { callback: (v) => '$' + v } } },
            animation: false // Disable animation for smoother updates
        }
    });
}

// Called by main.js when new API data arrives
function refreshStockWidget(newData) {
    if (!stockChart) {
        initStockWidget(newData);
    } else {
        // Update the raw data
        // Note: In a real app we would filter 'newData' based on 'currentRange'
        // For now, we update the whole dataset
        stockChart.data.datasets[0].data = newData;
        stockChart.update();
    }
}

function updateStockChart(range, btn) {
    // UI Update
    document.querySelectorAll('#stock-controls button').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    currentRange = range;

    // Filtering logic (client-side)
    // Assuming 'dashboardState.stock' is accessible or we rely on the chart's data
    // For this simple example, we are just triggering a visual update
    // You can implement slice logic here if the API returns full history
    stockChart.update();
}