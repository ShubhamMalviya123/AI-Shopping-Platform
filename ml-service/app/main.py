import io
import json
from typing import Optional
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from PIL import Image
from pydantic import BaseModel, Field

from .seed import load_listings
from .matching import group_listings, build_products
from .sentiment import analyse, summarise
from .nlp import parse_query
from .ranking import rank, public
from .forecast import forecast
from .assistant import plan
from .recommend import recommend
from .vision import features, dominant_rgb, product_color, similarity

app = FastAPI(title="Kharid ML Service", version="3.0.0")

class Listing(BaseModel):
    id: str
    marketplace: str
    title: str
    brand: str
    cat: str
    price: int
    rating: float
    rating_count: int
    ram: Optional[int] = None
    spec: dict[str, float] = {}
    reviews: list[str] = []
    history: list[int] = []
    url: str = ""

class Profile(BaseModel):
    preferred_brands: list[str] = []
    default_budget: Optional[int] = None

class AnalyzeIn(BaseModel):
    query: str = Field(min_length=3, max_length=300)
    listings: Optional[list[Listing]] = None
    profile: Optional[Profile] = None

class AssistantIn(BaseModel):
    message: str = Field(min_length=3, max_length=500)
    listings: Optional[list[Listing]] = None
    profile: Optional[Profile] = None

class RecommendIn(BaseModel):
    listings: Optional[list[Listing]] = None
    brands: list[str] = []
    categories: list[str] = []

class CatalogueIn(BaseModel):
    listings: Optional[list[Listing]] = None

def _listings(items) -> list:
    return [l.model_dump() for l in items] if items else load_listings()

def _profile(p: Optional[Profile]) -> dict:
    p = p or Profile()
    return {"brands": {b.lower() for b in p.preferred_brands}, "default_budget": p.default_budget}

def build_catalogue(listings: list) -> list:
    """Raw marketplace listings -> merged products with sentiment, summary and price forecast."""
    products = build_products(group_listings(listings))
    for p in products:
        p["sentiment"], p["pros"], p["cons"] = analyse(p["reviews"])
        p["summary"] = summarise(p["pros"], p["cons"])
        p["forecast"] = forecast(p["history"])
    return products

@app.get("/ml/health")
def health():
    return {"status": "ok"}

@app.get("/ml/sample-listings")
def sample_listings():
    return load_listings()

@app.post("/ml/analyze")
def analyze(body: AnalyzeIn):
    prof = _profile(body.profile)
    req = parse_query(body.query)
    if not req["category"]:
        return {"requirements": req, "picks": [], "products": [],
                "message": "Mention what you want to buy, for example a laptop, running shoes, a smartphone, a monitor, a keyboard, a mouse or headphones."}
    if not req["budget"] and prof["default_budget"]:
        req["budget"], req["budget_from_profile"] = prof["default_budget"], True
    pool, picks = rank(build_catalogue(_listings(body.listings)), req, prof)
    return {"requirements": req, "picks": picks, "products": [public(p) for p in pool], "message": None}

@app.post("/ml/assistant")
def assistant(body: AssistantIn):
    return plan(body.message, build_catalogue(_listings(body.listings)), _profile(body.profile))

@app.post("/ml/recommend")
def recommend_api(body: RecommendIn):
    return recommend(build_catalogue(_listings(body.listings)), body.brands, body.categories)

@app.post("/ml/catalogue")
def catalogue(body: CatalogueIn):
    return [{"id": p["id"], "name": p["name"], "low": p["low"], "marketplace": p["offers"][0]["marketplace"]}
            for p in build_catalogue(_listings(body.listings))]

@app.post("/ml/image-search")
async def image_search(file: UploadFile = File(...), listings: str = Form("[]"), category: str = Form(""), budget: int = Form(0)):
    try:
        img = Image.open(io.BytesIO(await file.read()))
        img.load()
    except Exception:
        raise HTTPException(status_code=400, detail="That file is not a readable image.")
    products = build_catalogue(json.loads(listings) or load_listings())
    qh, dom = features(img), dominant_rgb(img)
    out = []
    for p in products:
        if (category and p["cat"] != category) or (budget and p["low"] > budget):
            continue
        rgb = product_color(p["listing_ids"][0])
        out.append((similarity(qh, dom, rgb), p, rgb))
    out.sort(key=lambda t: -t[0])
    return {"dominant_color": list(dom),
            "products": [dict(public(p), similarity=round(s * 100), color=list(rgb)) for s, p, rgb in out[:6]]}
