# 微服务故障隔离实验报告

## 1. 实验目标

验证商品依赖故障时，网关快速返回事先设计的提示，用户服务和交易服务仍正常运行；依赖恢复后商品接口自动恢复。

## 2. 故障处理设计

- 网关连接上游超时：3 秒。
- 网关读取上游超时：30 秒。
- 上游出现 502、503 或 504 时，统一返回 HTTP 503。
- 统一错误码：`DEPENDENCY_UNAVAILABLE`。
- 统一提示：`依赖服务暂时不可用，请稍后重试`。
- 返回 `Retry-After: 5`，提示客户端稍后重试。
- 微服务分别部署为独立 Deployment、Pod 和 Service，商品故障不会停止用户与交易服务。

## 3. 故障注入方法

临时将 `product-service` Service 的 selector 从 `app=product-service` 改为不存在的 `app=product-service-fault-injection`，使 Service 没有可用 Endpoint。商品 Pod 本身不删除，数据库不修改。实验使用 `finally` 恢复原 selector。

## 4. 实验结果

| 阶段 | 接口 | HTTP | 响应时间 | 结果 |
| --- | --- | ---: | ---: | --- |
| 正常基线 | 商品列表 | 200 | 34 ms | 正常 |
| 商品依赖隔离 | 商品列表 | 503 | 9 ms | 返回预设 JSON 降级提示 |
| 商品依赖隔离 | 地区列表（用户服务） | 200 | 6 ms | 不受影响 |
| 商品依赖隔离 | 用户评分（交易服务） | 200 | 10 ms | 不受影响 |
| 恢复后首次请求 | 商品列表 | 503 | 2 ms | Nginx 仍处于最长 10 秒 DNS 缓存窗口 |
| DNS 缓存失效后 | 商品列表 | 200 | 约 60 ms | 自动恢复 |

故障期间返回体：

```json
{"success":false,"code":"DEPENDENCY_UNAVAILABLE","message":"依赖服务暂时不可用，请稍后重试"}
```

## 5. 结论

商品服务失去 Endpoint 后，网关在 9 ms 内返回明确的 HTTP 503 和预设中文提示，没有发生长时间等待。用户服务和交易服务接口仍返回 200，所有其他服务 Pod 保持运行。恢复 Service selector 后，商品接口在 Nginx 最长 10 秒 DNS 缓存失效后自动恢复为 200。实验满足故障隔离、快速失败、明确提示和自动恢复要求。

## 6. 原始材料

- `probe-results.json`：所有接口探测结果
- `pods-during-fault.log`：故障期间 Pod 状态
- `gateway.log`：网关故障日志
- `fault-injection.log`：故障注入记录
- `restore.log`：恢复记录
- `final-cluster-status.log`：实验结束后的集群状态
