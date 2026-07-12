#!/usr/bin/env python3
"""
create_issues.py — turn per-screen task files (docs/tasks/*.md) into GitHub issues.

This is the `gh` fallback path for the mocks-to-issues skill. If a GitHub MCP is
connected, the agent can create issues directly with the same file format instead
of running this. See SKILL.md.

For each screen file it creates:
  * one tracking ("epic") issue, and
  * one child issue per `## Task:` block,
then edits each child to add "Part of #<tracking>" and resolved "Depends on #<n>"
lines, and fills the tracking issue's checklist with links to its children.

Task-file format (see SKILL.md for the authoring contract):

    ---
    screen: Home
    slug: home
    ... (front matter key: value pairs) ...
    ---

    # Tracking: <epic title>
    <intro paragraph(s) — becomes the tracking issue body>

    ## Task: <title>
    labels: layer:viewmodel, screen:home
    depends-on: <another task title in this file>, or "none"

    <body markdown>

    ### Acceptance criteria
    - [ ] ...

Usage:
    create_issues.py docs/tasks/*.md --repo owner/repo [--dry-run]
    create_issues.py docs/tasks --repo owner/repo          # a directory = all *.md

Exit codes: 0 ok, 1 usage/parse error, 2 a gh command failed.
"""
import argparse
import glob
import os
import re
import subprocess
import sys

# Label -> (hex color, description). Prefix-matched; layer:* share a color scheme.
LABEL_META = {
    "type:tracking":     ("5319e7", "Epic / tracking issue for a screen"),
    "layer:domain":      ("0e8a16", "Domain model (data class)"),
    "layer:data":        ("1d76db", "Data layer (repository / preferences)"),
    "layer:viewmodel":   ("fbca04", "ViewModel (state)"),
    "layer:view":        ("d93f0b", "Stateless Compose View"),
    "layer:screen":      ("e99695", "Screen wiring"),
    "layer:navigation":  ("c5def5", "Navigation route + graph"),
    "layer:di":          ("bfd4f2", "Koin DI registration"),
    "layer:strings":     ("fef2c0", "String resources"),
    "layer:test":        ("0052cc", "Tests"),
}
DEFAULT_COLOR = "ededed"

DRY_RUN_BASE = 1000  # placeholder issue numbers assigned in --dry-run


class Task:
    def __init__(self, title):
        self.title = title
        self.labels = []
        self.depends_on = []
        self.body = ""
        self.number = None
        self.url = None


class Screen:
    def __init__(self):
        self.meta = {}
        self.tracking_title = ""
        self.tracking_intro = ""
        self.tasks = []
        self.number = None
        self.url = None


def parse_file(path):
    text = open(path, encoding="utf-8").read()
    s = Screen()

    # front matter
    fm = re.match(r"^---\n(.*?)\n---\n", text, re.DOTALL)
    if fm:
        for line in fm.group(1).splitlines():
            if ":" in line:
                k, v = line.split(":", 1)
                s.meta[k.strip()] = v.strip()
        text = text[fm.end():]

    lines = text.splitlines()
    i = 0
    # tracking H1
    while i < len(lines) and not lines[i].startswith("# "):
        i += 1
    if i >= len(lines):
        raise ValueError(f"{path}: no '# Tracking: ...' heading found")
    s.tracking_title = lines[i][2:].strip()
    i += 1

    # intro = everything up to first '## Task:'
    intro = []
    while i < len(lines) and not lines[i].startswith("## Task:"):
        intro.append(lines[i])
        i += 1
    s.tracking_intro = "\n".join(intro).strip()

    # tasks
    cur = None
    section = "body"  # body | accept
    body_lines, accept_lines = [], []

    def flush():
        if cur is None:
            return
        b = "\n".join(body_lines).strip()
        acc = [l for l in accept_lines if l.strip()]
        if acc:
            b += "\n\n### Acceptance criteria\n" + "\n".join(acc)
        cur.body = b.strip()
        s.tasks.append(cur)

    while i < len(lines):
        line = lines[i]
        if line.startswith("## Task:"):
            flush()
            cur = Task(line[len("## Task:"):].strip())
            body_lines, accept_lines = [], []
            section = "meta"
        elif cur is not None and section == "meta" and line.startswith("labels:"):
            cur.labels = [x.strip() for x in line[len("labels:"):].split(",") if x.strip()]
        elif cur is not None and section == "meta" and line.startswith("depends-on:"):
            raw = line[len("depends-on:"):].strip()
            if raw.lower() not in ("", "none", "(none)"):
                cur.depends_on = [x.strip() for x in raw.split(",") if x.strip()]
            section = "body"
        elif cur is not None and line.strip().startswith("### Acceptance criteria"):
            section = "accept"
        elif cur is not None:
            (accept_lines if section == "accept" else body_lines).append(line)
        i += 1
    flush()

    if not s.tasks:
        raise ValueError(f"{path}: no '## Task:' blocks found")
    return s


def run_gh(args, dry_run, repo):
    cmd = ["gh"] + args
    if repo:
        cmd += ["-R", repo]
    if dry_run:
        print("   $ " + " ".join(_q(c) for c in cmd))
        return ""
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        sys.stderr.write("gh failed: " + " ".join(cmd) + "\n" + res.stderr + "\n")
        sys.exit(2)
    return res.stdout.strip()


def _q(s):
    return f'"{s}"' if (" " in s or "\n" in s) else s


def num_from_url(url):
    m = re.search(r"/issues/(\d+)", url or "")
    return int(m.group(1)) if m else None


def ensure_labels(names, dry_run, repo):
    for name in sorted(set(names)):
        color, desc = LABEL_META.get(name, (DEFAULT_COLOR, ""))
        run_gh(["label", "create", name, "--color", color, "--description", desc,
                "--force"], dry_run, repo)


def create_issue(title, body, labels, dry_run, repo, counter):
    args = ["issue", "create", "--title", title, "--body", body]
    for l in labels:
        args += ["--label", l]
    out = run_gh(args, dry_run, repo)
    if dry_run:
        n = DRY_RUN_BASE + counter[0]
        counter[0] += 1
        return n, f"https://github.com/{repo or 'OWNER/REPO'}/issues/{n}"
    return num_from_url(out), out


def process(screen, dry_run, repo, counter):
    print(f"\n=== {screen.meta.get('screen', screen.tracking_title)} "
          f"({len(screen.tasks)} tasks) ===")

    all_labels = {l for t in screen.tasks for l in t.labels}
    tracking_labels = ["type:tracking"]
    slug = screen.meta.get("slug")
    if slug:
        tracking_labels.append(f"screen:{slug}")
    ensure_labels(all_labels | set(tracking_labels), dry_run, repo)

    # 1) children first (listed in build order, so deps precede dependents)
    by_title = {}
    for t in screen.tasks:
        title = f"{screen.meta.get('screen', '')}: {t.title}".strip(": ")
        t.number, t.url = create_issue(title, t.body, t.labels, dry_run, repo, counter)
        by_title[t.title] = t
        print(f"   -> #{t.number}  {title}")

    # 2) tracking issue with a checklist linking children
    checklist = "\n".join(f"- [ ] #{t.number} — {t.title}" for t in screen.tasks)
    body = screen.tracking_intro + "\n\n### Tasks\n" + checklist
    screen.number, screen.url = create_issue(
        screen.tracking_title, body, tracking_labels, dry_run, repo, counter)
    print(f"   -> #{screen.number}  [tracking] {screen.tracking_title}")

    # 3) edit children: Part of #tracking + resolved Depends on #n
    for t in screen.tasks:
        footer = [f"Part of #{screen.number}"]
        deps = []
        for d in t.depends_on:
            dep = by_title.get(d)
            if dep:
                deps.append(f"#{dep.number}")
            else:
                deps.append(f"`{d}` (other file)")
        if deps:
            footer.append("Depends on " + ", ".join(deps))
        new_body = t.body + "\n\n---\n" + "  ·  ".join(footer)
        run_gh(["issue", "edit", str(t.number), "--body", new_body], dry_run, repo)


REPO_RE = re.compile(r"^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+$")


def resolve_repo(repo, dry_run):
    """Determine the target owner/repo. Prompt if not supplied (this folder is not a
    git repo, so gh cannot infer one). In a non-interactive dry-run, fall back to a
    visible placeholder so the preview still renders."""
    if repo:
        if not REPO_RE.match(repo):
            sys.stderr.write(f"--repo must look like owner/repo, got: {repo}\n")
            sys.exit(1)
        return repo
    if sys.stdin.isatty():
        while True:
            repo = input("Which GitHub repo should the issues go under? (owner/repo): ").strip()
            if REPO_RE.match(repo):
                return repo
            print("  please enter as owner/repo, e.g. jian/forkune")
    if dry_run:
        print("(no --repo given; using placeholder OWNER/REPO for this dry run)")
        return "OWNER/REPO"
    sys.stderr.write("no --repo given and stdin is not a TTY to prompt; pass --repo owner/repo\n")
    sys.exit(1)


def collect_paths(inputs):
    paths = []
    for inp in inputs:
        if os.path.isdir(inp):
            paths += sorted(glob.glob(os.path.join(inp, "*.md")))
        else:
            paths += sorted(glob.glob(inp)) or [inp]
    return [p for p in paths if os.path.isfile(p)]


def main():
    ap = argparse.ArgumentParser(description="Create GitHub issues from per-screen task files.")
    ap.add_argument("inputs", nargs="+", help="task .md files or a directory")
    ap.add_argument("--repo", help="owner/repo (passed to gh -R); prompted if omitted")
    ap.add_argument("--dry-run", action="store_true", help="print gh commands, create nothing")
    args = ap.parse_args()

    paths = collect_paths(args.inputs)
    if not paths:
        sys.stderr.write("no task files matched\n")
        sys.exit(1)

    if args.dry_run:
        print("DRY RUN — no issues will be created. Numbers below are placeholders.")
    repo = resolve_repo(args.repo, args.dry_run)

    screens = [parse_file(p) for p in paths]
    counter = [1]
    for s in screens:
        process(s, args.dry_run, repo, counter)

    total = sum(len(s.tasks) for s in screens) + len(screens)
    print(f"\nDone. {len(screens)} screens · {total} issues "
          f"({'planned' if args.dry_run else 'created'}).")


if __name__ == "__main__":
    main()
