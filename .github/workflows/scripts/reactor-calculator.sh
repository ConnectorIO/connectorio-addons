#!/usr/bin/env bash

set -euo pipefail

baseBranch="${1}"
[ -z "$baseBranch" ] && baseBranch="HEAD"

referenceArg="${2}"
[ -z "$referenceArg" ] && referenceArg="master"
referenceBranch="refs/remotes/origin/${referenceArg}"

default_options="-Daether.connector.http.connectionMaxTtl=120 -Daether.connector.requestTimeout=300000 -Daether.dependencyCollector.impl=bf -Dmaven.artifact.threads=25 -Dsurefire.rerunFailingTestsCount=2 -Dfailsafe.rerunFailingTestsCount=2"
modules_to_build=""

# If the reference branch is not available locally (e.g. workflow_dispatch without fetch), fall back to full build.
if ! git rev-parse --verify "${referenceBranch}" >/dev/null 2>&1; then
  echo "${default_options}"
  exit 0
fi

changes=$(git diff --name-only "${referenceBranch}" HEAD | cut -d / -f 1-2)

for path in $changes; do
  if [[ ! "$path" =~ ^(bundles|features|itests|kars)/ ]]; then
    echo "${default_options}"
    exit 0
  fi

  module=$(cut -d'/' -f2 <<< "$path")

  if [[ ! "$modules_to_build" =~ "$module" ]]; then
    modules_to_build="${modules_to_build} $module"
  fi
done

[ -n "$modules_to_build" ] && echo "-Dgib.disable=false -Dgib.referenceBranch=HEAD~1 -Dgib.baseBranch=${baseBranch} -Dgib.uncommitted=false -Dgib.untracked=false -Dgib.buildDownstream=true -Dgib.buildUpstream=true -Dgib.buildUpstreamMode=impacted"
