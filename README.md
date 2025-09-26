# AI_CAR_SALES

자동차 추천 시스템 (MVP) — Ryan Carson 3단계 파일 시스템 방식.
- Backend: Spring Boot 3 + PostgreSQL(Flyway)
- Frontend: React/Vite (PWA)
- Rehydrate: bootstrap/rehydrate.sh → prompt_bundle.md → Codex에 주입

## 빠른 시작
1. **Infra**: `docker compose up -d postgres redis`
2. **Backend**: `./scripts/run_backend.sh`
   - 환경 변수로 `DB_HOST`, `DB_PORT`, `HOST_PORT` 등을 덮어쓸 수 있습니다.
3. **Frontend**:
   ```bash
   cd frontend
   npm install  # 최초 1회 (네트워크 필요)
   npm run dev  # 기본 포트 5173
   ```
4. 브라우저에서 `http://localhost:5173` 접속 → 설문 → 추천/즐겨찾기 플로우 확인

5. **샘플 데이터**: Postgres가 기동된 상태에서 `./scripts/seed_sample_cars.sh` 실행 → `data/samples/cars_valid.csv` 로 차량 기본값 적재
   - 다른 파일을 사용하려면 `CSV_PATH=path/to/file.csv ./scripts/seed_sample_cars.sh`

## 작업 방법
- Active Task: `bootstrap/context.md` (항상 1개)
- 히스토리: `docs/history/STATELOG.md` (append-only)
- 체크리스트: `deliverables/tasks.md`
- Contex.md의 status check : `./scripts/status.sh`
