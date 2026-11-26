#!/usr/bin/env python3
"""Generate GitHub Release notes from CHANGELOG.md with a git-commit fallback.

Usage:
  python3 .github/scripts/build_release_notes.py \
      --tag v1.1.2 \
      --repo owner/repo \
      --assets-dir release-assets \
      --output release-notes.md
"""

from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
from pathlib import Path
from typing import Iterable, Optional


SECTION_HEADER_RE = re.compile(
    r"^##\s+\[?v?(?P<version>[^\]\s]+)\]?(?:\s*-\s*.*)?$"
)
REF_LINK_RE = re.compile(r"^\[[^\]]+\]:\s+https?://")


def run_git(args: list[str]) -> tuple[int, str]:
    result = subprocess.run(
        ["git", *args],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    return result.returncode, result.stdout.strip()


def normalize_tag(raw_tag: str) -> str:
    raw = raw_tag.strip()
    if not raw:
        raise ValueError("Tag must not be empty")
    return raw if raw.startswith("v") else f"v{raw}"


def detect_repo(explicit_repo: Optional[str]) -> Optional[str]:
    if explicit_repo:
        return explicit_repo.strip()
    env_repo = os.environ.get("GITHUB_REPOSITORY", "").strip()
    if env_repo:
        return env_repo
    code, url = run_git(["remote", "get-url", "origin"])
    if code != 0 or not url:
        return None
    match = re.search(r"github\.com[:/](?P<slug>[^/]+/[^/]+?)(?:\.git)?$", url)
    return match.group("slug") if match else None


def extract_changelog_section(changelog_path: Path, tag: str) -> Optional[str]:
    """Extract the curated body for `tag` from CHANGELOG.md, or None if missing."""
    if not changelog_path.is_file():
        return None

    target_version = tag.lstrip("v")
    lines = changelog_path.read_text(encoding="utf-8").splitlines()

    capturing = False
    collected: list[str] = []

    for line in lines:
        if line.startswith("## "):
            if capturing:
                break
            match = SECTION_HEADER_RE.match(line.strip())
            if match and match.group("version").lstrip("v") == target_version:
                capturing = True
            continue

        if capturing:
            if REF_LINK_RE.match(line.strip()):
                break
            collected.append(line)

    if not capturing:
        return None

    body = "\n".join(collected).strip()
    return body or None


def tag_exists(tag: str) -> bool:
    code, _ = run_git(["rev-parse", "--verify", "--quiet", f"refs/tags/{tag}"])
    return code == 0


def find_previous_tag(tag: str) -> Optional[str]:
    """Find the closest ancestor version tag before `tag` (or HEAD if tag is new)."""
    if tag_exists(tag):
        code, prev = run_git(
            ["describe", "--tags", "--abbrev=0", "--match", "v*", f"{tag}^"]
        )
        if code == 0 and prev:
            return prev
        # Fallback: check version-sorted tag list in case of non-linear history
        code, all_tags = run_git(["tag", "--list", "v*", "--sort=v:refname"])
        if code == 0 and all_tags:
            tags = [t for t in all_tags.splitlines() if t]
            if tag in tags:
                idx = tags.index(tag)
                if idx > 0:
                    return tags[idx - 1]
        return None

    code, prev = run_git(["describe", "--tags", "--abbrev=0", "--match", "v*", "HEAD"])
    return prev if code == 0 and prev else None


def generate_commit_fallback(tag: str, prev_tag: Optional[str]) -> str:
    """Build bullet points from git commits when CHANGELOG.md lacks an entry."""
    end_ref = tag if tag_exists(tag) else "HEAD"
    rev_range = f"{prev_tag}..{end_ref}" if prev_tag else end_ref
    code, log_output = run_git(
        ["log", "--no-merges", "--pretty=format:- %s (%h)", rev_range]
    )
    if code == 0 and log_output.strip():
        return log_output.strip()
    if prev_tag:
        return f"- Maintenance release with no code changes since `{prev_tag}`."
    return "- Initial release."


def load_asset_names(
    assets_dir: Optional[Path], assets_file: Optional[Path]
) -> Optional[set[str]]:
    names: set[str] = set()

    if assets_dir and assets_dir.is_dir():
        for entry in assets_dir.iterdir():
            if entry.is_file():
                names.add(entry.name)

    if assets_file and assets_file.is_file():
        for line in assets_file.read_text(encoding="utf-8").splitlines():
            cleaned = line.strip()
            if cleaned:
                names.add(cleaned)

    return names if names else None


def pick_asset(
    asset_names: Optional[Iterable[str]], suffix: str, default_name: str
) -> Optional[str]:
    if asset_names is None:
        return default_name
    for name in sorted(asset_names):
        if name.endswith(suffix):
            return name
    return None


def build_downloads_section(tag: str, asset_names: Optional[set[str]]) -> str:
    arm64_dmg = pick_asset(
        asset_names, "-macos-arm64.dmg", f"SpaceShipsBlaster-{tag}-macos-arm64.dmg"
    )
    x64_dmg = pick_asset(
        asset_names, "-macos-x64.dmg", f"SpaceShipsBlaster-{tag}-macos-x64.dmg"
    )
    win_zip = pick_asset(
        asset_names, "-windows-x64.zip", f"SpaceShipsBlaster-{tag}-windows-x64.zip"
    )
    linux_tar = pick_asset(
        asset_names, "-linux-x64.tar.gz", f"SpaceShipsBlaster-{tag}-linux-x64.tar.gz"
    )
    jar_name = pick_asset(asset_names, ".jar", "SpaceShipsBlaster.jar")

    lines: list[str] = ["### Standalone Downloads (No Java Required)"]
    has_mac = False

    if arm64_dmg:
        has_mac = True
        lines.append(
            f"- **macOS (Apple Silicon / M1–M4):** Download `{arm64_dmg}`, drag **Space Ships Blaster.app** to Applications."
        )
    if x64_dmg:
        has_mac = True
        lines.append(
            f"- **macOS (Intel):** Download `{x64_dmg}`, drag **Space Ships Blaster.app** to Applications."
        )
    if has_mac:
        lines.append(
            '  *(First launch on unsigned Mac builds: right-click → **Open**, or run `xattr -cr "/Applications/Space Ships Blaster.app"`.)*'
        )
    if win_zip:
        lines.append(
            f"- **Windows (x64):** Download `{win_zip}`, extract, and run **`Space Ships Blaster.exe`**."
        )
    if linux_tar:
        lines.append(
            f"- **Linux (x64):** Download `{linux_tar}`, extract, and run **`./Space\\ Ships\\ Blaster/bin/Space\\ Ships\\ Blaster`**."
        )

    if jar_name:
        lines.extend(
            [
                "",
                "### Universal JAR (Requires Java 8+)",
                f"- Download **`{jar_name}`** and run:",
                "  ```bash",
                f"  java -jar {jar_name}",
                "  ```",
            ]
        )

    return "\n".join(lines)


def build_release_notes(
    tag: str,
    changelog_path: Path,
    repo: Optional[str],
    asset_names: Optional[set[str]],
) -> str:
    prev_tag = find_previous_tag(tag)
    curated = extract_changelog_section(changelog_path, tag)

    if curated:
        changes_body = curated
        source_label = "CHANGELOG.md"
    else:
        changes_body = generate_commit_fallback(tag, prev_tag)
        source_label = "git log fallback"

    print(
        f"[build_release_notes] {tag}: using {source_label} (prev={prev_tag or 'none'})",
        file=sys.stderr,
    )

    sections: list[str] = [
        "## What's Changed",
        "",
        changes_body,
    ]

    if repo and prev_tag:
        sections.extend(
            [
                "",
                f"**Full Changelog**: https://github.com/{repo}/compare/{prev_tag}...{tag}",
            ]
        )

    sections.extend(["", "---", "", "## Downloads", "", build_downloads_section(tag, asset_names), ""])
    return "\n".join(sections)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Build GitHub Release notes from CHANGELOG.md with commit fallback."
    )
    parser.add_argument("--tag", required=True, help="Release tag (e.g. v1.1.2)")
    parser.add_argument(
        "--changelog",
        default="CHANGELOG.md",
        help="Path to CHANGELOG.md (default: CHANGELOG.md)",
    )
    parser.add_argument(
        "--repo",
        default=None,
        help="GitHub repository slug owner/repo (defaults to GITHUB_REPOSITORY or git origin)",
    )
    parser.add_argument(
        "--assets-dir",
        default=None,
        help="Optional directory of release assets to tailor download instructions",
    )
    parser.add_argument(
        "--assets-file",
        default=None,
        help="Optional newline-separated file of release asset filenames",
    )
    parser.add_argument(
        "--output",
        default="-",
        help="Output markdown path (default: stdout)",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    tag = normalize_tag(args.tag)
    repo = detect_repo(args.repo)
    asset_names = load_asset_names(
        Path(args.assets_dir) if args.assets_dir else None,
        Path(args.assets_file) if args.assets_file else None,
    )

    notes = build_release_notes(
        tag=tag,
        changelog_path=Path(args.changelog),
        repo=repo,
        asset_names=asset_names,
    )

    if args.output == "-":
        sys.stdout.write(notes)
    else:
        Path(args.output).write_text(notes, encoding="utf-8")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
