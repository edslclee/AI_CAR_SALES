# 차량 CSV 업로드 스키마

관리자(혹은 개발자)가 차량 카탈로그를 대량으로 업로드할 때 사용하는 표준 포맷입니다. `UTF-8` 인코딩된 CSV를 가정하며, 헤더 행은 필수입니다.

| 컬럼 | 필수 | 설명 | 예시 |
|------|------|------|------|
| `oem_code` | ✅ | 제조사 고유 차량 코드. `brand-model` 형식 권장 | `HYUNDAI-SONATA` |
| `model_name` | ✅ | 모델명 | `Sonata` |
| `trim` | ⛔️ | 트림/세부 등급 | `N Line` |
| `price` | ✅ | 차량 가격 (KRW) | `32800000` |
| `body_type` | ✅ | 차체 타입 (Sedan, SUV, Hatchback, EV 등) | `Sedan` |
| `fuel_type` | ✅ | 연료 타입 (Gasoline, Diesel, Hybrid, EV 등) | `Hybrid` |
| `efficiency` | ⛔️ | 연비. 숫자만 입력, 단위는 km/L 또는 km/kWh | `17.2` |
| `seats` | ⛔️ | 좌석 수 | `5` |
| `drivetrain` | ⛔️ | 구동 방식 | `FWD` |
| `release_year` | ✅ | 출시 연식 (YYYY) | `2024` |
| `features` | ⛔️ | 차량 옵션 JSON 문자열 | `{"safety":["AEB","LKA"],"infotainment":["10in-display"]}` |
| `media_assets` | ⛔️ | 이미지/영상 자원 JSON 문자열 | `{"images":["https://.../front.png"]}` |

## 유효성 체크리스트
- 필수 컬럼 누락 시 전체 업로드를 거부해야 합니다.
- `price`, `seats`, `release_year` 등 숫자 필드는 정수여야 합니다.
- `efficiency`는 실수 값 허용.
- JSON 컬럼(`features`, `media_assets`)은 JSON 문자열로 존재해야 하며, 잘못된 JSON은 오류로 간주합니다.
- 중복 `(oem_code, model_name, trim)` 조합은 업데이트 시 덮어쓰기 정책, 신규 시엔 거부 정책 등 확정 필요.

## 샘플 데이터 위치
- 정상 예시: `data/samples/cars_valid.csv`
- 오류 예시: `data/samples/cars_invalid.csv`

## 추천 사용 흐름
1. 샘플 파일을 기준으로 CSV를 작성한다.
2. 업로드 전 `tools/check_csv.py`(추후 구현 예정) 등으로 사전 검증.
3. 관리자 API 또는 `scripts/seed_sample_cars.sh` 같은 도구로 DB에 반영한다.

> ⚠️ 프로덕션 환경에서는 CSV 파일을 버전 관리(예: Git LFS) 대신 별도 스토리지에 보관하고 업로드 로그를 `csv_upload_jobs` 테이블에 남겨 감사 추적을 수행하세요.
