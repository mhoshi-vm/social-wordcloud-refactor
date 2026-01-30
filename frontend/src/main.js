// src/main.js

const API_BASE_URL = 'http://localhost:8080';
const POLLING_INTERVAL = 30000; // 30 seconds

// Global State
const dashboardState = {
    stock: [],
    cloud: { daily: [], weekly: [], monthly: [] },
    map: []
};

/** --- API FETCHING --- **/

async function fetchStockData() {
    try {
        const response = await fetch(`${API_BASE_URL}/stocks`);
        const data = await response.json();
        // Transform: { bucket: "2026-01-01T...", avgPrice: 123.45 } -> { t: "2026-01-01", y: 123.45 }
        return data.map(item => ({
            t: item.bucket.split('T')[0], // Extract date part
            y: item.avgPrice
        })).sort((a, b) => new Date(a.t) - new Date(b.t));
    } catch (e) {
        console.error("Stock fetch failed", e);
        return [];
    }
}

async function fetchTermData(duration) {
    try {
        // duration enum: DAY, WEEK, MONTH
        const response = await fetch(`${API_BASE_URL}/term/${duration}`);
        const data = await response.json();
        // Transform: { term: "AI", count: 10 } -> { term: "AI", count: 10 } (Compatible)
        return data;
    } catch (e) {
        console.error(`Term fetch failed for ${duration}`, e);
        return [];
    }
}

async function fetchMapData() {
    try {
        const response = await fetch(`${API_BASE_URL}/messages/analysis`);
        const data = await response.json();
        // Transform: gisPoint "POINT(139.69 35.68)" -> geom (Compatible logic needed)
        // Map keys: sentimentLabel -> sentiment
        return data.map(item => ({
            id: item.messageId,
            origin: item.origin,
            url: item.url,
            sentiment: item.sentimentLabel,
            geom: item.gisPoint
        }));
    } catch (e) {
        console.error("Map fetch failed", e);
        return [];
    }
}

/** --- ORCHESTRATION --- **/

async function updateDashboard() {
    console.log("Fetching new data...");

    // 1. Fetch all data in parallel
    const [stock, dayTerms, weekTerms, monthTerms, mapMessages] = await Promise.all([
        fetchStockData(),
        fetchTermData('DAY'),
        fetchTermData('WEEK'),
        fetchTermData('MONTH'),
        fetchMapData()
    ]);

    // 2. Update State
    dashboardState.stock = stock;
    dashboardState.cloud = {
        daily: dayTerms,
        weekly: weekTerms,
        monthly: monthTerms
    };
    dashboardState.map = mapMessages;

    // 3. Refresh Widgets
    if (typeof refreshStockWidget === 'function') refreshStockWidget(dashboardState.stock);
    if (typeof refreshCloudWidget === 'function') refreshCloudWidget(dashboardState.cloud);
    if (typeof refreshMapWidget === 'function') refreshMapWidget(dashboardState.map);
}

// Initialization
document.addEventListener('DOMContentLoaded', async () => {
    // Initial Fetch
    await updateDashboard();

    // Start Polling
    setInterval(updateDashboard, POLLING_INTERVAL);
});