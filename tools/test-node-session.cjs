const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const {spawn} = require('node:child_process');
const {once} = require('node:events');
const bootstrap = path.resolve(__dirname, '../app/src/main/assets/node-runtime/session.cjs');

async function launch(t, source, extension = 'cjs', args = [], options = {}) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'pcw-session-'));
  const file = path.join(dir, `server.${extension}`);
  const statusPath = path.join(dir, 'status.json');
  const logPath = path.join(dir, 'session.log');
  fs.writeFileSync(file, source);
  fs.writeFileSync(path.join(dir, 'dependency.cjs'), 'module.exports = 42;');
  const configPath = path.join(dir, 'config.json');
  fs.writeFileSync(configPath, JSON.stringify({cwd: dir, file, args, env: {PCW_TEST: 'yes'}, statusPath, logPath, ...options}));
  const child = spawn(process.execPath, [bootstrap, configPath], {stdio: 'pipe', windowsHide: true});
  let stderr = '';
  child.stderr.on('data', data => { stderr += data; });
  t.after(async () => {
    if (child.exitCode === null && child.signalCode === null) {
      const exited = once(child, 'exit');
      child.kill();
      await exited;
    }
    fs.rmSync(dir, {recursive: true, force: true});
  });
  async function waitFor(predicate) {
    for (let n = 0; n < 200; n++) {
      let status;
      try { status = JSON.parse(fs.readFileSync(statusPath)); } catch {}
      if (status && predicate(status)) return status;
      await new Promise(resolve => setTimeout(resolve, 25));
    }
    throw new Error(`Session did not reach expected state: ${stderr}`);
  }
  const log = () => { try { return fs.readFileSync(logPath, 'utf8'); } catch { return ''; } };
  async function waitForLog(pattern) {
    for (let n = 0; n < 200; n++) {
      if (pattern.test(log())) return log();
      await new Promise(resolve => setTimeout(resolve, 25));
    }
    throw new Error(`Missing log output: ${pattern}`);
  }
  return {child, waitFor, log, waitForLog};
}

test('CommonJS backend serves asynchronous requests with arguments, environment and dependencies', async t => {
  const session = await launch(t, `
    const assert = require('assert');
    assert.strictEqual(require.main, module);
    assert.strictEqual(require('./dependency.cjs'), 42);
    assert.strictEqual(process.env.PCW_TEST, 'yes');
    assert.strictEqual(process.argv[2], 'two words');
    require('http').createServer((req, res) => setTimeout(() => {
      console.log('request', req.url);
      res.end('backend works');
    }, 10)).listen(0, '127.0.0.1');
  `, 'cjs', ['two words']);
  const status = await session.waitFor(s => s.urls?.length);
  assert.equal(await (await fetch(status.urls[0] + 'api')).text(), 'backend works');
  assert.match(await session.waitForLog(/request \/api/), /request \/api/);
});

test('ES modules and top-level await can start servers', async t => {
  const session = await launch(t, `
    import http from 'node:http';
    await new Promise(resolve => setTimeout(resolve, 20));
    http.createServer((req, res) => res.end('esm works')).listen(0, '127.0.0.1');
  `, 'mjs');
  const status = await session.waitFor(s => s.urls?.length);
  assert.equal(await (await fetch(status.urls[0])).text(), 'esm works');
});

test('async failures produce logs and failed status', async t => {
  const session = await launch(t, "setTimeout(() => { throw new Error('expected-async-failure'); }, 20);");
  await session.waitFor(s => s.state === 'failed');
  assert.match(session.log(), /expected-async-failure/);
});

test('finite programs finish and logs remain bounded', async t => {
  const session = await launch(t, "console.log('x'.repeat(200000)); console.log('done');");
  await session.waitFor(s => s.state === 'exited');
  assert.ok(session.log().length <= 128 * 1024);
  assert.match(session.log(), /done/);
});

test('bursty logs are batched instead of rewriting the file per message', async t => {
  const session = await launch(t, `
    const fs = require('fs');
    const original = fs.writeFileSync;
    let logWrites = 0;
    fs.writeFileSync = function(file, ...args) {
      if (String(file).endsWith('session.log')) logWrites++;
      return original.call(this, file, ...args);
    };
    for (let i = 0; i < 10000; i++) console.log('log entry', i);
    setTimeout(() => {
      require('assert').equal(logWrites, 0, 'running sessions must not synchronously write log contents');
      console.log('BATCH_OK', logWrites);
    }, 300);
  `);
  const status = await session.waitFor(s => s.state === 'exited' || s.state === 'failed');
  assert.equal(status.state, 'exited', session.log());
  assert.match(session.log(), /BATCH_OK/);
  assert.ok(session.log().length <= 128 * 1024);
});

test('exit during an asynchronous log flush preserves the newest output', async t => {
  const session = await launch(t, `
    const fs = require('fs');
    const original = fs.writeFile;
    fs.writeFile = function(file, data, callback) {
      if (String(file).endsWith('.pending')) {
        console.log('FINAL_OUTPUT');
        process.exit(0);
      }
      return original.call(this, file, data, callback);
    };
    console.log('FIRST_OUTPUT');
    setInterval(() => {}, 1000);
  `);
  await session.waitFor(s => s.state === 'exited');
  assert.match(session.log(), /FIRST_OUTPUT\nFINAL_OUTPUT/);
});

test('low-memory profile retains only the recent 64 Ki characters and flushes on exit', async t => {
  const session = await launch(t, `
    for (let i = 0; i < 20000; i++) console.log('entry', i);
    console.log('LOW_MEMORY_DONE');
  `, 'cjs', [], {maxLogChars: 64 * 1024, logFlushMs: 500});
  await session.waitFor(s => s.state === 'exited');
  assert.ok(session.log().length <= 64 * 1024);
  assert.match(session.log(), /entry 19999\nLOW_MEMORY_DONE/);
});

test('slow disk writes stay serialized while newer log entries accumulate', async t => {
  const session = await launch(t, `
    const fs = require('fs');
    const original = fs.writeFile;
    let active = 0;
    let maximum = 0;
    fs.writeFile = function(file, data, callback) {
      if (!String(file).endsWith('.pending')) return original.apply(this, arguments);
      maximum = Math.max(maximum, ++active);
      setTimeout(() => original.call(fs, file, data, error => {
        active--;
        callback(error);
      }), 250);
    };
    let sequence = 0;
    const producer = setInterval(() => console.log('sequence', ++sequence), 25);
    setTimeout(() => {
      clearInterval(producer);
      require('assert').equal(maximum, 1);
      console.log('SERIALIZED_FINAL', sequence);
    }, 850);
  `, 'cjs', [], {logFlushMs: 100});
  const status = await session.waitFor(s => s.state === 'exited' || s.state === 'failed');
  assert.equal(status.state, 'exited', session.log());
  assert.match(session.log(), /SERIALIZED_FINAL/);
});
