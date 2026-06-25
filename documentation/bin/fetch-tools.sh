#!/usr/bin/env bash
#
# fetch-tools.sh — download build-time tools that are not committed to the repo.
#
# Currently this fetches the PlantUML standalone jar used to render diagrams.
# Graphviz (the `dot` binary) must be installed separately via your OS package
# manager (e.g. `apt-get install graphviz`).
#
set -euo pipefail

DOC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PLANTUML_VERSION="${PLANTUML_VERSION:-1.2024.7}"
PLANTUML_JAR="${DOC_DIR}/lib/plantuml.jar"
URL="https://github.com/plantuml/plantuml/releases/download/v${PLANTUML_VERSION}/plantuml-${PLANTUML_VERSION}.jar"

mkdir -p "${DOC_DIR}/lib"
if [[ -f "${PLANTUML_JAR}" ]]; then
  echo "[tools] PlantUML jar already present: ${PLANTUML_JAR}"
  exit 0
fi
echo "[tools] Downloading PlantUML ${PLANTUML_VERSION}..."
curl -fsSL -o "${PLANTUML_JAR}" "${URL}"
echo "[tools] Saved to ${PLANTUML_JAR}"
