import { useEffect, useState } from "react";
import { adminStats, simulateDrop } from "../api.js";

export default function AdminPage() {
  const [s, setS] = useState(null);
  const [changes, setChanges] = useState([]);
  const [error, setError] = useState("");
  const load = () => adminStats().then(setS).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);
  const labels = { users: "Users", searches: "Searches", listings: "Listings", savedProducts: "Saved products", alerts: "Alerts", triggeredAlerts: "Alerts triggered" };
  return (
    <>
      <h1>Admin dashboard.</h1>
      <p className="sub">Monitor the platform and test price intelligence.</p>
      {error && <div className="empty">{error}</div>}
      {s && (<>
        <div className="stats">
          {Object.entries(labels).map(([k, l]) => <div key={k} className="stat"><b>{s[k]}</b><span>{l}</span></div>)}
          <div className="stat"><b className={s.mlService === "ok" ? "okc" : "badc"}>{s.mlService}</b><span>AI service</span></div>
        </div>
        <h2>Top searched categories</h2>
        <div className="chips">{Object.keys(s.topCategories).length === 0 && <span className="meta">No searches yet.</span>}
          {Object.entries(s.topCategories).map(([c, n]) => <span key={c} className="tag">{c}: {n}</span>)}</div>
        <h2>Price simulation</h2>
        <button className="btn" onClick={async () => { const r = await simulateDrop(); setChanges(r.changes); load(); }}>Simulate price drops</button>
        <ul>{changes.map((c) => <li key={c}>{c}</li>)}</ul>
      </>)}
    </>
  );
}
