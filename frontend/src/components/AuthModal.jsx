import { useState } from "react";
import { login, register, setToken } from "../api.js";

export default function AuthModal({ onClose, onAuth }) {
  const [mode, setMode] = useState("login");
  const [f, setF] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });

  async function submit(e) {
    e.preventDefault(); setBusy(true); setError("");
    try {
      const res = mode === "login" ? await login({ email: f.email, password: f.password }) : await register(f);
      setToken(res.token); onAuth(res.user);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  }
  return (
    <div className="overlay center" onClick={onClose}>
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <div className="panelhead"><h2>{mode === "login" ? "Welcome back" : "Create your account"}</h2><button type="button" className="chip" onClick={onClose}>Close</button></div>
        {mode === "register" && <label>Name <input required value={f.name} onChange={set("name")} /></label>}
        <label>Email <input required type="email" value={f.email} onChange={set("email")} /></label>
        <label>Password <input required type="password" minLength={6} value={f.password} onChange={set("password")} /></label>
        {error && <div className="empty" role="alert">{error}</div>}
        <button className="btn" disabled={busy}>{busy ? "Please wait…" : mode === "login" ? "Log in" : "Sign up"}</button>
        <p className="meta">{mode === "login" ? "New here? " : "Already registered? "}
          <button type="button" className="linkbtn" onClick={() => { setMode(mode === "login" ? "register" : "login"); setError(""); }}>{mode === "login" ? "Create an account" : "Log in"}</button></p>
      </form>
    </div>
  );
}
