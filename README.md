# Kharid: AI Shopping Intelligence Platform
React + Spring Boot (Security, JWT, JPA) + MySQL + Python AI/ML (FastAPI, scikit-learn, Pillow)

```
React (5173)  ->  Spring Boot API (8080)  ->  Python AI service (8000)
                   JWT login, validation,       NLP, product matching (TF-IDF), review sentiment,
                   MySQL (3306)                 ranking, price forecast, assistant, image search,
                                                recommendations
```

## Quick start (in this order)
1. **MySQL 8** on port 3306, user `root`, password `root`.
   No MySQL? Run `docker compose up -d` here. Different password? Edit `springboot-api/src/main/resources/application.properties`.
   If you ran an earlier version of this project, first run `DROP DATABASE kharid;` in MySQL (the tables changed).
2. **AI service:** double-click `start-ml-service.bat` (Windows) or run `./start-ml-service.sh`. Keep it open.
3. **Spring Boot:** IntelliJ > File > Open > `springboot-api/pom.xml` > Open as Project > wait for Maven > run `KharidApplication` (JDK 17+).
4. **React:** double-click `start-frontend.bat` (or `./start-frontend.sh`) and open http://localhost:5173

Check: http://localhost:8080/api/health must show `"mlService":"ok"`.
Admin login: `admin@kharid.com` / `admin123` (change in application.properties). Everyone else: use Sign up.

## Features (mapped to your proposal)
| Proposal module | Where |
|---|---|
| 1 User management: register, login, profile, preferences | Spring Security + JWT, `AuthController`, `ProfileController`, React Profile tab |
| 2 Data collection and normalisation | `ListingService` stores listings, reviews, price history in MySQL; sample data loads on first search |
| 3 Intelligent search: natural language, budget, features | `nlp.py`, Search tab, voice input (Chrome/Edge, English) |
| 4 Product matching and duplicates | `matching.py` (TF-IDF + cosine + model/RAM token check) |
| 5 Price comparison | offers per marketplace, savings, lowest price |
| 6 AI recommendation | `ranking.py`: Best overall, Cheapest, Best rated, Best value, with reasons |
| 7 Review intelligence | `sentiment.py`: aspect sentiment, pros, cons, review summary |
| 8 Personalisation | preferred brands boost ranking, default budget, "Recommended for you" (`recommend.py`) |
| 9 Price intelligence | price chart, 3-month forecast with buy/wait advice, price alerts, admin price-drop simulator |
| 10 Image search | Photo search tab (`vision.py`, colour and histogram features) |
| 11 AI shopping assistant | `assistant.py`: multi-product basket inside your budget with upgrades and cheaper alternatives |
| 12 Admin and monitoring | Admin tab: counts, top categories, AI service status |

## Database tables (MySQL, created automatically)
listing, review, price_point, app_user, user_preference, saved_product, price_alert, search_log

## Tests and model evaluation
```
cd ml-service
python -m pytest                      # 7 tests
python -m evaluation.evaluate         # matching F1, sentiment accuracy, forecast MAE
```
Results on the bundled sample data: matching F1 1.00, sentiment accuracy 0.92 (12 sentences), forecast MAE about ₹242.
These are small demo sets; measure again on real data.

## API (Spring Boot)
Public: POST /api/auth/register, /api/auth/login, /api/search, /api/assistant, /api/image-search, GET /api/recommendations, /api/history, /api/health
Logged in: GET /api/auth/me, GET and PUT /api/profile, GET/POST/DELETE /api/saved, GET/POST/DELETE /api/alerts
Admin: GET /api/admin/stats, POST /api/admin/simulate-drop

## Troubleshooting
- Access denied for user root: wrong MySQL password in application.properties.
- Communications link failure: MySQL is not running.
- No MySQL at all: IntelliJ > Run > Edit Configurations > KharidApplication > Active profiles: `h2`.
- "AI service is not running": do step 2 first.
- Python not found on Windows: install Python 3.10+ and tick "Add to PATH".
- Mic button missing: use Chrome or Edge.

## Honest limits
- All products, prices, reviews and links are sample data. Marketplace links are placeholders.
- Real marketplace data needs official or affiliate API access (check each site's terms; do not scrape). Replace `ListingService.ensureSeeded()` with an importer.
- Image search compares colour and visual features of stand-in product thumbnails. For real accuracy use product photos and CNN or ViT embeddings with FAISS.
- The assistant is rule-based, so it needs no API key. To make it chatty, pass its structured answer to an LLM API.
- Query understanding and voice are English only.
- Sentiment is a lexicon baseline (weak on negation). Upgrade to a Hugging Face aspect model.
