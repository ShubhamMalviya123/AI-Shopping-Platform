import { useEffect, useMemo, useState } from "react";
import { search, history, rupees } from "../api.js";
import ProductCard from "./ProductCard.jsx";
import Recommendations from "./Recommendations.jsx";
import VoiceButton from "./VoiceButton.jsx";

const EXAMPLES = [
  "I need a laptop under ₹60,000 for programming, with at least 16GB RAM and good battery life",
  "Running shoes under ₹2,000 with good quality and high customer ratings",
  "Smartphone under ₹20,000 with a good camera and battery",
];
const SORTS = { match: (a, b) => b.match - a.match, price: (a, b) => a.low - b.low, rating: (a, b) => b.rating - a.rating };

export default function SearchPage({ user, saved, toggleSave, setAlert }) {
  const [q, setQ] = useState(EXAMPLES[0]);
  const [data, setData] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [hist, setHist] = useState([]);
  const [sort, setSort] = useState("match");

  const loadHist = () => history().then(setHist).catch(() => {});
  useEffect(() => { loadHist(); }, [user]);

  async function run(text = q) {
    setBusy(true); setError("");
    try { setData(await search(text)); loadHist(); }
    catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }
  const r = data?.requirements;
  const byId = useMemo(() => Object.fromEntries((data?.products ?? []).map((p) => [p.id, p])), [data]);
  const products = useMemo(() => [...(data?.products ?? [])].sort(SORTS[sort]), [data, sort]);

  return (
    <>
      <h1>Describe what you need. We find what’s best.</h1>
      <p className="sub">Write your budget and priorities the way you would tell a friend, or just speak. Kharid compares offers across marketplaces, reads the reviews for you and explains every pick.</p>
      <div className="search">
        <textarea aria-label="Describe what you want to buy" value={q} onChange={(e) => setQ(e.target.value)}
          onKeyDown={(e) => { if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); run(); } }} />
        <button className="btn" onClick={() => run()} disabled={busy}>{busy ? "Searching…" : "Find best product"}</button>
      </div>
      <div className="chips">
        <VoiceButton onText={(t) => { setQ(t); run(t); }} />
        {EXAMPLES.map((x) => <button key={x} className="chip" onClick={() => { setQ(x); run(x); }}>{x.slice(0, 44)}…</button>)}
      </div>
      {hist.length > 0 && <div className="chips"><span className="meta">Recent searches:</span>{hist.map((h) => <button key={h.id} className="chip" onClick={() => { setQ(h.query); run(h.query); }}>{h.query.slice(0, 40)}</button>)}</div>}
      {error && <div className="empty" role="alert">{error}</div>}
      {busy && <div className="skeletons"><div className="sk" /><div className="sk" /><div className="sk" /></div>}
      {!busy && !r && <Recommendations user={user} />}

      {!busy && r && (<>
        <div className="read"><b>What I understood</b><br />
          {r.category && <span className="tag">Category: {r.category}</span>}
          {r.budget && <span className="tag">Budget: up to {rupees(r.budget)}{r.budget_from_profile ? " (from your profile)" : ""}</span>}
          {r.min_ram && <span className="tag">RAM: {r.min_ram}GB or more</span>}
          {r.priorities.map((x) => <span key={x} className="tag">Priority: {x}</span>)}
          {r.prefers_rating && <span className="tag">Priority: customer ratings</span>}
        </div>
        {data.message && <div className="empty">{data.message}</div>}
        {!data.message && data.products.length === 0 && <div className="empty">Nothing matches your filters. Try a higher budget or a lower RAM requirement.</div>}
        {data.products.length > 0 && (<>
          <h2>Our picks for you</h2>
          <div className="picks">
            {data.picks.map((k, i) => { const p = byId[k.product_id]; return (
              <div key={k.label} className={"pick" + (i === 0 ? " main" : "")}>
                <small>{k.label}</small>
                <h3>{p.name}</h3>
                <div className="pr">{rupees(p.low)}</div>
                <div className="at">on {p.offers[0].marketplace}</div>
                <p className="why-text">{k.why}</p>
                <a className="deal" href={p.offers[0].url} target="_blank" rel="noreferrer">View deal →</a>
              </div>); })}
          </div>
          <div className="toolbar">
            <h2>All matches ({products.length})</h2>
            <label className="meta">Sort by{" "}
              <select value={sort} onChange={(e) => setSort(e.target.value)}>
                <option value="match">Best match</option><option value="price">Lowest price</option><option value="rating">Highest rating</option>
              </select></label>
          </div>
          {products.map((p, i) => <ProductCard key={p.id} p={p} rank={i + 1} saved={saved.some((s) => s.productId === p.id)}
            onToggleSave={() => toggleSave(p)} onSetAlert={setAlert} />)}
        </>)}
      </>)}
    </>
  );
}
