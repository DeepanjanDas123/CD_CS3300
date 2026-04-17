#!/usr/bin/env bash

# Exit on any error
#set -e

# Create output directory if it doesn't exist
mkdir -p MIPSParser/output

# Loop through all .miniIR files in input/
for filepath in input/*.miniRA; do
    # Skip if no files match (avoids literal 'input/*.miniIR' on empty dir)
    [ -e "$filepath" ] || { echo "No .miniRA files found in input/"; exit 0; }

    # Extract filename without directory or extension
    filename=$(basename "$filepath" .miniRA)

    echo "Processing: $filename.miniRA"

    # Run the Java translator
    java P6 < "input/$filename.miniRA" > "MIPSParser/output/$filename.s"

    echo "Generated: MIPSParser/output/$filename.s"
done

echo "✅ All files processed successfully."
