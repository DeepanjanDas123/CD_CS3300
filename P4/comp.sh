#!/usr/bin/env bash

# Exit on any serious error
set -e

# Create necessary directories if missing
mkdir -p input
mkdir -p comp
mkdir -p MicroIRParser/comp

# Colors for nicer output (optional)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=== Running MiniIR vs MicroIR output comparison ==="

# Track mismatch count
mismatch_count=0
total_count=0

# Loop over all input miniIR files
for filepath in input/*.miniIR; do
    [ -e "$filepath" ] || { echo "No .miniIR files found in input/"; exit 0; }

    filename=$(basename "$filepath" .miniIR)
    total_count=$((total_count + 1))

    echo -e "\n→ Processing: ${YELLOW}$filename.miniIR${NC}"

    # 1️⃣ Run the MiniIR file through PGI
    java -jar pgi.jar < "input/$filename.miniIR" > "comp/$filename"

    # 2️⃣ Compare with the MicroIRParser output
    micro_output="MicroIRParser/comp/$filename"

    if [ ! -f "$micro_output" ]; then
        echo -e "${RED}✗ Missing reference output:${NC} $micro_output"
        mismatch_count=$((mismatch_count + 1))
        continue
    fi

    # 3️⃣ Diff the outputs
    if diff -q "comp/$filename" "$micro_output" > /dev/null; then
        echo -e "${GREEN}✓ Match:${NC} comp/$filename == $micro_output"
    else
        echo -e "${RED}✗ Mismatch:${NC} comp/$filename differs from $micro_output"
        echo "  Differences:"
        diff -u "comp/$filename" "$micro_output" | sed 's/^/    /'
        mismatch_count=$((mismatch_count + 1))
    fi
done

# Summary
echo -e "\n=== Comparison Summary ==="
echo "Total files checked: $total_count"
if [ "$mismatch_count" -eq 0 ]; then
    echo -e "${GREEN}All outputs match perfectly! 🎉${NC}"
else
    echo -e "${RED}$mismatch_count mismatches found.${NC}"
fi
