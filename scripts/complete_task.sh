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

# 체크박스 토글: 대시/공백 변형 허용, 매칭 실패 시 비정상 종료(히스토리 미기록)
python3 - "$TASKS" "$PATTERN" <<'PY'
import re, sys, pathlib
p=pathlib.Path(sys.argv[1]); rx=re.compile(sys.argv[2])
s=p.read_text()

# 체크박스 패턴: "- [ ]" / "-  [ ]" / "* [ ]" 등 변형 허용
cb = re.compile(r'^([ \t]*[-*][ \t]*\[[ \t]\])[ \t]*(.*)$', re.M)

changed = False
lines = s.splitlines(True)
for i, line in enumerate(lines):
    if rx.search(line):
        m = cb.match(line)
        if m:
            # 첫 체크박스를 [x]로 토글
            lines[i] = re.sub(r'\[[ \t]\]', '[x]', line, count=1)
            changed = True
            break

if not changed:
    print("WARN: no line matched the pattern OR no checkbox to toggle", file=sys.stderr)
    sys.exit(2)

p.write_text(''.join(lines))
PY
rc=$? || true
if [ ${rc:-0} -ne 0 ]; then
  echo "No change made. Aborting without logging." >&2
  exit $rc
fi

# 여기 도달하면 실제 변경이 있었음 → 히스토리 append
{
  echo "## $NOW"
  echo "- complete: $PATTERN"
  echo
} >> "$LOG"

echo "Task completed (matched by regex): $PATTERN"