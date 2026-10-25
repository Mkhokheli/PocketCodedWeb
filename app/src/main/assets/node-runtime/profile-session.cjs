'use strict';

// Capture V8 CPU samples in the same VM as the user's CommonJS/ESM entry point.
module.exports = async function profileSession(config) {
  const fs = require('fs');
  const path = require('path');
  const {fileURLToPath} = require('url');
  const session = new (require('inspector').Session)();
  const statePath = config.profileStatePath;
  const publish = data => {
    const pending = statePath + '.pending';
    fs.writeFileSync(pending, JSON.stringify(data));
    try { fs.renameSync(pending, statePath); }
    catch (error) {
      if (!['EPERM', 'EACCES'].includes(error.code)) throw error;
      fs.copyFileSync(pending, statePath);
    }
  };
  const post = method => new Promise((resolve, reject) => session.post(method, (error, result) => error ? reject(error) : resolve(result)));
  session.connect();
  await post('Profiler.enable');
  await post('Profiler.start');
  publish({state: 'running'});
  let finished = false;
  function finish() {
    if (finished) return;
    finished = true;
    session.post('Profiler.stop', (error, result) => {
      try {
        if (error) throw error;
        const profile = result.profile;
        const directory = path.join(config.root, 'profiles');
        fs.mkdirSync(directory, {recursive: true});
        const reportPath = path.join(directory, `cpu-${Date.now()}.cpuprofile`);
        fs.writeFileSync(reportPath, JSON.stringify(profile));
        const times = new Map();
        (profile.samples || []).forEach((id, index) => times.set(id, (times.get(id) || 0) + (profile.timeDeltas?.[index] || 0)));
        const functions = profile.nodes.map(node => {
          const frame = node.callFrame;
          let file = frame.url || '';
          try { if (file.startsWith('file:')) file = fileURLToPath(file); } catch (_) {}
          return {name: frame.functionName || '(anonymous)', file, line: frame.lineNumber + 1,
            selfMs: (times.get(node.id) || 0) / 1000, hits: node.hitCount || 0};
        }).filter(node => node.selfMs > 0).sort((a, b) => b.selfMs - a.selfMs).slice(0, 100);
        publish({state: 'complete', durationMs: (profile.endTime - profile.startTime) / 1000,
          samples: profile.samples?.length || 0, heapUsed: process.memoryUsage().heapUsed, functions, reportPath});
      } catch (failure) { publish({state: 'failed', error: failure.message}); }
      finally { session.disconnect(); }
    });
  }
  // Finite scripts finish naturally. Servers produce a bounded five-second sample
  // while their session continues running until the user stops it.
  const timeout = setTimeout(finish, 5000);
  timeout.unref();
  process.once('beforeExit', finish);
  process.once('exit', finish);
  require('module').runMain(config.file);
};
