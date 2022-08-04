#!/usr/bin/env sh

# Small helper script to keep directories aligned across multiple modules locations

component="$1"

if [ -z "$component" ]; then
  echo "Usage $0 <component>"
  exit 1
fi

if [ -L "pages/$component" ]; then
  echo "Unlinking component $component"
  unlink pages/$component
fi

if [ -L "examples/$component" ]; then
  echo "Unlink $component examples"
  unlink examples/$component
fi
