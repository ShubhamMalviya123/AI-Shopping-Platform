"""Step 3: aspect-based sentiment (lexicon baseline). Swap for a Hugging Face model later."""
import re

ASPECTS = {
    "performance": {"fast", "processor", "lag", "lags", "gaming", "performance", "speed", "coding", "smooth", "slow"},
    "battery": {"battery", "backup", "charging", "drains"},
    "camera": {"camera", "photos", "photo"},
    "quality": {"build", "quality", "sole", "durable", "plastic", "comfortable", "stitching", "body"},
    "heating": {"heat", "heats", "hot"},
}
POS = {"good", "great", "excellent", "fast", "smooth", "amazing", "comfortable", "best", "solid", "lasts", "durable", "clean", "easily", "very", "light"}
NEG = {"bad", "poor", "slow", "heats", "heat", "lag", "lags", "weak", "plastic", "drains", "average", "wears", "noisy", "tight", "small"}

def analyse(reviews: list):
    stats = {}
    for review in reviews:
        for sentence in re.split(r"[.,;!]| but ", review.lower()):
            words = set(re.findall(r"[a-z]+", sentence))
            pos, neg = len(words & POS), len(words & NEG)
            for aspect, kws in ASPECTS.items():
                if words & kws:
                    s = stats.setdefault(aspect, [0, 0])
                    s[0] += pos
                    s[1] += neg
    aspects = {a: {"score": round((p + 1) / (p + n + 2), 2), "mentions": p + n} for a, (p, n) in stats.items() if p + n}
    pros = [f"Reviewers praise the {a}" for a, v in aspects.items() if v["score"] >= 0.6 and a != "heating"]
    cons = [f"Reviewers report {a} issues" for a, v in aspects.items() if v["score"] <= 0.4]
    return aspects, pros, cons


def summarise(pros: list, cons: list) -> str:
    good = [x.replace("Reviewers praise the ", "") for x in pros]
    bad = [x.replace("Reviewers report ", "").replace(" issues", "") for x in cons]
    parts = []
    if good: parts.append("Reviewers praise " + ", ".join(good) + ".")
    if bad: parts.append("Common complaints: " + ", ".join(bad) + ".")
    return " ".join(parts) or "Not enough review text to summarise yet."
