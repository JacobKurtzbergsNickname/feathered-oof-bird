# Ralph Ticket Authoring Guide

## Objective
Write stories that can be completed in one loop iteration with machine-verifiable outcomes.

## Story size rule
A story is correctly sized if one agent iteration can implement, verify, and commit it.

Good examples:
- Add one endpoint.
- Add one value object.
- Add one migration.

Too large:
- Implement complete auth system.
- Build dashboard.

## Required fields
Each story in `.ralph/prd.json` must include:
- `id`: 3-digit string (`001`, `002`)
- `type`: `feature|bug|refactor|test`
- `category`: `setup|infrastructure|functional|testing|quality|documentation`
- `priority`: `critical|high|medium|low`
- `description`: one sentence
- `steps`: 3-10 concrete steps
- `estimated_complexity`: `small|medium|large`
- `depends_on`: array of story IDs
- `passes`: boolean
- `iterations_taken`: integer
- `blocked_reason`: string or null

## Acceptance criteria style
Use checks an agent can verify deterministically:
- Required test files (`test_files`) exist.
- Backend verify passes.
- Frontend build passes.
- Lint/test pass when scripts exist.

Avoid vague criteria:
- "works correctly"
- "looks good"

## Dependency rules
- Reference only IDs in the same PRD batch.
- Keep dependency chains shallow.
- Prefer linear dependencies for initial batches.

## Branch and commit policy
- One story per commit.
- Commit message: `feat(ralph): [ID] short description`.
- Work on feature branches, not protected branches.
