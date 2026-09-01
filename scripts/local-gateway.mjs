import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(scriptDir, '..');
const templatePath = path.join(projectRoot, 'gateway', 'default.conf.template');
const listenHost = process.env.GATEWAY_HOST || '127.0.0.1';
const listenPort = Number(process.env.GATEWAY_PORT || 18080);
const timeoutMs = Number(process.env.GATEWAY_TIMEOUT_MS || 30000);
const targets = {
  'user-service': { host: '127.0.0.1', port: 18081 },
  'product-service': { host: '127.0.0.1', port: 18082 },
  'trade-service': { host: '127.0.0.1', port: 18083 },
};

function loadRoutes() {
  const routes = [];
  for (const line of fs.readFileSync(templatePath, 'utf8').split(/\r?\n/)) {
    const match = line.match(/^\s*~(\^\S+)\s+(user-service|product-service|trade-service):8080;\s*$/);
    if (match) routes.push({ pattern: new RegExp(match[1]), service: match[2] });
  }
  if (!routes.length) throw new Error('未从 gateway/default.conf.template 读取到 API 路由');
  return routes;
}
const routes = loadRoutes();

function selectService(pathname) {
  if (pathname.startsWith('/uploads/avatars/')) return 'user-service';
  if (pathname.startsWith('/uploads/products/')) return 'product-service';
  return routes.find((item) => item.pattern.test(pathname))?.service || null;
}
function writeJson(response, statusCode, body) {
  const data = Buffer.from(JSON.stringify(body), 'utf8');
  response.writeHead(statusCode, { 'content-type': 'application/json; charset=utf-8', 'content-length': data.length });
  response.end(data);
}

const server = http.createServer((request, response) => {
  const pathname = new URL(request.url || '/', 'http://localhost').pathname;
  if (pathname === '/healthz') {
    response.writeHead(200, { 'content-type': 'text/plain; charset=utf-8' });
    response.end('ok');
    return;
  }
  if (pathname.startsWith('/internal/') || pathname.startsWith('/actuator/')) {
    writeJson(response, 404, { success: false, code: 'NOT_FOUND', message: '资源不存在' });
    return;
  }
  const service = selectService(pathname);
  if (!service) {
    writeJson(response, 404, { success: false, code: 'NOT_FOUND', message: '资源不存在' });
    return;
  }
  const target = targets[service];
  const headers = { ...request.headers };
  delete headers.connection;
  delete headers['proxy-connection'];
  headers.host = target.host + ':' + target.port;
  headers['x-forwarded-host'] = request.headers.host || '';
  headers['x-forwarded-proto'] = 'http';
  headers['x-forwarded-for'] = request.socket.remoteAddress || '';

  let completed = false;
  const proxyRequest = http.request({
    hostname: target.host, port: target.port, method: request.method,
    path: request.url, headers, timeout: timeoutMs,
  }, (proxyResponse) => {
    const responseHeaders = { ...proxyResponse.headers };
    delete responseHeaders.connection;
    response.writeHead(proxyResponse.statusCode || 502, responseHeaders);
    proxyResponse.pipe(response);
  });
  const fail = (error) => {
    if (completed) return;
    completed = true;
    console.error(new Date().toISOString(), request.method, pathname, service, error.message);
    if (!response.headersSent) {
      writeJson(response, 503, { success: false, code: 'DEPENDENCY_UNAVAILABLE', message: '依赖服务暂时不可用，请稍后重试' });
    } else response.destroy(error);
  };
  proxyRequest.on('timeout', () => proxyRequest.destroy(new Error('上游请求超时')));
  proxyRequest.on('error', fail);
  proxyRequest.on('close', () => { completed = true; });
  request.on('aborted', () => proxyRequest.destroy());
  request.pipe(proxyRequest);
});

server.listen(listenPort, listenHost, () => {
  console.log('本地 API 网关已启动：http://' + listenHost + ':' + listenPort);
  console.log('已加载 ' + routes.length + ' 条 API 路由');
});
