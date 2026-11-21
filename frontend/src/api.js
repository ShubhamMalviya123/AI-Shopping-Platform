let token = localStorage.getItem("kharid_token") || "";
export const getToken = () => token;
export const setToken = (t) => {
  token = t || "";
  if (t) localStorage.setItem("kharid_token", t); else localStorage.removeItem("kharid_token");
};

async function call(url, options = {}) {
  const headers = { ...(options.headers || {}) };
  if (token) headers.Authorization = "Bearer " + token;
  let res;
  try { res = await fetch(url, { ...options, headers }); }
  catch (e) { throw new Error("Cannot reach the Spring Boot API. Run KharidApplication in IntelliJ (port 8080)."); }
  if (!res.ok) {
    let msg = res.status === 401 ? "Please log in first." : "The API returned an error.";
    try { msg = (await res.json()).message || msg; } catch (e) { /* ignore */ }
    const err = new Error(msg); err.status = res.status; throw err;
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}
const json = (method, body) => ({ method, headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });

export const register = (b) => call("/api/auth/register", json("POST", b));
export const login = (b) => call("/api/auth/login", json("POST", b));
export const me = () => call("/api/auth/me");
export const search = (query) => call("/api/search", json("POST", { query }));
export const history = () => call("/api/history");
export const recommendations = () => call("/api/recommendations");
export const assistant = (message) => call("/api/assistant", json("POST", { message }));
export const imageSearch = (file, category, budget) => {
  const f = new FormData();
  f.append("file", file);
  if (category) f.append("category", category);
  if (budget) f.append("budget", budget);
  return call("/api/image-search", { method: "POST", body: f });
};
export const getSaved = () => call("/api/saved");
export const saveProduct = (p) => call("/api/saved", json("POST", p));
export const removeSaved = (id) => call("/api/saved/" + id, { method: "DELETE" });
export const getAlerts = () => call("/api/alerts");
export const createAlert = (a) => call("/api/alerts", json("POST", a));
export const deleteAlert = (id) => call("/api/alerts/" + id, { method: "DELETE" });
export const getProfile = () => call("/api/profile");
export const saveProfile = (p) => call("/api/profile", json("PUT", p));
export const adminStats = () => call("/api/admin/stats");
export const simulateDrop = () => call("/api/admin/simulate-drop", { method: "POST" });
export const rupees = (n) => "₹" + Number(n).toLocaleString("en-IN");
