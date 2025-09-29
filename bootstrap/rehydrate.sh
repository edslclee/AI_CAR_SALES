#!/usr/bin/env bash
set -euo pipefail

# Rebuild prompt bundle (context + PRD + tasks + state)
# Priority: v2 files first (prd_v2.md, tasks_v2.md)

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
OUT="$ROOT/bootstrap/prompt_bundle.md"
CTX="$ROOT/bootstrap/context.md"

# Prefer v2, fallback to v1
PRD="$ROOT/deliverables/prd_v2.md"
[ -f "$PRD" ] || PRD="$ROOT/deliverables/prd.md"

TASKS="$ROOT/deliverables/tasks_v2.md"
[ -f "$TASKS" ] || TASKS="$ROOT/deliverables/tasks.md"

STATELOG="$ROOT/docs/history/STATELOG.md"

TS="$(date '+%Y-%m-%d %H:%M %Z')"

# Safety checks (context + at least one of PRD/TASKS)
[ -f "$CTX" ] || { echo "ERROR: $CTX not found"; exit 1; }
[ -f "$PRD" ] || { echo "ERROR: PRD not found (expected prd_v2.md or prd.md)"; exit 1; }
[ -f "$TASKS" ] || { echo "ERROR: tasks not found (expected tasks_v2.md or tasks.md)"; exit 1; }

mkdir -p "$(dirname "$OUT")"

cat > "$OUT" <<EOF
# Prompt Bundle (rehydrated)
- Generated: $TS
- Workspace: $(basename "$ROOT")

---

## 1) Active Context (bootstrap/context.md)
\`\`\`md
$(sed -e 's/\r$//' "$CTX")
\`\`\`

---

## 2) Product Requirements (PRD) — $(basename "$PRD")
\`\`\`md
$(sed -e 's/\r$//' "$PRD")
\`\`\`

---

## 3) Tasks Checklist — $(basename "$TASKS")
\`\`\`md
$(sed -e 's/\r$//' "$TASKS")
\`\`\`

EOF

if [ -f "$STATELOG" ]; then
  cat >> "$OUT" <<EOF

---

## 4) State Log (tail)
\`\`\`md
$(tail -n 50 "$STATELOG" 2>/dev/null || true)
\`\`\`
EOF
fi

# Copy to clipboard if available (macOS pbcopy / xclip / wl-copy)
if command -v pbcopy >/dev/null 2>&1; then
  pbcopy < "$OUT" || true
  echo "Wrote and copied: $OUT"
elif command -v xclip >/dev/null 2>&1; then
  xclip -selection clipboard < "$OUT" || true
  echo "Wrote and xclip-copied: $OUT"
elif command -v wl-copy >/dev/null 2>&1; then
  wl-copy < "$OUT" || true
  echo "Wrote and wl-copied: $OUT"
else
  echo "Wrote bundle: $OUT (no clipboard tool found)"
fi

chmod +x bootstrap/rehydrate.sh