# AI Car Sales 유지보수 지침서

## 1. 인프라 구성 (INF-1)

### 1.1 구성 개요
| 요소 | 이미지/버전 | 목적 | 포트 | 영속 스토리지 |
|------|-------------|------|------|----------------|
| PostgreSQL | `postgres:15-alpine` | 핵심 데이터베이스. `app_db` 스키마에 업무 테이블 저장 | `5432` | `pg_data` 볼륨 (`/var/lib/postgresql/data`) |
| Redis | `redis:7-alpine` | 추천 결과 캐싱 및 CSV 업로드 큐 스텁 등 단기 데이터 관리 | `6379` | `redis_data` 볼륨 (`/data`) |

- `docker-compose.yml` 루트에 위치. 기본적으로 컨테이너명을 `ai_car_sales_postgres`, `ai_car_sales_redis` 로 고정하여 스크립트에서 참조하기 쉬움.
- 헬스체크
  - Postgres: `pg_isready -U app -d app_db`
  - Redis: `redis-cli ping`
- 기본 계정/DB
  - `POSTGRES_DB=app_db`
  - `POSTGRES_USER=app`
  - `POSTGRES_PASSWORD=app_password`  *(운영 배포 시 비밀 변수로 교체 필요)*

### 1.2 기동 및 점검 절차
1. **서비스 시작**
   ```bash
   docker compose up -d postgres redis
   ```
2. **컨테이너 상태 확인**
   ```bash
   docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
   ```
   두 컨테이너가 `Up (healthy)` 여야 백엔드가 DB 접속 가능.
3. **DB/캐시 헬스 체크**
   ```bash
   docker exec ai_car_sales_postgres pg_isready -U app -d app_db
   docker exec ai_car_sales_redis redis-cli ping
   ```
   각각 `accepting connections`, `PONG` 이면 정상.
4. **백업/볼륨 안내**
   - Postgres: `pg_data` 볼륨 사용. 개발환경에서는 삭제 시 데이터 초기화.
   - Redis: 세션성 데이터라 필요 시 볼륨 삭제 가능.

### 1.3 업무 프로세스와의 연결
- **CSV 업로드 파이프라인**(BE-5)과 **추천/즐겨찾기 API**가 모두 PostgreSQL을 Data Source로 사용한다.
- 향후 비동기 큐를 Redis로 전환할 수 있도록 서비스 레이어에서 추상화(현재는 `SimpleAsyncTaskExecutor`). 운영환경 도입 시 Redis Streams 혹은 별도 큐를 고려.
- 프런트엔드 개발 서버는 별도로 동작하지만, Docker Compose의 포트(5432/6379)가 로컬 호스트와 충돌하지 않도록 관리 필요.

### 1.4 운영 시 주의사항
- `docker compose down` 실행 시 볼륨은 유지되므로 데이터는 남는다. 완전 초기화가 필요하면 `docker compose down -v`로 볼륨까지 제거.
- 비밀번호 등 민감 정보는 `.env` 파일로 외부화하여 `docker-compose.yml`에서 참조하도록 개선 권장.
- CI 환경에서 테스트 컨테이너 활용 시 동일한 포트 충돌 가능성에 대비해 `ports` 매핑을 옵션화할 것.

---
## 2. 로컬 인프라 기동 검증 (INF-2)

로컬 개발 환경에서 백엔드·DB·프런트를 동시에 점검하는 절차입니다. QA/운영 전 단계에서도 동일한 순서를 참고하세요.

### 2.1 사전 조건
- `docker compose up -d postgres redis` 로 DB와 캐시가 기동되어 있어야 함.
- 백엔드 실행: `./scripts/run_backend.sh`
  - 내부적으로 `gradle:8.7-jdk17` 컨테이너를 사용해 `gradle bootRun` 수행.
  - 기본 환경 변수는 `DB_HOST=host.docker.internal`, `SPRING_PROFILES_ACTIVE=local`.
- 프런트 실행(선택): `cd frontend && npm install && npm run dev -- --host`

### 2.2 상태 점검 스크립트
- `./scripts/status.sh`
  - Postgres/Redis 컨테이너 상태, `/health` API, Vite Dev 서버(5173) 응답을 간단히 체크.
  - 결과는 `bootstrap/context.md`와 `logs/status_check.md`에 기록됩니다.

### 2.3 수동 점검 절차
1. **백엔드 헬스 체커**
   ```bash
   curl -sf http://localhost:8080/health
   ```
   `{"status":"ok", ...}` JSON이 반환되면 정상. 실패 시 백엔드 로그/DB 연결을 확인.
2. **데이터베이스 연결 확인**
   ```bash
   docker exec ai_car_sales_postgres pg_isready -U app -d app_db
   docker exec ai_car_sales_postgres psql -U app -d app_db -c '\dt'
   ```
   주요 테이블(`cars`, `recommendations`, `csv_upload_jobs` 등)이 목록에 보여야 합니다.
3. **Redis 핑 테스트**
   ```bash
   docker exec ai_car_sales_redis redis-cli ping
   ```
   `PONG`이면 정상. CSV 업로드 큐를 Redis 기반으로 확장할 때 이 체크가 기본이 됩니다.
4. **프런트 dev 서버 확인** (선택)
   ```bash
   curl -I http://localhost:5173
   ```
   개발 서버가 기동 중이면 `200 OK` 응답 헤더가 반환됩니다.

### 2.4 자주 발생하는 이슈
| 증상 | 원인 | 해결 방법 |
|------|------|-----------|
| 백엔드 기동 시 `Connection refused` (Postgres) | Docker 데몬/컨테이너 미기동 또는 포트 충돌 | `docker compose ps`, 포트 점유 확인. 필요 시 `docker ps`로 중복 컨테이너 종료 |
| 프런트 `react-router-dom` 모듈 오류 | npm install 실패(네트워크 차단) | 네트워크 가능한 환경에서 `cd frontend && npm install && npm run build` 재실행 |
| CSV 업로드 400 에러 | CSV 헤더 누락/데이터 형식 오류 | `docs/admin/csv_schema.md` 참고, `CsvValidator` 규칙에 맞춰 CSV 수정 |

### 2.5 운영 체크리스트 연결
- 상태 점검 결과는 `bootstrap/context.md`에 기록해 두면 팀원 간 공유가 용이합니다.
- CI 파이프라인에서 `./scripts/status.sh` 유사 스크립트를 활용하면 배포 전 자동 점검이 가능.
- CSV 업로드, 추천 API 테스트는 MockMvc 통합 테스트(`src/test/java/com/aicarsales/app/controller`, `.../admin`)로 커버되지만, 필요 시 Postman/Swagger 테스트 플로우도 준비해 두면 좋습니다.

---
## 3. 백엔드 초기화 (BE-1)

Spring Boot 3 기반 백엔드의 기본 구조와 실행 방법을 정리합니다.

### 3.1 프로젝트 골격
| 항목 | 위치 | 설명 |
|------|------|------|
| 엔트리 포인트 | `src/main/java/com/aicarsales/app/AiCarSalesApplication.java` | `SpringApplication.run` 으로 서비스 기동 |
| 헬스 체크 | `src/main/java/com/aicarsales/app/HealthController.java` (`/health`) | 인프라 확인 시 사용 |
| 설정 파일 | `src/main/resources/application.yml` | DB 연결, JPA 설정, 관리 엔드포인트 등의 기본값. 환경변수로 오버라이드 가능 |
| 빌드 스크립트 | `build.gradle`, `settings.gradle` | Spring Boot 3.2.5, Java 17, Flyway, JPA, CSV 처리 등 의존성 관리 |

주요 의존성
- `org.springframework.boot:spring-boot-starter-web`
- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.flywaydb:flyway-core`
- `org.apache.commons:commons-csv`
- 테스트: `spring-boot-starter-test`, H2 (테스트 프로필)

### 3.2 실행 방법
- 로컬 JDK/Gradle 없이 Docker로 실행: `./scripts/run_backend.sh`
  ```bash
  ./scripts/run_backend.sh
  ```
  - `DB_HOST`, `SPRING_PROFILES_ACTIVE`, `HOST_PORT` 등의 환경 변수로 조정 가능.
- 직접 실행 (JDK 17/Gradle 설치 시):
  ```bash
  ./gradlew bootRun
  ```

### 3.3 테스트
- 단위/통합 테스트: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.7-jdk17 gradle -Dorg.gradle.daemon=false test`
  - Admin CSV 업로드, 추천 API, 사용자 서비스 등 핵심 흐름을 MockMvc/Mockito로 검증.
- 테스트 프로필 설정: `src/test/resources/application-test.yml` 에서 H2 사용 및 `allow-bean-definition-overriding` 활성화.

### 3.4 업무 프로세스 연결
- 백엔드는 Postgres를 Primary DB로 사용하며 `Flyway` 마이그레이션(V1~V3)로 테이블 스키마를 관리.
- `/api/v1/surveys`, `/api/v1/recommendations`, `/api/v1/favorites`, `/api/v1/admin/cars/upload` 등의 REST 엔드포인트가 도메인 서비스와 연결됩니다.
- 비동기 작업: CSV 업로드 작업은 `SimpleAsyncTaskExecutor` 기반으로 백그라운드 처리되며, 추후 Redis 큐로 교체 가능.

### 3.5 운영 시 참고사항
- Flyway 스크립트 추가 시 마이그레이션 파일명 패턴 유지(`V{번호}__desc.sql`).
- `application.yml` 의 DB 설정은 환경 변수(`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`)로 덮어쓸 수 있으므로, CI/운영에서 별도 프로필을 추가해도 좋습니다.
- 헬스 체크 `/health`는 단순 JSON 반환이므로, 운영 환경에서 보안상 필요 시 보호해야 합니다.

---
## 4. 데이터 모델 & 마이그레이션 (BE-2)

Flyway 기반으로 관리되는 핵심 데이터 테이블과 마이그레이션 전략을 설명합니다.

### 4.1 Flyway 구조
| 마이그레이션 | 위치 | 주요 내용 |
|--------------|------|-----------|
| `V1__init.sql` | `src/main/resources/db/migration/` | `users`, `preferences`, `cars` 등 기본 테이블 생성 |
| `V2__recommendations_and_favorites.sql` | same | 추천 결과 관련(`recommendations`, `recommendation_items`, `favorites`) 테이블 추가 |
| `V3__csv_upload_jobs.sql` | same | 관리자 CSV 업로드 잡 상태 테이블 추가 |

Flyway는 애플리케이션 기동 시 자동 적용되며, 테스트 프로필에서는 `src/test/resources/schema-test.sql`로 H2 스키마를 동기화합니다.

### 4.2 테이블 요약
- **users** : 사용자 계정 정보 (`email`, `password_hash`, `role` 등). `preferences`, `recommendations`, `favorites`와 연관.
- **preferences** : 설문 응답 기반 선호 정보 (`budget_min`, `preferred_body_types`, JSON 옵션 등). `users` FK.
- **cars** : 차량 카탈로그. CSV 업로드 파이프라인이 upsert 처리. 유니크 키 `(oem_code, model_name, trim)`.
- **recommendations / recommendation_items** : 추천 세션과 그 결과 목록. `recommendation_items`는 `car_id` FK로 `cars`와 연결.
- **favorites** : 즐겨찾기한 차량 목록. `users`와 `cars`를 참조.
- **csv_upload_jobs** : CSV 업로드 상태 추적 테이블. `status`, `message`, `error_report` 컬럼으로 진행 상황을 기록.

### 4.3 모델 ↔ 업무 프로세스
| 프로세스 | 관련 테이블 | 설명 |
|----------|-------------|------|
| 설문 입력 → 추천 생성 (BE-4) | `preferences`, `recommendations`, `recommendation_items`, `cars` | 설문 응답 저장 후 추천 엔진이 상위 5개 차량을 `recommendation_items`에 기록 |
| 즐겨찾기 관리 | `favorites`, `cars` | 추천 UI에서 즐겨찾기 토글 → 해당 사용자와 차량의 조합을 저장 |
| CSV 업로드 (BE-5) | `csv_upload_jobs`, `cars` | 업로드 요청 → `csv_upload_jobs` 상태 갱신, 검증 성공 시 `cars` upsert |

### 4.4 확장/변경 시 주의사항
- 새 테이블/컬럼을 추가할 때는 Flyway 파일을 새 버전으로 작성하고, H2 스키마(`schema-test.sql`)도 동일하게 업데이트하여 테스트 실패를 방지합니다.
- JSON 컬럼(`features`, `media_assets`, `options`)은 객체 구조 변경 시 서비스/프론트 파싱 로직을 함께 점검해야 합니다.
- FK 제약조건(`ON DELETE CASCADE` 등)을 활용하므로, 삭제 시 연쇄 영향(예: 사용자 삭제 → 즐겨찾기 삭제)을 고려하세요.

---
### 4.5 ER 다이어그램 (텍스트 표현)
```
users (id PK)
  └─< preferences (user_id FK)
  └─< recommendations (user_id FK)
        └─< recommendation_items (recommendation_id FK, car_id FK)
  └─< favorites (user_id FK, car_id FK)

cars (id PK, UNIQUE oem_code+model_name+trim)
  └─< recommendation_items (car_id FK)
  └─< favorites (car_id FK)

csv_upload_jobs (id PK) ↔ CSV 파이프라인 상태 추적
```
- `<` 표시는 1:N 관계(예: `users` 1 → N `preferences`).
- `recommendation_items`는 `recommendations`와 `cars` 모두를 참조하여 추천 결과를 연결합니다.
- `csv_upload_jobs`는 특정 엔터티와 FK로 연결되어 있진 않지만, 업로드 프로세스 상태를 추적하는 래퍼 테이블로 동작합니다.

### 4.6 시각 다이어그램 생성 팁
- DB 시각화 도구 (예: `pgAdmin`, `DbSchema`, `draw.io`)에서 아래 SQL을 사용해 관계 뷰를 확인할 수 있습니다.
  ```sql
  SELECT tc.constraint_type,
         tc.constraint_name,
         tc.table_name,
         kcu.column_name,
         ccu.table_name AS foreign_table,
         ccu.column_name AS foreign_column
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name
   AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage ccu
    ON ccu.constraint_name = tc.constraint_name
  WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = 'public'
  ORDER BY tc.table_name;
  ```
- 출력된 관계를 기반으로 Mermaid/PlantUML 스타일 다이어그램을 생성할 수도 있습니다. 예:
  ```text
  erDiagram
      USERS ||--o{ PREFERENCES : "has"
      USERS ||--o{ RECOMMENDATIONS : "requests"
      RECOMMENDATIONS ||--o{ RECOMMENDATION_ITEMS : "contains"
      CARS ||--o{ RECOMMENDATION_ITEMS : "suggested"
      USERS ||--o{ FAVORITES : "saves"
      CARS ||--o{ FAVORITES : "bookmarked"
  ```
  위 스니펫을 Mermaid 지원 마크다운 뷰어에 붙여 넣으면 시각 ER 다이어그램을 바로 확인할 수 있습니다.

---
## 5. 도메인 엔티티 / 리포지토리 / 서비스 (BE-3)

Spring Data JPA 구조와 서비스 계층의 뼈대를 정리합니다.

### 5.1 엔티티 개요
| 엔티티 | 위치 | 주요 필드 | 특징 |
|--------|------|-----------|------|
| `User` | `src/main/java/com/aicarsales/app/domain/User.java` | `email`, `passwordHash`, `role`, `createdAt` | 사용자 계정. 즐겨찾기/추천/설문과 연관 |
| `Preference` | same | `budgetMin/Max`, `preferredBodyTypes`, `options(JSONB)` | 설문 응답. `User` FK |
| `Car` | same | `oemCode`, `modelName`, `trim`, `features(JSONB)` | 차량 카탈로그. CSV 업로드로 upsert |
| `Recommendation` & `RecommendationItem` | same | 추천 시점, score, breakdown | 설문 결과 기반 추천
| `Favorite` | same | `user`, `car`, `note`, `createdAt` | 즐겨찾기 저장 |
| `CsvUploadJob` | `src/main/java/com/aicarsales/app/admin` | `status`, `message`, `errorReport` | CSV 업로드 상태 추적 |

### 5.2 리포지토리 패턴
Spring Data JPA `Repository`를 활용하여 CRUD/쿼리를 관리합니다. 예:
- `UserRepository` – `findByEmail`, `findById`
- `CarRepository` – `findByBodyTypeIgnoreCase`, `findByReleaseYearBetween`, `findByOemCodeAndModelNameAndTrim`
- `RecommendationRepository` – `@EntityGraph`로 `items`를 eager 로딩
- `FavoriteRepository` – 사용자별 즐겨찾기 조회/중복 체크
- `CsvUploadJobRepository` – 업로드 Job 조회

### 5.3 서비스 계층
| 서비스 | 책임 | 주요 메서드 |
|--------|------|-------------|
| `UserService` (`UserServiceImpl`) | 사용자 생성/조회 | `create`, `findByEmail`, `findById` |
| `PreferenceService` | 설문 응답 저장/조회 | `save`, `findByUser` |
| `CarCatalogService` | 차량 조회/검색 | `listAll`, `findByBodyType` |
| `RecommendationService` | 추천 점수 계산 & 결과 저장 | `createRecommendation`, `findById` (Stub 점수 로직 포함) |
| `FavoriteService` | 즐겨찾기 토글/조회 | `addFavorite`, `listFavorites` |
| `CsvUploadService` | CSV 업로드, 검증, 비동기 처리 | `enqueueUpload`, `getStatus`, `process` |
| `CarImportProcessor`, `CsvValidator` | CSV → `Car` 변환 | JSON/숫자 파싱, 중복 처리 logic |

### 5.4 프로세스-서비스 흐름
1. **설문 제출 → 추천 생성**
   - 컨트롤러 `SurveyController` → `UserService`/`PreferenceService` → `RecommendationService` → `CarRepository` & `RecommendationRepository`
2. **추천 조회**
   - `RecommendationController`에서 `RecommendationService.findById` 호출 → `RecommendationRepository`가 `items` + `car` 정보를 불러와 응답 DTO 구성.
3. **즐겨찾기**
   - `FavoritesController` → `FavoriteService.addFavorite` → 중복 시 기존 즐겨찾기 반환, 신규면 저장.
4. **CSV 업로드**
   - `AdminCsvUploadController` → `CsvUploadService.enqueueUpload`
   - `CsvValidator`가 필수 컬럼/JSON 검증 → `CarImportProcessor.importRecords`가 `CarRepository`를 이용해 upsert → 결과를 `CsvUploadJob`에 기록.

### 5.5 테스트
- 서비스/리포지토리 테스트는 `src/test/java/...`에 위치.
  - `UserServiceImplTest` – Mockito 기반 단위 테스트
  - `AdminCsvUploadControllerTest`, `RecommendationFlowIntegrationTest` – MockMvc 통합 테스트
- H2 스키마(`schema-test.sql`)가 실제 Postgres 스키마와 동기화되어 있어야 테스트가 성공합니다.

### 5.6 유지보수 팁
- 새 엔티티 추가 시 `Flyway` 마이그레이션, JPA 엔티티, Repository, Service, Controller(필요 시)를 세트로 추가하고 Test까지 포함하는 것을 권장합니다.
- JSON 필드(`features`, `mediaAssets`, `options`) 로직을 변경하면 Validator & Service 레이어를 함께 검토해야 합니다.
- 비동기 서비스는 현재 스텁(`SimpleAsyncTaskExecutor`)이므로, 운영 환경에서 확장 시 메시지 큐나 Work Queue로 대체할 수 있도록 인터페이스/서비스 구조를 유지합니다.

---
## 6. 추천 API 플로우 (BE-4)

설문 입력부터 추천 결과 조회, 즐겨찾기까지의 API 흐름을 정리합니다.

### 6.1 핵심 API 요약
| 엔드포인트 | 메서드 | 설명 | 주요 서비스 |
|------------|--------|------|--------------|
| `/api/v1/surveys` | POST | 설문 응답 저장 및 추천 생성 | `SurveyController` → `PreferenceService`, `RecommendationService` |
| `/api/v1/recommendations/{id}` | GET | 추천 결과 상세 조회 | `RecommendationController` → `RecommendationService.findById` |
| `/api/v1/favorites` | POST/GET | 즐겨찾기 추가/조회 | `FavoritesController` → `FavoriteService` |

### 6.2 설문 → 추천 생성 프로세스
1. **설문 제출 (`POST /surveys`)**
   - 입력: 사용자 ID, 예산, 선호 차종/브랜드, 옵션 등.
   - `PreferenceService.save`가 설문 응답을 저장하고, `RecommendationService.createRecommendation`이 후보 차량 (`CarRepository.findAll`)을 순회.
   - Stub 로직: 예산/차종 가중치를 기반으로 점수를 산출 후 상위 5개 차량을 `recommendation_items`에 저장.
   - 출력: 추천 ID (`recommendations.id`).

2. **추천 상세 조회 (`GET /recommendations/{id}`)**
   - `RecommendationRepository.findById`에서 `@EntityGraph`로 `items`와 `car`를 함께 로딩하여 DTO로 반환.
   - 응답: 추천 생성 시각, 점수 breakdown, 차량 요약 정보.

3. **즐겨찾기 (`/favorites`)**
   - `FavoriteService.addFavorite`이 차량 존재 여부를 확인하여 중복 시 기존 엔티티 반환, 아니면 저장.
   - `FavoriteService.listFavorites`는 사용자별 즐겨찾기 목록을 조회하여 간단한 카드 형태로 제공.

### 6.3 데이터 흐름 요약
```
User → SurveyController → PreferenceService.save
                   ↓
              RecommendationService.createRecommendation
                   ↓ saves → recommendation_items + cars
Client → RecommendationController → Recommendation items DTO 반환
Client → FavoritesController → FavoriteService.addFavorite/listFavorites
```

### 6.4 검증/에러 처리
- 설문 제출 시 사용자 존재 여부, 필수 입력 검증 (`SurveyController`에서 `UserService.findById`).
- 추천 조회 시 존재하지 않는 ID는 `404 NOT FOUND`.
- 즐겨찾기 추가 시 존재하지 않는 차량은 `400/404` 반환.
- CSV 업로드로 차량 데이터가 갱신되므로, 추천 로직은 최신 `cars` 테이블을 기준으로 동작합니다.

### 6.5 테스트 커버리지
- `src/test/java/com/aicarsales/app/controller/RecommendationFlowIntegrationTest.java`
  - 설문 → 추천 → 즐겨찾기 시나리오가 통합 테스트로 검증되어 있습니다.
  - H2를 이용해 테스트 데이터 삽입, MockMvc로 API 요청을 재현합니다.

### 6.6 향후 확장 아이디어
- Stub 점수 로직을 ML 모델/가중치 기반으로 교체.
- 추천 결과 캐싱을 Redis에 적용하여 `/recommendations/{id}` 응답 최적화.
- 추천 이유 설명(Toasts, bullet points)을 프런트에서 표현할 수 있도록 DTO 확장.

---
## 7. 관리자 CSV 업로드 파이프라인 (BE-5)

차량 카탈로그를 CSV로 일괄 갱신하는 관리자 기능의 구성을 정리합니다.

### 7.1 작업 흐름
1. **업로드 요청 (`POST /api/v1/admin/cars/upload`)**
   - 입력: CSV 파일 (UTF-8, 헤더 포함). 컨트롤러에서 `MultipartFile` 검증.
   - 검증 실패(빈 파일, 필수 컬럼 누락, JSON 파싱 오류 등)는 즉시 `400 BAD REQUEST`.
   - 성공 시 `202 Accepted` + `jobId`를 반환하고 비동기 처리 큐에 등록.
2. **CSV 검증 (`CsvValidator`)**
   - Apache Commons CSV로 파싱, `docs/admin/csv_schema.md` 기준 검증.
   - 숫자/JSON 필드 변환(`CarCsvRecord`) 및에러 메시지 축적. 오류 있으면 `CsvValidationException`.
3. **카탈로그 반영 (`CarImportProcessor`)**
   - `CarRepository`를 통해 `(oem_code, model_name, trim)` 기준 upsert.
   - 결과(생성/갱신 건수)를 메시지로 반환.
4. **잡 상태 업데이트 (`CsvUploadService` + `CsvUploadJob`)**
   - 상태: `PENDING` → `PROCESSING` → `SUCCEEDED/FAILED`.
   - `csv_upload_jobs` 테이블에 메시지/에러 로그 저장.
5. **상태 조회 (`GET /api/v1/admin/cars/upload/{jobId}`)**
   - 업로드 완료 여부, 결과 메시지, 오류 리포트를 확인.

### 7.2 테이블 및 서비스 연계
- `csv_upload_jobs` : 업로드 요청 및 처리 결과 기록. `status`, `message`, `error_report` 컬럼 유지.
- `cars` : CSV의 실제 반영 대상. upsert 정책으로 중복을 업데이트.
- 서비스: `CsvUploadService`, `CsvValidator`, `CarImportProcessor`, `AdminCsvUploadController`.

### 7.3 운영 절차
1. CSV 작성 시 `docs/admin/csv_schema.md` 참고 (정상/에러 샘플: `data/samples/cars_valid.csv`, `data/samples/cars_invalid.csv`).
2. 업로드 전 사전 검증(ADM-2 예정): CLI 스크립트나 `CsvValidator`를 활용.
3. 업로드 후 상태 확인: `GET /api/v1/admin/cars/upload/{jobId}`.
4. Loganlysis: 실패시 `csv_upload_jobs.error_report`에 상세 메시지가 저장되므로, DB에서 직접 확인 가능.

### 7.4 테스트
- `AdminCsvUploadControllerTest` (MockMvc + H2)에서 성공/검증 실패 시나리오 검증.
- 테스트 시 `Executor`를 동기로 오버라이드하여 즉시 결과를 확인.

### 7.5 향후 개선 사항
- Redis 등의 외부 큐를 도입하여 다중 업로드/재시도 처리 강화.
- 에러 리포트를 별도 파일로 생성하거나 관리자 UI에서 다운로드 할 수 있도록 확장.
- 업로드 결과가 추천/프런트에 반영되는 시점에 캐시 무효화 전략 적용.

---
## 8. 프런트엔드 초기화 (FE-1)

React + Vite 기반 PWA 스캐폴드의 구조와 실행 방법을 정리합니다.

### 8.1 프로젝트 구조
```
frontend/
├── package.json           # React, react-router-dom, Vite 설정
├── src/
│   ├── App.jsx            # 레이아웃 및 Router 구성
│   ├── main.jsx           # BrowserRouter + SW 등록
│   ├── registerSW.js      # 서비스 워커 등록 로직
│   ├── routes/            # Home, Survey, Recommendations, Favorites, NotFound
│   └── App.css / index.css# 기본 테마 스타일
├── public/
│   ├── manifest.webmanifest
│   └── sw.js              # 단순 캐시 전략
└── vite.config.js         # React 플러그인, dev server 설정
```

### 8.2 의존성
- React 18.x, React Router DOM 6.x
- Vite 5.x (`@vitejs/plugin-react`)

설치 명령:
```bash
cd frontend
npm install
```
> 네트워크 제약이 있는 환경에서는 패키지 확보 후 실행 필요.

### 8.3 실행/빌드
- 개발 서버: `npm run dev -- --host`
  - 기본 포트 5173. 환경 변수(`VITE_PORT`)로 변경 가능.
- 프로덕션 빌드: `npm run build`
- 빌드 결과 미리보기: `npm run preview`

### 8.4 PWA 구성
- `public/manifest.webmanifest`에 name/short_name/start_url/icons 설정.
- `public/sw.js`: 오프라인 캐시(기본적인 network-first + cache fallback)
- `src/registerSW.js`가 프로덕션 환경에서만 서비스 워커 등록.

### 8.5 라우팅 플레이스홀더
| 경로 | 컴포넌트 | 설명 |
|------|-----------|------|
| `/` | `Home` | 온보딩 안내 및 CTA |
| `/survey` | `Survey` | 설문 단계 로드맵 (FE-2에서 상세 구현 예정) |
| `/results` | `Recommendations` | 추천 결과 머리말 |
| `/favorites` | `Favorites` | 즐겨찾기 빈 상태 안내 |
| `*` | `NotFound` | 404 fallback |

### 8.6 스타일
- `App.css`에 Topbar/CTA/카드 레이아웃, 공용 버튼 스타일 정의.
- 모바일 대응(≤640px) 미디어 쿼리 포함.

### 8.7 운영/배포 팁
- 빌드 산출물(`frontend/dist`)은 정적 파일로 배포 가능. 백엔드와 통합 시 Nginx/Reverse Proxy에서 `/api`와 `/`를 분리 라우팅.
- 환경 변수: API base URL을 `.env` 또는 `import.meta.env` 로 주입하여 배포 차이 관리.
- PWA 진단: `npm run build` → `npm run preview` 후 브라우저 DevTools의 Application 탭에서 manifest/Service Worker 등록 상태 확인.

---
## 9. 온보딩 설문 UI (FE-2)

### 9.1 구성 요소
- `SurveyWizard.jsx` : 다단계 설문 흐름 제어, 상태 관리, 검증, 제출 스텁 호출.
- `useSurveyForm.js` : 설문 데이터 구조/상태/검증 로직 제공.
- `surveyApi.js` : `submitSurvey` 스텁. 향후 `/api/v1/surveys` 연동 시 여기서 호출.
- `steps/` 디렉터리: 각 단계별 컴포넌트 (`BudgetStep`, `UsageStep`, `PreferenceStep`, `ConditionStep`, `OptionsStep`, `SummaryStep`).
- `survey.css` : 설문 UI에 필요한 스타일 (진행바, 폼, 에러 메시지 등).

### 9.2 단계 구성
1. **예산** – 최소/최대 예산, 구매 방식(리스/할부/일시불) 입력.
2. **사용 용도** – 주요 목적, 평균 탑승 인원 선택.
3. **선호도** – 선호 차종/브랜드 다중 선택 및 커스텀 입력.
4. **차량 조건** – 허용 연식/주행거리 지정.
5. **추가 옵션** – 필수 옵션 자유 입력.
6. **요약** – 입력한 정보를 확인하고 제출.

각 단계 이동 시 `validateStep` 로직으로 필수값 검증 후 다음 단계로 이동합니다.

### 9.3 상태 및 제출
- `useSurveyForm`가 `formData`, `currentStep`, `status`, `error`, `result` 상태를 관리.
- 제출 시 `submitSurvey` 스텁이 호출되고, 성공하면 `recommendationId`를 반환하여 화면에 안내.
- API가 준비되면 `surveyApi.js` 내 Fetch 로직을 실제 엔드포인트로 교체하면 됩니다.

### 9.4 향후 확장 포인트
- 단계별 API 프리페치 (예: 브랜드/차종 목록을 서버에서 가져오기).
- 폼 라이브러리 도입 (react-hook-form, yup 등)으로 검증 및 상태 관리를 고도화.
- A/B 테스트를 위한 설문 플로우 분기, 설문 중간 저장 기능.

---
## 10. 추천 결과 & 즐겨찾기 UI (FE-3)

### 10.1 구성 요소
- `RecommendationsBoard.jsx` : 추천 결과 목록 + 비교 선택 + 즐겨찾기 토글 관리.
- `RecommendationCard.jsx` : 차량 정보 카드, 점수/옵션 표시.
- `useRecommendations.js` : 추천 데이터 로딩, 비교/즐겨찾기 상태 관리, API 스텁 연동.
- `favorites/FavoritesList.jsx` : 즐겨찾기 목록 렌더링, 삭제 기능.
- `recommendations.css`, `favorites.css` : UI 스타일.

### 10.2 동작 요약
1. `fetchRecommendations` 스텁이 추천 목록을 반환 (향후 `/api/v1/recommendations/{id}`로 대체).
2. 사용자가 비교 버튼을 누르면 최대 4개까지 `compareList`에 유지.
3. 즐겨찾기 토글 시 `toggleFavorite` → 스텁 저장소 업데이트 (`favoritesApi`).
4. 즐겨찾기 화면(`/favorites`)에서 저장된 차량을 확인하고 삭제할 수 있음.

### 10.3 상태 관리
- `useRecommendations` 훅에서 추천/비교/즐겨찾기를 일괄 관리.
- 비교 목록은 한도 초과 시 가장 오래된 항목을 제거하여 항상 4개 이하 유지.
- 즐겨찾기는 스텁 Map을 사용하나, 실제 구현 시 백엔드 `/favorites` API로 교체 예정.

### 10.4 테스트
- `useRecommendations.test.js` 에서 비교 한도 상수를 검증. (향후 Vitest 환경에서 확장 권장)
- 종속 패키지(`vitest`, `@testing-library/react`, `lucide-react`)를 설치한 후 `npm run test`로 실행.

### 10.5 향후 개선 포인트
- 실제 백엔드 API 연동 (추천 ID/즐겨찾기 API).
- 비교표 UI 확장: 선택된 차량을 표 형태로 비교, 내보내기 기능.
- 즐겨찾기 공유/정렬 옵션, 필터 기능 추가.

---
