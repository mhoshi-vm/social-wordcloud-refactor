let mapInstance;
const markersMap = {};

function initMapWidget(data) {
    mapInstance = L.map('map').setView([35.68, 139.69], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(mapInstance);

    // Fix for blank map tiles
    setTimeout(() => { mapInstance.invalidateSize(); }, 100);

    const sentimentEmojis = { 'positive': '😀', 'neutral': '😐', 'negative': '😠' };

    data.forEach(item => {
        const coordsRaw = item.geom.replace('POINT(', '').replace(')', '').split(' ');
        const emoji = sentimentEmojis[item.sentiment.toLowerCase()] || '❓';

        const marker = L.marker([parseFloat(coordsRaw[1]), parseFloat(coordsRaw[0])], {
            icon: L.divIcon({
                className: '',
                html: `<div class="emoji-marker" style="background:#3357FF; width:30px; height:30px; color:white;">${emoji}</div>`
            })
        }).addTo(mapInstance);

        const popupContent = `
            <div class="custom-popup">
                <div class="popup-val"><span class="popup-label">Origin:</span> ${item.origin}</div>
                <div class="popup-val"><span class="popup-label">URL:</span> <a href="${item.url || '#'}" target="_blank">Link</a></div>
                <div class="popup-val"><span class="popup-label">Sentiment:</span> ${item.sentiment}</div>
            </div>
        `;

        marker.bindPopup(popupContent);
        marker.on('mouseover', function() { this.openPopup(); });
        markersMap[item.id] = marker;
    });

    document.getElementById('search-input').addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        data.forEach(item => {
            const el = markersMap[item.id].getElement();
            if (el) {
                const markerDiv = el.querySelector('.emoji-marker');
                if (query && item.origin.toLowerCase().includes(query)) {
                    markerDiv.classList.add('highlight-marker');
                } else {
                    markerDiv.classList.remove('highlight-marker');
                }
            }
        });
    });
}