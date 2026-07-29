#!/usr/bin/env bash
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

current="$(sed -n 's/^mod_version=//p' gradle.properties)"
if [[ ! "$current" =~ ^[0-9]+([.][0-9]+)+$ ]]; then
  echo "Unsupported mod_version: $current" >&2
  exit 1
fi

IFS=. read -r -a parts <<< "$current"
last=$((${#parts[@]} - 1))
parts[$last]=$((10#${parts[$last]} + 1))
next="$(IFS=.; echo "${parts[*]}")"

if [[ "${1:-}" == "--dry-run" && $# == 1 ]]; then
  echo "$current -> $next"
  exit 0
fi
if (( $# != 0 )); then
  echo "Usage: $0 [--dry-run]" >&2
  exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Working tree must be clean before releasing." >&2
  exit 1
fi
if git rev-parse --verify --quiet "refs/tags/v$next" >/dev/null; then
  echo "Tag v$next already exists." >&2
  exit 1
fi

sed -i "s/^mod_version=.*/mod_version=$next/" gradle.properties
bash gradlew build --no-daemon
git add gradle.properties
git commit -m "chore: release v$next"
git tag -a "v$next" -m "v$next"
git push --atomic origin HEAD "v$next"

echo "Released v$next"
