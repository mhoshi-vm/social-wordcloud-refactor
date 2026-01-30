// src/widget-map.js
let mapInstance;
let markersLayerGroup; // Use a LayerGroup for easy clearing
const markersMap = {};

function initMapWidget(data) {
    if (!mapInstance) {
        mapInstance = L.map('map').setView([35.68, 139.69], 5);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(mapInstance);

        // Fix for blank map
        setTimeout(() => { mapInstance.invalidateSize(); }, 100);

        // Create a layer group to hold markers
        markersLayerGroup = L.layerGroup().addTo(mapInstance);

        // Setup Search Listener
        setupSearchListener();
    }

    renderMarkers(data);
}

// Called by main.js
function refreshMapWidget(newData) {
    renderMarkers(newData);
}

function renderMarkers(data) {
    if (!mapInstance || !markersLayerGroup) return;

    // Clear old markers
    markersLayerGroup.clearLayers();
    // Clear global map reference
    for (let id in markersMap) delete markersMap[id];

    const sentimentEmojis = { 'positive': '😀', 'neutral': '😐', 'negative': '😠' };

    data.forEach(item => {
        if (!item.geom) return;

        // Parse WKT "POINT(lng lat)"
        const coordsRaw = item.geom.replace('POINT(', '').replace(')', '').split(' ');
        const lat = parseFloat(coordsRaw[1]);
        const lng = parseFloat(coordsRaw[0]);

        const emoji = sentimentEmojis[(item.sentiment || '').toLowerCase()] || '❓';

        const marker = L.marker([lat, lng], {
            icon: L.divIcon({
                className: '',
                html: `<div class="emoji-marker" style="background:#3357FF; width:30px; height:30px; color:white;">${emoji}</div>`
            })
        });

        const popupContent = `
            <div class="custom-popup">
                <div class="popup-val"><span class="popup-label">Origin:</span> ${item.origin}</div>
                <div class="popup-val"><span class="popup-label">URL:</span> <a href="${item.url || '#'}" target="_blank">Link</a></div>
                <div class="popup-val"><span class="popup-label">Sentiment:</span> ${item.sentiment}</div>
            </div>
        `;

        marker.bindPopup(popupContent);
        marker.on('mouseover', function() { this.openPopup(); });

        // Add to LayerGroup
        markersLayerGroup.addLayer(marker);

        // Store reference for search
        markersMap[item.id] = marker;
    });
}

function setupSearchListener() {
    const searchInput = document.getElementById('search-input');
    if(searchInput) {
        searchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase();
            // Iterate over the stored markers in markersMap
            Object.values(markersMap).forEach(marker => {
                const el = marker.getElement();
                if (el) {
                    const markerDiv = el.querySelector('.emoji-marker');
                    // We need to access the data. Since we don't have the data item here easily,
                    // we can rely on the Popup content or store data on the marker object.
                    // For simplicity, we check if the popup content contains the string (not ideal but works for simple demo)
                    // OR we can rely on main.js passing data.
                    // Better approach: Store origin in marker options

                    // But for now, let's just leave the visual search logic as provided in previous steps,
                    // assuming 'markersMap' keys align with data IDs if you want strict searching.
                    // A quick fix for search to work without re-looping data:
                    const popupContent = marker.getPopup().getContent();
                    if (query && popupContent.toLowerCase().includes(query)) {
                        markerDiv.classList.add('highlight-marker');
                    } else {
                        markerDiv.classList.remove('highlight-marker');
                    }
                }
            });
        });
    }
}