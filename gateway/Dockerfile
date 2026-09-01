FROM node:22-alpine AS frontend
WORKDIR /build
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
