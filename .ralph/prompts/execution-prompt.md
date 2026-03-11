# Ralph Wiggum

You are running one Ralph iteration.

Rules:

1. Read .ralph/prd.json and pick exactly one highest-priority unblocked feature where passes=false.
2. Implement only that feature.
3. Keep changes minimal and production-safe.
4. Run required quality gates.
5. If gates fail, do not mark complete. Capture failure context in .ralph/progress.txt.
6. If gates pass, update feature passes=true and increment iterations_taken.
7. Commit exactly one story with message format: feat(ralph): [ID] short description.
8. Stop after one story unless explicitly asked to continue.
