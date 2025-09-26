# Context (AI_SPRING_CAR)

## 규칙
- 규칙 파일: rules/1_prd_rules.md, rules/2_task_gen_rules.md, rules/3_task_exec_rules.md
- 한 번에 하나의 Task만 수행.

## 현재 상태
- Backend: /health ❌ (접속 불가)
- DB: Postgres ✅ / Redis ✅
- Web: vite dev ❌ (포트 닫힘)
- Last Checked: 2025-09-26 10:17 KST

## Active Task
- Task ID: FE-3: – 추천 결과 & 즐겨찾기 화면
- 목표: 추천 결과 & 즐겨찾기 화면
- DoD(완료조건): 추천 리스트, 비교표 선택, 즐겨찾기 토글 UI, API 연동 모듈, 테스트 1건 이상
- Deliverable: `src/features/recommendations/*`, `src/features/favorites/*`
- 승인 규칙: 완료 보고 후 승인 받고 다음 Task 이동

