FROM node:22-alpine AS frontend
WORKDIR /build
# 国内网络无法稳定访问 registry.npmjs.org，改用 npmmirror 镜像源
ENV npm_config_registry=https://registry.npmmirror.com
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build
FROM nginx:1.28-alpine
ENV DNS_RESOLVER=127.0.0.11
ENV SERVICE_DNS_SUFFIX=
COPY gateway/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=frontend /build/dist/ /usr/share/nginx/html/
EXPOSE 8080
