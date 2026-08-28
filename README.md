# Second-Hand-Transaction Microservices

从二手交易平台单体代码独立建立的微服务改造仓库。当前是**第一阶段**，不是完整替代原后端的生产版本。

## 当前结构

- `backend/`、`frontend/`：保留的单体参考代码，根 Maven 构建不包含旧 backend。
- `services/user-service/`：注册、登录、当前用户、内部身份校验与用户摘要。
- `services/product-service/`：基础商品发布/查询、库存预占/确认/释放。
- `services/trade-service/`：原价下单、本人订单查询、取消、持久化库存恢复。
- `platform/`：通用 HTTP、错误响应和 RSA 凭证验证；不共享业务 Entity 或 Repository。
- `test-support/`、`tests/system-tests/`：独立 MySQL 测试工具及真实三服务 HTTP 回归。
- `docs/微服务设计/`：原设计方案；其中标记“目标/拟新增”的内容不代表全部实现。

改造前版本：标签 `monolith-before-microservices`（原提交 fa88c48）。改造工作分支：`codex/microservices-phase1`。

## 本轮范围

已实现首条业务链：注册登录 → 发布商品 → 创建订单并预占库存 → 取消并释放库存。

特别验证重复请求、并发抢最后一件、释放先于预占、审核下架保护、服务身份/受众校验、跨库访问被拒绝，以及**商品服务已扣库存但响应丢失，交易服务重启后恢复且不重复扣减**。

尚未迁移：支付/退款、发货、售后、议价、收藏评论举报、地址接口、完整消息与 outbox、管理后台、前端路由适配、生产 Kubernetes 部署和回滚。当前不应将原前端直接切换到这些服务。

## 构建和测试

需要 Java 17+、Maven 3.9+、可访问的 Docker。测试使用临时 MySQL 容器，不连接现有开发/生产数据库。

```powershell
mvn --batch-mode --no-transfer-progress clean verify
python scripts/summarize_microservice_tests.py
```

单独构建/测试某个服务（同时构建通用依赖，不编译其他业务服务）：

```powershell
mvn -pl services/product-service -am clean verify
```

可运行 JAR 为各服务 `target/*-exec.jar`；不带 exec 的 JAR 只用于测试组合。完整构建后可生成本地开发凭证：

```powershell
java -cp platform/target/classes com.secondhand.micro.platform.GenerateDevEnvironment
docker compose -f compose.microservices.yml up --build
```

服务端口仅绑定本机：用户 18081、商品 18082、交易 18083。健康与就绪检查为 `/actuator/health/liveness` 和 `/actuator/health/readiness`。

数据库初始化仅针对该 Compose 项目的新数据卷，未提供已有数据库的自动升级。不要把根目录旧 `docker-compose.yml`、旧 `k8s/` 和旧 `scripts/deploy.sh` 用于微服务部署；它们只是单体参考。

## 安全与数据边界

- 三个数据库分别是 secondhand_user、secondhand_product、secondhand_trade；各服务运行账号只有本库 DML 权限，建表由初始化流程完成。
- 每个服务只持有自己的 RSA 私钥，接收方持有受信公钥。内部 JWT 限定签发者、目标服务、类型和到期时间；控制器再次校验允许调用的服务。
- 本地 Compose 不提供公网入口。生产必须增加 TLS、NetworkPolicy、凭证轮换和专用内部网络，当前 Compose 不构成生产安全方案。
- `.env` 包含私钥和数据库密码，已被 Git 忽略；生成工具拒绝覆盖。示例文件不包含可用凭证。
- 库存恢复任务持久化并使用固定业务操作号；当前采用固定间隔重试，指数退避、任务租约和人工告警面板仍是下一阶段工作。
- 旧的 CI/CD 已改为 `.disabled` 文件；新增 CI 只验证测试和构建镜像，不发布、不部署到原项目环境。

## 证据与进度

测试报告：`reports/phase1-test-report.md`（运行汇总命令生成）；原始 XML 位于各模块 target/failsafe-reports。
详细范围、已知限制与下一步见 [第一阶段改造记录](docs/第一阶段改造记录.md)。
