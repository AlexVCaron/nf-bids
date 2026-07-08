---
description: "Guided release preparation checklist for nf-bids: version bump, CHANGELOG, full test gate, and confirmation gates before any publish action."
agent: "agent"
tools: [execute, read, edit]
---

Guide the user through a nf-bids release. **Never push, tag, or publish without explicit confirmation at each gate.**

See [documentation/modules/ROOT/pages/guides/release.adoc](../documentation/modules/ROOT/pages/guides/release.adoc) for the authoritative process.

## Checklist

1. **Ask for the new version number** before touching any files.

2. **Run the full test suite** — stop if any step fails:
   - `make test`
   - `make install`
   - `cd test/validation && bash test_datasets.sh`

3. **Version bump** — TWO files must be updated atomically (same commit):
   - Show current `version = 'X.Y.Z'` in `build.gradle` and proposed change.
   - Show current `version`, `display_version`, and `plugin-version` in `documentation/antora.yml` (also check `nextflow-min-version` and `libbids-sh-version` if changed).
   - **Wait for user confirmation**, then edit both files.

4. **CHANGELOG update** — show the current top section of `CHANGELOG.md`. Ask the user to provide the new entry. Edit the file after confirmation.

5. **Smoke test** — `cd test/validation && nextflow run main.nf`

6. **Docs gate** — `./gradlew docs` must succeed clean. Verify: new version visible in header, all xrefs resolve, all diagrams render. Fix any failures before proceeding.

7. **Commit** — stage and show the diff. Suggest a commit message (≤50 char summary, blank line, bullet details). Wait for user to approve or adjust.

8. **⛔ GATE — confirm before continuing** — ask: "Ready to tag, push, and publish? (yes/no)"
   - If no: stop here.
   - If yes: proceed.

9. **Tag and push** — `git tag -a v<version> -m "Release v<version>"` and `git push origin main --follow-tags`

10. **Publish** — `make release` (runs `./gradlew releasePlugin`)

11. **Report** — summarize what was done; remind the user to push the commit and tag if not already done.
