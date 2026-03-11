# Ralph Runbook

## Default workflow

1. Run one iteration in human mode.
2. Review code and quality gate output.
3. Re-run until current PRD batch is complete.
4. Move to continuous mode only after multiple clean iterations.

## Commands

- Human-in-loop (default):
  - `./scripts/ralph-loop.ps1`
- Continuous mode:
  - `./scripts/ralph-loop.ps1 -RunMode continuous -MaxIterations 20`
- Manual agent flow (no auto command):
  - `./scripts/ralph-loop.ps1`
- Scripted agent flow:
  - `./scripts/ralph-loop.ps1 -AgentCommand "<your-agent-cli-command>"`

## Additional Commands

- Scripted agent via named profile:
  - Copy `docs/ralph-agent-profiles.example.json` to `.ralph/agent-profiles.json`
  - `./scripts/ralph-loop.ps1 -AgentProfile copilot`

## Additional PRD batches

- `docs/ralph-prd-batch-m4-m6.json`
- `docs/ralph-prd-batch-m7-m10.json`

## Quality gates enforced

- Backend: `./mvnw.cmd verify`
- Frontend: `npm run build`
- Frontend optional: `npm run lint` and `npm run test` when scripts exist

## Branch safety

- Protected branches blocked by default: `main`, `master`
- Override only when intentional: `-AllowProtectedBranch`

## Completion criteria for one story

- Story implemented.
- Quality gates pass.
- Story marked `passes: true` in PRD.
- Progress log appended.
- One commit created.
