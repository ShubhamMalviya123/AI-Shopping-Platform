"""Model evaluation (proposal section 16).  Run from ml-service:  python -m evaluation.evaluate"""
import itertools
import numpy as np
from app.seed import load_listings
from app.matching import group_listings
from app.sentiment import analyse
from app.forecast import forecast

def matching_scores():
    listings = load_listings()
    pairs = lambda groups: {frozenset((a["id"], b["id"])) for g in groups for a, b in itertools.combinations(g, 2)}
    truth_groups = {}
    for l in listings:
        truth_groups.setdefault(l["id"].rsplit("-", 1)[0], []).append(l)
    truth, pred = pairs(truth_groups.values()), pairs(group_listings(listings))
    tp = len(truth & pred)
    p, r = tp / max(len(pred), 1), tp / max(len(truth), 1)
    return p, r, 2 * p * r / max(p + r, 1e-9)

LABELLED = [("The battery backup is excellent", "battery", 1), ("Battery drains fast", "battery", 0),
            ("Camera is excellent in daylight", "camera", 1), ("The camera is poor", "camera", 0),
            ("Very fast and smooth performance", "performance", 1), ("The performance is slow", "performance", 0),
            ("Great build quality", "quality", 1), ("Build feels plastic", "quality", 0),
            ("The phone heats while gaming", "heating", 0), ("Comfortable and light", "quality", 1),
            ("Battery is not good", "battery", 0), ("Amazing camera and clear photos", "camera", 1)]

def sentiment_accuracy():
    ok = 0
    for text, aspect, label in LABELLED:
        a = analyse([text])[0].get(aspect)
        ok += int(a is not None and (a["score"] > 0.5) == bool(label))
    return ok / len(LABELLED)

def forecast_mae():
    errs = []
    for l in load_listings():
        h = l["history"]
        errs.append(np.mean(np.abs(np.array(forecast(h[:6], 2)["values"]) - np.array(h[6:8]))))
    return float(np.mean(errs))

if __name__ == "__main__":
    p, r, f = matching_scores()
    print(f"Product matching   precision={p:.2f} recall={r:.2f} F1={f:.2f}")
    print(f"Review sentiment   accuracy={sentiment_accuracy():.2f}  (12 labelled sentences; negation is a known weakness)")
    print(f"Price forecast     MAE=₹{forecast_mae():.0f} on a 2-month hold-out")
