"""Run Android instrumentation and foreground activities rejected by MIUI's test-launch policy."""
import argparse
import pathlib
import re
import subprocess
import threading
import time

parser = argparse.ArgumentParser()
parser.add_argument('--serial', default='1c93ec120013')
parser.add_argument('--classes')
parser.add_argument('--output', required=True)
args = parser.parse_args()
adb = r'C:\Users\User-PC\AppData\Local\Android\Sdk\platform-tools\adb.exe'
base = [adb, '-s', args.serial]
package = 'com.mkhokheli.pocketcodedweb'
stop = threading.Event()
log = subprocess.Popen(base + ['logcat', '-T', '1', '-v', 'brief', '-s', 'ActivityTaskManager'], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True, encoding='utf-8', errors='replace')
def assist_launches():
    pending = None
    for line in log.stdout:
        if stop.is_set(): return
        launch = re.search(r'START u0 .*cmp=(' + re.escape(package) + r'/[^\s}]+).*from uid (\d+)', line)
        if launch:
            pending = (launch.group(1), launch.group(2), time.monotonic())
        rejected = re.search(r'Abort background activity starts from (\d+)', line)
        if rejected and pending and rejected.group(1) == pending[1] and time.monotonic() - pending[2] < 3:
            component = pending[0]
            pending = None
            print('MIUI launch assistance: ' + component, flush=True)
            subprocess.run(base + ['shell', 'am', 'start', '-W', '-a', 'android.intent.action.MAIN', '-c', 'android.intent.category.LAUNCHER', '-f', '0x10008000', '-n', component], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=20)
threading.Thread(target=assist_launches, daemon=True).start()
command = base + ['shell', 'am', 'instrument', '-w', '-r', '-e', 'timeout_msec', '180000']
if args.classes: command += ['-e', 'class', args.classes]
command += [package + '.test/androidx.test.runner.AndroidJUnitRunner']
try:
    with pathlib.Path(args.output).open('w', encoding='utf-8') as output:
        process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding='utf-8', errors='replace')
        for line in process.stdout:
            output.write(line); output.flush()
            if any(token in line for token in ['INSTRUMENTATION_STATUS: test=', 'INSTRUMENTATION_STATUS_CODE:', 'INSTRUMENTATION_RESULT:', 'INSTRUMENTATION_CODE:', 'FAILURES', 'OK (']): print(line.rstrip(), flush=True)
        process.wait()
finally:
    stop.set(); log.terminate()
