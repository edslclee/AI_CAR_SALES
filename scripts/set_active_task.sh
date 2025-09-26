#!/usr/bin/env bash
set -euo pipefail

# 사용법: set_active_task.sh <TASK_ID>
TASK_ID="${1:?Usage: set_active_task.sh <TASK_ID>  # e.g., BE-5 }"

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
CTX="$ROOT/bootstrap/context.md"
TASKS="$ROOT/deliverables/tasks.md"
LOG="$ROOT/docs/history/STATELOG.md"
NOW="$(date '+%Y-%m-%d %H:%M %Z')"

[ -f "$CTX" ]  || { echo "ERROR: $CTX not found."; exit 1; }
[ -r "$TASKS" ] || { echo "ERROR: $TASKS not readable (권한/소유권 확인 필요)"; exit 1; }

# 이전 Active Task ID 추출(없으면 <none>)
PREV="$(grep -E '^- Task ID:' "$CTX" | tail -n1 | sed -E 's/^- Task ID: *//; s/ *–.*$//' || true)"
[ -n "${PREV:-}" ] || PREV="<none>"

# tasks.md에서 TASK_ID 블록 찾아 Title/Goal/DoD/Deliverable 추출 (JSON 한 덩어리로 받기)
RESULT_JSON="$(
python3 - "$TASKS" "$TASK_ID" <<'PY'
import re, sys, pathlib, json
tasks_path, tid = sys.argv[1], sys.argv[2]
try:
    s = pathlib.Path(tasks_path).read_text(encoding="utf-8")
except Exception as e:
    print(json.dumps({"ok": False, "error": f"read_error: {e}"}))
    sys.exit(0)

# 전각/엔대시 정규화
trans = str.maketrans({'［':'[','］':']','–':'-','—':'-','−':'-'})
s = s.translate(trans)

# 체크박스에서 해당 Task 찾기
# 예: "- [ ] BE-5: 관리자 CSV 업로드 파이프라인 초안"
m = re.search(rf'^\s*-\s*\[\s*[xX ]\s*\]\s*({re.escape(tid)})\s*([:-])?\s*(?:-\s*)?(.*)$', s, flags=re.M)
if not m:
    print(json.dumps({"ok": False, "error":"Task ID not found"}))
    sys.exit(0)

line_start = m.start()
lines = s.splitlines(True)
start_idx = s[:line_start].count('\n')  # 0-based
title = (m.group(3) or "").strip()
sep   = m.group(2) or ":"  # 기본 콜론

# 블록 경계(다음 체크박스 전까지)에서 Goal/DoD/Deliverable 찾기
goal = ""
dod = ""
deliver = ""
for i in range(start_idx+1, len(lines)):
    line = lines[i]
    if re.match(r'^\s*-\s*\[\s*[xX ]\s*\]\s*', line): break
    mg  = re.match(r'^\s*-\s*Goal:\s*(.*)$', line)
    md  = re.match(r'^\s*-\s*DoD:\s*(.*)$', line)
    mdl = re.match(r'^\s*-\s*Deliverable:\s*(.*)$', line)
    if mg:  goal     = mg.group(1).strip()
    if md:  dod      = md.group(1).strip()
    if mdl: deliver  = mdl.group(1).strip()

# Goal 기본값: 체크박스 제목(없으면 tid 자체)
if not goal:
    goal = title or tid

print(json.dumps({
    "ok": True,
    "id": tid,
    "sep": sep,
    "title": title,
    "goal": goal,
    "dod": dod,
    "deliver": deliver
}, ensure_ascii=False))
PY
)"

# JSON 유효성 체크
echo "$RESULT_JSON" | grep -q '"ok": true' || { echo "ERROR: parse failed → $RESULT_JSON" >&2; exit 1; }

# 필드 추출(파이썬 원라이너; jq 불필요)
TITLE="$(python3 -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("title",""))' <<<"$RESULT_JSON")"
SEP="$(python3   -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("sep",":"))'   <<<"$RESULT_JSON")"
GOAL="$(python3  -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("goal",""))'  <<<"$RESULT_JSON")"
DOD="$(python3   -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("dod",""))'   <<<"$RESULT_JSON")"
DELIV="$(python3 -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("deliver",""))'<<<"$RESULT_JSON")"

# 기본값 보정
[ -n "$TITLE" ] || TITLE="$TASK_ID"
[ -n "$GOAL" ]  || GOAL="$TITLE"
[ -n "$DOD" ]   || DOD="(작성/업데이트 요망)"

# context.md: 기존 모든 Active Task 블록 제거 → 새 블록 1개 삽입
python3 - "$CTX" "$TASK_ID" "$SEP" "$TITLE" "$GOAL" "$DOD" "$DELIV" <<'PY'
import re, sys, pathlib
ctx, tid, sep, title, goal, dod, deliver = sys.argv[1:8]
p = pathlib.Path(ctx)
text = p.read_text(encoding="utf-8")

# 기존 블록 제거
text = re.sub(r"## Active Task[\s\S]*?(?=\n## |\Z)", "", text, flags=re.M).rstrip() + "\n\n"

# 요청 형식: "- Task ID: BE-5: – 제목" (ID 뒤 콜론 유지 + en dash)
dash = "–"
line_id = f"- Task ID: {tid}{sep} {dash} {title or tid}"

block = f"""## Active Task
{line_id}
- 목표: {goal}
- DoD(완료조건): {dod}""" + (f"\n- Deliverable: {deliver}" if deliver else "") + """
- 승인 규칙: 완료 보고 후 승인 받고 다음 Task 이동
"""
p.write_text(text + block + "\n", encoding="utf-8")
PY

# 전환 로그
mkdir -p "$(dirname "$LOG")"
{
  echo "## $NOW"
  echo "- set-active: $PREV → $TASK_ID"
  echo "- title: $TITLE"
  echo "- goal: $GOAL"
  echo "- auto-filled: Goal/DoD$( [ -n "$DELIV" ] && echo '/Deliverable' ) from deliverables/tasks.md"
  echo
} >> "$LOG"

echo "Active Task set: $PREV → $TASK_ID"
chmod +x scripts/set_active_task.sh