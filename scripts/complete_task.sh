#!/usr/bin/env bash
set -euo pipefail

PATTERN="${1:?Usage: complete_task.sh 'regex to identify a task block' }"

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
TASKS1="$ROOT/deliverables/tasks.md"
TASKS2="$ROOT/deliverables/tasks_v2.md"
LOG="$ROOT/docs/history/STATELOG.md"
NOW="$(date '+%Y-%m-%d %H:%M %Z')"

# 후보 파일 목록 (v2 우선)
CANDIDATES=()
[ -r "$TASKS2" ] && CANDIDATES+=("$TASKS2")
[ -r "$TASKS1" ] && CANDIDATES+=("$TASKS1")

if [ "${#CANDIDATES[@]}" -eq 0 ]; then
  echo "ERROR: neither $TASKS2 nor $TASKS1 is readable." >&2
  exit 1
fi

# 파이썬으로: 후보 파일을 순서대로 탐색 → 첫 매칭 파일에서 토글 수행
RESULT="$(
python3 - "$PATTERN" "${CANDIDATES[@]}" <<'PY'
import re, sys, pathlib, json

pattern = sys.argv[1]
files = sys.argv[2:]
rx = re.compile(pattern, re.I)

def try_toggle(path: pathlib.Path):
    s = path.read_text(encoding="utf-8")

    # 전각/엔대시 정규화
    trans = str.maketrans({'［':'[','］':']','–':'-','—':'-','−':'-'})
    s_norm = s.translate(trans)

    # 블록 분리(빈 줄 포함 보존)
    parts = re.split(r'(\n\s*\n)', s_norm)  # 본문, 구분자, 본문, 구분자...
    cb_line = re.compile(r'^([ \t]*[-*][ \t]*\[[ xX]\])[ \t]*(.*)$', re.M)
    unchecked = re.compile(r'\[[ \t]\]')

    for i in range(0, len(parts), 2):
        block = parts[i]
        if not block.strip():
            continue
        if rx.search(block):
            # 블록 내 첫 체크박스 미체크 항목 토글
            def toggle_one(m):
                line = m.group(0)
                if unchecked.search(line):
                    return unchecked.sub('[x]', line, count=1)
                return line
            new_block, n = cb_line.subn(toggle_one, block, count=1)
            if n > 0 and new_block != block:
                parts[i] = new_block
                out = ''.join(parts)
                return True, out
            # 블록은 맞았지만 미체크 체크박스가 없음
            return False, None
    # 매칭되는 블록이 없음
    return None, None

for f in files:
    p = pathlib.Path(f)
    try:
        ok, out = try_toggle(p)
    except FileNotFoundError:
        continue
    if ok is True:
        # 성공
        p.write_text(out, encoding="utf-8")
        print(json.dumps({"ok": True, "file": str(p)}))
        sys.exit(0)
    elif ok is False:
        # 블록은 찾았으나 토글할 체크박스가 없음
        print(json.dumps({"ok": False, "file": str(p), "error": "no unchecked checkbox in matched block"}))
        sys.exit(2)

# 어느 파일에서도 블록을 못 찾음
print(json.dumps({"ok": False, "error": "no matching block in any tasks file"}))
sys.exit(2)
PY
)"

# 파싱
echo "$RESULT" | grep -q '"ok": true' || {
  echo "WARN: toggle failed → $RESULT" >&2
  echo "No change made. Aborting without logging." >&2
  exit 2
}

# 성공한 파일 경로 추출
SRC_FILE="$(python3 -c 'import json,sys; print(__import__("json").loads(sys.stdin.read())["file"])' <<<"$RESULT")"

# STATELOG append-only
mkdir -p "$(dirname "$LOG")"
{
  echo "## $NOW"
  echo "- complete: $PATTERN"
  echo "- source: $SRC_FILE"
  echo
} >> "$LOG"

echo "Task completed (matched by regex): $PATTERN"
echo "Source file: $SRC_FILE"

chmod +x scripts/complete_task.sh
