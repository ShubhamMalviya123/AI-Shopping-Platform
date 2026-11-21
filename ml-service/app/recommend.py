"""Personalised recommendations (content-based): preferred brands + recent search categories + rating."""
import math
from collections import Counter
from .ranking import public

def recommend(products: list, brands: list, categories: list, k: int = 6) -> list:
    brands = {b.lower() for b in brands}
    freq = Counter(categories)
    total = sum(freq.values()) or 1
    scored = []
    for p in products:
        liked = p["brand"].lower() in brands
        f = freq.get(p["cat"], 0) / total
        score = 0.45 * p["rating"] / 5 + 0.3 * liked + 0.3 * f + 0.05 * min(math.log10(p["rating_count"]) / 4.3, 1)
        reason = f"Matches your preferred brand {p['brand']}" if liked else (f"You searched for {p['cat']} recently" if f else "Highly rated by shoppers")
        scored.append((score, p, reason))
    scored.sort(key=lambda t: -t[0])
    return [dict(public(p), reason=r) for _, p, r in scored[:k]]
