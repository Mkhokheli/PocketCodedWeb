'use strict';
const fs = require('fs');
const http = require('http');
const {monitorEventLoopDelay} = require('perf_hooks');
const delay = monitorEventLoopDelay({resolution: 10});
delay.enable();
let busy = false;
let cpuStart = process.cpuUsage();
let started = Date.now();
const bundle = '/*' + 'generated module documentation '.repeat(200000) + '*/\nglobalThis.moduleCount=6000;';
const work = setInterval(() => {
  if (!busy) return;
  const until = performance.now() + 6;
  while (performance.now() < until) Math.sqrt(Math.random());
  for (let n=0; n<50; n++) console.log('development rebuild trace', n, 'x'.repeat(200));
}, 20);
const page = `<!doctype html><meta name="viewport" content="width=device-width,initial-scale=1">
<style>body{margin:0;background:#111827;color:white;font:16px sans-serif}.grid{display:grid;grid-template-columns:repeat(4,1fr);gap:8px;padding:8px}.card{height:65px;background:#2563eb;border-radius:10px;display:grid;place-items:center}.moving{animation:slide 1.2s infinite alternate ease-in-out}@keyframes slide{to{transform:translateY(7px) rotate(2deg);background:#7c3aed}}h1{font-size:18px}</style>
<h1>Chrome performance test</h1><div class="grid"></div><script src="/bundle.js"></script><script>
(async function(){
const grid=document.querySelector('.grid');
grid.innerHTML=Array.from({length:2000},(_,i)=>'<div class="card '+(i<80?'moving':'')+'">'+i+'</div>').join('');
let tasks=[]; try{new PerformanceObserver(list=>tasks.push(...list.getEntries().map(e=>e.duration))).observe({type:'longtask',buffered:false});}catch(e){}
const results=[];
for(const mode of ['idle','busy']) {
  await fetch('/mode/'+mode);
  document.querySelector('h1').textContent='Chrome animations / server '+mode;
  await new Promise(r=>setTimeout(r,1500));
  tasks=[]; const frames=[],requests=[]; let last=0, hidden=false;
  const api=setInterval(async()=>{let t=performance.now();try{await (await fetch('/api')).text();requests.push(performance.now()-t);}catch(e){}},100);
  const start=performance.now();
  await new Promise(resolve=>requestAnimationFrame(function tick(t){
    hidden=hidden || document.hidden;
    if(last)frames.push(t-last); last=t;
    if(t-start<10000)requestAnimationFrame(tick);else resolve();
  }));
  clearInterval(api);frames.sort((a,b)=>a-b);requests.sort((a,b)=>a-b);
  const stats=await (await fetch('/stats')).json();
  const percentile=(a,p)=>a[Math.min(a.length-1,Math.floor(a.length*p))]||0;
  results.push({mode,hidden,frames:frames.length,p50:percentile(frames,.5),p95:percentile(frames,.95),over32:frames.filter(x=>x>32).length,longTasks:tasks.length,longTaskMs:tasks.reduce((a,b)=>a+b,0),apiP95:percentile(requests,.95),...stats});
}
await fetch('/result',{method:'POST',body:JSON.stringify({moduleCount,results})});
location.replace('about:blank');
})();</script>`;
http.createServer((req,res)=>{
  if(req.url.startsWith('/mode/')) {busy=req.url.endsWith('busy');delay.reset();cpuStart=process.cpuUsage();started=Date.now();res.end('ok');}
  else if(req.url==='/api') res.end('ok');
  else if(req.url==='/stats') {const cpu=process.cpuUsage(cpuStart);res.end(JSON.stringify({nodeCpuPercent:(cpu.user+cpu.system)/((Date.now()-started)*10),nodeDelayP95Ms:delay.percentile(95)/1e6}));}
  else if(req.url==='/result') {let body='';req.on('data',chunk=>{body+=chunk;if(body.length>65536)req.destroy();});req.on('end',()=>{busy=false;fs.writeFileSync('browser-result.json',body);res.end('ok');});}
  else if(req.url==='/bundle.js') {res.setHeader('Content-Type','text/javascript');res.end(bundle);}
  else {res.setHeader('Content-Type','text/html');res.end(page);}
}).listen(0,'127.0.0.1');
