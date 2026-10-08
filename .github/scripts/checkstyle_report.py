#!/usr/bin/env python3
"""Summarize Checkstyle violations separately for production and test code."""
from collections import Counter
from pathlib import Path
import xml.etree.ElementTree as ET

print("## Checkstyle report\n")
for kind in ("main", "test"):
    reports = sorted(Path(".").glob(f"**/target/checkstyle-{kind}.xml"))
    counts = Counter()
    findings = []
    for report in reports:
        root = ET.parse(report).getroot()
        for file in root.findall("file"):
            for error in file.findall("error"):
                check = error.get("source", "unknown").rsplit(".", 1)[-1]
                counts[check] += 1
                findings.append((file.get("name", ""), error.get("line", "?"), check, error.get("message", "")))
    print(f"### {kind.title()} sources\n")
    print(f"Reports: {len(reports)} | Findings: {len(findings)}\n")
    if not reports:
        print("_No XML reports found; check Maven logs._\n")
        continue
    print("| Check | Findings |\n|---|---:|")
    for check, count in counts.most_common():
        print(f"| {check} | {count} |")
    if not counts:
        print("| No violations | 0 |")
    print("\n<details><summary>First 50 findings</summary>\n")
    for file, line, check, message in findings[:50]:
        print(f"- `{Path(file).name}:{line}` **{check}**: {message.replace(chr(10), ' ').replace('|', '/')} ")
    print("\n</details>\n")
print("_These are threshold violation counts, not exact per-method complexity scores._")
