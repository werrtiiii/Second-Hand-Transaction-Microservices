#!/usr/bin/env bash
set -Eeuo pipefail
# 只部署新命名空间；镜像标签必须来自已通过测试的同一提交。
prefix=${1:?image-prefix}; version=${2:?immutable-version}
[[ "$prefix" =~ ^[a-z0-9./:_-]+$ && "$version" =~ ^[A-Za-z0-9._-]+$ && "$version" != latest ]] || exit 2
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
namespace=secondhand-microservices
kubectl get namespace "$namespace" >/dev/null
for service in user product trade; do
  kubectl -n "$namespace" get secret "$service-service-secrets" "$service-migration" >/dev/null
done
kubectl -n "$namespace" get configmap service-public-config >/dev/null
mkdir -p "$root/reports/deployment"
kubectl -n "$namespace" get deployments -o yaml > "$root/reports/deployment/before.yaml"
trap 'bash "$root/scripts/collect-microservice-diagnostics.sh" "$root/reports/deployment/failure"; echo "Deployment failed; review diagnostics before rollback" >&2' ERR
for service in user product trade; do
  kubectl -n "$namespace" create configmap "$service-migrations" --from-file="$root/db/$service" --from-file="migrate.sh=$root/scripts/migrate-service.sh" --dry-run=client -o yaml | kubectl apply -f -
  job="$service-migrate-${version:0:12}-$(date +%s)"
  kubectl create -f - <<YAML
apiVersion: batch/v1
kind: Job
metadata: {name: $job, namespace: $namespace}
spec:
  backoffLimit: 0
  activeDeadlineSeconds: 180
  template:
    spec:
      restartPolicy: Never
      containers:
        - name: migrate
          image: mysql:8.0
          command: [bash, /migrations/migrate.sh]
          envFrom: [{secretRef: {name: $service-migration}}]
          env: [{name: MIGRATIONS_DIR, value: /migrations}]
          volumeMounts: [{name: scripts, mountPath: /migrations, readOnly: true}]
      volumes: [{name: scripts, configMap: {name: $service-migrations}}]
YAML
  kubectl -n "$namespace" wait --for=condition=complete "job/$job" --timeout=180s
  kubectl -n "$namespace" logs "job/$job" > "$root/reports/deployment/$service-migration.log"
done
kubectl -n "$namespace" create configmap gateway-nginx-template --from-file="default.conf.template=$root/gateway/default.conf.template" --dry-run=client -o yaml | kubectl apply -f -
python3 - "$root/deploy/kubernetes/services.yaml" "$prefix" "$version" <<'PY' | kubectl apply -f -
import sys
from pathlib import Path
print(Path(sys.argv[1]).read_text().replace('IMAGE_PREFIX',sys.argv[2]).replace('VERSION_TAG',sys.argv[3]))
PY
kubectl apply -f "$root/deploy/kubernetes/network-policy.yaml"
for service in user-service product-service trade-service gateway; do
  kubectl -n "$namespace" rollout status "deployment/$service" --timeout=180s
done
kubectl -n "$namespace" get deployments -o wide > "$root/reports/deployment/after.txt"
echo "Deployed $version"
