'use strict';
const fs = require('fs');
const {workerData, parentPort} = require('worker_threads');
const {pathToFileURL, fileURLToPath} = require('url');
const inspector = require('inspector');
const config = workerData.config;
// Inspector callbacks alone do not keep a worker event loop alive during attachment.
const keepAlive = setInterval(() => {}, 1000);
const session = new inspector.Session();
let paused = null;
let selectedFrame = 0;
let revision = 0;
let state = 'starting';
let lastCommand = 0;
let busy = false;
let breakpointIds = [];
let entryBreakpointId = null;
const scripts = new Map();
const post = (method, params = {}) => new Promise((resolve, reject) => session.post(method, params, (error, result) => error ? reject(error) : resolve(result)));
function publish(extra = {}) {
  const temporary = config.debugStatePath + '.pending';
  fs.writeFileSync(temporary, JSON.stringify({state, revision: ++revision, selectedFrame, commandSequence: lastCommand, ...extra}));
  try { fs.renameSync(temporary, config.debugStatePath); }
  catch (error) {
    if (error.code !== 'EPERM' && error.code !== 'EACCES') throw error;
    fs.writeFileSync(config.debugStatePath, fs.readFileSync(temporary));
  }
}
function source(url) {
  try { return url.startsWith('file:') ? fileURLToPath(url) : url; } catch { return url; }
}
function frameJson(frame) {
  return {id: frame.callFrameId, function: frame.functionName || '(anonymous)', file: source(frame.url || scripts.get(frame.location.scriptId) || ''), line: frame.location.lineNumber + 1, column: frame.location.columnNumber + 1};
}
async function snapshot(error) {
  const current = paused;
  if (!current) return;
  const frame = current.callFrames[selectedFrame] || current.callFrames[0];
  const variables = [];
  for (const scope of frame.scopeChain) {
    // Global objects are very large. Inspect lexical/local/closure scopes without invoking getters.
    if (scope.type === 'global') continue;
    const properties = await post('Runtime.getProperties', {objectId: scope.object.objectId, ownProperties: true, generatePreview: false});
    for (const item of properties.result || []) {
      if (variables.length >= 100) break;
      const value = item.value;
      variables.push({name: item.name, scope: scope.type, type: value?.type || 'accessor', value: String(value?.description ?? value?.unserializableValue ?? value?.value ?? (value?.type === 'undefined' ? 'undefined' : '(getter)')).slice(0, 2000)});
    }
  }
  if (current !== paused) return;
  publish({reason: current.reason, stack: current.callFrames.slice(0, 50).map(frameJson), variables, error});
}
async function setBreakpoints(points) {
  for (const id of breakpointIds) await post('Debugger.removeBreakpoint', {breakpointId: id}).catch(() => {});
  breakpointIds = [];
  for (const point of points.slice(0, 500)) {
    const result = await post('Debugger.setBreakpointByUrl', {url: pathToFileURL(point.file).href, lineNumber: point.line - 1});
    breakpointIds.push(result.breakpointId);
  }
}
async function start() {
  session.connectToMainThread();
  session.on('Debugger.scriptParsed', ({params}) => scripts.set(params.scriptId, params.url));
  session.on('Debugger.paused', ({params}) => {
    paused = params; selectedFrame = 0; state = 'paused';
    if (entryBreakpointId) { post('Debugger.removeBreakpoint', {breakpointId: entryBreakpointId}).catch(() => {}); entryBreakpointId = null; }
    snapshot().catch(error => publish({error: error.message, stack: params.callFrames.slice(0, 50).map(frameJson)}));
  });
  session.on('Debugger.resumed', () => { paused = null; state = 'running'; publish(); });
  await post('Debugger.enable');
  await post('Debugger.setPauseOnExceptions', {state: 'uncaught'});
  await setBreakpoints(config.breakpoints || []);
  // Always pause on entry, even when no user breakpoint has been set.
  if (!(config.breakpoints || []).some(point => point.file === config.file && point.line === 1)) {
    entryBreakpointId = (await post('Debugger.setBreakpointByUrl', {url: pathToFileURL(config.file).href, lineNumber: 0})).breakpointId;
  }
  state = 'running'; publish();
  parentPort.postMessage({ready: true});
  setInterval(async () => {
    if (busy) return;
    let command;
    try { command = JSON.parse(fs.readFileSync(config.debugCommandPath, 'utf8')); } catch { return; }
    if (command.sequence <= lastCommand) return;
    lastCommand = command.sequence; busy = true;
    try {
      if (command.command === 'breakpoints') await setBreakpoints(command.breakpoints || []);
      else if (command.command === 'inspect' && paused) {
        selectedFrame = Math.max(0, Math.min(command.frame || 0, paused.callFrames.length - 1));
        await snapshot();
      } else if (command.command === 'pause' && !paused) await post('Debugger.pause');
      else if (paused) {
        const method = {continue: 'Debugger.resume', into: 'Debugger.stepInto', over: 'Debugger.stepOver', out: 'Debugger.stepOut'}[command.command];
        if (method) await post(method);
      }
    } catch (error) {
      if (paused) await snapshot(error.message).catch(() => {});
      else publish({error: error.message});
    } finally {
      if (paused) await snapshot().catch(() => {});
      else publish();
      busy = false;
    }
  }, 75);
}
start().catch(error => {
  state = 'failed'; publish({error: error.message});
  parentPort.postMessage({error: error.message});
  clearInterval(keepAlive);
  session.disconnect();
});
