#!/bin/sh
cd "$(dirname "$0")/ml-service"
[ -d .venv ] || python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --port 8000
