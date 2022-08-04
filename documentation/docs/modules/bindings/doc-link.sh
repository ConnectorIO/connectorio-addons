#!/usr/bin/env sh

# Small helper script to keep directories aligned across multiple modules locations

component="$1"

if [ -z "$component" ]; then
  echo "Usage $0 <component>"
  exit 1
fi

if [ ! -d "../../../../bundles/org.connectorio.addons.binding.$component/docs/$component" ]; then
  source=$(realpath -m ../../../../bundles/org.connectorio.addons.binding.$component/docs/$component)
  echo "Component $component not found in $source"
  exit 1
fi

ln -rs ../../../../bundles/org.connectorio.addons.binding.$component/docs/$component pages/$component

if [ -d ../../../../bundles/org.connectorio.addons.binding.$component/docs/examples ]; then
  ln -rs ../../../../bundles/org.connectorio.addons.binding.$component/docs/examples/$component examples/$component
fi
