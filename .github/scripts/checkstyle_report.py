#!/usr/bin/env python3
"""Summarize Checkstyle findings and optionally enforce a repository-wide limit."""
import argparse
from collections import Counter
from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--max-violations",
        type=int,
        default=None,
        help="Fail if the combined main/test violation count exceeds this value",
    )
    args = parser.parse_args()
    if args.max_violations is not None and args.max_violations < 0:
        parser.error("--max-violations must be non-negative")

    print("## Checkstyle report\n")
    total = 0
    errors = []
    for kind in ("main", "test"):
        reports = sorted(Path(".").glob(f"**/target/checkstyle-{kind}.xml"))
        counts = Counter()
        findings = []
        for report in reports:
            try:
                root = ET.parse(report).getroot()
                if root.tag != "checkstyle":
                    raise ValueError(f"Unexpected root element: {root.tag}")
                for file in root.findall("file"):
                    for error in file.findall("error"):
                        check = error.get("source", "unknown").rsplit(".", 1)[-1]
                        counts[check] += 1
                        findings.append(
                            (file.get("name", ""), error.get("line", "?"),
                             check, error.get("message", ""))
                        )
            except (ET.ParseError, OSError, ValueError) as exc:
                errors.append(f"Cannot read {report}: {exc}")
        total += len(findings)
        print(f"### {kind.title()} sources\n")
        print(f"Reports: {len(reports)} | Findings: {len(findings)}\n")
        if not reports:
            errors.append(f"No {kind} Checkstyle XML reports found")
            print("_No XML reports found; check Maven logs._\n")
            continue
        print("| Check | Findings |\n|---|---:|")
        for check, count in counts.most_common():
            print(f"| {check} | {count} |")
        if not counts:
            print("| No violations | 0 |")
        print("\n<details><summary>First 50 findings</summary>\n")
        for file, line, check, message in findings[:50]:
            safe_message = message.replace("\n", " ").replace("|", "/")
            print(f"- `{Path(file).name}:{line}` **{check}**: {safe_message}")
        print("\n</details>\n")

    print(f"### Repository total: {total} violation(s)\n")
    if args.max_violations is not None:
        print(f"Allowed maximum: {args.max_violations}\n")
    print("_These are threshold violation counts, not exact per-method complexity scores._")
    if errors:
        print("\n### Report errors\n")
        for error in errors:
            print(f"- {error}")
        print("\n**Checkstyle gate: FAIL (missing or invalid reports)**")
        return 2
    if args.max_violations is not None:
        if total > args.max_violations:
            print(f"\n**Checkstyle gate: FAIL ({total} > {args.max_violations})**")
            return 1
        print(f"\n**Checkstyle gate: PASS ({total} <= {args.max_violations})**")
    return 0


if __name__ == "__main__":
    sys.exit(main())
