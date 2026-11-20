# Claude Code memory dump

Verbatim copy of `~/.claude/projects/-home-david-projects-Distance-From-Me-Android/memory/`, taken on 2026-11-20 for review. Three files.

## MEMORY.md (index)

```markdown
- [Konsist cleanup series](konsist-cleanup-series.md) — skill-driven cleanup on develop: what's committed, the approved 6-step plan, and what's deferred
- [Commit only what is asked](commit-only-what-is-asked.md) — never commit work the user did not name; leave it for their review
```

## konsist-cleanup-series.md

```markdown
---
name: konsist-cleanup-series
description: "State of the skill-guided (android/compose/kotlin skills) cleanup of this repo on develop, and the approved plan for the remaining findings"
metadata:
  node_type: memory
  type: project
  originSessionId: 48899b67-0df0-47ef-a9fb-e0767c19cb40
  modified: 2026-11-20T21:47:39.869Z
---

Since Oct 2026 the user has been cleaning up the codebase with the Android/Compose/Kotlin skills, one commit per fix on `develop`. No transcript of the session that started it was saved locally; rebuild context from `git log 34cf8d8..HEAD`.

Done and committed by 2026-11-20:
- Konsist guards (7cbfa11).
- Sealed results instead of kotlin.Result: geocoding (4ca82b1), elevation (847eba7), stored positions (7e25961, `PositionListResult`). `ClearDistancesUseCase`/`SaveDistanceUseCase` return Unit and let failures propagate; the user chose this over single-variant sealed types.
- Save and clear distances in Room transactions (2da98c9).
- Result models moved into `domain/model` packages (9f8ea8a).

Next, approved on 2026-11-20: the plan at `~/.claude/plans/let-s-elaborate-a-plan-keen-stearns.md`, 6 steps covering bugs, Android platform, Compose cleanups and the location stack. Every finding is tagged with the skill whose rule it comes from. Step 6 replaces the location Service with a Flow and fixes the `:app` Konsist layer violation, so that violation is now in scope.

Deferred by the user:
- The MainActivity ViewModel relays and the chart-visibility bug (the user is investigating).
- The build/test group: R8 rules, Compose UI tests, Room transaction test.

**Why:** in 2026-11 I mistook the Konsist failures for the to-do list and refactored `:app` before being asked. The user had me revert it, and later approved that work explicitly as plan step 6.
**How to apply:** when resuming, follow the approved plan in order and check `git status` first. Don't pick up deferred items unless the user brings them up. Related: [[commit-only-what-is-asked]].
```

## commit-only-what-is-asked.md

```markdown
---
name: commit-only-what-is-asked
description: Commit only the exact commits the user names; leave all other changes uncommitted for them to review
metadata:
  node_type: memory
  type: feedback
  originSessionId: 48899b67-0df0-47ef-a9fb-e0767c19cb40
  modified: 2026-11-20T10:21:07.978Z
---

Only create the commits the user explicitly asks for. Leave all other work in the working tree, uncommitted, for the user to review, even when the wording ("commit X before the Y changes") seems to imply Y gets committed too.

**Why:** in Nov 2026 the user asked me to commit a transaction fix "as a separate commit before the use cases changes". I committed the use-case changes too. The user said they never asked for that, and had me soft-reset it so they could examine it first.

**How to apply:** when a request names one commit, make that one only, then ask before committing anything else. The same goes for squashes and rebases: rewrite only what was named. Related: [[konsist-cleanup-series]].
```
