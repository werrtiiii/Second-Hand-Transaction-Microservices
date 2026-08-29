#!/usr/bin/env bash
set -uo pipefail
namespace=secondhand-microservices
destination=${1:-reports/deployment-diagnostics}
mkdir -p "$destination"
kubectl -n "$namespace" get pods,deploy,svc,jobs -o wide > "$destination/resources.txt" 2>&1
kubectl -n "$namespace" get events --sort-by=.lastTimestamp > "$destination/events.txt" 2>&1
for service in user-service product-service trade-service gateway; do
  kubectl -n "$namespace" describe deployment "$service" > "$destination/$service-describe.txt" 2>&1
  kubectl -n "$namespace" logs "deployment/$service" --all-containers --tail=300 > "$destination/$service.log" 2>&1
done

for job in $(kubectl -n "$namespace" get jobs -o name); do
  kubectl -n "$namespace" logs "$job" --all-containers --tail=100 > "$destination/${job#job.batch/}-migration.log" 2>&1
done
