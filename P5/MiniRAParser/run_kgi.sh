#!/usr/bin/env bash

# Exit on any error
set -e

# Ensure required directories exist
mkdir -p output
mkdir -p comp

# Loop through all .microIR files in the output/ directory
for filepath in output/*.miniRA; do
    # Handle case where no .microIR files are found
    [ -e "$filepath" ] || { echo "No .miniRA files found in output/"; exit 0; }

    # Extract filename without directory or extension
    filename=$(basename "$filepath" .miniRA)

    echo "Running KGI on: $filename.miniRA"

    # Run PGI interpreter/compiler
    java -jar kgi.jar < "output/$filename.miniRA" > "comp/$filename"

    echo "→ Result saved to: comp/$filename"
done

echo "✅ All .miniRA files processed successfully."
