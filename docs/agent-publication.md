# Agent publication guide

This repository is published through the `SwampDad` GitHub account. Agents do not
have their own GitHub identity: a push uses the repository remote plus the local
Git/SSH credential selected for it.

Only publish when the owner explicitly asks. Before committing, pushing, tagging,
creating a release, or changing repository visibility, verify the working tree, remote
owner, commit author, and SSH identity. The remote must resolve to `SwampDad`, and the
SSH authentication greeting must name `SwampDad`.

Use a repository-local no-reply commit email; never put access tokens, private keys,
device codes, local paths, player data, diagnostic tooling, or private research into a
commit or release. Inspect the staged files, build/test from a fresh public checkout,
and record the runtime JAR SHA-256 before publishing a mod release.

For the complete internal procedure—including separate-account SSH setup and safe
standalone migration order—see the workbench's `docs/github-game-account-publication.md`.
