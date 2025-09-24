# AI_CAR_SALES

자동차 추천 시스템 (MVP) — Ryan Carson 3단계 파일 시스템 방식.
- Backend: Spring Boot 3 + PostgreSQL(Flyway)
- Frontend: React/Vite (PWA)
- Rehydrate: bootstrap/rehydrate.sh → prompt_bundle.md → Codex에 주입

## 빠른 시작
1) docker compose -f infra/docker-compose.yml up -d
2) backend/ 실행 → GET /health 200
3) web/ 실행 → /onboarding → /results

## 작업 방법
- Active Task: bootstrap/context.md (항상 1개)
- 히스토리: docs/history/STATELOG.md (append-only)
- 체크리스트: deliverables/tasks.md