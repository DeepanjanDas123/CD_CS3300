#!/usr/bin/env bash

# Exit on any error
set -e

# Ensure required directories exist
mkdir -p output
mkdir -p comp

# Loop through all .microIR files in the output/ directory
for filepath in output/*.microIR; do
    # Handle case where no .microIR files are found
    [ -e "$filepath" ] || { echo "No .microIR files found in output/"; exit 0; }

    # Extract filename without directory or extension
    filename=$(basename "$filepath" .microIR)

    echo "Running PGI on: $filename.microIR"

    # Run PGI interpreter/compiler
    java -jar pgi.jar < "output/$filename.microIR" > "comp/$filename"

    echo "→ Result saved to: comp/$filename"
done

echo "✅ All .microIR files processed successfully."
