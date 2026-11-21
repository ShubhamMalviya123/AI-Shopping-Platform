"""Step 1: turn a natural-language request into structured requirements."""
import re

CATEGORIES = {
    "laptop": r"laptop|notebook",
    "phone": r"\bphone|smartphone|mobile",
    "shoes": r"shoe|sneaker|running",
    "monitor": r"monitor",
    "keyboard": r"keyboard",
    "mouse": r"\bmouse\b|\bmice\b",
    "headphones": r"headphone|earphone|earbud|headset",
}
PRIORITIES = {
    "camera": r"camera|photo",
    "battery": r"battery",
    "performance": r"performan|fast|processor|programming|coding|gaming",
    "quality": r"quality|durable|comfort",
}
MULT = {"k": 1000, "thousand": 1000, "lakh": 100000, "lac": 100000}
BUDGET = re.compile(r"(?:\b(?:under|below|within|upto|up to|budget|rs\.?)|₹)\s*(?:of\s*)?₹?\s*([\d,]+(?:\.\d+)?)\s*(k\b|thousand|lakh|lac)?")
FALLBACK = re.compile(r"\b(\d[\d,]*)(k)?\b(?!\s*(?:gb|tb|mah|mp|hz|inch|ram))")

def _number(text: str) -> float:
    return float(text.replace(",", "").strip(".") or 0)

def parse_query(text: str) -> dict:
    t = text.lower()
    cats = [c for c, rx in CATEGORIES.items() if re.search(rx, t)]
    budget = None
    if m := BUDGET.search(t):
        budget = int(_number(m.group(1)) * MULT.get(m.group(2), 1)) or None
    if budget is None:
        for m in FALLBACK.finditer(t):
            v = _number(m.group(1)) * (1000 if m.group(2) else 1)
            if v >= 1000:
                budget = int(v)
                break
    ram = re.search(r"(\d+)\s*gb\s*ram|ram\s*(\d+)", t)
    return {
        "category": cats[0] if cats else None,
        "categories": cats,
        "budget": budget,
        "min_ram": int(ram.group(1) or ram.group(2)) if ram else None,
        "priorities": [k for k, rx in PRIORITIES.items() if re.search(rx, t)],
        "prefers_rating": bool(re.search(r"rating|rated|reviews", t)),
    }
