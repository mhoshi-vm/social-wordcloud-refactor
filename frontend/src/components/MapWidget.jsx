import { useEffect, useRef, useState, useMemo } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const SENTIMENT_EMOJI = { positive: '\u{1F600}', neutral: '\u{1F610}', negative: '\u{1F620}' };

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function parsePoint(wkt) {
  if (!wkt) return null;
  const m = wkt.match(/POINT\(\s*([-\d.]+)\s+([-\d.]+)\s*\)/);
  if (!m) return null;
  const lng = parseFloat(m[1]);
  const lat = parseFloat(m[2]);
  if (Number.isNaN(lat) || Number.isNaN(lng)) return null;
  return [lat, lng];
}

export default function MapWidget({ data }) {
  const mapRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const layerGroupRef = useRef(null);
  const markersRef = useRef([]);
  const [query, setQuery] = useState('');

  // Initialize Leaflet map once
  useEffect(() => {
    if (mapInstanceRef.current) return;
    const map = L.map(mapRef.current).setView([35.68, 139.69], 3);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(map);
    mapInstanceRef.current = map;
    layerGroupRef.current = L.layerGroup().addTo(map);

    const onResize = () => map.invalidateSize();
    window.addEventListener('resize', onResize);
    // Fix initial blank tiles
    setTimeout(onResize, 200);

    return () => {
      window.removeEventListener('resize', onResize);
      map.remove();
      mapInstanceRef.current = null;
    };
  }, []);

  // Render markers when data changes
  useEffect(() => {
    const lg = layerGroupRef.current;
    if (!lg) return;
    lg.clearLayers();
    const newMarkers = [];

    (data || []).forEach((item) => {
      const coords = parsePoint(item.gisPoint);
      if (!coords) return;

      const label = (item.sentimentLabel || '').toLowerCase();
      const emoji = SENTIMENT_EMOJI[label] || '\u2753';

      const marker = L.marker(coords, {
        icon: L.divIcon({
          className: '',
          html: `<div class="emoji-marker">${emoji}</div>`,
          iconSize: [30, 30],
          iconAnchor: [15, 15],
        }),
      });

      const safe = {
        origin: escapeHtml(item.origin),
        url: escapeHtml(item.url),
        sentiment: escapeHtml(item.sentimentLabel),
      };
      const href = item.url && /^https?:\/\//.test(item.url) ? safe.url : '#';

      marker.bindPopup(`
        <div class="custom-popup">
          <div class="popup-val"><span class="popup-label">Origin:</span> ${safe.origin}</div>
          <div class="popup-val"><span class="popup-label">URL:</span> <a href="${href}" target="_blank" rel="noopener noreferrer">Link</a></div>
          <div class="popup-val"><span class="popup-label">Sentiment:</span> ${safe.sentiment}</div>
        </div>
      `);
      marker.on('mouseover', function () { this.openPopup(); });

      lg.addLayer(marker);
      newMarkers.push({ marker, searchText: `${item.origin} ${item.sentimentLabel} ${item.url}`.toLowerCase() });
    });

    markersRef.current = newMarkers;
  }, [data]);

  // Search highlighting with debounce built into the controlled input
  const lowerQuery = query.toLowerCase();
  useEffect(() => {
    const timer = setTimeout(() => {
      markersRef.current.forEach(({ marker, searchText }) => {
        const el = marker.getElement();
        if (!el) return;
        const div = el.querySelector('.emoji-marker');
        if (!div) return;
        if (lowerQuery && searchText.includes(lowerQuery)) {
          div.classList.add('highlight-marker');
        } else {
          div.classList.remove('highlight-marker');
        }
      });
    }, 200);
    return () => clearTimeout(timer);
  }, [lowerQuery]);

  return (
    <section className="card map-card">
      <h2>Message Origin Map</h2>
      <div className="search-container">
        <label htmlFor="map-search" className="sr-only">Search origins</label>
        <input
          id="map-search"
          type="text"
          placeholder="Search origins..."
          autoComplete="off"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        {query && (
          <button className="search-clear" onClick={() => setQuery('')} aria-label="Clear search">
            &times;
          </button>
        )}
      </div>
      <div className="map-container">
        <div id="map" ref={mapRef} />
      </div>
    </section>
  );
}
