#!/usr/bin/env python3
"""Render a GitHub Actions Markdown summary from PMD XML reports."""
from __future__ import annotations

from collections import Counter
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

MODULES = ("runtime", "deployment")
MAX_DETAILS = 50


def parse_report(path: Path):
    root = ET.parse(path).getroot()
    violations = []
    for element in root.iter():
        if element.tag.rsplit("}", 1)[-1] != "violation":
            continue
        violations.append({
            "rule": element.get("rule", "unknown"),
            "line": element.get("beginline", "?"),
            "message": " ".join((element.text or "").split()),
            "file": next(
                (
                    parent.get("name", "")
                    for parent in root.iter()
                    if parent.tag.rsplit("}", 1)[-1] == "file"
                    and element in list(parent)
                ),
                "",
            ),
        })
    return violations


def main():
    print("## PMD Cognitive Complexity report\n")
    count = 0
    failures = []
    all_violations = []
    print("| Module | Violations | Report |")
    print("| --- | ---: | --- |")
    for module in MODULES:
        path = Path(module) / "target" / "pmd.xml"
        if not path.exists():
            failures.append(f"Missing PMD XML: `{path}`")
            print(f"| `{module}` | — | Missing |")
            continue
        try:
            violations = parse_report(path)
        except ET.ParseError as exc:
            failures.append(f"Invalid PMD XML: `{path}` ({exc})")
            print(f"| `{module}` | — | Invalid |")
            continue
        count += len(violations)
        all_violations.extend((module, v) for v in violations)
        print(f"| `{module}` | {len(violations)} | Generated |")
    print(f"\n**Total violations:** {count}. `integration-tests` is excluded.\n")
    if all_violations:
        print("### Findings by rule\n")
        for rule, amount in sorted(Counter(v["rule"] for _, v in all_violations).items()):
            print(f"- `{rule}`: {amount}")
        print("\n### Findings (first 50)\n")
        print("| Module | File:line | Rule | Message |")
        print("| --- | --- | --- | --- |")
        for module, v in all_violations[:MAX_DETAILS]:
            location = (v["file"].replace("|", "\\|") + ":" + v["line"])
            msg = v["message"].replace("|", "\\|").replace("`", "'")
            print(f"| `{module}` | `{location}` | `{v['rule']}` | {msg} |")
    else:
        print("No violations were reported in the available XML files.\n")
    if failures:
        print("\n### Report problems\n")
        for failure in failures:
            print(f"- {failure}")
        print("\nPMD reporting did not complete as expected.")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
