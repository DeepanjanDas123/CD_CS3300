#!/usr/bin/env bash

# Exit on any error
set -e

# Create output directory if it doesn't exist
mkdir -p MicroParser/output

# Loop through all .miniIR files in input/
for filepath in input/*.miniIR; do
    # Skip if no files match (avoids literal 'input/*.miniIR' on empty dir)
    [ -e "$filepath" ] || { echo "No .miniIR files found in input/"; exit 0; }

    # Extract filename without directory or extension
    filename=$(basename "$filepath" .miniIR)

    echo "Processing: $filename.miniIR"

    # Run the Java translator
    java P4 < "input/$filename.miniIR" > "MicroIRParser/output/$filename.microIR"

    echo "Generated: MicroParser/output/$filename.microIR"
done

echo "✅ All files processed successfully."
