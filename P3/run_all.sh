#!/usr/bin/env bash
set -u
# run_all.sh
# For each .java file in input/, run:
#   java P3.java < input/<name>.java > output/<name>.miniIR 2> output/<name>.txt

INPUT_DIR="input"
OUTPUT_DIR="output"

mkdir -p "$INPUT_DIR"
mkdir -p "$OUTPUT_DIR"

shopt -s nullglob

files=("$INPUT_DIR"/*.java)
if [ ${#files[@]} -eq 0 ]; then
  echo "No .java files found in '$INPUT_DIR'." >&2
  exit 0
fi

for infile in "${files[@]}"; do
  fname=$(basename -- "$infile")
  base="${fname%.java}"
  out_mini="$OUTPUT_DIR/${base}.miniIR"
  out_err="$OUTPUT_DIR/${base}.txt"

  echo "Running: java P3.java < \"$infile\"  > \"$out_mini\" 2> \"$out_err\"" >&2

  # Run the exact command requested: capture stdout (MiniIR) and stderr (diagnostics).
  if java P3.java < "$infile" > "$out_mini" 2> "$out_err"; then
    echo "OK:   $fname -> $out_mini (stderr -> $out_err)" >&2
  else
    rc=$?
    echo "FAIL: $fname (exit $rc). MiniIR -> $out_mini ; stderr -> $out_err" >&2
  fi
done

echo "Done. MiniIR files: $OUTPUT_DIR/*.miniIR ; stderr logs: $OUTPUT_DIR/*.txt" >&2
