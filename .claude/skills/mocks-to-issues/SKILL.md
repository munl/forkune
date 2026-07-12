---
name: mocks-to-issues
description: >-
  Turn UI mocks (a PDF of screen mockups in a docs directory) into a set of small,
  workable GitHub issues. Analyzes each screen, breaks it into per-layer tasks along
  the KMP + Compose clean-architecture convention (domain / data / ViewModel / View /
  Screen / navigation / DI / strings / tests), writes one review file per screen to
  docs/tasks/, and — after you approve — creates plain repo issues (a tracking epic
  per screen plus child layer-tasks, with per-layer labels, dependency links, and
  acceptance criteria). Use when asked to break down mocks/mockups/designs into
  tasks/issues/tickets, decompose a design PDF, plan a feature from a mockup, or
  file issues from a mock.
---

# Mocks → GitHub issues

Two phases, with a review gate in between:

1. **Analyze** a mock PDF → write **one task file per screen** to `docs/tasks/<slug>.md`.
2. **You review/edit** those files → then **create the issues** (MCP-first, `gh` fallback).

The decomposition mirrors the repo's **`clean-architecture`** skill — read it first;
its layers and build order are the task taxonomy this skill files against. Paths below
are relative to the project root (where `docs/` and `.claude/` live).

## Prerequisites

```bash
brew install poppler          # pdfinfo + pdftoppm, to read the PDF as images
```

- `python3` (stdlib only) — runs the create harness.
- `gh` authenticated **for the create step**: `gh auth login`. Analyze + dry-run need no auth.
- Or a GitHub MCP connected (preferred if available — see Phase 2).

## Phase 1 — Analyze the mock → per-screen files

The mock is a PDF in `docs/` (here: `docs/foodpicker app.pdf`). It's often a single
large page with several phone frames on it. Rasterize it, then **actually look** at
the image:

```bash
pdfinfo "docs/foodpicker app.pdf" | grep -iE 'pages|page size'
pdftoppm -png -r 150 "docs/foodpicker app.pdf" /tmp/mock
```

Read the resulting `/tmp/mock-*.png` with the Read tool (it renders images).

To pull a **single screen's frame** off the A2 sheet (e.g. for embedding in an issue —
see *Attach the screen's mock* below), crop at render time with `pdftoppm`'s region
flags rather than a separate image tool (no PIL/ImageMagick is assumed present; macOS
`sips` only center-crops). Read the whole page once to eyeball the frame's pixel box at
your chosen `-r`, then re-render just that box:

```bash
# -x/-y = top-left corner, -W/-H = width/height, all in pixels at the given -r
pdftoppm -png -r 300 -x 492 -y 546 -W 1286 -H 2746 "docs/foodpicker app.pdf" /tmp/home-crop
```

For each distinct screen in the mock:

- Name it and slugify it (`Home` → `home`).
- Run the `clean-architecture` **Decision Checklist**: does it persist data (Room vs.
  preferences vs. none)? new domain model? use-case? navigable? — this decides which
  layer-tasks exist.
- Emit one task per applicable layer, in build order: domain → data → ViewModel →
  strings → View → Screen → navigation → DI → tests. Shared models (e.g. a
  `Restaurant` reused across screens) live in the screen that first needs them; note
  the cross-screen reuse in prose.

Write each screen to `docs/tasks/<slug>.md` in the format the harness parses (see
**Task-file format** below). The four files in `docs/tasks/` were generated from
`docs/foodpicker app.pdf` and are the worked example — copy their shape.

## Phase 2 — Create the issues

**Review gate:** stop and let the user edit `docs/tasks/*.md` before anything is
created. Nothing touches GitHub until they approve.

**Confirm which repo the issues go under.** Never create against an assumed repo. If the
project has a git remote (`git remote -v`), treat that `owner/repo` as the default and
**confirm it** with the user (e.g. via AskUserQuestion) before creating anything; if there
is no remote, prompt for `owner/repo` outright. Pass the confirmed value as `--repo` (or as
the MCP owner/repo args). The harness also prompts for it if you run it without `--repo`.

**Preferred — GitHub MCP** (if one is connected): create issues directly with the MCP's
create-issue tool, consuming the same files: create the child issues, then a tracking
issue whose body lists them, then edit children to add `Part of #<tracking>` and
`Depends on #<n>`. Apply the same labels the harness uses.

**Fallback — `gh` via the harness.** Always dry-run first (no auth needed):

```bash
python3 .claude/skills/mocks-to-issues/create_issues.py docs/tasks --repo OWNER/REPO --dry-run
```

That prints every `gh` command it would run. When it looks right and `gh auth login`
is done, drop `--dry-run` to create for real:

```bash
python3 .claude/skills/mocks-to-issues/create_issues.py docs/tasks --repo OWNER/REPO
```

The harness (`create_issues.py`, in this skill dir) per screen: ensures the labels
exist (idempotent `--force`), creates each `## Task:` as a child issue, creates the
tracking epic with a checklist linking the children, then edits children to add
`Part of #<tracking>` + resolved `Depends on #<n>`. In-file dependencies resolve to
issue numbers; cross-file deps are noted as `(other file)`.

`--repo` is a plain `owner/repo` string. If you omit `--repo`, the harness **prompts**
for it (and rejects anything not shaped like `owner/repo`). Point it at file(s) or a
directory: `docs/tasks`, `docs/tasks/*.md`, or a single file.

## (Optional) Attach the screen's mock to its issue

Not part of the default flow — do this only when asked to add the screenshot/mock to an
issue. Crop the screen's frame (see the `pdftoppm` region-crop in Phase 1), commit it to
`docs/tasks/assets/<slug>.png`, and push. Then reference it from the tracking issue body.

**How it renders depends on repo visibility — this is the gotcha:**

- **Public repo:** an inline image works. GitHub proxies issue images through its
  anonymous *camo* proxy, which can fetch a public raw URL, so
  `<img src="https://raw.githubusercontent.com/OWNER/REPO/BRANCH/docs/tasks/assets/<slug>.png" width="320">`
  renders inline.
- **Private repo:** an inline `<img>`/markdown image **will NOT render** — camo fetches
  with no credentials and 404s on private raw content (verify with an unauthenticated
  `curl -sIL <raw-url>` → 404). The only inline-rendering path for a private repo is a
  true issue *attachment* (`github.com/user-attachments/assets/…`), and that upload endpoint
  is web-UI/GraphQL only — **not exposed by the REST API or the GitHub MCP**, so neither the
  MCP nor `gh` can perform it. Either the user drag-drops the PNG into the issue editor
  themselves, or fall back to a **markdown link** to the committed file
  (`[Home mock](https://github.com/OWNER/REPO/blob/BRANCH/docs/tasks/assets/<slug>.png)`),
  which opens fine in the viewer's authenticated session.

Don't suggest flipping the repo public→private to sneak the render in: it re-breaks on
re-privatize (any surviving image is an unreliable camo-cache accident), and a
briefly-public repo is exposed to crawlers/forks in that window.

## Task-file format

Front matter + one `# Tracking:` epic + N `## Task:` blocks. This is the contract the
harness parses and what a future analyze pass should emit:

```markdown
---
screen: Home
slug: home
purpose: one line
source: docs/foodpicker app.pdf — screen 1 (Home)
navigable: yes
persistence: preferences
domain_model: none new
use_case: no
---

# Tracking: Home screen

Intro paragraph(s) — becomes the tracking issue body, above the auto-generated checklist.

## Task: HomeViewModel
labels: layer:viewmodel, screen:home
depends-on: Preferences — last pick & location

Body markdown describing what to build (reference clean-architecture sections).

### Acceptance criteria
- [ ] ...
- [ ] ...
```

Rules the parser relies on: `## Task: <title>` starts a task; the `labels:` and
`depends-on:` lines must come immediately after it (use `none` if no deps);
`depends-on` values are **other task titles in the same file**; `### Acceptance
criteria` ends the body. Labels: one `layer:*` per task (`layer:domain|data|viewmodel|
view|screen|navigation|di|strings|test`) plus `screen:<slug>`; tracking issues get
`type:tracking` + `screen:<slug>`.

## Gotchas

- **The PDF is one A2 page, not one-page-per-screen.** `pdfinfo` reports `Pages: 1`;
  all four phone frames are laid out on that single canvas. Render the whole page and
  read the screens off it — don't expect a page per screen.
- **Filename has a space** (`foodpicker app.pdf`) — always quote it in shell commands.
- **Em dashes / backticks in task titles & bodies** flow into `gh issue create --title/
  --body` fine because the harness passes them as argv (never through a shell). If you
  hand-run `gh`, quote carefully.
- **Cross-screen shared models** (`Restaurant`, `PlacesRepository`) can't be auto-linked
  by issue number because they live in another file; the harness marks them
  `Depends on ... (other file)`. Land shared-foundation tasks first. When creating via MCP
  you *can* cross-link them by number (create the shared-foundation screen first, then
  reference `#<n>` in later epics) — as done here (`shuffling.md`'s `Restaurant`/
  `PlacesRepository` linked from the Home & Your-pick epics).
- **MCP `issue_write` strips HTML comments and tag-like tokens from issue bodies.** A
  literal `<!-- Home -->` or `<HomeViewModel>` — even inside backticks — is silently
  removed (an *invalid* tag like `composable<...>` survives, just HTML-escaped). Escape
  intended-literal angle brackets as `&lt;`/`&gt;` in the body you send, e.g.
  `` `&lt;!-- Home --&gt;` `` and `` `koinViewModel&lt;HomeViewModel&gt;()` `` — they
  render correctly and aren't stripped. The Strings tasks (`<!-- Screen -->` section
  markers) are the usual victims. The `gh`/harness path passes bodies as argv and is
  unaffected.
- **`pdftoppm` prints `Syntax Warning: Bad bounding box in Type 3 glyph`** — harmless
  (a font quirk in the export); the PNG renders fine.

## Troubleshooting

- `pdftoppm: command not found` / Read says "pdftoppm is not installed" → `brew install poppler`.
- `gh: authentication failed` / "token ... no longer valid" → `gh auth login`. Until
  then use `--dry-run`, which needs no auth.
- Harness prints `no task files matched` → check the path; it globs `*.md` in a dir, or
  takes explicit files.
- `gh failed: ...` mid-run (exit 2) → a label or issue create failed (bad `--repo`, no
  push access). The harness stops; fix and re-run — label creation is idempotent, but
  already-created issues from that run will duplicate, so prefer getting a clean
  `--dry-run` first.
