# Prompt Bundle (rehydrated)
- Generated: 2025-09-26 18:38 KST
- Workspace: AI_CAR_SALES

---

## 1) Active Context (bootstrap/context.md)
```md
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
```

---

## 2) Product Requirements (PRD) — prd_v2.md
```md
# 제품 요구사항 문서 (PRD v2) — 자동차 추천 시스템

## 0. 배경 & 목표
- 배경: MVP 릴리즈 후 초기 사용자 피드백 및 운영 결과를 반영
- 목표: 사용자 리텐션 30% 이상, 추천 클릭률 40% 달성, 운영 효율성 개선

---

## 1. 사용자 피드백 요약
- [ ] 설문 과정 단축 요청
- [ ] 추천 이유(Explainability) 노출 필요
- [ ] CSV 업로드 에러 리포트 가독성 개선
- [ ] 앱 설치 요구(모바일 접근성)

---

## 2. 범위 (포함 / 제외)
- 포함:
  - 소셜 로그인 (카카오/구글)
  - 추천 결과 근거 API
  - 개인화 추천
  - 관리자 CSV 업로드 개선
  - 외부 차량 데이터 API PoC
- 제외:
  - 결제/계약 플로우
  - 다국어 지원
  - 네이티브 앱 정식 출시

---

## 3. UX 개선
- 설문 단계 통합 (5 → 3)
- 추천 이유 팝업 추가
- 유지비/연비 비교 차트
- 관리자 업로드 오류 리포트 다운로드, 업로드 이력 조회

---

## 4. API 설계 (초안)
- `GET /api/v2/recommendations/{id}` → explanations 필드 추가
- `POST /api/v2/favorites` → 메모 확장
- `POST /api/v2/admin/cars/upload` → error_report_url 반환
- `GET /api/v2/admin/cars/history`
- `GET /api/v2/external/cars/search` (PoC)

---

## 5. 데이터 모델 확장
- recommendations.explanations (JSON 배열)
- favorites.note 확장
- csv_upload_jobs.error_report_path
- external_api_logs 테이블 추가

---

## 6. 아키텍처 확장
- 외부 API 연동 모듈
- Explainability decorator 패턴
- Redis 캐시 확장
- CI/CD: staging 외부 API 테스트 분리

---

## 7. 비기능 요구사항
- 응답 시간: 1초 이내 캐싱 응답
- 가용성: 99.7%
- 보안: 외부 API 키 Vault 관리
- 확장성: ML 모델 교체 용이성 확보

---

## 8. 테스트 전략
- 단위: 추천 이유 생성, 외부 API 파서
- 통합: 업로드 → 오류 리포트 생성
- E2E: 소셜 로그인 → 설문 → 추천 → 이유 팝업
- 비기능: 부하/장애 fallback

---

## 9. 릴리즈 계획
- M3: 소셜 로그인, 추천 이유, 업로드 개선
- M4: 개인화 추천, 외부 API PoC
- M5: 베타 테스트 → 정식 v2 론칭
```

---

## 3) Tasks Checklist — tasks_v2.md
```md
# 작업 체크리스트 (PRD v2 — 자동차 추천 시스템)

---

## M3 (소셜 로그인, 추천 이유, 업로드 개선)

- [ ] BE-6: 구글 OAuth2 로그인 연동
  - DoD: OAuth2 클라이언트 등록, 토큰 교환 플로우, 통합 테스트
  - Deliverable: `src/main/java/.../security/*`, `docs/auth_setup.md`

- [ ] BE-7: 추천 결과 근거(Explainability) 필드 추가
  - DoD: recommendations.explanations JSON 필드 저장/조회
  - Deliverable: `src/main/java/.../recommendations/*`, DB 마이그레이션

- [ ] FE-5: 추천 결과 이유 팝업 UI
  - DoD: 리스트에서 “추천 이유” 버튼 → 팝업/툴팁 표시
  - Deliverable: `src/features/recommendations/ExplainPopup.tsx`

- [ ] ADM-4: CSV 업로드 오류 리포트 다운로드
  - DoD: 업로드 결과 → downloadable error_report_url 제공
  - Deliverable: `src/main/java/.../admin/*`, `logs/csv_reports/*`

---

## M4 (개인화 추천, 외부 API PoC)

- [ ] BE-8: 사용자 히스토리 기반 개인화 추천
  - DoD: favorites/recommendations 이력 기반 가중치 계산
  - Deliverable: `src/main/java/.../personalization/*`

- [ ] BE-9: 외부 차량 데이터 API 연동 PoC
  - DoD: 제휴 API 호출, 응답 파싱, 캐싱
  - Deliverable: `src/main/java/.../external/*`, `docs/external_api.md`

- [ ] FE-6: 외부 API 검색 UI (PoC)
  - DoD: 검색 폼 + 결과 리스트, API 연동
  - Deliverable: `src/features/external/Search.tsx`

- [ ] QA-3: 외부 API 장애 fallback 시나리오 테스트
  - DoD: 장애 시 캐시 응답 반환 확인
  - Deliverable: `src/test/java/.../external/*`

---

## M5 (베타 테스트, 정식 v2 론칭)

- [ ] OPS-1: 성능 튜닝 및 부하 테스트
  - DoD: 200 rps 부하 테스트 통과, 응답시간 < 1s
  - Deliverable: `logs/performance_tests.md`

- [ ] SEC-1: API 키 Vault 관리 적용
  - DoD: 외부 API 키 환경변수 대신 Vault 연동
  - Deliverable: `docs/security/vault_setup.md`

- [ ] QA-4: E2E 테스트 시나리오 확장
  - DoD: 소셜 로그인 → 설문 → 추천 이유 → 즐겨찾기 플로우 자동화
  - Deliverable: `cypress/e2e/v2_flows.cy.ts`

- [ ] DOC-1: 관리자 운영 가이드 v2 업데이트
  - DoD: 업로드 개선, 오류 리포트, 외부 API PoC 포함
  - Deliverable: `docs/admin/operations_v2.md`
```


---

## 4) State Log (tail)
```md

## 2025-09-26 18:11 KST
- set-active: BE-6: → BE-6
- title: 구글 OAuth2 로그인 연동
- goal: 구글 OAuth2 로그인 연동
- parsed: goal="구글 OAuth2 로그인 연동" dod="" deliver=""
- source: tasks_v2.md

## 2025-09-26 18:12 KST
- set-active: BE-6: → BE-6
- title: 구글 OAuth2 로그인 연동
- goal: 구글 OAuth2 로그인 연동
- parsed: goal="구글 OAuth2 로그인 연동" dod="" deliver=""
- source: tasks_v2.md

## 2025-09-26 18:15 KST
- set-active: BE-6: → BE-6
- title: 구글 OAuth2 로그인 연동
- goal: 구글 OAuth2 로그인 연동
- parsed: goal="구글 OAuth2 로그인 연동" dod="" deliver=""
- source: tasks_v2.md

## 2025-09-26 18:15 KST
- set-active: BE-6: → BE-6
- title: 구글 OAuth2 로그인 연동
- goal: 구글 OAuth2 로그인 연동
- parsed: goal="구글 OAuth2 로그인 연동" dod="" deliver=""
- source: tasks_v2.md

## 2025-09-26 18:20 KST
- set-active: BE-6: → BE-6
- title: 구글 OAuth2 로그인 연동
- goal: 구글 OAuth2 로그인 연동
- parsed: goal="구글 OAuth2 로그인 연동" dod="" deliver=""
- source: tasks_v2.md

## 2025-09-26 18:31 KST
- set-active: BE-6: → BE-6
- title: 구글 OAuth2 로그인 연동
- goal: 구글 OAuth2 로그인 연동
- parsed: goal="구글 OAuth2 로그인 연동" dod="OAuth2 클라이언트 등록, 토큰 교환 플로우, 통합 테스트" deliver="`src/main/java/.../security/*`, `docs/auth_setup.md`"
- source: tasks_v2.md

## 2025-09-26 18:32 KST
- set-active: BE-6: → BE-6
- title: 구글 OAuth2 로그인 연동
- goal: 구글 OAuth2 로그인 연동
- parsed: goal="구글 OAuth2 로그인 연동" dod="OAuth2 클라이언트 등록, 토큰 교환 플로우, 통합 테스트" deliver="`src/main/java/.../security/*`, `docs/auth_setup.md`"
- source: tasks_v2.md
```
