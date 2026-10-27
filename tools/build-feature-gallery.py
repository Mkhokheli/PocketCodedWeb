"""Build a portable gallery from real, unmodified ADB screenshots."""
from pathlib import Path
import hashlib
import html
import json
import struct

OUT = Path(__file__).resolve().parents[1] / 'output/feature-screenshots/2026-10-26'
NOTES = {
    1: 'Project home and project search; existing project left unchanged.',
    2: 'Create a blank project or import a file/ZIP. Import was not completed.',
    3: 'Creating the separate FeatureShowcase demonstration project.',
    4: 'HTML editor, file explorer, preview entry point and terminal.',
    5: 'Menu of debugger, profiler, tests, recovery, history and inspection tools.',
    6: 'Run-configuration creation form.',
    7: 'Actual node:test execution: two passed, one intentionally skipped.',
    8: 'Recovery interface; no recovery drafts existed. Restoration not demonstrated.',
    9: 'Local-history interface; no earlier saved versions existed for this file. Restoration not demonstrated.',
    10: 'Problems panel reports no language-service problems for the selected file.',
    11: 'Fullscreen explorer with JavaScript, HTML, SQLite, configuration and Markdown files.',
    12: 'Project search results for FeatureShowcase, scope, replacement and filter controls. Replacement not applied.',
    13: 'Fullscreen JavaScript editor, syntax highlighting, line numbers and completion suggestions.',
    14: 'Actual paused Node debugger: local prices and sum variables, stepping controls and call stack.',
    15: 'Completed V8 CPU profile with samples, duration, heap, hot functions and exported profile path.',
    16: 'SQLite products table populated with three demo rows, SQL input and Execute control. No SQL update demonstrated.',
    17: 'In-app preview, browser localhost and explicit preview URL choices.',
    18: 'Demo HTML/CSS page rendered in the in-app localhost preview.',
    19: 'The same demo page rendered in the connected phone\'s Chrome browser.',
    20: 'AI chat starter interface and project-context notice. The greeting is built in, not a provider response.',
    21: 'AI provider selection and settings menu.',
    22: 'AI endpoint, model and blank API-key configuration. No provider request was sent.',
    23: 'Open file/folder and save-current-folder interface. Export not completed.',
    24: 'Terminal copy, clear and recent-command controls.',
    25: 'Real JavaScript output and localhost preview/browser activity in the terminal.',
    26: 'Built-in terminal help describing available commands.',
    27: 'Actual local git init and git status in the demo project. No remote repository contacted.',
    28: 'SFTP command help. No server connection or transfer performed.',
    29: 'npm command help. No dependency download or package installation performed.',
    30: 'Offline language service active; no external language server connected.',
    31: 'Recent commands populated by this capture session.',
    32: 'Terminal in the Cyan Orbit Light theme.',
    33: 'Workspace in the light theme.',
    34: 'Create file and create folder actions.',
    35: 'File rename and delete menu. No existing user files changed.',
    36: 'Completion details for the user-defined total() function; no bundled reference exists for this custom symbol.',
    37: 'A saved named command configuration for the demo project.',
}

def build():
    records = []
    for path in sorted(OUT.glob('[0-9][0-9]-*.png')):
        data = path.read_bytes()
        assert data[:8] == b'\x89PNG\r\n\x1a\n', path
        width, height = struct.unpack('>II', data[16:24])
        number = int(path.name[:2])
        records.append(dict(file=path.name, title=path.stem[3:].replace('-', ' ').title(),
                            note=NOTES[number], width=width, height=height,
                            sha256=hashlib.sha256(data).hexdigest()))
    limitations = ('Not captured: sign-in, email verification, password reset and Premium purchase/restore '
                   '(the app was already signed in; no sign-out or billing action was performed); '
                   'AI responses, approved AI edits and batch undo; live SFTP transfers; Git remotes; '
                   'external language-server integration; package installation and framework scaffolding; '
                   'large-file editing; completed recovery/history restoration and refactoring. '
                   'Menu or help screenshots show available controls, not successful execution of those operations. '
                   'Autosave, session retention and offline behavior are not established by still screenshots.')
    metadata = dict(date='2026-10-26', device='Xiaomi 2209116AG (sweet)', app='PocketCoded Web',
                    version='4.1', versionCode=34, resolution='1080 x 2400',
                    source='ADB exec-out screencap -p; original PNG pixels preserved',
                    project='FeatureShowcase', limitations=limitations, screenshots=records)
    (OUT/'manifest.json').write_text(json.dumps(metadata, indent=2), encoding='utf-8')
    cards = '\n'.join(f'<figure><a href="{r["file"]}"><img loading="lazy" src="{r["file"]}" alt="{html.escape(r["title"])}"></a><figcaption><h2>{html.escape(r["title"])}</h2><p>{html.escape(r["note"])}</p><a href="{r["file"]}">Open original PNG</a></figcaption></figure>' for r in records)
    page = '''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>PocketCoded Web - Real phone feature gallery</title><style>
    *{box-sizing:border-box}body{margin:0;background:#080f1b;color:#eef6ff;font:16px/1.55 system-ui,sans-serif}header,main{max-width:1400px;margin:auto;padding:28px}h1{font-size:clamp(28px,4vw,48px);margin:0}header p{max-width:1000px;color:#b9cde0}.note{padding:18px;border:1px solid #45617a;border-radius:12px}nav{display:flex;gap:20px}a{color:#7bdef5}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(270px,1fr));gap:24px}figure{margin:0;padding:16px;background:#102236;border:1px solid #30465b;border-radius:14px}img{display:block;width:100%;height:560px;object-fit:contain;background:#070d14}h2{font-size:20px}figcaption p{color:#c1d1df}@media print{main{grid-template-columns:repeat(2,1fr)}figure{break-inside:avoid}img{height:430px}body{background:white;color:black}}
    </style><header><h1>PocketCoded Web</h1><p>Real phone feature gallery · 26 October 2026 · Xiaomi 2209116AG · App 4.1 (34)</p><p>COUNT original 1080 × 2400 screenshots captured from the connected phone. Click any image to open its full-resolution PNG. A separate FeatureShowcase project supplied the demo code, tests and database. Existing user projects were left unchanged.</p><nav><a href="README.md">Capture notes</a><a href="manifest.json">Screenshot manifest and SHA-256 hashes</a></nav><p class="note">LIMITATIONS</p></header><main>CARDS</main></html>'''
    (OUT/'index.html').write_text(page.replace('COUNT',str(len(records))).replace('LIMITATIONS',html.escape(limitations)).replace('CARDS',cards), encoding='utf-8')
    readme = f'# PocketCoded Web real phone screenshots\n\nCaptured 26 October 2026 from Xiaomi 2209116AG. Installed app: 4.1, version code 34.\n\nOpen [index.html](index.html) for the browsable gallery of {len(records)} original full-resolution screenshots. [manifest.json](manifest.json) records dimensions, captions and SHA-256 hashes.\n\nScreenshots were captured using ADB, with no fabricated screens or modified PNG pixels. Demo JavaScript, node:test tests and a small SQLite database were added only to the separate FeatureShowcase project. Tests, debugger, CPU profiling, preview and local Git were actually exercised. AI and transfer screenshots show configuration/help only.\n\n## Coverage limits\n\n{limitations}\n\n## Capture list\n\n'
    readme += '\n'.join(f'- [{r["title"]}]({r["file"]}): {r["note"]}' for r in records)
    readme += '\n\nThe demonstration project remains on the phone for review. The existing app installation was used; no APK was rebuilt or installed.\n'
    (OUT/'README.md').write_text(readme,encoding='utf-8')
    print(f'Built gallery: {len(records)} validated PNG files')

if __name__ == '__main__':
    build()
