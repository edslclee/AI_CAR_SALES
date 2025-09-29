# Context (AI_SPRING_CAR)

## 규칙
- 규칙 파일: rules/1_prd_rules.md, rules/2_task_gen_rules.md, rules/3_task_exec_rules.md
- 한 번에 하나의 Task만 수행.

## 현재 상태
- Backend: /health ✅ (200 OK)
- DB: Postgres ✅ / Redis ✅
- Web: vite dev ❌ (포트 닫힘)
- Last Checked: 2025-09-26 16:59 KST

## Active Task
- Task ID: BE-6: – 구글 OAuth2 로그인 연동
- 목표: 구글 OAuth2 로그인 연동
- DoD(완료조건): OAuth2 클라이언트 등록, 토큰 교환 플로우, 통합 테스트
- Deliverable: `src/main/java/.../security/*`, `docs/auth_setup.md`
- 승인 규칙: 완료 보고 후 승인 받고 다음 Task 이동

