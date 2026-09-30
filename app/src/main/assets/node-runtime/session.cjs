'use strict';

// This runs in a dedicated Android process. Stopping that process releases every
// socket, timer and module, and allows the next session to start a fresh Node VM.
const fs = require('fs');
const config = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const MAX_LOG_CHARS = Math.max(16 * 1024, Math.min(Number(config.maxLogChars) || 128 * 1024, 128 * 1024));
const LOG_FLUSH_MS = Math.max(100, Math.min(Number(config.logFlushMs) || 200, 1000));
let chunks = [];
let head = 0;
let logLength = 0;
let flushTimer = null;
let dirty = false;
let writing = false;
let logGeneration = 0;
function scheduleFlush() {
  if (!flushTimer) flushTimer = setTimeout(flushLog, LOG_FLUSH_MS).unref();
}
function flushLog() {
  if (flushTimer) clearTimeout(flushTimer);
  flushTimer = null;
  if (!dirty || writing) return;
  dirty = false;
  writing = true;
  const generation = logGeneration;
  const temporary = config.logPath + '.pending';
  // Only one bounded snapshot can be in flight. Disk writes must not stall HTTP,
  // HMR or application timers on Node's JavaScript thread.
  fs.writeFile(temporary, chunks.slice(head).join(''), error => {
    writing = false;
    if (generation !== logGeneration) return;
    if (!error) {
      try {
        // Publish a complete snapshot; the reader never sees a partial log.
        fs.renameSync(temporary, config.logPath);
      } catch (_) { error = true; }
    }
    if (error) dirty = true;
    if (dirty) scheduleFlush();
  });
}
function flushLogOnExit() {
  if (flushTimer) clearTimeout(flushTimer);
  flushTimer = null;
  if (!dirty && !writing) return;
  // An older asynchronous snapshot cannot replace the final output.
  logGeneration++;
  fs.writeFileSync(config.logPath, chunks.slice(head).join(''));
  dirty = false;
}
const urls = new Set();
let state = 'running';
function publish(extra = {}) {
  const temporary = config.statusPath + '.tmp';
  const payload = JSON.stringify({state, urls: [...urls], ...extra});
  fs.writeFileSync(temporary, payload);
  try {
    fs.renameSync(temporary, config.statusPath);
  } catch (error) {
    // Windows readers can briefly prevent replacement during host-side tests.
    // Readers already tolerate incomplete JSON, so fall back to a direct write.
    if (error.code !== 'EPERM' && error.code !== 'EACCES') throw error;
    fs.writeFileSync(config.statusPath, payload);
  }
}
function write(chunk, encoding, callback) {
  const value = (Buffer.isBuffer(chunk) ? chunk.toString('utf8') : String(chunk)).slice(-MAX_LOG_CHARS);
  if (value.length) {
    // Coalesce tiny writes into small blocks instead of retaining thousands of strings.
    const last = chunks.length - 1;
    if (last >= head && chunks[last].length + value.length <= 1024) chunks[last] += value;
    else chunks.push(value);
    logLength += value.length;
    while (logLength > MAX_LOG_CHARS) {
      const excess = logLength - MAX_LOG_CHARS;
      if (chunks[head].length <= excess) logLength -= chunks[head++].length;
      else {
        chunks[head] = chunks[head].slice(excess);
        logLength -= excess;
      }
    }
    if (head > 0 && head * 2 >= chunks.length) { chunks = chunks.slice(head); head = 0; }
    dirty = true;
    // Do not keep finite scripts alive just to flush logs; exit flushes synchronously.
    scheduleFlush();
  }
  const done = typeof encoding === 'function' ? encoding : callback;
  if (done) done();
  return true;
}
process.stdout.write = write;
process.stderr.write = write;
process.on('uncaughtExceptionMonitor', error => {
  write((error.stack || String(error)) + '\n');
  flushLogOnExit();
  state = 'failed';
  publish();
});
process.on('exit', code => {
  flushLogOnExit();
  state = code === 0 ? 'exited' : 'failed';
  publish({exitCode: code});
});
const net = require('net');
const originalListen = net.Server.prototype.listen;
net.Server.prototype.listen = function (...args) {
  this.once('listening', () => {
    const address = this.address();
    if (address && typeof address === 'object') {
      const host = address.address === '::' || address.address === '0.0.0.0'
        ? '127.0.0.1' : address.address.includes(':') ? `[${address.address}]` : address.address;
      const protocol = this instanceof require('https').Server ? 'https' : 'http';
      // Only advertise HTTP servers, not database or arbitrary TCP sockets.
      if (this instanceof require('http').Server || this instanceof require('https').Server) {
        const url = `${protocol}://${host}:${address.port}/`;
        urls.add(url);
        publish();
        write(`[preview] ${url}\n`);
        this.once('close', () => { urls.delete(url); publish(); });
      }
    }
  });
  return originalListen.apply(this, args);
};
process.chdir(config.cwd);
Object.assign(process.env, config.env || {});
process.argv = ['node', config.file, ...(config.args || [])];
publish();
// runMain preserves CommonJS require.main and ESM entry-point semantics.
require('module').runMain(config.file);
