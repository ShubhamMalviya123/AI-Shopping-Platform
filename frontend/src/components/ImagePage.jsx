import { useState } from "react";
import { imageSearch } from "../api.js";
import MiniCard from "./MiniCard.jsx";

const CATS = ["", "laptop", "phone", "shoes", "monitor", "keyboard", "mouse", "headphones"];

export default function ImagePage() {
  const [file, setFile] = useState(null);
  const [preview, setPreview] = useState("");
  const [category, setCategory] = useState("");
  const [budget, setBudget] = useState("");
  const [res, setRes] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  function pick(f) {
    setFile(f); setRes(null); setError("");
    setPreview(f ? URL.createObjectURL(f) : "");
  }
  async function go() {
    if (!file) return;
    setBusy(true); setError("");
    try { setRes(await imageSearch(file, category, budget)); }
    catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }
  return (
    <>
      <h1>Search with a photo.</h1>
      <p className="sub">Upload a picture of a product and optionally set a category and budget. The demo compares colour and visual features. With real product images, the same pipeline can use CNN embeddings.</p>
      <div className="formcard">
        <input type="file" accept="image/*" onChange={(e) => pick(e.target.files[0])} aria-label="Choose an image" />
        {preview && <img className="preview" src={preview} alt="Your upload" />}
        <div className="formrow">
          <label>Category <select value={category} onChange={(e) => setCategory(e.target.value)}>{CATS.map((c) => <option key={c} value={c}>{c || "Any"}</option>)}</select></label>
          <label>Max budget (₹) <input type="number" min="0" value={budget} onChange={(e) => setBudget(e.target.value)} /></label>
        </div>
        <button className="btn" disabled={!file || busy} onClick={go}>{busy ? "Searching…" : "Find similar products"}</button>
      </div>
      {error && <div className="empty" role="alert">{error}</div>}
      {res && (<>
        <h2>Visually similar products</h2>
        {res.products.length === 0 && <div className="empty">No products match those filters.</div>}
        <div className="minigrid">{res.products.map((p) => <MiniCard key={p.id} p={p} color={p.color} note={`${p.similarity}% visual match`} />)}</div>
      </>)}
    </>
  );
}
