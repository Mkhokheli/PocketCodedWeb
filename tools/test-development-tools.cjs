'use strict';
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const {spawn} = require('node:child_process');
const {once} = require('node:events');
const bootstrap = path.resolve(__dirname, '../app/src/main/assets/node-runtime/session.cjs');
async function launch(t, mode, source, extension = 'cjs') {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'pcw-dev-'));
  const file = path.join(dir, 'entry.' + extension);
  fs.writeFileSync(file, source);
  const config = {mode, root: dir, cwd: dir, file, args: [], statusPath: path.join(dir, 'status.json'), logPath: path.join(dir, 'session.log'), debugStatePath: path.join(dir, 'debug.json'), debugCommandPath: path.join(dir, 'command.json'), profileStatePath: path.join(dir, 'profile.json'), testResultsPath: path.join(dir, 'tests.json'), testFiles: [file]};
  const configPath = path.join(dir, 'config.json');
  fs.writeFileSync(configPath, JSON.stringify(config));
  const args = mode === 'tests' ? ['--test-reporter=tap'] : [];
  const env = {...process.env}; delete env.NODE_TEST_CONTEXT;
  const child = spawn(process.execPath, [...args, bootstrap, configPath], {windowsHide: true, env});
  let stderr = '';
  child.stderr.on('data', data => { stderr += data; });
  t.after(async () => {
    if (child.exitCode === null && child.signalCode === null) { const exited = once(child, 'exit'); child.kill(); await exited; }
    fs.rmSync(dir, {recursive: true, force: true});
  });
  function read(filename) { try { return JSON.parse(fs.readFileSync(filename)); } catch { return {}; } }
  async function wait(filename, predicate) {
    for (let index = 0; index < 400; index++) {
      const data = read(filename);
      if (predicate(data)) return data;
      await new Promise(resolve => setTimeout(resolve, 25));
    }
    throw new Error(`Missing expected state. stderr=${stderr}; status=${JSON.stringify(read(config.statusPath))}; debug=${JSON.stringify(read(config.debugStatePath))}; log=${fs.existsSync(config.logPath) ? fs.readFileSync(config.logPath, 'utf8') : ''}`);
  }
  let sequence = 0;
  function command(command, extra = {}) {
    const temporary = config.debugCommandPath + '.tmp';
    fs.writeFileSync(temporary, JSON.stringify({sequence: ++sequence, command, ...extra}));
    try { fs.renameSync(temporary, config.debugCommandPath); }
    catch (error) { if (error.code !== 'EPERM' && error.code !== 'EACCES') throw error; fs.copyFileSync(temporary, config.debugCommandPath); }
  }
  return {config, child, read, wait, command};
}

test('debugger pauses on entry, steps, inspects variables and continues to completion', async t => {
  const session = await launch(t, 'debug', 'let answer = 40;\nanswer += 2;\nconsole.log(answer);\n');
  const first = await session.wait(session.config.debugStatePath, data => data.state === 'paused' && data.stack?.length);
  assert.equal(first.stack[0].file, session.config.file);
  assert.equal(first.stack[0].line, 1);
  session.command('over');
  const next = await session.wait(session.config.debugStatePath, data => data.state === 'paused' && data.stack?.[0].line === 2);
  assert.ok(next.variables.some(variable => variable.name === 'answer' && variable.value === '40'));
  session.command('continue');
  await session.wait(session.config.statusPath, data => data.state === 'exited');
  assert.match(fs.readFileSync(session.config.logPath, 'utf8'), /42/);
});

test('debugger supports ESM and dynamically installed breakpoints', async t => {
  const session = await launch(t, 'debug', 'let answer = 1;\nanswer += 1;\nconsole.log(answer);\n', 'mjs');
  await session.wait(session.config.debugStatePath, data => data.state === 'paused' && data.stack?.length);
  session.command('breakpoints', {breakpoints: [{file: session.config.file, line: 3}]});
  await session.wait(session.config.debugStatePath, data => data.commandSequence >= 1);
  session.command('continue');
  const pause = await session.wait(session.config.debugStatePath, data => data.state === 'paused' && data.stack?.[0].line === 3);
  assert.ok(pause.variables.some(variable => variable.name === 'answer' && variable.value === '2'));
  session.command('continue');
  await session.wait(session.config.statusPath, data => data.state === 'exited');
});

test('visual tests retain passing, failing, skipped results and source locations', async t => {
  const session = await launch(t, 'tests', `const test = require('node:test');
const assert = require('node:assert/strict');
test('passing', () => assert.equal(2, 2));
test('failing', () => assert.equal(1, 2));
test.skip('skipped', () => {});
`);
  const results = await session.wait(session.config.testResultsPath, data => data.state === 'failed');
  assert.ok(results.results.some(result => result.name === 'passing' && result.status === 'passed'));
  const failure = results.results.find(result => result.name === 'failing');
  assert.equal(failure.status, 'failed');
  assert.equal(failure.file, session.config.file);
  assert.equal(failure.line, 4);
  assert.match(failure.details, /AssertionError|ERR_ASSERTION/);
  assert.ok(results.results.some(result => result.name === 'skipped' && result.status === 'skipped'));
});

test('async ESM tests run without spawning a Node executable', async t => {
  const session = await launch(t, 'tests', `import test from 'node:test';
import assert from 'node:assert/strict';
test('async case', async () => { await new Promise(resolve => setTimeout(resolve, 25)); assert.equal(2, 2); });`, 'mjs');
  const results = await session.wait(session.config.testResultsPath, data => data.state === 'passed');
  assert.equal(results.results[0].name, 'async case');
});

test('a test-file import failure is shown as a runner failure', async t => {
  const session = await launch(t, 'tests', "throw new Error('broken import')");
  const results = await session.wait(session.config.testResultsPath, data => data.state === 'failed');
  assert.equal(results.results[0].status, 'failed');
});


test('CPU profiler records a finite script and exports a standard profile', async t => {
  const session = await launch(t, 'profile', "function busyWork() { let value = 0; const end = Date.now() + 150; while (Date.now() < end) value += Math.sqrt(value + 1); return value; } console.log(busyWork());");
  const report = await session.wait(session.config.profileStatePath, data => data.state === 'complete');
  assert.ok(report.durationMs > 0);
  assert.ok(report.samples > 0);
  assert.ok(report.functions.some(item => item.name === 'busyWork'));
  const profile = JSON.parse(fs.readFileSync(report.reportPath, 'utf8'));
  assert.ok(profile.nodes.length > 0);
  assert.equal(profile.samples.length, report.samples);
  await session.wait(session.config.statusPath, data => data.state === 'exited');
});

test('CPU profiler supports ESM and samples servers without stopping them', async t => {
  const session = await launch(t, 'profile', "import fs from 'node:fs'; function serverWork() { const end = Date.now() + 5; while (Date.now() < end) fs.existsSync('.'); } setInterval(serverWork, 25);", 'mjs');
  const report = await session.wait(session.config.profileStatePath, data => data.state === 'complete');
  assert.ok(report.durationMs >= 4500);
  assert.ok(report.functions.some(item => item.name === 'serverWork'));
  assert.equal(session.child.exitCode, null);
});
