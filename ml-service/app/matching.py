"""Step 2: product matching. TF-IDF + cosine similarity finds listings that describe the same product."""
import hashlib
import re
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

STOP = {"intel", "amd", "laptop", "processor", "ram", "memory", "ssd", "for", "men", "s", "with", "and", "the", "5g"}
THRESHOLD = 0.5

def _norm(title: str) -> str:
    t = title.lower()
    t = re.sub(r"(\d+)\s*ssd", r"\1gb ", t)          # "512 SSD" -> "512gb"
    t = re.sub(r"(\d+)\s*(gb|tb)", r"\1\2", t)        # "16 GB" -> "16gb"
    t = re.sub(r"[^a-z0-9 ]", " ", t)
    return " ".join(w for w in t.split() if w not in STOP)

def _keys(norm: str) -> set:
    """Tokens containing digits (model numbers, RAM, storage) must not conflict."""
    return {w for w in norm.split() if any(c.isdigit() for c in w)}

def group_listings(listings: list) -> list:
    texts = [_norm(l["title"]) for l in listings]
    sim = cosine_similarity(TfidfVectorizer(ngram_range=(1, 2)).fit_transform(texts))
    keys = [_keys(t) for t in texts]
    parent = list(range(len(listings)))

    def find(i):
        while parent[i] != i:
            parent[i] = parent[parent[i]]
            i = parent[i]
        return i

    for i in range(len(listings)):
        for j in range(i + 1, len(listings)):
            a, b = listings[i], listings[j]
            if a["cat"] != b["cat"] or a["brand"] != b["brand"] or a["marketplace"] == b["marketplace"]:
                continue
            if sim[i][j] >= THRESHOLD and (keys[i] <= keys[j] or keys[j] <= keys[i]):
                parent[find(i)] = find(j)
    groups = {}
    for i, l in enumerate(listings):
        groups.setdefault(find(i), []).append(l)
    return list(groups.values())

def build_products(groups: list) -> list:
    products = []
    for n, g in enumerate(groups, 1):
        g = sorted(g, key=lambda l: l["price"])
        votes = sum(l["rating_count"] for l in g)
        products.append({
            "id": "p" + hashlib.md5("|".join(sorted(l["id"] for l in g)).encode()).hexdigest()[:8], "name": min((l["title"] for l in g), key=len), "brand": g[0]["brand"], "cat": g[0]["cat"],
            "ram": g[0]["ram"], "spec": g[0]["spec"],
            "rating": round(sum(l["rating"] * l["rating_count"] for l in g) / votes, 1), "rating_count": votes,
            "offers": [{"marketplace": l["marketplace"], "price": l["price"], "url": l["url"]} for l in g],
            "listing_ids": [l["id"] for l in g], "low": g[0]["price"], "reviews": [r for l in g for r in l["reviews"]], "history": g[0]["history"],
        })
    return products
