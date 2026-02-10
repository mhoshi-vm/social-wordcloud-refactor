import { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';
import cloud from 'd3-cloud';

const PERIODS = ['daily', 'weekly', 'monthly'];

export default function CloudWidget({ data }) {
  const containerRef = useRef(null);
  const [period, setPeriod] = useState('daily');

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const words = (data?.[period] || []).filter((d) => d.term && d.count > 0);
    if (words.length === 0) {
      d3.select(container).selectAll('*').remove();
      d3.select(container)
        .append('div')
        .attr('class', 'empty-state')
        .text('No term data available');
      return;
    }

    const width = container.offsetWidth || 500;
    const height = 300;

    const minCount = d3.min(words, (d) => d.count);
    const maxCount = d3.max(words, (d) => d.count);
    const sizeScale = d3.scaleLinear()
      .domain([minCount, minCount === maxCount ? minCount + 1 : maxCount])
      .range([16, 72]);

    cloud()
      .size([width, height])
      .words(words.map((d) => ({ text: d.term, size: sizeScale(d.count) })))
      .padding(4)
      .rotate(() => (~~(Math.random() * 2)) * 90)
      .fontSize((d) => d.size)
      .on('end', (laid) => {
        d3.select(container).selectAll('*').remove();
        const svg = d3.select(container)
          .append('svg')
          .attr('width', width)
          .attr('height', height)
          .attr('role', 'img')
          .attr('aria-label', `Word cloud showing ${period} term frequency`);

        svg.append('g')
          .attr('transform', `translate(${width / 2},${height / 2})`)
          .selectAll('text')
          .data(laid)
          .enter()
          .append('text')
          .style('font-size', (d) => `${d.size}px`)
          .style('fill', (_, i) => d3.schemeTableau10[i % 10])
          .attr('text-anchor', 'middle')
          .attr('transform', (d) => `translate(${d.x},${d.y})rotate(${d.rotate})`)
          .text((d) => d.text);
      })
      .start();
  }, [data, period]);

  return (
    <section className="card">
      <h2>Term Frequency Analysis</h2>
      <div className="controls" role="group" aria-label="Time period">
        {PERIODS.map((p) => (
          <button
            key={p}
            className={period === p ? 'active' : ''}
            onClick={() => setPeriod(p)}
            aria-pressed={period === p}
          >
            {p.charAt(0).toUpperCase() + p.slice(1)}
          </button>
        ))}
      </div>
      <div ref={containerRef} className="cloud-container" />
    </section>
  );
}
