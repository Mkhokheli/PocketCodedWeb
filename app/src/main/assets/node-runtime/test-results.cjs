'use strict';
const fs = require('fs');
// Incremental TAP reader: retain complete records, not just a rendered log tail.
module.exports = function createTestResults(config) {
  let pending = '';
  let current = null;
  let inDetails = false;
  let revision = 0;
  let scheduled = null;
  const results = [];
  function flush(state = 'running') {
    if (scheduled) clearTimeout(scheduled);
    scheduled = null;
    const payload = {revision: ++revision, state, results, truncated: results.length >= 1000};
    const temporary = config.testResultsPath + '.pending';
    fs.writeFileSync(temporary, JSON.stringify(payload));
    try { fs.renameSync(temporary, config.testResultsPath); }
    catch (error) {
      if (error.code !== 'EPERM' && error.code !== 'EACCES') throw error;
      fs.writeFileSync(config.testResultsPath, JSON.stringify(payload));
    }
  }
  function line(text) {
    const match = /^(\s*)(not ok|ok)\s+(\d+)(?:\s+-)?\s*(.*)$/.exec(text);
    if (match && results.length < 1000) {
      const name = match[4].replace(/\s+#\s*(SKIP|TODO)\b.*$/i, '').trim();
      current = {id: String(results.length), name, status: /#\s*(SKIP|TODO)\b/i.test(match[4]) ? 'skipped' : match[2] === 'ok' ? 'passed' : 'failed', file: config.testFiles?.length === 1 ? config.testFiles[0] : '', line: 1, details: ''};
      results.push(current); inDetails = false;
    } else if (current) {
      if (/^\s+---\s*$/.test(text)) inDetails = true;
      else if (/^\s+\.\.\.\s*$/.test(text)) inDetails = false;
      else if (inDetails) {
        current.details = (current.details + text + '\n').slice(0, 16000);
        const location = /^\s*location:\s*['"]?(.*?):(\d+)(?::\d+)?['"]?\s*$/.exec(text);
        if (location) { current.file = location[1].replace(/^file:\/\//, '').replace(/\\\\/g, '\\'); current.line = Number(location[2]); }
      }
    }
    if (!scheduled) scheduled = setTimeout(() => flush(), 100).unref();
  }
  return {
    write(chunk) {
      pending += String(chunk);
      let end;
      while ((end = pending.indexOf('\n')) >= 0) { line(pending.slice(0, end).replace(/\r$/, '')); pending = pending.slice(end + 1); }
      pending = pending.slice(-32000);
    },
    finish(code) {
      if (pending) line(pending);
      if (code !== 0 && !results.some(result => result.status === 'failed')) results.push({id: 'runtime-error', name: 'Test runner failed', status: 'failed', file: config.file, line: 1, details: 'The runner exited before completing successfully. See terminal output.'});
      if (!results.length) results.push({id: 'no-tests', name: 'No tests reported', status: 'failed', file: config.file, line: 1, details: 'Use node:test tests. No TAP test records were emitted.'});
      flush(code === 0 && !results.some(result => result.status === 'failed') ? 'passed' : 'failed');
    },
  };
};
