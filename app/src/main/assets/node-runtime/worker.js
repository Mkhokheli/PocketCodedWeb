'use strict';

const http = require('http');
const fs = require('fs');
const path = require('path');
const vm = require('vm');
const {createRequire} = require('module');

const configPath = process.argv[2];
if (!configPath) throw new Error('Missing runtime configuration path');
const config = JSON.parse(fs.readFileSync(configPath, 'utf8'));
const MAX_BODY = 1024 * 1024;
const MAX_OUTPUT = 512 * 1024;
const MAX_TIMEOUT = 120000;
let jobQueue = Promise.resolve();

function inside(parent, child) {
  const relative = path.relative(path.resolve(parent), path.resolve(child));
  return relative === '' || (!relative.startsWith('..' + path.sep) && relative !== '..' && !path.isAbsolute(relative));
}

function reply(response, status, value) {
  const body = JSON.stringify(value);
  response.writeHead(status, {'content-type': 'application/json', 'content-length': Buffer.byteLength(body)});
  response.end(body);
}

function runJob(job) {
  return new Promise((resolve) => {
    const root = path.resolve(String(job.root || ''));
    const cwd = path.resolve(String(job.cwd || root));
    if (!inside(config.workspaceBase, root) || !inside(root, cwd)) {
      resolve({ok: false, exitCode: 126, stderr: 'Execution path is outside the app project sandbox.'});
      return;
    }
    const timeoutMs = Math.max(100, Math.min(Number(job.timeoutMs) || 15000, MAX_TIMEOUT));
    let stdout = '';
    let stderr = '';
    const append = (name, text) => {
      const value = String(text);
      if (name === 'stdout') stdout = (stdout + value).slice(-MAX_OUTPUT);
      else stderr = (stderr + value).slice(-MAX_OUTPUT);
    };
    const outputConsole = {
      log: (...values) => append('stdout', values.join(' ') + '\n'),
      info: (...values) => append('stdout', values.join(' ') + '\n'),
      warn: (...values) => append('stderr', values.join(' ') + '\n'),
      error: (...values) => append('stderr', values.join(' ') + '\n')
    };
    try {
      process.chdir(cwd);
      let source;
      let filename;
      if (job.mode === 'eval') {
        source = String(job.code || '');
        filename = '[eval]';
      } else if (job.mode === 'file') {
        filename = path.resolve(cwd, String(job.file || ''));
        if (!inside(root, filename) || !fs.statSync(filename).isFile()) throw new Error('Script is outside the project sandbox or does not exist.');
        source = fs.readFileSync(filename, 'utf8');
      } else {
        throw new Error(`Unsupported execution mode: ${job.mode}`);
      }
      const localRequire = job.mode === 'file' ? createRequire(filename) : require;
      const sandbox = {
        console: outputConsole,
        Buffer,
        require: localRequire,
        module: {exports: {}},
        exports: {},
        __filename: filename,
        __dirname: job.mode === 'file' ? path.dirname(filename) : cwd,
        process: {argv: ['node', filename, ...(job.args || [])], env: Object.freeze({...process.env}), cwd: () => cwd, platform: process.platform, arch: process.arch, version: process.version}
      };
      new vm.Script(source, {filename}).runInNewContext(sandbox, {timeout: timeoutMs});
      resolve({ok: true, exitCode: 0, stdout, stderr});
    } catch (error) {
      const timedOut = error && (error.code === 'ERR_SCRIPT_EXECUTION_TIMEOUT' || /timed out/i.test(error.message || ''));
      append('stderr', (error && (error.stack || error.message) || String(error)) + '\n');
      resolve({ok: false, timedOut, exitCode: timedOut ? 124 : 1, stdout, stderr});
    }
  });
}

const server = http.createServer((request, response) => {
  if (request.socket.remoteAddress !== '127.0.0.1' && request.socket.remoteAddress !== '::ffff:127.0.0.1') {
    request.destroy();
    return;
  }
  if (request.headers.authorization !== `Bearer ${config.token}`) return reply(response, 401, {ok: false, error: 'Unauthorized'});
  if (request.method === 'GET' && request.url === '/health') return reply(response, 200, {ok: true, version: process.version, abi: process.arch});
  if (request.method !== 'POST' || request.url !== '/run') return reply(response, 404, {ok: false, error: 'Not found'});
  let body = '';
  request.on('data', chunk => {
    body += chunk;
    if (Buffer.byteLength(body) > MAX_BODY) request.destroy();
  });
  request.on('end', async () => {
    try {
      const job = JSON.parse(body);
      const result = await (jobQueue = jobQueue.then(() => runJob(job), () => runJob(job)));
      reply(response, 200, result);
    }
    catch (error) { reply(response, 400, {ok: false, error: error.message}); }
  });
});

server.listen(0, '127.0.0.1', () => {
  const port = server.address().port;
  fs.writeFileSync(config.readyPath, JSON.stringify({port, pid: process.pid, version: process.version}), {mode: 0o600});
});
