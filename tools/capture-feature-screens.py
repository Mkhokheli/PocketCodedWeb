"""Capture unmodified screenshots and inspect the connected Android UI via ADB."""
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ADB = str(Path(os.environ['LOCALAPPDATA']) / 'Android/Sdk/platform-tools/adb.exe')
OUT = Path(__file__).resolve().parents[1] / 'output/feature-screenshots/2026-10-26'

def adb(*args):
    return subprocess.check_output([ADB, *args], stderr=subprocess.DEVNULL)

def nodes():
    adb('shell', 'uiautomator', 'dump', '/sdcard/pocketcoded-ui.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/pocketcoded-ui.xml')).iter('node')

if __name__ == '__main__':
    action, *args = sys.argv[1:]
    if action == 'shot':
        OUT.mkdir(parents=True, exist_ok=True)
        path = OUT / (args[0] + '.png')
        path.write_bytes(adb('exec-out', 'screencap', '-p'))
        print(path)
    elif action == 'ui':
        for n in nodes():
            a = n.attrib
            label = a.get('text') or a.get('content-desc')
            if label:
                print(a['bounds'], label[:240])
    elif action == 'tap':
        for n in nodes():
            if args[0] in (n.get('text'), n.get('content-desc')):
                x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
                adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
                break
        else:
            raise SystemExit('UI label not found: ' + args[0])
    elif action == 'adb':
        print(adb(*args).decode('utf-8', errors='replace'))
    elif action == 'command':
        prompt = [n for n in nodes() if n.get('text', '').strip() == '~$'][-1]
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', prompt.get('bounds')))
        adb('shell', 'input', 'tap', '250', str((y1+y2)//2))
        time.sleep(.4)
        for c in args[0]:
            adb('shell', 'input', 'text', '%s' if c == ' ' else c)
            time.sleep(.08)
        adb('shell', 'input', 'tap', '995', '2180')
        time.sleep(.5)
        adb('shell', 'input', 'keyevent', '4')
