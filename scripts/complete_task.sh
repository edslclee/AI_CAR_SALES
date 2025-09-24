#!/usr/bin/env bash
set -euo pipefail
PATTERN="${1:?Usage: complete_task.sh 'regex to identify a line in deliverables/tasks.md' }"

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
TASKS="$ROOT/deliverables/tasks.md"
LOG="$ROOT/docs/history/STATELOG.md"
NOW="$(date '+%Y-%m-%d %H:%M %Z')"

if [ ! -f "$TASKS" ]; then
  echo "ERROR: $TASKS not found." >&2
  exit 1
fi

# 체크박스 [ ] → [x] 로 치환
python3 - "$TASKS" "$PATTERN" <<'PY'
import re, sys, pathlib
p=pathlib.Path(sys.argv[1]); rx=re.compile(sys.argv[2])
s=p.read_text()
def rep(m):
    line=m.group(0)
    return line.replace("- [ ]", "- [x]", 1) if "- [ ]" in line else line
s2 = re.sub(rx, rep, s, count=1)
if s2 == s:
    print("WARN: no line matched the pattern", file=sys.stderr)
p.write_text(s2)
PY

# 히스토리 append
{
  echo "## $NOW"
  echo "- complete: $PATTERN"
  echo
} >> "$LOG"

echo "Task completed (matched by regex): $PATTERN"
