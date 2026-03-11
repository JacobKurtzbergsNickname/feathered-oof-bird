# Ralph Loop Setup (Windows-first)

## What this adds

- Scripted Ralph harness at `scripts/ralph-loop.ps1`
- Execution PRD at `.ralph/prd.json`
- Loop memory log at `.ralph/progress.txt`
- Operator runbook at `.ralph/runbook.md`

## Prerequisites

- PowerShell 5.1+
- Git in PATH
- Java + Maven wrapper support
- Node + npm

## Quick start

1. Run one safe iteration:
   - `./scripts/ralph-loop.ps1`
2. Optional continuous mode:
   - `./scripts/ralph-loop.ps1 -RunMode continuous -MaxIterations 20`
3. Optional scripted agent command:
   - `./scripts/ralph-loop.ps1 -AgentCommand "<agent-cli-command>"`
4. Optional named agent profile:
   - Copy `docs/ralph-agent-profiles.example.json` to `.ralph/agent-profiles.json`
   - `./scripts/ralph-loop.ps1 -AgentProfile copilot`

## Script flags

- `-RunMode once|continuous`
- `-MaxIterations <int>`
- `-AllowProtectedBranch`
- `-AutoCreateBranch`
- `-AgentProfile manual|copilot|claude|custom`
- `-AgentCommand <string>`
- `-AgentProfilesPath <path>`
- `-PrdPath <path>`
- `-ProgressPath <path>`

## Quality gates

Per iteration, completion requires:

1. Backend: `./mvnw.cmd verify`
2. Frontend: `npm run build`
3. Optional frontend lint/test if package scripts exist

## Safety defaults

- Protected branches are blocked by default (`main`, `master`).
- Push is not automated.
- Continuous mode is opt-in.

## Suggested operating pattern

1. Calibrate with 3-5 one-shot iterations.
2. Fix prompt and story sizing until pass rate is stable.
3. Move to short continuous runs.
4. Use overnight continuous only after stability is proven.
