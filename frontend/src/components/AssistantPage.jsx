import { useRef, useState } from "react";
import { assistant, rupees } from "../api.js";
import MiniCard from "./MiniCard.jsx";
import VoiceButton from "./VoiceButton.jsx";

const IDEAS = [
  "I have ₹70,000. Find me the best laptop for programming, a monitor, a keyboard and a mouse.",
  "I have ₹50,000 to set up work from home: laptop, monitor, keyboard and mouse",
  "Best wireless headphones under ₹2,000 with good battery",
];

export default function AssistantPage() {
  const [msgs, setMsgs] = useState([{ role: "bot", text: "Hi, I am your shopping assistant. Tell me what you need and your total budget. I can plan a full set, like a laptop plus monitor, keyboard and mouse, and keep the total inside your budget." }]);
  const [text, setText] = useState("");
  const [busy, setBusy] = useState(false);
  const end = useRef(null);

  async function send(t = text) {
    if (!t.trim() || busy) return;
    setMsgs((m) => [...m, { role: "me", text: t }]); setText(""); setBusy(true);
    try {
      const r = await assistant(t);
      setMsgs((m) => [...m, { role: "bot", text: r.reply, basket: r.basket, products: r.products }]);
    } catch (e) { setMsgs((m) => [...m, { role: "bot", text: e.message }]); }
    finally { setBusy(false); setTimeout(() => end.current?.scrollIntoView({ behavior: "smooth" }), 50); }
  }

  return (
    <>
      <h1>Your AI shopping assistant.</h1>
      <p className="sub">Ask in plain English. For several products, it splits your budget, picks the best item in each category and spends any leftover on upgrades.</p>
      <div className="chat">
        {msgs.map((m, i) => (
          <div key={i} className={"bubble " + m.role}>
            <p>{m.text}</p>
            {m.basket && (
              <div className="basket">
                {m.basket.items.map((it) => (
                  <div key={it.id} className="bitem">
                    <div className="grow"><small>{it.category}</small><b>{it.name}</b>
                      <div className="meta">{it.why}{it.alternative && ` Cheaper option: ${it.alternative.name} (${rupees(it.alternative.price)}).`}</div></div>
                    <div className="bprice">{rupees(it.price)}<a href={it.url} target="_blank" rel="noreferrer">{it.marketplace}</a></div>
                  </div>
                ))}
                <div className="btotal"><span>Total {rupees(m.basket.total)}</span>
                  {m.basket.budget && <span>Budget {rupees(m.basket.budget)} · Left {rupees(m.basket.remaining)}</span>}</div>
              </div>
            )}
            {m.products?.length > 0 && <div className="minigrid">{m.products.map((p) => <MiniCard key={p.id} p={p} />)}</div>}
          </div>
        ))}
        {busy && <div className="bubble bot"><p>Thinking…</p></div>}
        <div ref={end} />
      </div>
      <div className="chips">{IDEAS.map((x) => <button key={x} className="chip" onClick={() => send(x)}>{x.slice(0, 50)}…</button>)}</div>
      <div className="search">
        <textarea aria-label="Message the assistant" value={text} onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => { if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); send(); } }} placeholder="e.g. I have ₹70,000. Find me a laptop, monitor, keyboard and mouse." />
        <div className="stack"><VoiceButton onText={(t) => send(t)} /><button className="btn" onClick={() => send()} disabled={busy}>Send</button></div>
      </div>
    </>
  );
}
