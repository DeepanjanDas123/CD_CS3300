#!/usr/bin/env bash

# Exit on any error
#set -e

# Create output directory if it doesn't exist
mkdir -p MiniRAParser/output

# Loop through all .miniIR files in input/
for filepath in input/*.microIR; do
    # Skip if no files match (avoids literal 'input/*.miniIR' on empty dir)
    [ -e "$filepath" ] || { echo "No .microIR files found in input/"; exit 0; }

    # Extract filename without directory or extension
    filename=$(basename "$filepath" .microIR)

    echo "Processing: $filename.microIR"

    # Run the Java translator
    java P5 < "input/$filename.microIR" > "MiniRAParser/output/$filename.miniRA"

    echo "Generated: MiniRAParser/output/$filename.miniRA"
done

echo "✅ All files processed successfully."
