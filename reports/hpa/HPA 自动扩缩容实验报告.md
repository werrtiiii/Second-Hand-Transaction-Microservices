# 商品服务 Kubernetes HPA 自动扩缩容实验报告

## 1. 实验信息

- 实验日期：2026-08-31
- Kubernetes 客户端版本：v1.34.1
- Kubernetes 服务端版本：v1.34.3
- 命名空间：`secondhand-microservices`
- 扩缩容对象：`Deployment/product-service`
- HPA 范围：最少 1 个副本，最多 5 个副本
- 扩缩容指标：CPU 平均利用率目标 50%
- 压测工具：Grafana k6（Docker 镜像 `grafana/k6:latest`）

## 2. 压测场景

压测接口：`GET /api/products?page=0&size=20`。

k6 按阶段将虚拟用户从 20、100 提升到 300，在 300 VU 下保持负载，最后于 30 秒内降至 0。总压测时间 6 分钟。阈值为错误率低于 1%，P95 响应时间低于 1500 ms。

## 3. 扩缩容结果

| 阶段 | CPU/HPA 状态 | 商品服务副本数 | 结果 |
| --- | --- | ---: | --- |
| 压测前 | CPU 约 3%～4%，目标 50% | 1 | 基线正常 |
| 升压阶段 | CPU 峰值约 1001%，超过目标 | 1 → 2 → 4 → 5 | 成功扩容 |
| 300 VU 稳定阶段 | HPA 达到最大副本限制 | 5 | 持续承载压力 |
| 停止压力后 | CPU 回落至约 3%～4% | 5 → 2 → 1 | 成功缩容 |

HPA 事件明确记录了 `SuccessfulRescale`：扩容到 2、4、5 个副本；所有指标低于目标后缩容到 2，最后恢复到 1。

## 4. 性能结果

| 指标 | 结果 |
| --- | ---: |
| 完成请求数 | 305,912 |
| 中断迭代数 | 0 |
| 平均吞吐量 | 849.79 请求/秒 |
| 平均响应时间 | 223.27 ms |
| P90 响应时间 | 495.62 ms |
| P95 响应时间 | 594.01 ms |
| 最大响应时间 | 1.89 s |
| HTTP 错误率 | 0.00%（0/305,912） |
| 最大虚拟用户数 | 300 |

错误率和 P95 两项 k6 阈值均通过。

## 5. 实验结论

商品服务在 CPU 压力超过 50% 目标后自动从 1 个副本扩至 5 个副本。压力解除后，HPA 按 60 秒稳定窗口及每周期最多缩减 50% 的策略，将副本数逐步恢复至 1。扩容、稳定承载和缩容流程均符合配置预期。

## 6. 原始材料

- `hpa-observation-run2.log`：每 15 秒记录的 HPA、Pod、CPU 和内存变化
- `k6-product-hpa-run2.log`：k6 完整压测输出
- `hpa-final-status.log`：HPA 最终配置、状态和 SuccessfulRescale 事件
- `kubernetes-events.log`：Kubernetes 事件
## 7. 压测复现方法

### 7.1 前置条件

1. 在 Windows PowerShell 中进入项目根目录：

```powershell
cd D:\OneDrive\Desktop\Second-Hand-Transaction-Microservices
```

2. 确认 Docker 和 Kubernetes 集群已经启动，且 `kubectl` 当前上下文指向本机实验集群：

```powershell
docker info
kubectl config current-context
kubectl cluster-info
kubectl -n secondhand-microservices get deployments
kubectl -n secondhand-microservices get pods
```

所有微服务 Pod 应处于 `Running`，容器就绪数量应为 `1/1`。如果出现 `Unable to connect to the server: dial tcp 127.0.0.1:443`，说明 Kubernetes 未启动或当前上下文无效，应先启动 Docker Desktop Kubernetes、Rancher Desktop、Minikube 等实际使用的集群，并切换到正确上下文。

3. 确认 Metrics Server 可以提供 CPU 指标：

```powershell
kubectl get deployment metrics-server -n kube-system
kubectl top nodes
kubectl -n secondhand-microservices top pods
```

`kubectl top` 必须返回 CPU 和内存数据。若提示 `Metrics API not available`，应先安装或修复 Metrics Server，否则 HPA 无法计算期望副本数。

4. 确认商品服务已配置 CPU request。当前部署清单为 `100m`，HPA 的 CPU 利用率按照“实际 CPU 使用量 ÷ CPU request”计算：

```powershell
kubectl -n secondhand-microservices get deployment product-service -o jsonpath="{.spec.template.spec.containers[0].resources}"
```

### 7.2 建立实验基线并启用 HPA

先记录实验前的副本数，便于结束后复原；随后应用 HPA 配置：

```powershell
$namespace = "secondhand-microservices"
$originalReplicas = kubectl -n $namespace get deployment product-service -o jsonpath="{.spec.replicas}"

"实验前 product-service 副本数：$originalReplicas"

kubectl apply -f .\deploy\kubernetes\product-hpa.yaml
kubectl -n $namespace get hpa product-service
kubectl -n $namespace describe hpa product-service
```

预期看到 `MINPODS=1`、`MAXPODS=5`、CPU 目标为 `50%`。刚创建时指标可能短暂显示 `<unknown>`，通常等待 15～60 秒即可；持续为 `<unknown>` 时应检查 Metrics Server、Pod 就绪状态和 CPU request。

### 7.3 启动网关端口转发

打开第一个 PowerShell 窗口并保持运行：

```powershell
cd D:\OneDrive\Desktop\Second-Hand-Transaction-Microservices
kubectl -n secondhand-microservices port-forward service/gateway 18080:8080
```

在另一个窗口验证接口：

```powershell
$response = Invoke-RestMethod -Uri "http://127.0.0.1:18080/api/products?page=0&size=20"
$response
```

接口应返回业务成功结果。若返回 `502 Bad Gateway`，执行以下命令检查商品服务 Pod、Service Endpoint 和网关日志，问题排除后再开始压测：

```powershell
kubectl -n secondhand-microservices get pods -l app=product-service
kubectl -n secondhand-microservices get endpoints product-service
kubectl -n secondhand-microservices logs deployment/gateway --tail=100
kubectl -n secondhand-microservices logs deployment/product-service --tail=100
```

### 7.4 持续记录 HPA、Pod 和资源指标

打开第二个 PowerShell 窗口，执行下面的循环。每 15 秒采集一次数据，同时显示在终端并追加到复现实验日志：

```powershell
cd D:\OneDrive\Desktop\Second-Hand-Transaction-Microservices
New-Item -ItemType Directory -Force .\reports\hpa | Out-Null

$observationLog = ".\reports\hpa\hpa-observation-reproduce.log"
Remove-Item $observationLog -ErrorAction SilentlyContinue

while ($true) {
  @(
    "===== $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ====="
    (kubectl -n secondhand-microservices get hpa product-service 2>&1)
    (kubectl -n secondhand-microservices get pods -l app=product-service -o wide 2>&1)
    (kubectl -n secondhand-microservices top pods -l app=product-service 2>&1)
    ""
  ) | Tee-Object -FilePath $observationLog -Append

  Start-Sleep -Seconds 15
}
```

该写法把 `Tee-Object` 放在循环内部，可避免把 `while` 代码块直接接到管道后产生“不允许使用空管道元素”的 PowerShell 解析错误。观察结束时按 `Ctrl+C`。

### 7.5 执行 k6 压测

打开第三个 PowerShell 窗口。首次运行会自动拉取 `grafana/k6:latest` 镜像，出现 `Unable to find image ... locally` 后继续显示 `Pulling` 属正常现象，应等待下载完成：

```powershell
cd D:\OneDrive\Desktop\Second-Hand-Transaction-Microservices
New-Item -ItemType Directory -Force .\reports\hpa | Out-Null

$loadPath = (Resolve-Path .\tests\load).Path

docker run --rm -e BASE_URL=http://host.docker.internal:18080 --mount "type=bind,source=$loadPath,target=/scripts,readonly" grafana/k6:latest run /scripts/product-hpa.js 2>&1 | Tee-Object -FilePath .\reports\hpa\k6-product-hpa-reproduce.log
```

压测脚本位于 `tests/load/product-hpa.js`，阶段如下：

| 阶段 | 时长 | 目标虚拟用户数（VU） |
| --- | ---: | ---: |
| 预热 | 30 秒 | 0 → 20 |
| 第一次升压 | 1 分钟 | 20 → 100 |
| 第二次升压 | 2 分钟 | 100 → 300 |
| 稳定压力 | 2 分钟 | 300 |
| 降压 | 30 秒 | 300 → 0 |

每个 VU 会持续请求 `GET /api/products?page=0&size=20`，并检查 HTTP 状态码为 200、响应 JSON 的 `success` 为 `true`。脚本判定阈值为 HTTP 错误率低于 1%，P95 响应时间低于 1500 ms。

### 7.6 观察扩容和缩容

压测期间重点观察第二个窗口。也可以在第四个 PowerShell 窗口实时观察副本变化：

```powershell
kubectl -n secondhand-microservices get hpa product-service -w
```

负载升高后，CPU 利用率应超过 50% 目标，`REPLICAS` 应从 1 逐步增加，最高不超过 5。压测停止后不要立即结束观察；继续等待约 2～5 分钟，确认 CPU 回落且副本按缩容稳定窗口逐步恢复到 1。

复现实验应至少保存以下证据：

- k6 汇总中的请求数、吞吐量、平均响应时间、P95、错误率和阈值结果；
- HPA 的 CPU 当前值/目标值和副本数变化；
- `product-service` Pod 从 1 增加到多个，再恢复到 1；
- HPA 事件中的 `SuccessfulRescale` 记录。

### 7.7 收集最终状态和 Kubernetes 事件

确认完成扩容、承压和缩容后执行：

```powershell
kubectl -n secondhand-microservices get hpa product-service -o wide 2>&1 | Tee-Object -FilePath .\reports\hpa\hpa-final-status-reproduce.log

kubectl -n secondhand-microservices describe hpa product-service 2>&1 | Tee-Object -FilePath .\reports\hpa\hpa-final-status-reproduce.log -Append

kubectl -n secondhand-microservices get events --sort-by=.metadata.creationTimestamp 2>&1 | Tee-Object -FilePath .\reports\hpa\kubernetes-events-reproduce.log
```

在 `describe hpa` 输出底部应能找到扩容或缩容原因及 `SuccessfulRescale`。一次完整复现会生成：

- `reports/hpa/k6-product-hpa-reproduce.log`
- `reports/hpa/hpa-observation-reproduce.log`
- `reports/hpa/hpa-final-status-reproduce.log`
- `reports/hpa/kubernetes-events-reproduce.log`
