#!/usr/bin/env bash
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
CTX="$ROOT/bootstrap/context.md"
LOG="$ROOT/logs/status_check.md"
NOW="$(date '+%Y-%m-%d %H:%M %Z')"

# 도구 확인
command -v curl >/dev/null || { echo "ERROR: curl 필요"; exit 1; }
command -v docker >/dev/null || echo "WARN: docker 미설치/미실행 (DB 체크 생략)"

# 1) Backend /health (기본 8080)
BACKEND_URL="${BACKEND_URL:-http://localhost:8080/health}"
BACKEND_OK="❌"
BACKEND_NOTE=""
if curl -sS --max-time 2 "$BACKEND_URL" >/dev/null; then
  # 상태 페이로드까지 확인(선택)
  STATUS=$(curl -sS --max-time 2 "$BACKEND_URL" || true)
  if echo "$STATUS" | grep -qi '"ok"\|"status":"ok"'; then
    BACKEND_OK="✅"
    BACKEND_NOTE="200 OK"
  else
    BACKEND_OK="⚠️"
    BACKEND_NOTE="응답 있음(내용 확인 필요)"
  fi
else
  BACKEND_NOTE="접속 불가"
fi

# 2) Docker Compose DB/Redis (서비스 이름 유연 인식)
DB_OK="❌"; REDIS_OK="❌"
if command -v docker >/dev/null && docker info >/dev/null 2>&1; then
  # ps 출력에서 db/redis 유사명 매칭
  NAMES=$(docker ps --format '{{.Names}}' || true)
  if echo "$NAMES" | grep -Eiq '(db|postgres)'; then DB_OK="✅"; fi
  if echo "$NAMES" | grep -Eiq 'redis'; then REDIS_OK="✅"; fi
fi

DB_SUMMARY="$DB_OK"
if [ "$REDIS_OK" = "✅" ]; then
  DB_SUMMARY="Postgres ${DB_OK} / Redis ✅"
else
  DB_SUMMARY="Postgres ${DB_OK} / Redis ❌"
fi

# 3) Web (Vite dev 5173)
WEB_PORT="${WEB_PORT:-5173}"
WEB_OK="❌"; WEB_NOTE=""
if curl -sS --max-time 2 "http://localhost:${WEB_PORT}" >/dev/null; then
  WEB_OK="✅"; WEB_NOTE="포트 ${WEB_PORT} 응답"
else
  # macOS lsof 체크
  if command -v lsof >/dev/null && lsof -iTCP -sTCP:LISTEN -P | grep -q ":${WEB_PORT} "; then
    WEB_OK="⚠️"; WEB_NOTE="포트 열림(HTTP 응답 없음)"
  else
    WEB_NOTE="포트 닫힘"
  fi
fi

# 4) context.md의 '## 현재 상태' 블록 갱신
if [ ! -f "$CTX" ]; then
  echo "# Context" > "$CTX"
fi

python3 - "$CTX" "$BACKEND_OK" "$DB_SUMMARY" "$WEB_OK" "$NOW" "$BACKEND_NOTE" "$WEB_NOTE" <<'PY'
import re, sys, pathlib
ctx = pathlib.Path(sys.argv[1])
b_ok = sys.argv[2]
db_sum = sys.argv[3]
w_ok = sys.argv[4]
now = sys.argv[5]
b_note = sys.argv[6]
w_note = sys.argv[7]

text = ctx.read_text(encoding="utf-8")

block = f"""## 현재 상태
- Backend: /health {b_ok} ({b_note})
- DB: {db_sum}
- Web: vite dev {w_ok} ({w_note})
- Last Checked: {now}
"""

if re.search(r"## 현재 상태[\s\S]*?(?=\n## |\Z)", text, flags=re.M):
    text = re.sub(r"## 현재 상태[\s\S]*?(?=\n## |\Z)", block.strip()+"\n", text, flags=re.M)
else:
    text = text.rstrip()+"\n\n"+block+"\n"

ctx.write_text(text, encoding="utf-8")
PY

# 5) 로그 남기기
mkdir -p "$(dirname "$LOG")"
{
  echo "## $NOW"
  echo "- Backend: $BACKEND_OK $BACKEND_NOTE ($BACKEND_URL)"
  echo "- DB: $DB_SUMMARY"
  echo "- Web: $WEB_OK $WEB_NOTE (:${WEB_PORT})"
  echo
} >> "$LOG"

echo "상태 갱신 완료 → bootstrap/context.md / logs/status_check.md"