# Context (AI_SPRING_CAR)

## 규칙
- 규칙 파일: rules/1_prd_rules.md, rules/2_task_gen_rules.md, rules/3_task_exec_rules.md
- 한 번에 하나의 Task만 수행.

## 현재 상태
- Backend: /health ❌ (접속 불가)
- DB: Postgres ✅ / Redis ✅
- Web: vite dev ❌ (포트 닫힘)
- Last Checked: 2025-09-26 11:52 KST

## Active Task
- Task ID: QA-2: – Frontend 테스트 및 린트 파이프라인
- 목표: Frontend 테스트 및 린트 파이프라인
- DoD(완료조건): Vitest/Testing Library 설정, ESLint/Prettier 구성, GitHub Actions 워크플로 추가, 주요 컴포넌트 테스트 1건 이상
- Deliverable: `vitest.config.ts`, `.eslintrc.cjs`, `.github/workflows/frontend-ci.yml`
- 승인 규칙: 완료 보고 후 승인 받고 다음 Task 이동

