# 작업 체크리스트 (PRD v2 - 자동차 추천 시스템)

---

## M3 (소셜 로그인, 추천 이유, 업로드 개선)

- [x] BE-6: 구글 OAuth2 로그인 연동
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