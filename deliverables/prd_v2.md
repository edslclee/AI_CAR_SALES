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