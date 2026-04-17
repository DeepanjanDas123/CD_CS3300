#!/usr/bin/env bash
set -euo pipefail

INPUT_DIR="input"
OUTPUT_DIR="output"
PGI_JAR="pgi.jar"   # path to your Piglet interpreter jar

shopt -s nullglob
files=("$INPUT_DIR"/*.java)

if [ ${#files[@]} -eq 0 ]; then
  echo "No .java files found in '$INPUT_DIR'."
  exit 0
fi

ok=0
fail=0

for infile in "${files[@]}"; do
  fname="$(basename -- "$infile")"
  base="${fname%.java}"
  irfile="$OUTPUT_DIR/${base}.miniIR"

  echo "=== Testing $fname ==="

  if [ ! -f "$irfile" ]; then
    echo "❌ Skipping: missing IR file '$irfile'"
    echo
    continue
  fi

  # Run original Java file
  java_out="$(mktemp)"
  java_err="$(mktemp)"
  if java "$infile" >"$java_out" 2>"$java_err"; then
    true
  else
    echo "⚠️  Java program crashed: $fname"
    cat "$java_err"
    echo
    fail=$((fail + 1))
    continue
  fi

  # Run Piglet interpreter
  ir_out="$(mktemp)"
  ir_err="$(mktemp)"
  if java -jar "$PGI_JAR" <"$irfile" >"$ir_out" 2>"$ir_err"; then
    true
  else
    echo "⚠️  Piglet interpreter crashed for $irfile"
    cat "$ir_err"
    echo
    fail=$((fail + 1))
    continue
  fi

  # Print both outputs
  echo "--- Java Output ---"
  cat "$java_out"
  echo "-------------------"
  echo "--- MiniIR Output ---"
  cat "$ir_out"
  echo "---------------------"

  # Compare outputs
  if diff -u --strip-trailing-cr "$java_out" "$ir_out" >/dev/null; then
    echo "✅ PASS: Outputs match for $fname"
    ok=$((ok + 1))
  else
    echo "❌ FAIL: Outputs differ for $fname"
    echo "--- Diff ---"
    diff -u --strip-trailing-cr "$java_out" "$ir_out" || true
    fail=$((fail + 1))
  fi

  echo
  rm -f "$java_out" "$java_err" "$ir_out" "$ir_err"
done

echo "Summary: $ok passed, $fail failed."
exit $fail
