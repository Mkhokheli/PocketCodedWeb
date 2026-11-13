"""Inventory the supplied Py source archive without executing its contents.

Usage: python tools/compare-py-web.py PATH_TO_MAIN_ZIP
Outputs a complete archive manifest and Kotlin declaration comparison in artifacts/py-comparison.
"""
import hashlib
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "artifacts" / "py-comparison"
WEB = ROOT / "app/src/main/java/com/mkhokheli/pocketcodedweb"
RENAMES = {
    "DevelopmentTools.kt": "WebDevelopmentTools.kt",
    "RunConfigurations.kt": "WebRunConfigurations.kt",
    "ProjectSearchReplace.kt": "WebProjectSearch.kt",
    "FileHistoryDialog.kt": "WebFileHistoryDialog.kt",
    "PythonRunnerService.kt": "NodeSessionService.kt",
}
DECLARATION = re.compile(
    r"^\s*(?:(?:private|internal|public|protected|override|suspend|inline|tailrec|open|abstract|sealed|data|enum|annotation|value|expect|actual)\s+)*"
    r"(fun|class|interface|object)\s+(?:<[^>]+>\s*)?([\w.]+)", re.MULTILINE
)


def normalize(text):
    return text.replace("pocketcodedpy", "pocketcodedweb").replace("\r\n", "\n").strip()


def declarations(text):
    return [{"kind": match[1], "name": match[2], "line": text.count("\n", 0, match.start()) + 1}
            for match in DECLARATION.finditer(text)]


def main(archive_path):
    OUTPUT.mkdir(parents=True, exist_ok=True)
    archive_path = Path(archive_path)
    manifest, comparisons = [], []
    with zipfile.ZipFile(archive_path) as archive:
        for entry in archive.infolist():
            if entry.is_dir():
                continue
            data = archive.read(entry)
            item = {"path": entry.filename, "bytes": len(data), "sha256": hashlib.sha256(data).hexdigest()}
            counterpart = ROOT / "app/src" / entry.filename
            if counterpart.is_file():
                item["web_counterpart"] = counterpart.relative_to(ROOT).as_posix()
                item["identical_bytes"] = hashlib.sha256(counterpart.read_bytes()).hexdigest() == item["sha256"]
            manifest.append(item)
            prefix = "main/java/com/mkhokheli/pocketcodedpy/"
            if not entry.filename.startswith(prefix) or not entry.filename.endswith(".kt"):
                continue
            relative = entry.filename[len(prefix):]
            counterpart = WEB / RENAMES.get(relative, relative)
            py_text = data.decode("utf-8-sig")
            web_text = counterpart.read_text(encoding="utf-8-sig") if counterpart.exists() else None
            comparisons.append({
                "py": relative,
                "web": str(counterpart.relative_to(ROOT)).replace("\\", "/") if web_text is not None else None,
                "comparison": "same after package normalization" if web_text is not None and normalize(py_text) == normalize(web_text)
                              else "different implementation" if web_text is not None else "no direct file counterpart",
                "py_declarations": declarations(py_text),
                "web_declarations": declarations(web_text) if web_text is not None else [],
            })
    result = {"archive": archive_path.name, "archive_sha256": hashlib.sha256(archive_path.read_bytes()).hexdigest(),
              "note": "Structural inventory, not proof of behavioral or UI equivalence. Declaration matching is lexical.",
              "manifest": manifest, "kotlin_comparison": comparisons}
    (OUTPUT / "source-inventory.json").write_text(json.dumps(result, indent=2), encoding="utf-8")
    rows = ["# Source inventory", "", f"Archive SHA-256: `{result['archive_sha256']}`", "",
            f"Inspected {len(manifest)} archive files and {len(comparisons)} Kotlin source files.", "",
            "Structural comparison only; feature findings and verification are recorded separately.", "",
            "| Python source | Web counterpart | Structural result |", "| --- | --- | --- |"]
    rows.extend(f"| {item['py']} | {item['web'] or 'See feature audit'} | {item['comparison']} |" for item in comparisons)
    (OUTPUT / "source-inventory.md").write_text("\n".join(rows) + "\n", encoding="utf-8")
    print(f"Inventoried {len(manifest)} archive files; compared {len(comparisons)} Kotlin files.")
    for item in comparisons:
        print(f"{item['py']}: {item['comparison']}")


if __name__ == "__main__":
    main(sys.argv[1])
