#!/usr/bin/env bash
set -euo pipefail
TASK_ID="${1:?Usage: set_active_task.sh <TASK_ID>}"
ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
CTX="$ROOT/bootstrap/context.md"
TASKS1="$ROOT/deliverables/tasks.md"
TASKS2="$ROOT/deliverables/tasks_v2.md"
LOG="$ROOT/docs/history/STATELOG.md"
NOW="$(date '+%Y-%m-%d %H:%M %Z')"
DEBUG="${SET_ACTIVE_DEBUG:-0}"

[ -f "$CTX" ] || { echo "ERROR: $CTX not found."; exit 1; }
[ -r "$TASKS1" -o -r "$TASKS2" ] || { echo "ERROR: neither $TASKS1 nor $TASKS2 readable"; exit 1; }

PREV="$(grep -E '^- Task ID:' "$CTX" 2>/dev/null | tail -n1 | sed -E 's/^- Task ID: *//; s/ *–.*$//' || true)"
[ -n "${PREV:-}" ] || PREV="<none>"

RESULT_JSON="$(
python3 - "$TASKS2" "$TASKS1" "$TASK_ID" <<'PY'
import re, sys, pathlib, json

tasks2, tasks1, tid = sys.argv[1], sys.argv[2], sys.argv[3]

def load(p):
    try:
        t = pathlib.Path(p)
        if not t.exists(): return None
        txt = t.read_text(encoding="utf-8").replace('\r\n','\n').replace('\r','\n')
        # 공백/불릿/대시 정규화
        for ch in ['\u00A0','\u2000','\u2001','\u2002','\u2003','\u2004','\u2005','\u2006','\u2007','\u2008','\u2009','\u200A','\u200B']:
            txt = txt.replace(ch, ' ')
        txt = txt.translate(str.maketrans({'［':'[','］':']','–':'-','—':'-','−':'-'}))
        for ch in ['•','●','▪','▫','○','※','‣','◦']:
            txt = txt.replace(ch, '-')
        return txt
    except Exception:
        return None

def parse(text, tid):
    if not text: return None

    # 체크박스 라인
    m = re.search(rf'^\s*-\s*\[\s*[xX ]\s*\]\s*({re.escape(tid)})\s*([:-])?\s*(?:-\s*)?(.*)$', text, flags=re.M)
    if not m: return None

    line_start = m.start()
    lines = text.splitlines(True)
    start_idx = text[:line_start].count('\n')
    title = (m.group(3) or "").strip()
    sep   = m.group(2) or ":"

    # 1차: 불릿 라벨 매칭
    rx_label = re.compile(r'^[ \t]*[-*+][ \t]*(.+?)[ \t]*(?:[:：]|[-–—])[ \t]*(.*)$')
    goal = dod = deliver = ""

    for i in range(start_idx+1, len(lines)):
        line = lines[i]
        if re.match(r'^\s*-\s*\[\s*[xX ]\s*\]\s*', line):
            if i == start_idx + 1:
                continue  # skip the originating checkbox line
            break
        mlabel = rx_label.match(line)
        if not mlabel: continue
        key = mlabel.group(1).strip().lower()
        val = mlabel.group(2).strip()
        if ("goal" in key) or ("목표" in key): goal = val
        elif ("dod" in key) or ("완료" in key) or ("조건" in key): dod = val
        elif ("deliverable" in key) or ("deliverables" in key) or ("산출물" in key): deliver = val

    # 2차 백업: 불릿 없어도 라벨/구분자 형태만 맞으면 캡처
    if True:
        block = []
        block.append(lines[start_idx])
        for i in range(start_idx+1, len(lines)):
            t = lines[i]
            if re.match(r'^\s*-\s*\[\s*[xX ]\s*\]\s*', t): break
            block.append(t)
        blob = "".join(block)

        # 구분자: : ／ ： / - – — | 중 아무거나
        SEP = r'(?:[:：／/|\-–—])'

        if not dod:
            md = re.search(rf'(?im)^\s*(?:[-*+]\s*)?(?:dod|do\s*?d|완료(?:\s*조건)?){SEP}\s*(.+)$', blob)
            if md: dod = md.group(1).strip()

        if not deliver:
            mdl = re.search(rf'(?im)^\s*(?:[-*+]\s*)?(?:deliverables?|산출물){SEP}\s*(.+)$', blob)
            if mdl: deliver = mdl.group(1).strip()

        if not goal:
            mg = re.search(rf'(?im)^\s*(?:[-*+]\s*)?(?:goal|목표){SEP}\s*(.+)$', blob)
            if mg: goal = mg.group(1).strip()

    if not goal: goal = title or tid
    return {"ok": True, "id": tid, "sep": sep, "title": title, "goal": goal, "dod": dod, "deliver": deliver}

t2 = load(tasks2)
t1 = load(tasks1)
res = parse(t2, tid) or parse(t1, tid)
print(json.dumps(res if res else {"ok": False, "error":"Task ID not found"}, ensure_ascii=False))
PY
)"

[ "$DEBUG" = "1" ] && echo "DEBUG RESULT_JSON: $RESULT_JSON"

echo "$RESULT_JSON" | grep -q '"ok": true' || { echo "ERROR: parse failed → $RESULT_JSON" >&2; exit 1; }

TITLE="$(python3 -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("title",""))' <<<"$RESULT_JSON")"
SEP="$(python3   -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("sep",":"))'   <<<"$RESULT_JSON")"
GOAL="$(python3  -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("goal",""))'  <<<"$RESULT_JSON")"
DOD="$(python3   -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("dod",""))'   <<<"$RESULT_JSON")"
DELIV="$(python3 -c 'import json,sys; j=json.loads(sys.stdin.read()); print(j.get("deliver",""))'<<<"$RESULT_JSON")"

[ -n "$TITLE" ] || TITLE="$TASK_ID"
[ -n "$GOAL" ]  || GOAL="$TITLE"

python3 - "$CTX" "$TASK_ID" "$SEP" "$TITLE" "$GOAL" "$DOD" "$DELIV" <<'PY'
import re, sys, pathlib
ctx, tid, sep, title, goal, dod, deliver = sys.argv[1:8]
p = pathlib.Path(ctx)
text = p.read_text(encoding="utf-8")
text = re.sub(r"## Active Task[\s\S]*?(?=\n## |\Z)", "", text, flags=re.M).rstrip() + "\n\n"
dash = "–"
line_id = f"- Task ID: {tid}{sep} {dash} {title or tid}"
block = f"""## Active Task
{line_id}
- 목표: {goal}
- DoD(완료조건): {dod or "(작성/업데이트 요망)"}""" + (f"\n- Deliverable: {deliver}" if (deliver or "").strip() else "") + """
- 승인 규칙: 완료 보고 후 승인 받고 다음 Task 이동
"""
p.write_text(text + block + "\n", encoding="utf-8")
PY

mkdir -p "$(dirname "$LOG")"
{
  echo "## $NOW"
  echo "- set-active: $PREV → $TASK_ID"
  echo "- title: $TITLE"
  echo "- goal: $GOAL"
  echo "- parsed: $(printf 'goal=\"%s\" dod=\"%s\" deliver=\"%s\"' "$GOAL" "$DOD" "$DELIV")"
  if [ -f "$TASKS2" ] && grep -q "$TASK_ID" "$TASKS2"; then
    echo "- source: tasks_v2.md"
  elif [ -f "$TASKS1" ] && grep -q "$TASK_ID" "$TASKS1"; then
    echo "- source: tasks.md"
  else
    echo "- source: <unknown>"
  fi
  echo
} >> "$LOG"

echo "Active Task set: $PREV → $TASK_ID"
chmod +x scripts/set_active_task.sh
