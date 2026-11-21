import { useEffect, useState } from "react";
import { me, getSaved, saveProduct, removeSaved, createAlert, setToken, getToken } from "./api.js";
import SearchPage from "./components/SearchPage.jsx";
import AssistantPage from "./components/AssistantPage.jsx";
import ImagePage from "./components/ImagePage.jsx";
import AlertsPage from "./components/AlertsPage.jsx";
import ProfilePage from "./components/ProfilePage.jsx";
import AdminPage from "./components/AdminPage.jsx";
import AuthModal from "./components/AuthModal.jsx";
import SavedPanel from "./components/SavedPanel.jsx";

const TABS = [["search", "Search"], ["assistant", "AI Assistant"], ["image", "Photo search"], ["alerts", "Price alerts"], ["profile", "Profile"]];

export default function App() {
  const [page, setPage] = useState("search");
  const [user, setUser] = useState(null);
  const [showAuth, setShowAuth] = useState(false);
  const [saved, setSaved] = useState([]);
  const [showSaved, setShowSaved] = useState(false);
  const [notice, setNotice] = useState("");

  const notify = (m) => { setNotice(m); setTimeout(() => setNotice(""), 4000); };
  const needLogin = (m) => { if (m) notify(m); setShowAuth(true); };

  useEffect(() => { if (getToken()) me().then(setUser).catch(() => setToken("")); }, []);
  useEffect(() => { if (user) getSaved().then(setSaved).catch(() => {}); else setSaved([]); }, [user]);

  const logout = () => { setToken(""); setUser(null); setPage("search"); };
  async function toggleSave(p) {
    if (!user) return needLogin("Log in to save products.");
    try {
      if (saved.some((s) => s.productId === p.id)) await removeSaved(p.id);
      else await saveProduct({ productId: p.id, name: p.name, price: p.low, marketplace: p.offers[0].marketplace, url: p.offers[0].url });
      setSaved(await getSaved());
    } catch (e) { notify(e.message); }
  }
  async function setAlert(p, target) {
    if (!user) return needLogin("Log in to set price alerts.");
    try {
      await createAlert({ productId: p.id, productName: p.name, targetPrice: Number(target), currentPrice: p.low });
      notify("Alert set for ₹" + Number(target).toLocaleString("en-IN") + ". Check the Price alerts tab.");
    } catch (e) { notify(e.message); }
  }
  const tabs = user?.role === "ADMIN" ? [...TABS, ["admin", "Admin"]] : TABS;

  return (
    <div className="wrap">
      <header className="top">
        <div className="brand"><i />Kharid</div>
        <div className="auth">
          {user && <button className="chip" onClick={() => setShowSaved(true)}>♥ Saved ({saved.length})</button>}
          {user ? <><span className="meta">Hi, {user.name}</span><button className="chip" onClick={logout}>Log out</button></>
            : <button className="btn small" onClick={() => setShowAuth(true)}>Log in / Sign up</button>}
        </div>
      </header>
      <nav className="tabs" aria-label="Sections">
        {tabs.map(([k, l]) => <button key={k} className={"tab" + (page === k ? " on" : "")} onClick={() => setPage(k)}>{l}</button>)}
      </nav>
      {notice && <div className="toast" role="status">{notice}</div>}

      {page === "search" && <SearchPage user={user} saved={saved} toggleSave={toggleSave} setAlert={setAlert} />}
      {page === "assistant" && <AssistantPage />}
      {page === "image" && <ImagePage />}
      {page === "alerts" && <AlertsPage user={user} needLogin={needLogin} />}
      {page === "profile" && <ProfilePage user={user} needLogin={needLogin} notify={notify} />}
      {page === "admin" && user?.role === "ADMIN" && <AdminPage />}

      <p className="note">Sample data for demonstration. In production every price shows its source and last-updated time, and sponsored listings are labelled separately from AI rankings.</p>
      {showAuth && <AuthModal onClose={() => setShowAuth(false)} onAuth={(u) => { setUser(u); setShowAuth(false); notify("Welcome, " + u.name + "."); }} />}
      {showSaved && <SavedPanel items={saved} onRemove={async (id) => { await removeSaved(id); setSaved(await getSaved()); }} onClose={() => setShowSaved(false)} />}
    </div>
  );
}
