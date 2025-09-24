#!/usr/bin/env bash
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
OUT="$ROOT/bootstrap/prompt_bundle.md"
TREE="$ROOT/bootstrap/tree.txt"

git -C "$ROOT" status --porcelain=v1 > "$ROOT/bootstrap/git_status.txt" || true
( cd "$ROOT" && command -v tree >/dev/null && tree -a -L 2 || find . -maxdepth 2 -print ) > "$TREE"

cat > "$OUT" <<'MD'
# === BOOTSTRAP BUNDLE START ===
아래 파일들을 읽고 Active Task 하나만 수행.
MD

for f in \
  "$ROOT/bootstrap/context.md" \
  "$ROOT/deliverables/prd.md" \
  "$ROOT/deliverables/tasks.md" \
  "$ROOT/rules/1_prd_rules.md" \
  "$ROOT/rules/2_task_gen_rules.md" \
  "$ROOT/rules/3_task_exec_rules.md" \
  "$ROOT/bootstrap/git_status.txt" \
  "$TREE"
do
  [ -f "$f" ] || continue
  echo -e "\n\n---\n# FILE: ${f#$ROOT/}\n" >> "$OUT"
  cat "$f" >> "$OUT"
done

echo -e "\n# === BOOTSTRAP BUNDLE END ===\n" >> "$OUT"
echo "Generated: $OUT"