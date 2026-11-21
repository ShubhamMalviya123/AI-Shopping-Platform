import io
from fastapi.testclient import TestClient
from PIL import Image
from app.main import app, build_catalogue
from app.nlp import parse_query
from app.ranking import rank
from app.seed import load_listings
from app.assistant import plan

Q = "I need a laptop under ₹60,000 for programming, with at least 16GB RAM and good battery life"
client = TestClient(app)

def catalogue():
    return build_catalogue(load_listings())

def test_matching_merges_same_product():
    ps = catalogue()
    assert len(ps) == 22
    assert all(len(p["offers"]) == 2 for p in ps)
    assert len({p["id"] for p in ps}) == 22

def test_query_understanding():
    r = parse_query(Q)
    assert (r["category"], r["budget"], r["min_ram"]) == ("laptop", 60000, 16)
    assert set(r["priorities"]) == {"performance", "battery"}
    assert parse_query("phone under 20k")["budget"] == 20000
    assert parse_query("I have 50000 for headphones")["budget"] == 50000
    assert parse_query("headphones")["category"] == "headphones"

def test_ranking_respects_budget_and_ram():
    pool, picks = rank(catalogue(), parse_query(Q))
    assert pool and all(p["low"] <= 60000 and p["ram"] >= 16 for p in pool)
    assert [x["label"] for x in picks] == ["Best overall", "Cheapest", "Best rated", "Best value"]

def test_personalisation_boosts_brand():
    pool, _ = rank(catalogue(), parse_query(Q), {"brands": {"acer"}})
    assert next(p for p in pool if p["brand"] == "Acer")["boosted"]

def test_assistant_basket_within_budget():
    r = plan("I have ₹70,000. Find me a laptop for programming, a monitor, a keyboard and a mouse.", catalogue())
    b = r["basket"]
    assert len(b["items"]) == 4 and b["total"] <= 70000 and b["remaining"] >= 0

def test_image_search_finds_blue_product():
    buf = io.BytesIO(); Image.new("RGB", (80, 80), (30, 65, 150)).save(buf, "PNG")
    res = client.post("/ml/image-search", files={"file": ("x.png", buf.getvalue(), "image/png")}, data={"category": "shoes"})
    assert res.status_code == 200 and res.json()["products"][0]["brand"] == "Campus"

def test_api_roundtrip():
    r = client.post("/ml/analyze", json={"query": "smartphone under 20k with good camera"}).json()
    assert r["products"] and r["picks"][0]["label"] == "Best overall"
