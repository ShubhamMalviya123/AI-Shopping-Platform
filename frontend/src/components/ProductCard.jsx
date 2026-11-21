import { useState } from "react";
import { rupees } from "../api.js";
import PriceChart from "./PriceChart.jsx";
const COLORS = { Amazon: "#F59E0B", Flipkart: "#2874F0", Meesho: "#9F2089" };

export default function ProductCard({ p, rank, saved, onToggleSave, onSetAlert }) {
  const [alertOpen, setAlertOpen] = useState(false);
  const [target, setTarget] = useState(Math.round(p.low * 0.95));
  const high = p.offers[p.offers.length - 1].price, saving = high - p.low;
  return (
    <article className="row">
      <div className="rowtop">
        <div className="rank">{rank}</div>
        <div className="grow">
          <h3>{p.name}</h3>
          <div className="meta">★ {p.rating} · {p.rating_count.toLocaleString("en-IN")} reviews</div>
          {p.boosted && <span className="badge">Matches your preferred brand</span>}
          {saving > 0 && <div className="saving">Save {rupees(saving)} by buying on {p.offers[0].marketplace}</div>}
          {p.summary && <p className="summary">{p.summary}</p>}
        </div>
        <div className="score"><b>{p.match}</b><span className="meta">match</span>
          <div className="bar"><span style={{ width: p.match + "%" }} /></div></div>
      </div>
      <div className="offers">
        {p.offers.map((o, i) => (
          <a key={o.marketplace} href={o.url} target="_blank" rel="noreferrer" className={"offer" + (i === 0 ? " low" : "")}>
            <span className="dot" style={{ background: COLORS[o.marketplace] }} />{o.marketplace} <b>{rupees(o.price)}</b>{i === 0 && <em>lowest</em>}
          </a>
        ))}
        <button className="save" onClick={() => setAlertOpen(!alertOpen)}>🔔 Alert</button>
        <button className={"save" + (saved ? " on" : "")} onClick={onToggleSave} aria-pressed={saved}>{saved ? "♥ Saved" : "♡ Save"}</button>
      </div>
      {alertOpen && (
        <div className="alertbox">
          <label>Tell me when the price is at or below ₹ <input type="number" min="1" value={target} onChange={(e) => setTarget(e.target.value)} /></label>
          <button className="chip" onClick={() => { onSetAlert(p, target); setAlertOpen(false); }}>Set alert</button>
        </div>
      )}
      <details>
        <summary>Review insights and price trend</summary>
        <div className="why">
          <div><b>What reviewers say</b>
            {p.pros.length + p.cons.length === 0 && <p className="meta">Not enough review text yet.</p>}
            <ul className="pros">{p.pros.map((x) => <li key={x}>{x}</li>)}</ul>
            <ul className="cons">{p.cons.map((x) => <li key={x}>{x}</li>)}</ul></div>
          <div><b>Price: last 8 months and next 3</b>
            <PriceChart history={p.history} forecast={p.forecast} />
            {p.forecast && <div className="meta"><span className={"trend " + p.forecast.trend}>{p.forecast.trend}</span> {p.forecast.advice}</div>}</div>
        </div>
      </details>
    </article>
  );
}
