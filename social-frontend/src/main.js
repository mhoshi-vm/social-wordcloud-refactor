// Global Mock Data
const dashboardData = {
    stock: [
        { t: '2026-01-07', y: 332.53 }, { t: '2026-01-08', y: 344.87 },
        { t: '2026-01-09', y: 344.87 }, { t: '2026-01-10', y: 344.87 },
        { t: '2026-01-11', y: 352.20 }, { t: '2026-01-12', y: 354.55 },
        { t: '2026-01-13', y: 339.89 }, { t: '2026-01-14', y: 342.46 },
        { t: '2026-01-15', y: 351.77 }, { t: '2026-01-16', y: 351.77 },
        { t: '2026-01-17', y: 351.77 }, { t: '2026-01-18', y: 351.77 },
        { t: '2026-01-19', y: 332.81 }, { t: '2026-01-20', y: 328.92 },
        { t: '2026-01-21', y: 325.52 }, { t: '2026-01-22', y: 320.03 },
        { t: '2026-01-23', y: 320.03 }, { t: '2026-01-24', y: 320.03 },
        { t: '2026-01-25', y: 324.95 }, { t: '2026-01-26', y: 332.79 },
        { t: '2026-01-27', y: 333.24 }
    ],
    cloud: {
        daily: [
            {term: "2026", count: 23}, {term: "char", count: 15}, {term: "market", count: 14},
            {term: "report", count: 13}, {term: "ai", count: 13}, {term: "broadcom", count: 13},
            {term: "opportun", count: 9}, {term: "electron", count: 8}, {term: "cpo", count: 8}
        ],
        weekly: [
            {term: "market", count: 85}, {term: "ai", count: 72}, {term: "growth", count: 60},
            {term: "broadcom", count: 45}, {term: "report", count: 40}
        ],
        monthly: [
            {term: "ai", count: 310}, {term: "technolog", count: 250}, {term: "global", count: 210},
            {term: "market", count: 195}, {term: "intelligence", count: 180}
        ]
    },
    map: [
        { id: 'm1', origin: 'NewsBot', url: 'http://google.com', sentiment: 'positive', geom: 'POINT(139.69 35.68)' },
        { id: 'm2', origin: 'MarketWatch', url: 'http://market.com', sentiment: 'neutral', geom: 'POINT(135.50 34.69)' },
        { id: 'm3', origin: 'SocialFeed', url: 'http://social.com', sentiment: 'negative', geom: 'POINT(136.90 35.18)' },
        { id: 'm4', origin: 'TechPortal', url: 'http://tech.com', sentiment: 'positive', geom: 'POINT(130.40 33.59)' },
        { id: 'm5', origin: 'WeatherAlert', url: 'http://weather.com', sentiment: 'negative', geom: 'POINT(141.35 43.06)' }
    ]
};

document.addEventListener('DOMContentLoaded', () => {
    initStockWidget(dashboardData.stock);
    initCloudWidget(dashboardData.cloud);
    initMapWidget(dashboardData.map);
});