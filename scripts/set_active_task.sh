#!/usr/bin/env bash
set -euo pipefail

TASK_ID="${1:?Usage: set_active_task.sh TASK_ID 'Title' 'Goal' }"
TITLE="${2:?Title}"
GOAL="${3:?Goal}"

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
CTX="$ROOT/bootstrap/context.md"
LOG="$ROOT/docs/history/STATELOG.md"
NOW="$(date '+%Y-%m-%d %H:%M %Z')"

# context.md 없으면 가드
if [ ! -f "$CTX" ]; then
  echo "ERROR: $CTX not found. Create it first." >&2
  exit 1
fi

# 이전 Task 추출
PREV="$(grep -E '^- Task ID:' "$CTX" | head -n1 | sed -E 's/^- Task ID: *//; s/ *–.*$//' || true)"
[ -n "$PREV" ] || PREV="<none>"

# ## Active Task 블록 교체
python3 - "$CTX" "$TASK_ID" "$TITLE" "$GOAL" <<'PY'
import re, sys, pathlib
p=pathlib.Path(sys.argv[1]); tid=sys.argv[2]; title=sys.argv[3]; goal=sys.argv[4]
s=p.read_text()
block=(f"## Active Task\n"
       f"- Task ID: {tid} – {title}\n"
       f"- 목표: {goal}\n"
       f"- DoD(완료조건): (작성/업데이트 요망)\n"
       f"- 승인 규칙: 완료 보고 후 승인 받고 다음 Task 이동\n")
if re.search(r"## Active Task[\\s\\S]*?(?=\\n## |\\Z)", s, flags=re.M):
    s=re.sub(r"## Active Task[\\s\\S]*?(?=\\n## |\\Z)", block, s, flags=re.M)
else:
    s = s.rstrip() + "\n\n" + block + "\n"
p.write_text(s)
PY

# 히스토리 append
{
  echo "## $NOW"
  echo "- set-active: $PREV → $TASK_ID"
  echo "- title: $TITLE"
  echo "- goal: $GOAL"
  echo
} >> "$LOG"

echo "Active Task set to $TASK_ID" 
