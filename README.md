# Roomies 🏠📋

Roomies is a mobile app for people living in a shared household. The app combines **task management** and a **shared shopping list** in one place, so everyone in the collective has a simple overview of what needs to be done and what needs to be bought.

## Features
- ✅ User authentication (JWT)
- 🧹 Household tasks / chores (create, assign, complete)
- 🛒 Shared shopping list (add, update, mark as bought)
- 👥 Household/group logic (members, shared data)
- 🧪 Automated tests + CI pipeline (GitHub Actions)
- 🐳 Docker setup for local development (database)

## Tech stack
**Backend:** Java / Spring Boot  
**Database:** MySQL  
**Auth:** JWT + refresh tokens  
**Dev tooling:** Docker / docker-compose, GitHub Actions (CI)  
**Frontend (mobile):** React Native

## Project structure
- `src/` – application code
- `.github/workflows/` – CI pipeline
- `docker-compose.yml` – local database setup
- `Dockerfile` – container build for backend
