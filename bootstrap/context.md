# Context (AI_SPRING_CAR)

## 규칙
- 규칙 파일: rules/1_prd_rules.md, rules/2_task_gen_rules.md, rules/3_task_exec_rules.md
- 한 번에 하나의 Task만 수행.

## 현재 상태
- Backend: /health ✅ (200 OK)
- DB: Postgres ✅ / Redis ✅
- Web: vite dev ✅ (포트 5173 응답)
- Last Checked: 2025-09-25 11:16 KST

## Active Task
- Task ID: FE-1 – Vite + React 초기화
- 목표: Vite + React 초기화
- DoD(완료조건): Vite 프로젝트 생성, React Router 기본 구성, PWA(Service Worker, manifest) 설정, CI 빌드 통과
- Deliverable: `package.json`, `src/main.tsx`, `public/manifest.json`
- 승인 규칙: 완료 보고 후 승인 받고 다음 Task 이동

