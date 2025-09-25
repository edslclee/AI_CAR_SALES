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
- [ ] BE-5: 관리자 CSV 업로드 파이프라인 초안
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
- [ ] ADM-1: CSV 스키마 정의 및 샘플 데이터 작성
  - DoD: 관리자 업로드용 CSV 필드 목록/데이터 타입 문서화, 필수/선택 구분, 샘플 파일 2종(정상/에러) 작성
  - Deliverable: `docs/admin/csv_schema.md`, `data/samples/cars_valid.csv`, `data/samples/cars_invalid.csv`
- [ ] ADM-2: CSV 검증 자동화 스크립트 초안
  - DoD: 업로드 전 사전 검증용 스크립트(`check_csv.py`) 작성, 필수 필드/데이터 타입/중복 검사를 포함, 샘플 데이터로 테스트 로그 확보
  - Deliverable: `tools/check_csv.py`, `logs/csv_validation.md`
- [ ] ADM-3: 관리자 운영 가이드 작성
  - DoD: CSV 업로드 절차, 오류 대응 프로세스, 추천 결과 검수 체크리스트 포함한 운영 가이드 초안 작성
  - Deliverable: `docs/admin/operations_guide.md`

