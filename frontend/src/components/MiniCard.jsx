import { rupees } from "../api.js";

export default function MiniCard({ p, note, color }) {
  const o = p.offers[0];
  return (
    <div className="mini">
      <div className="minihead">
        {color && <span className="swatch" style={{ background: `rgb(${color.join(",")})` }} />}
        <b>{p.name}</b>
      </div>
      <div className="meta">★ {p.rating} · {p.rating_count.toLocaleString("en-IN")} reviews</div>
      <div className="pr small">{rupees(p.low)} <span className="at">on {o.marketplace}</span></div>
      {note && <div className="saving">{note}</div>}
      <a className="deal" href={o.url} target="_blank" rel="noreferrer">View deal →</a>
    </div>
  );
}
