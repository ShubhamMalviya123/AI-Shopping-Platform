@echo off
cd /d "%~dp0ml-service"
if not exist .venv ( python -m venv .venv || py -m venv .venv )
call .venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --port 8000
pause
