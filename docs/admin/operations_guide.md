# 관리자 운영 가이드 (CSV 업로드 & 추천 검수)

본 문서는 AI Car Sales MVP에서 차량 카탈로그를 관리하고 추천 품질을 보장하기 위한 관리자 운용 절차를 정리한 가이드입니다. 월별 카탈로그 갱신 담당자(제휴/MD), 운영 엔지니어, QA 담당자가 참고할 수 있도록 단계별 체크리스트를 제공합니다.

---
## 1. 준비 사항
- **접속 권한**
  - 관리자 콘솔(웹) 계정
  - 백엔드 API 인증 토큰(필요 시 운영팀 발급)
  - 데이터베이스 읽기 전용 계정 (선택)
- **필요 도구**
  - `git`, `python3`, `npm`
  - CSV 작성 툴(Excel/Google Sheets 등, UTF-8 인코딩 저장 지원)
  - 터미널에서 `python tools/check_csv.py` 실행 가능해야 합니다.
- **환경 상태** *(scripts/status.sh 참고)*
  - PostgreSQL / Redis 컨테이너 기동 (`docker compose ps`)
  - 백엔드 `/health` 200 응답
  - 프런트 관리자 페이지 접속 가능 (`/admin/upload`)

---
## 2. CSV 작성 및 사전 검증
1. **스키마 확인**
   - `docs/admin/csv_schema.md`의 필수/선택 컬럼을 준수합니다.
   - JSON 컬럼(`features`, `media_assets`)은 이스케이프 포함한 유효한 JSON 문자열이어야 합니다.
2. **파일 저장 규칙**
   - UTF-8 (BOM 허용)로 저장
   - 확장자 `.csv`
   - 파일명은 `cars_YYYYMMDD.csv` 형태 권장 (예: `cars_202509.csv`)
3. **사전 검증 스크립트 실행**
   ```bash
   python tools/check_csv.py data/upload/cars_YYYYMMDD.csv
   ```
   - **검증 대상**: 필수 컬럼 누락, 정수/실수 필드 오류, JSON 파싱 오류, `(oem_code, model_name, trim)` 중복
   - **통과 기준**: `검증 결과: 통과`
   - **실패 시**: 출력된 행 번호/메시지를 기반으로 CSV를 수정 후 재검증
   - `--allow-empty-trim` 옵션으로 동일 모델/트림이 없는 경우 경고를 완화할 수 있으나, 기본값 유지 권장
4. **감사 로그 작성** (선택)
   - `logs/csv_validation.md`에 실행 결과를 추가해 추후 감사용으로 보관합니다.

---
## 3. 관리자 콘솔 업로드 절차
1. **접속**: 브라우저에서 관리자 페이지 `/admin/upload` 로 이동
2. **업로드 실행**
   - `CSV 업로드` 카드 내 파일 선택 → `업로드 시작`
   - UI에 Job ID가 표시되며 `검증 중` 상태로 전환
3. **상태 모니터링**
   - UI 로그 패널에서 진행 로그 확인 (`헤더 검증`, `유효성 검사 완료` 등)
   - 성공 시 `업로드 완료` 메시지와 녹색 상태 배지 표시
4. **백엔드 상태 API 사용(선택)**
   ```bash
   curl -H "Authorization: Bearer <TOKEN>" \
        https://<host>/api/v1/admin/cars/upload/<jobId>
   ```
   - 반환 필드: `status`(`PENDING|PROCESSING|SUCCEEDED|FAILED`), `message`, `errorReport`, `completedAt`
5. **업로드 실패 시**
   - UI/상태 API에서 `FAILED` 표시 및 `errorReport` 확인
   - 발생 가능한 원인: CSV 필드 오류, DB 제약 조건 위반, 내부 예외
   - 조치: CSV 수정 후 재업로드 또는 백엔드 로그(`/logs/app.log`) 확인 요청

---
## 4. 오류 대응 프로세스
| 상황 | 조치 | 후속 조치 |
|------|------|-----------|
| CSV 검증 실패 (사전 스크립트) | 행별 오류 메시지 확인 후 CSV 수정 | 수정된 파일로 다시 `check_csv.py` 실행 |
| 업로드 후 `FAILED` | `errorReport` 기반으로 데이터 오류 확인 | CSV 재작성 후 업로드. 동일 Job ID는 재사용 불가 → 새 업로드 필요 |
| 업로드 중단/네트워크 끊김 | `status` API로 Job ID 상태 확인 | 미완료(PENDING) 상태면 재시도 필요 여부 결정 |
| 차량이 DB에 반영되지 않음 | `csv_upload_jobs` 테이블 조회: `SELECT * FROM csv_upload_jobs ORDER BY created_at DESC LIMIT 5;` | `message`, `error_report`를 확인하고 개발팀에 공유 |
| 긴급 롤백 필요 | 이전 버전 CSV를 재업로드하거나 DB 백업에서 복원 | 운영팀 승인 후 수행, 변경 이력 `csv_upload_jobs` 기록 확보 |

**백엔드 로그 위치**: `logs/app.log` (운영 서버 기준). 오류 스택을 첨부해 개발팀에 전달하세요.

---
## 5. 추천 결과 검수 체크리스트
업로드 완료 후, 추천 품질을 보장하기 위해 최소한 아래 항목을 점검합니다.

1. **UI 확인**
   - 추천 결과 페이지 `/results` 에서 새 차량이 노출되는지 확인
   - 즐겨찾기/비교표 기능 동작 여부 (버튼 토글, 비교 리스트 업데이트)
2. **데이터 검증**
   - `cars` 테이블 내 가격/연식 등 주요 필드 검증
     ```sql
     SELECT oem_code, model_name, trim, price, release_year
     FROM cars
     ORDER BY updated_at DESC LIMIT 10;
     ```
   - 중복 여부 확인
     ```sql
     SELECT oem_code, model_name, trim, COUNT(*)
     FROM cars
     GROUP BY 1,2,3 HAVING COUNT(*) > 1;
     ```
3. **추천 품질 샘플링**
   - 설문 응답(예: 예산 3천만원, SUV)을 입력해 추천 리스트가 기대와 일치하는지 확인
   - 추천 점수 및 주요 스펙이 CSV 내용과 일치하는지 확인
4. **로그/지표**
   - `csv_upload_jobs` 최근 레코드 `status=SUCCEEDED`, `message`에 업데이트 수 확인
   - 운영 지표: 업로드 완료 시점 기록, 처리 소요 시간 측정(추후 SLA 관리)

체크리스트 결과를 내부 공유 문서(예: Confluence, Notion)에 기록하고, 이상 발생 시 운영 회신 SLA에 따라 개발팀에 전달합니다.

---
## 6. 주기 및 책임 구분
| 역할 | 주요 업무 | 빈도 |
|------|-----------|------|
| 제휴/MD | 신차/모델 가격 변동 수집, CSV 작성 | 월 1회 (필요 시 ad-hoc) |
| 운영 엔지니어 | 사전 검증, 업로드 진행, 상태 모니터링 | 업로드 발생 시 |
| QA | 추천 결과 샘플링, 오류 재현/보고 | 업로드 직후 |

권장 스케줄: 월 초 CSV 반영, 월 중/말 추가 업데이트 시 동일 절차 반복. 모든 업로드는 `csv_upload_jobs` 기록과 `logs/csv_validation.md`를 통해 감사 추적이 가능해야 합니다.

---
## 7. FAQ
- **Q. 업로드 중 브라우저를 닫으면 어떻게 되나요?**
  - 서버 측 Job은 계속 진행되며, Job ID로 상태를 조회할 수 있습니다. 단, 진행 상황을 UI에서 잃으므로 종료 전 Job ID를 기록하세요.
- **Q. 일부 차량만 수정하고 싶습니다.**
  - 현재 버전은 전체 CSV를 재업로드하여 upsert합니다. 부분 수정을 하려면 CSV에 대상 차량만 포함해도 되지만, 누락된 차량은 기존 데이터를 유지합니다.
- **Q. JSON 필드를 작성하기 어렵습니다.**
  - `docs/admin/csv_schema.md` 예시를 참고하거나, 기존 CSV에서 필요한 구조를 복사해 수정하세요. 사전 검증 스크립트가 JSON 문법 오류를 알려줍니다.
- **Q. 서버가 다운되어 업로드가 불가능합니다.**
  - `scripts/status.sh`로 현재 상태를 확인하고, 장애 대응 프로세스에 따라 백엔드 재기동 혹은 개발팀에 신고하세요. 긴급 시 CSV를 SFTP 등으로 전달해 수동 DB 반영을 요청할 수 있습니다.

---
## 8. 참고 자료
- 스키마 문서: `docs/admin/csv_schema.md`
- 검증 스크립트: `tools/check_csv.py`
- 검증 로그 템플릿: `logs/csv_validation.md`
- 백엔드 업로드 API: `POST /api/v1/admin/cars/upload`, `GET /api/v1/admin/cars/upload/{jobId}`
- 프런트 관리자 화면 소스: `frontend/src/features/admin/upload/*`
- 장애 대응 연락처/슬랙 채널: *(운영팀 표 작성 필요)*

---
본 가이드는 MVP 운용 기준이며, CSV 업로드 자동화나 외부 제휴 API 연동 시 절차가 변경될 수 있습니다. 변경 사항은 본 문서와 `docs/maintenance_guide.md`에 동시에 반영하세요.
