import { useEffect, useState } from "react";
import { recommendations } from "../api.js";
import MiniCard from "./MiniCard.jsx";

export default function Recommendations({ user }) {
  const [items, setItems] = useState([]);
  useEffect(() => { recommendations().then(setItems).catch(() => {}); }, [user]);
  if (!items.length) return null;
  return (
    <section>
      <h2>{user ? "Recommended for you" : "Popular with shoppers"}</h2>
      <div className="minigrid">{items.map((p) => <MiniCard key={p.id} p={p} note={p.reason} />)}</div>
    </section>
  );
}
