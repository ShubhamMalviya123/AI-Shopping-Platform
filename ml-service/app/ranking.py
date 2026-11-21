"""Step 4: weighted ranking engine. Later: train XGBoost / learning-to-rank on click data."""
import math
from typing import Optional

def _fmt(n): return f"₹{n:,}"

def _attr(p, req):
    keys = [k for k in (req["priorities"] or p["spec"]) if k in p["spec"]] or list(p["spec"])
    vals = []
    for k in keys:
        s, sen = p["spec"][k] / 10, p["sentiment"].get(k)
        vals.append(0.65 * s + 0.35 * sen["score"] if sen else s)   # blend specs with review sentiment
    return sum(vals) / len(vals), keys

def rank(products: list, req: dict, profile: Optional[dict] = None):
    pool = [p for p in products if p["cat"] == req["category"]
            and (not req["budget"] or p["low"] <= req["budget"])
            and (not req["min_ram"] or not p["ram"] or p["ram"] >= req["min_ram"])]
    w_rating = 0.4 if req["prefers_rating"] else 0.3
    for p in pool:
        attr, p["_keys"] = _attr(p, req)
        trust = min(math.log10(p["rating_count"]) / 4.3, 1)
        fit = max(1 - p["low"] / req["budget"], 0) if req["budget"] else 0.5
        p["_score"] = 0.35 * attr + w_rating * p["rating"] / 5 + 0.1 * trust + (0.55 - w_rating) * fit
        p["_value"] = (0.5 * attr + 0.5 * p["rating"] / 5) / p["low"]
        p["boosted"] = bool(profile and p["brand"].lower() in profile.get("brands", set()))
        if p["boosted"]:
            p["_score"] += 0.06          # personalisation: preferred brand
    pool.sort(key=lambda p: -p["_score"])
    if not pool:
        return [], []
    top = max(p["_score"] for p in pool)
    for p in pool:
        p["match"] = round(p["_score"] / top * 100)
    cheap = min(pool, key=lambda p: p["low"])
    rated = max(pool, key=lambda p: (p["rating"], p["rating_count"]))
    value = max(pool, key=lambda p: p["_value"])
    best = pool[0]
    picks = [
        ("Best overall", best, f"Balances a {best['rating']}/5 rating, {_fmt(best['low'])} and strong {', '.join(best['_keys'])} signals from specs and reviews."),
        ("Cheapest", cheap, f"Lowest price in your results at {_fmt(cheap['low'])} on {cheap['offers'][0]['marketplace']}."),
        ("Best rated", rated, f"Highest customer rating ({rated['rating']}/5 from {rated['rating_count']:,} reviews)."),
        ("Best value", value, f"Most quality per rupee: {value['rating']}/5 rating at {_fmt(value['low'])}."),
    ]
    return pool, [{"label": l, "product_id": p["id"], "why": w} for l, p, w in picks]


FIELDS = ("id", "name", "brand", "rating", "rating_count", "offers", "low", "pros", "cons", "summary",
          "history", "forecast", "match", "boosted")

def public(p: dict) -> dict:
    return {k: p.get(k) for k in FIELDS}
