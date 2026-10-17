# Agent Guidelines

## Documentation

- Update `docs/architecture-decisions.md` in the same commit as any behavior change.
- Update `docs/project-guide.md` only when the code it describes changes.
- Keep the Spanish mirrors (`docs/architecture-decisions_ES.md` and `docs/project-guide_ES.md`) in sync with their English originals.

## Commits

- Before committing, review the changes for technical debt (stale comments or docs, dead code, unused resources, leftover debugging, duplicated logic). If any was introduced, resolve it first and then commit.