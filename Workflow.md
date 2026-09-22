### 1. Workflow and Rationale

**Chosen workflow:** Feature Branch Workflow

* With 9 team members developing different features in parallel, separate feature branches minimize conflicts.
* Keeps the `main` branch in a working state at all times.
* Enables code review on a per-PR basis.
* Simpler than Git Flow, making it suitable for a one-semester project.

### 3. Commit Rules

* Each commit should contain one minimal, functional unit of change.
* Avoid including unrelated changes in the same commit.
* Commit messages should briefly and clearly describe the changes made.

### 4. Pull Requests and Review
* All changes to the main branch should be made through Pull Requests.
* Direct pushes to main are not allowed.
* When a Pull Request is opened, 1–2 team members should review the changes and merge them after approval.
* If a merge conflict occurs, the Pull Request should not be merged until the conflict is resolved.

### 5. Merge Strategy

* Use the standard **Merge** strategy for pull requests.
* Discuss and manage issues through the **Issues** tab.
* Use **Slack** for team communication and coordination.
