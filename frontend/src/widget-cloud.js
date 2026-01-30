// src/widget-cloud.js
let cloudDataRef = { daily: [], weekly: [], monthly: [] };
let currentPeriod = 'daily';

function initCloudWidget(dataObj) {
    cloudDataRef = dataObj;
    updateCloud(currentPeriod);
}

// Called by main.js
function refreshCloudWidget(newDataObj) {
    cloudDataRef = newDataObj;
    // Re-render current view with new data
    updateCloud(currentPeriod);
}

function updateCloud(period, btn) {
    if(btn) {
        document.querySelectorAll('#cloud-controls button').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        currentPeriod = period;
    }

    const container = document.getElementById('word-cloud');
    if (!container) return;

    const width = container.offsetWidth || 500;
    const height = 300;

    // Safety check for empty data
    const data = cloudDataRef[period] || [];

    const sizeScale = d3.scaleLinear()
        .domain([d3.min(data, d => d.count) || 0, d3.max(data, d => d.count) || 10])
        .range([20, 80]);

    d3.layout.cloud()
        .size([width, height])
        .words(data.map(d => ({text: d.term, size: sizeScale(d.count)})))
        .padding(5)
        .rotate(() => (~~(Math.random() * 2) * 90))
        .fontSize(d => d.size)
        .on("end", words => {
            d3.select("#word-cloud").selectAll("*").remove();
            d3.select("#word-cloud").append("svg")
                .attr("width", width).attr("height", height)
                .append("g")
                .attr("transform", `translate(${width/2},${height/2})`)
                .selectAll("text").data(words).enter().append("text")
                .style("font-size", d => d.size + "px")
                .style("fill", (d, i) => d3.schemeCategory10[i % 10])
                .attr("text-anchor", "middle")
                .attr("transform", d => `translate(${[d.x, d.y]})rotate(${d.rotate})`)
                .text(d => d.text);
        }).start();
}