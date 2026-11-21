"""AI shopping assistant: understands a request, and for several products builds a basket inside the budget.
Rule-based so it runs without an API key. To add an LLM, send the returned structure to an LLM API for friendlier wording."""
from .nlp import parse_query
from .ranking import rank, public

WEIGHTS = {"laptop": .5, "phone": .5, "shoes": .4, "monitor": .22, "keyboard": .07, "mouse": .05, "headphones": .1}

def _fmt(n): return f"₹{int(n):,}"

def plan(message: str, products: list, profile=None) -> dict:
    req = parse_query(message)
    if not req["categories"]:
        return {"mode": "help", "reply": "Tell me what you need and your budget. For example: “I have ₹70,000. Find me a laptop for programming, a monitor, a keyboard and a mouse.”"}
    if len(req["categories"]) == 1:
        return _single(req, products, profile)
    return _basket(req, products, profile)

def _single(req, products, profile):
    pool, picks = rank(products, req, profile)
    if not pool:
        return {"mode": "search", "reply": f"I could not find a {req['category']} that fits. Try a higher budget.", "products": []}
    best = pool[0]
    return {"mode": "search", "products": [public(p) for p in pool[:3]],
            "reply": f"For your {req['category']} search I recommend {best['name']} at {_fmt(best['low'])} on {best['offers'][0]['marketplace']}. {picks[0]['why']}"}

def _basket(req, products, profile):
    ranked = {}
    for c in req["categories"]:
        r = dict(req, category=c, budget=None, min_ram=req["min_ram"] if c == "laptop" else None)
        pool, _ = rank(products, r, profile)
        if pool:
            ranked[c] = pool
    cats = list(ranked)
    if not cats:
        return {"mode": "basket", "reply": "I could not find those products.", "basket": None}
    budget = req["budget"] or sum(ranked[c][0]["low"] for c in cats)
    wsum = sum(WEIGHTS.get(c, .2) for c in cats)
    share = {c: budget * WEIGHTS.get(c, .2) / wsum for c in cats}
    choice = {c: next((p for p in ranked[c] if p["low"] <= share[c]), min(ranked[c], key=lambda p: p["low"])) for c in cats}
    upgraded = set()
    while True:                                   # spend leftover budget on the biggest quality upgrade
        total = sum(p["low"] for p in choice.values())
        best = None
        for c in cats:
            cur = choice[c]
            for cand in ranked[c][:ranked[c].index(cur)]:
                if total + cand["low"] - cur["low"] <= budget:
                    gain = cand["_score"] - cur["_score"]
                    if best is None or gain > best[0]:
                        best = (gain, c, cand)
        if not best:
            break
        choice[best[1]] = best[2]
        upgraded.add(best[1])
    total = sum(p["low"] for p in choice.values())
    items = []
    for c in cats:
        p = choice[c]
        alt = next((q for q in ranked[c] if q["id"] != p["id"] and q["low"] < p["low"]), None)
        items.append({"category": c, "id": p["id"], "name": p["name"], "price": p["low"], "marketplace": p["offers"][0]["marketplace"],
                      "url": p["offers"][0]["url"], "rating": p["rating"],
                      "why": "Upgraded using leftover budget." if c in upgraded else f"Best-ranked {c} within about {_fmt(share[c])}.",
                      "alternative": {"name": alt["name"], "price": alt["low"]} if alt else None})
    if not req["budget"]:
        reply = f"No budget given, so I picked the best-ranked option in each category. Total {_fmt(total)}."
    elif total <= budget:
        reply = f"Here is a setup within {_fmt(budget)}. Total {_fmt(total)}, leaving {_fmt(budget - total)}."
    else:
        reply = f"Even the cheapest good options cost {_fmt(total)}, which is {_fmt(total - budget)} above your budget."
    return {"mode": "basket", "reply": reply, "basket": {"items": items, "total": total, "budget": req["budget"], "remaining": (budget - total) if req["budget"] else None}}
