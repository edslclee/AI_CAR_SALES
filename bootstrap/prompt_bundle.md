# === BOOTSTRAP BUNDLE START ===
아래 파일들을 읽고 Active Task 하나만 수행.


---
# FILE: bootstrap/context.md

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



---
# FILE: deliverables/prd.md

# PRD: 자동차 추천 시스템 (MVP)

## 0. 사전 가정 & 미확정 항목
- 대상 시장/국가: 대한민국, 통화 KRW, 언어 한국어, 거리 km, 연비 km/L 기준
- 초기 출시 범위: Web/PWA (모바일 반응형 웹 포함), 앱 스토어 출시는 이후 단계로 미포함
- 추천 기준 가중치(초기값): 예산 적합도 40%, 사용 용도 적합도 30%, 선호 옵션 적합도 20%, 연식/주행거리 조건 10%
- 데이터 확보 경로: 내부 관리자 CSV 업로드 + 향후 외부 제휴사 연동(외부 API 연동은 범위 외)
- 로그인 방식: 이메일/비밀번호 + 소셜 로그인(카카오) 1순위, 소셜 미확정 → 2차 릴리즈 후보
- 개인정보 보관 정책: 최소 3년 보관, 파기 요청 시 즉시 삭제, 암호화 저장(비밀번호 Bcrypt, 민감정보 AES)

## 1. 개요 / 비전 / 목표
- 비전: 개인 맞춤형 자동차 추천을 통해 초기 구매 리서치를 단축하고 신뢰있는 의사결정을 돕는다.
- 비즈니스 목표: MVP 출시 3개월 내 월간 활성 사용자 1만 명 확보, 추천 리스트 클릭률 30% 이상, 즐겨찾기 전환율 15% 달성.
- 제품 목표: 5분 이하 설문 완료, 맞춤형 추천 5개 이상 제공, 비교표/즐겨찾기로 재방문 동기 부여.

## 2. 페르소나 & 시나리오
- **페르소나 A: 신차/중고 경계 고객**
  - 30대 직장인, 첫 가족용 차량 검색. 예산 3천만 원, SUV 선호.
  - 시나리오: 설문 입력 → 추천 리스트에서 5대 확인 → 비교표로 연비/크기 비교 → 즐겨찾기 저장 → 영업사 컨택.
- **페르소나 B: 중고차 업그레이드 수요**
  - 40대 자영업자, 현재 세단 보유, 유지비 절감이 목표.
  - 시나리오: 기존 차량 주행거리/연식을 입력 → 예산/연비 필터 → 추천 결과 공유 → CSV 내보내기 요청.
- **페르소나 C: 관리자(내부)**
  - 제조사 제휴 담당자. 월 1회 최신 카탈로그 CSV 업로드, 차량 등록/수정.

## 3. 범위 (포함 / 제외)
- **포함**: 설문 기반 추천 로직, 추천 리스트/비교표/즐겨찾기, 관리자 CSV 업로드, 기본 이메일 회원 가입, 추천 결과 저장.
- **제외**: 실시간 재고 연동, 결제/계약 체결, 다국어 지원, 전용 모바일 앱, 외부 API 연동(향후 단계), 소셜 로그인(후속).

## 4. UX 개요
- 온보딩 설문 흐름: (1) 예산/구매 방식 → (2) 사용 용도/승차 인원 → (3) 선호 차종/브랜드 → (4) 연식/주행거리 허용 범위 → (5) 추가 옵션.
- 추천 결과 페이지: 추천 점수, 주요 스펙, 예상 유지비, 즐겨찾기 버튼, 비교표에 추가 버튼.
- 비교표: 최대 4대 차량 셀렉트, 스펙/가격/연비/유지비 비교.
- 즐겨찾기: 사용자별 저장 목록, 메모 기능(간단한 메모만).
- 관리자 업로드: CSV 업로드 → 검증 결과 표시(필드 누락/형식 오류) → 적용 버튼.

## 5. API 설계 (초안)
- `POST /api/v1/surveys` : 설문 응답 저장 및 추천 결과 트리거 (Request: 사용자 ID, 설문 답변 리스트 / Response: 추천 ID)
- `GET /api/v1/recommendations/{id}` : 추천 결과 상세 (추천 차량 리스트, 점수, 이유 요약)
- `POST /api/v1/favorites` : 즐겨찾기 추가 (차량 ID, 메모)
- `GET /api/v1/favorites` : 사용자 즐겨찾기 목록 조회
- `POST /api/v1/compare` : 비교표 대상 등록 및 결과 반환
- `POST /api/v1/admin/cars/upload` : 관리자 CSV 업로드 (Multipart), 비동기 처리 큐 등록
- `GET /api/v1/admin/cars/upload/{jobId}` : 업로드 검증/처리 상태 조회

## 6. 데이터 모델 (개념 수준)
- `users(id, email, password_hash, name, role, created_at)`
- `preferences(id, user_id, budget_min, budget_max, usage, passengers, preferred_body_types, preferred_brands, year_range, mileage_range, options, created_at)`
- `cars(id, oem_code, model_name, trim, price, body_type, fuel_type, efficiency, seats, drivetrain, release_year, features, media_assets, created_at)`
- `recommendations(id, user_id, generated_at, rationale, scoring_weights)`
- `recommendation_items(id, recommendation_id, car_id, score_breakdown, rank)`
- `favorites(id, user_id, car_id, note, created_at)`
- `csv_upload_jobs(id, admin_id, original_filename, status, error_report_path, created_at)`

## 7. 아키텍처
- 프론트엔드: React + Vite PWA, Service Worker로 기본 캐싱, axios 기반 API 통신.
- 백엔드: Spring Boot 3 (Java 17), REST Controller, Service, Repository 계층, Flyway 기반 DB 마이그레이션, 추천 로직 모듈화.
- 데이터베이스: PostgreSQL 15, JSONB 컬럼으로 옵션/스펙 확장성 확보.
- 인프라: Docker Compose (app, postgres, redis). Redis는 추천 캐싱과 업로드 작업 큐용.
- 배포 (MVP): 단일 리전 VM/컨테이너 배포, GitHub Actions CI, staging/prod 환경 분리.

## 8. 비기능 요구사항
- 응답 시간: 주요 API 300ms 이하, 추천 생성은 2초 이하(비동기 처리 시 5초 내 완료).
- 가용성: MVP 99.5% 목표, 장애 시 재시도 로직(추천 캐시 fallback).
- 보안: TLS 적용, 관리자 API JWT + Role 기반 인증, 비밀번호 Bcrypt 암호화.
- 확장성: 추천 엔진은 전략 패턴으로 구현, 향후 ML 모델 교체 용이성 확보.
- 로깅/모니터링: 구조화된 로그(JSON), 기본 APM(New Relic or OpenTelemetry) 도입 고려.

## 9. 테스트 전략
- 단위 테스트: 추천 점수 계산, CSV 파서 검증, 서비스 레이어 비즈니스 로직.
- 통합 테스트: REST API -> DB 흐름, Flyway 마이그레이션 검증.
- E2E 테스트: Cypress 기반 설문 → 추천 → 즐겨찾기 시나리오(주요 브라우저).
- 비기능 테스트: 부하 테스트(100 rps), CSV 업로드 스트레스 테스트.

## 10. 릴리즈 계획
- M0 (2주): 설문/추천 API 프로토타입, 기본 UI 와이어프레임, Docker Compose 환경 구축.
- M1 (4주): 추천 로직 확정, 즐겨찾기/비교표, 관리자 CSV 업로드 베타.
- M2 (2주): 성능 튜닝, 보안 점검, 스테이징/실 사용자 베타, QA 통과 후 정식 베타 론칭.
- 이후: 소셜 로그인 추가, 외부 데이터 제휴 탐색, 모바일 앱 검토.


---
# FILE: deliverables/tasks.md

# 작업 체크리스트 (자동차 추천 시스템)

## Infra
- [x] INF-1: Docker Compose 골격 구성
  - DoD: `docker-compose.yml`에 app/postgres/redis 서비스 정의, 환경 변수 템플릿(`.env.example`) 준비, README에 기동 명령 문서화
  - Deliverable: `docker-compose.yml`, `.env.example`, `docs/local_setup.md`
- [x] INF-2: 로컬 인프라 기동 검증
  - DoD: `docker compose up` 이후 Postgres/Redis 컨테이너 헬스 체크 통과, 애플리케이션 컨테이너에서 `/health` 200 응답 확인, 확인 로그 캡처
  - Deliverable: `docs/local_setup.md` 업데이트(트러블슈팅 포함), 검증 로그(`logs/infra_bootstrap.md`)

## Backend
- [x] BE-1: Spring Boot 3 프로젝트 초기화
  - DoD: Gradle 기반 프로젝트 생성, 기본 패키지 구조 정의, 헬스체크 컨트롤러(`/health`) 200 응답, Flyway 의존성 포함
  - Deliverable: `build.gradle`, `src/main/java/.../Application.java`, `src/main/resources/application.yml`
- [x] BE-2: 기본 데이터 모델 & Flyway 마이그레이션 추가
  - DoD: users/preferences/cars 기본 테이블 스키마 정의, Flyway V1 스크립트 작성 및 로컬 적용 검증
  - Deliverable: `src/main/resources/db/migration/V1__init.sql`, 적용 결과 로그
- [x] BE-3: 도메인 엔티티/리포지토리/서비스 뼈대 구현
  - DoD: JPA 엔티티 생성(users, preferences, cars, recommendations, favorites), Spring Data Repository 인터페이스, 서비스 인터페이스 초안, 단위 테스트 1건 이상
  - Deliverable: `src/main/java/.../domain/*.java`, `src/test/java/.../repository/*.java`
- [x] BE-4: 추천 API 초안 구현
  - DoD: `/api/v1/surveys`, `/api/v1/recommendations/{id}`, `/api/v1/favorites` 기본 흐름 구현, 추천 점수 계산 스텁, 통합 테스트 1건 이상 통과
  - Deliverable: `src/main/java/.../controller/*.java`, `src/test/java/.../controller/*.java`
- [x] BE-5: 관리자 CSV 업로드 파이프라인 초안
  - DoD: Multipart 업로드 엔드포인트, CSV 파서 유효성 검증, 비동기 큐 스텁, 실패/성공 응답 정의, 단위 테스트 포함
  - Deliverable: `src/main/java/.../admin/*.java`, `src/test/java/.../admin/*.java`

## Frontend
- [ ] FE-1: Vite + React PWA 초기화
  - DoD: Vite 프로젝트 생성, React Router 기본 구성, PWA(Service Worker, manifest) 설정, CI 빌드 통과
  - Deliverable: `package.json`, `src/main.tsx`, `public/manifest.json`
- [ ] FE-2: 온보딩 설문 UI 구현
  - DoD: 설문 단계별 폼 컴포넌트(예산, 용도, 선호 등) 구현, 유효성 검사, API 연동 스텁, 사용자 입력 상태 관리
  - Deliverable: `src/features/survey/*`, 스토리북/테스트 케이스(optional)
- [ ] FE-3: 추천 결과 & 즐겨찾기 화면
  - DoD: 추천 리스트, 비교표 선택, 즐겨찾기 토글 UI, API 연동 모듈, 테스트 1건 이상
  - Deliverable: `src/features/recommendations/*`, `src/features/favorites/*`
- [ ] FE-4: 관리자 CSV 업로드 화면
  - DoD: CSV 업로드 폼, 상태 표시(검증 중/완료/오류), 결과 로그 뷰어, 에러 핸들링, API 연동 스텁
  - Deliverable: `src/features/admin/upload/*`

## QA / CI
- [ ] QA-1: Backend 테스트 기반 구축
  - DoD: Testcontainers(Postgres/Redis) 연동, 통합 테스트 템플릿, GitHub Actions에서 테스트 실행 성공
  - Deliverable: `build.gradle` 테스트 설정, `.github/workflows/backend-ci.yml`
- [ ] QA-2: Frontend 테스트 및 린트 파이프라인
  - DoD: Vitest/Testing Library 설정, ESLint/Prettier 구성, GitHub Actions 워크플로 추가, 주요 컴포넌트 테스트 1건 이상
  - Deliverable: `vitest.config.ts`, `.eslintrc.cjs`, `.github/workflows/frontend-ci.yml`

## Admin
- [x] ADM-1: CSV 스키마 정의 및 샘플 데이터 작성
  - DoD: 관리자 업로드용 CSV 필드 목록/데이터 타입 문서화, 필수/선택 구분, 샘플 파일 2종(정상/에러) 작성
  - Deliverable: `docs/admin/csv_schema.md`, `data/samples/cars_valid.csv`, `data/samples/cars_invalid.csv`
- [ ] ADM-2: CSV 검증 자동화 스크립트 초안
  - DoD: 업로드 전 사전 검증용 스크립트(`check_csv.py`) 작성, 필수 필드/데이터 타입/중복 검사를 포함, 샘플 데이터로 테스트 로그 확보
  - Deliverable: `tools/check_csv.py`, `logs/csv_validation.md`
- [ ] ADM-3: 관리자 운영 가이드 작성
  - DoD: CSV 업로드 절차, 오류 대응 프로세스, 추천 결과 검수 체크리스트 포함한 운영 가이드 초안 작성
  - Deliverable: `docs/admin/operations_guide.md`



---
# FILE: rules/1_prd_rules.md

# PRD 생성 규칙

## 목적
- AI가 결정론적으로 제품 요구사항 문서(PRD)를 생성하도록 한다.

## 사전 질문
1. 대상 시장/국가, 언어/통화/단위
2. 초기 출시 범위(Web/Mobile)
3. 추천 기준 가중치
4. 데이터 확보 경로
5. 로그인 방식
6. 개인정보 보관 정책

## PRD 구성
1. 개요/비전/목표
2. 페르소나 & 시나리오
3. 범위(포함/제외)
4. UX 개요
5. API 설계
6. 데이터 모델
7. 아키텍처
8. 비기능 요구사항
9. 테스트 전략
10. 릴리즈 계획

## 산출물
- `deliverables/prd.md`

---
# FILE: rules/2_task_gen_rules.md

# 작업(Task) 생성 규칙

- Markdown 체크박스 형식으로 생성한다.
- 1~3시간 내 끝낼 수 있는 단위로 쪼갠다.
- DoD(완료조건)와 산출물을 명시한다.
- 위험/승인 필요 항목은 라벨을 붙인다.

## 출력
- `deliverables/tasks.md`

---
# FILE: rules/3_task_exec_rules.md

# 작업(Task) 실행 규칙

1. 한 번에 하나의 Task만 수행한다.
2. 시작 전 예상/위험/계획 요약.
3. 완료 후 체크박스 갱신, 커밋 규칙 적용.
4. 다음 Task 전 사용자 승인 필수.
5. 실패 시 최소 복구안 제시.

---
# FILE: bootstrap/git_status.txt

 M README.md
 M bootstrap/context.md
 M bootstrap/prompt_bundle.md
 M bootstrap/tree.txt
 M build.gradle
 M deliverables/tasks.md
 M docs/history/STATELOG.md
 M scripts/complete_task.sh
 M scripts/set_active_task.sh
 M src/main/java/com/aicarsales/app/repository/CarRepository.java
 M src/main/resources/application.yml
 M src/test/resources/application-test.yml
 M src/test/resources/schema-test.sql
?? data/
?? docs/admin/
?? frontend/
?? scripts/run_backend.sh
?? scripts/seed_sample_cars.sh
?? scripts/status.sh
?? src/main/java/com/aicarsales/app/admin/
?? src/main/resources/db/migration/V3__csv_upload_jobs.sql
?? src/test/java/com/aicarsales/app/admin/


---
# FILE: bootstrap/tree.txt

.
./tools
./bootstrap
./bootstrap/context.md
./bootstrap/git_status.txt
./bootstrap/rehydrate.sh
./bootstrap/tree.txt
./bootstrap/prompt_bundle.md
./frontend
./frontend/index.html
./frontend/node_modules
./frontend/vite.config.js
./frontend/README.md
./frontend/public
./frontend/.gitignore
./frontend/package-lock.json
./frontend/package.json
./frontend/src
./docs
./docs/admin
./docs/decisions
./docs/history
./README.md
./deliverables
./deliverables/prd.md
./deliverables/tasks.md
./logs
./logs/status_check.md
./.gitignore
./scripts
./scripts/seed_sample_cars.sh
./scripts/status.sh
./scripts/set_active_task.sh
./scripts/run_backend.sh
./scripts/complete_task.sh
./build.gradle
./.gradle
./.gradle/vcs-1
./.gradle/8.7
./.gradle/buildOutputCleanup
./.gradle/9.0.0
./rules
./rules/3_task_exec_rules.md
./rules/2_task_gen_rules.md
./rules/1_prd_rules.md
./docker-compose.yml
./build
./build/generated
./build/classes
./build/resolvedMainClassName
./build/resources
./build/test-results
./build/tmp
./build/reports
./.git
./.git/config
./.git/objects
./.git/HEAD
./.git/info
./.git/logs
./.git/description
./.git/hooks
./.git/refs
./.git/index
./.git/COMMIT_EDITMSG
./.git/FETCH_HEAD
./.vscode
./.vscode/task.json
./settings.gradle
./data
./data/samples
./src
./src/test
./src/main

# === BOOTSTRAP BUNDLE END ===

