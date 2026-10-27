const http = require('node:http');

http.createServer((request, response) => {
  response.setHeader('Content-Type', 'application/json');
  if (request.url === '/api/health') {
    response.end(JSON.stringify({ status: 'ok', app: 'PocketCoded Web' }));
    return;
  }
  if (request.url === '/api/echo' && request.method === 'POST') {
    let body = '';
    request.setEncoding('utf8');
    request.on('data', chunk => { body += chunk; });
    request.on('end', () => response.end(JSON.stringify({ received: body })));
    return;
  }
  response.statusCode = 404;
  response.end(JSON.stringify({ error: 'Route not found' }));
}).listen(8000, '127.0.0.1', () => {
  console.log('API running at http://127.0.0.1:8000');
});
