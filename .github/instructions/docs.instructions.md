---
description: "Use when editing or building the Antora documentation site under documentation/: adoc pages, nav, PlantUML diagrams, build/serve scripts."
applyTo: "documentation/**"
---

# nf-bids documentation site (Antora)

## Toolchain

- The site is built with Antora (structure/versioning), AsciiDoc (content),
  GroovyDoc (API reference), and PlantUML (diagrams).
- Pages are AsciiDoc files under `documentation/modules/ROOT/pages/`. The legacy
  repo-level `docs/` directory is retired — author only in Antora sources.
- Navigation is manually maintained in `documentation/modules/ROOT/nav.adoc`.
  Adding a page REQUIRES adding an `xref:` entry to `nav.adoc` or it will not
  appear in the site menu.
- Component descriptor: `documentation/antora.yml` (component `nf-bids`; its
  `version:` tracks the plugin version in `build.gradle` and both are bumped
  together on release). Playbook: `documentation/antora-playbook.yml`.

## Building

- Full build (repo root): `make docs` or `./gradlew docs`. The `docs` task runs
  `integrateApiDocs`, which depends on `apiDocs` (GroovyDoc) and `buildDocsSite`
  (renders diagrams then builds the Antora site via `bin/build-docs.sh`, and
  copies GroovyDoc into the site under `nf-bids/<version>/api/`).
- Partial builds: `./gradlew apiDocs` (GroovyDoc only),
  `./gradlew renderDiagrams` / `make docs-diagrams` (diagrams only).
- From `documentation/`: `npm install` (first time), then `npm run build`
  (= `bin/build-docs.sh`) or `npm run diagrams`
  (= `bin/build-docs.sh --diagrams-only`). Note: `bin/build-docs.sh` run
  directly also generates and integrates GroovyDoc and applies the UI overrides
  from `documentation/ui/`.
- Output goes to `documentation/build/site/`.

## Serving

- `make docs-serve` or `documentation/bin/serve-docs.sh` (also `npm run serve`
  from `documentation/`). Default port 5050; if busy it auto-picks the next free
  port (scans up to +20). Force a port with `DOCS_PORT=<port>`. If the site is
  not built yet, the serve script builds it first.

## Prerequisites

- Java 17+ (GroovyDoc, PlantUML jar), Node.js 18+ and npm (Antora), and
  Graphviz `dot` on PATH (needed by PlantUML for class/package diagrams).
- The PlantUML jar is downloaded automatically by
  `documentation/bin/fetch-tools.sh` into `documentation/lib/` (not committed).
  For the pinned PlantUML version, see `fetch-tools.sh` (overridable via
  `PLANTUML_VERSION`).

## Gotchas

- `DOCS_STRICT=1` (CI mode) makes missing diagram tooling (PlantUML jar,
  Graphviz) a hard failure instead of a warning, and also promotes Antora
  warnings (broken xrefs, missing images, unresolved includes) to build
  failures.
- Never edit `documentation/build/` — it is generated output.
- Diagram sources live in `documentation/diagrams/*.puml` and render to SVG in
  `documentation/modules/ROOT/images/generated/`.
- Antora reads content through git. In a linked git worktree (`.git` is a
  file), `bin/build-docs.sh` transparently snapshots the component into a
  temporary git repo and builds from there — no action needed.
- CI can inject the published site URL via `DOCS_SITE_URL`.

## Content ownership

- `documentation/` is the AUTHORITATIVE knowledge source for this repo. When
  code behavior changes, update the relevant `.adoc` page under
  `modules/ROOT/pages/` in the same PR.
