#!/usr/bin/env bash
set -Eeuo pipefail
# 只回滚应用镜像；不删除数据库、不执行破坏性的反向 DDL。
prefix=${1:?image-prefix}; version=${2:?known-good-version}
[[ "$prefix" =~ ^[a-z0-9./:_-]+$ && "$version" =~ ^[A-Za-z0-9._-]+$ && "$version" != latest ]] || exit 2
namespace=secondhand-microservices
for service in user-service product-service trade-service gateway; do
  kubectl -n "$namespace" set image "deployment/$service" "app=$prefix/$service:$version"
  if [[ "$service" != gateway ]]; then kubectl -n "$namespace" set env "deployment/$service" "APP_VERSION=$version"; fi
done
for service in user-service product-service trade-service gateway; do
  kubectl -n "$namespace" rollout status "deployment/$service" --timeout=180s
done
echo "Application images rolled back to $version; database retained"
