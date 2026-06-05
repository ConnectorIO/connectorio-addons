#!/usr/bin/env bash

set -euo pipefail

commits="${1:-HEAD~1}"
default_options="-T 1.5C"
default_modules_to_build=""

changes=$(git diff --name-only $commits | cut -d / -f 1-2)

for path in $changes; do
  if [[ ! "$path" =~ ^(bundles|features|itests|kars)/ ]]; then
    echo "${default_options}"
    exit 0
  fi

  module=$(cut -d'/' -f2 <<< "$path")

  if [[ ! "$modules_to_build" =~ "$module" ]]; then
    modules_to_build="${modules_to_build} -pl :$module"
  fi
done

[ -n "$modules_to_build" ] && echo "${default_options} $modules_to_build -am -amd"
