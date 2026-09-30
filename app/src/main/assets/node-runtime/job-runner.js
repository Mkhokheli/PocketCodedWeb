'use strict';

const fs = require('fs');
const path = require('path');
const {parentPort, workerData} = require('worker_threads');

const send = (stream, values) => parentPort.postMessage({stream, text: values.map(value => typeof value === 'string' ? value : require('util').inspect(value)).join(' ') + '\n'});

try {
  console.log = (...values) => send('stdout', values);
  console.info = (...values) => send('stdout', values);
  console.warn = (...values) => send('stderr', values);
  console.error = (...values) => send('stderr', values);
  process.argv = ['node', workerData.file || '[eval]', ...(workerData.args || [])];
  if (workerData.mode === 'eval') {
    const script = new (require('vm').Script)(String(workerData.code || ''), {filename: '[eval]'});
    script.runInThisContext({timeout: workerData.timeoutMs});
  } else if (workerData.mode === 'file') {
    const target = path.resolve(workerData.cwd, String(workerData.file || ''));
    const relative = path.relative(workerData.root, target);
    if (relative === '..' || relative.startsWith('..' + path.sep) || path.isAbsolute(relative)) throw new Error('Script is outside the project sandbox.');
    if (!fs.statSync(target).isFile()) throw new Error('Script does not exist.');
    require(target);
  } else {
    throw new Error(`Unsupported execution mode: ${workerData.mode}`);
  }
  parentPort.postMessage({done: true, exitCode: 0});
} catch (error) {
  const timedOut = error && (error.code === 'ERR_SCRIPT_EXECUTION_TIMEOUT' || /timed out/i.test(error.message || ''));
  send('stderr', [error && (error.stack || error.message) || String(error)]);
  parentPort.postMessage({done: true, timedOut, exitCode: timedOut ? 124 : 1});
}
