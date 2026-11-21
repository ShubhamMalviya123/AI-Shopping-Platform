import { rupees } from "../api.js";

export default function SavedPanel({ items, onRemove, onClose }) {
  return (
    <div className="overlay" onClick={onClose}>
      <aside className="panel" onClick={(e) => e.stopPropagation()} aria-label="Saved products">
        <div className="panelhead"><h2>Saved products</h2><button className="chip" onClick={onClose}>Close</button></div>
        {items.length === 0 && <p className="meta">Nothing saved yet. Tap “Save” on any product to keep it here.</p>}
        {items.map((s) => (
          <div key={s.productId} className="saved">
            <div className="grow"><b>{s.name}</b><div className="meta">{rupees(s.price)} on {s.marketplace}</div></div>
            <a className="chip" href={s.url} target="_blank" rel="noreferrer">Open</a>
            <button className="chip" onClick={() => onRemove(s.productId)}>Remove</button>
          </div>
        ))}
      </aside>
    </div>
  );
}
