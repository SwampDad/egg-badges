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

## Stable download links

The permanent player-facing releases page is
`https://github.com/SwampDad/<repository>/releases`.

For a stable release, include a byte-identical copy of the runtime JAR named exactly
`<repository>.jar` alongside the versioned JAR. This makes the permanent direct link
work across versions:

```text
https://github.com/SwampDad/<repository>/releases/latest/download/<repository>.jar
```

Publish test builds as prereleases. GitHub excludes prereleases from
`/releases/latest`, so never advertise that URL until a normal stable release exists.
Verify the alias and versioned JAR have the same SHA-256 before sharing the link.
