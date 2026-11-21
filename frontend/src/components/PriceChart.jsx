export default function PriceChart({ history, forecast }) {
  const W = 320, H = 80, all = [...history, ...(forecast?.values ?? [])];
  const min = Math.min(...all), max = Math.max(...all), span = max - min || 1;
  const pt = (v, i) => `${(i / (all.length - 1)) * W},${H - 8 - ((v - min) / span) * (H - 16)}`;
  const hist = history.map(pt).join(" ");
  const fut = forecast ? [history.length - 1, ...forecast.values.map((_, i) => history.length + i)].map((i) => pt(all[i], i)).join(" ") : "";
  return (
    <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Price history and forecast" className="chart">
      <polyline points={hist} fill="none" stroke="var(--accent)" strokeWidth="2.5" />
      {fut && <polyline points={fut} fill="none" stroke="var(--hot)" strokeWidth="2.5" strokeDasharray="5 4" />}
    </svg>
  );
}
