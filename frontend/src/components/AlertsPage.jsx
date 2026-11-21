import { useEffect, useState } from "react";
import { getAlerts, deleteAlert, rupees } from "../api.js";

export default function AlertsPage({ user, needLogin }) {
  const [alerts, setAlerts] = useState(null);
  const [error, setError] = useState("");
  const load = () => getAlerts().then(setAlerts).catch((e) => setError(e.message));
  useEffect(() => { if (user) load(); }, [user]);

  if (!user) return <Gate onLogin={needLogin} text="Log in to create and see price alerts." />;
  return (
    <>
      <h1>Price alerts.</h1>
      <p className="sub">Set an alert on any product card. Prices are re-checked every time you open this page. Admins can simulate price drops to test it.</p>
      {error && <div className="empty">{error}</div>}
      {alerts && alerts.length === 0 && <div className="empty">No alerts yet. Open a product in Search and press “Alert”.</div>}
      {alerts?.map((a) => (
        <div key={a.id} className={"alertrow" + (a.triggered ? " hit" : "")}>
          <div className="grow"><b>{a.productName}</b>
            <div className="meta">Target {rupees(a.targetPrice)} · Current lowest {a.currentPrice ? rupees(a.currentPrice) : "unknown"}</div></div>
          <span className={"trend " + (a.triggered ? "falling" : "")}>{a.triggered ? "Target reached" : "Watching"}</span>
          <button className="chip" onClick={async () => { await deleteAlert(a.id); load(); }}>Delete</button>
        </div>
      ))}
    </>
  );
}

export function Gate({ onLogin, text }) {
  return <div className="empty">{text} <button className="chip" onClick={() => onLogin()}>Log in</button></div>;
}
