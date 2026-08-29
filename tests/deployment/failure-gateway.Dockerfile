# 仅用于本地隔离故障演练，不能用于正式镜像构建。
ARG BASE_IMAGE
FROM ${BASE_IMAGE}
RUN printf 'intentional_invalid_directive;\n' > /etc/nginx/templates/default.conf.template
