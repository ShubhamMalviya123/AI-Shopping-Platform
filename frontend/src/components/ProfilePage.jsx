import { useEffect, useState } from "react";
import { getProfile, saveProfile } from "../api.js";
import { Gate } from "./AlertsPage.jsx";

export default function ProfilePage({ user, needLogin, notify }) {
  const [f, setF] = useState(null);
  useEffect(() => { if (user) getProfile().then(setF).catch((e) => notify(e.message)); }, [user]);
  if (!user) return <Gate onLogin={needLogin} text="Log in to manage your profile." />;
  if (!f) return null;
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  async function save() {
    try {
      await saveProfile({ name: f.name, preferredBrands: f.preferredBrands, budget: f.budget ? Number(f.budget) : null, priorities: f.priorities });
      notify("Profile saved. Your searches are now personalised.");
    } catch (e) { notify(e.message); }
  }
  return (
    <>
      <h1>Your profile.</h1>
      <p className="sub">These preferences personalise your results. Preferred brands get a small ranking boost, and your default budget is used when you do not mention one.</p>
      <div className="formcard">
        <label>Name <input value={f.name} onChange={set("name")} /></label>
        <label>Email <input value={f.email} disabled /></label>
        <label>Preferred brands (comma separated) <input value={f.preferredBrands} onChange={set("preferredBrands")} placeholder="HP, Samsung, Logitech" /></label>
        <label>Default budget (₹) <input type="number" min="0" value={f.budget ?? ""} onChange={set("budget")} /></label>
        <label>Priorities <input value={f.priorities} onChange={set("priorities")} placeholder="battery, camera" /></label>
        <button className="btn" onClick={save}>Save profile</button>
      </div>
    </>
  );
}
