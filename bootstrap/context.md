# Context (AI_SPRING_CAR)

## 규칙
- 규칙 파일: rules/1_prd_rules.md, rules/2_task_gen_rules.md, rules/3_task_exec_rules.md
- 한 번에 하나의 Task만 수행.

## 현재 상태
- Backend: /health 200 ?
- DB: docker compose up ?
- Web: vite dev ?

## Active Task
- Task ID: M0-1 – 로컬 인프라 기동
- 목표: Docker Compose로 PostgreSQL(+Redis) 컨테이너 기동 및 연결 확인
- DoD(완료조건):
  1) `docker ps`에서 `postgres`와 `redis` 컨테이너가 Up
  2) `pg_isready -h localhost -p 5432 -U app` 가 “accepting connections”
- 산출물: 없음 (환경 기동 확인 로그)
- 승인 규칙: DoD 충족 후 “M0-1 완료” 보고 → 사용자 승인 후 Backend Task 진행