# Second-Hand-Transaction Microservices

二手交易平台的三业务微服务版本。**原方案104个公开接口已迁移**，前端统一通过网关接入。仓库仅保留拆分后的微服务实现及其构建、测试和部署资料。

| 服务 | 业务职责 | 公开接口 | 数据库 |
|---|---|---:|---|
| user-service | 注册登录、账号/地址/头像、聊天、消息中心、用户管理、后台聚合 | 28 | secondhand_user |
| product-service | 商品、分类、图片、收藏、评论、举报、库存及商品审核 | 25 | secondhand_product |
| trade-service | 下单、议价、支付退款、发货、售后仲裁、结算、评价、订单管理 | 51 | secondhand_trade |

详细架构、表归属、跨服务调用与失败处理见[完整改造与交付说明](docs/完整微服务改造与交付说明.md)。逐项用例与实测结果见[测试报告](reports/microservices-test-report.md)。

## 验证与运行

需要Java17+、Maven3.9+、Node22和Docker。测试创建临时MySQL，不连接已有业务数据库。

```powershell
python scripts/check-service-boundaries.py
mvn --batch-mode --no-transfer-progress clean verify
cd frontend
npm ci
npm test -- --reporter=default --reporter=junit --outputFile=../reports/frontend-junit.xml
npm run build
cd ..
python scripts/summarize_microservice_tests.py
```

某个服务可独立构建与测试，例如`mvn -pl services/product-service -am clean verify`；三个服务的Dockerfile也分别只构建本服务及通用模块。

首次本地运行生成开发凭证（拒绝覆盖已有`.env`）：

```powershell
java -cp platform/target/classes com.secondhand.micro.platform.GenerateDevEnvironment
./scripts/deploy-microservices-local.ps1 -Version dev-001
```

打开`http://localhost:18080`。三个服务调试端口为18081/18082/18083，仅绑定本机。就绪、存活、版本分别为`/actuator/health/readiness`、`/actuator/health/liveness`、`/actuator/info`，网关不公开这些运维入口或`/internal/`。

本地脚本会执行三条实际网关业务流程，创建专用测试账号和模拟交易；没有真实支付。回滚已有固定镜像：`./scripts/rollback-microservices-local.ps1 -Version dev-001`，不删除数据库或上传卷。

## CI/CD与Kubernetes

`.github/workflows/microservices-ci.yml`：边界检查→后端全部API/E2E→前端测试/构建→完整报告检查→四镜像构建。任一测试失败，不发布或部署镜像；原始报告始终归档。

远端`master`推送成功时以提交SHA发布GHCR镜像；配置独立的`microservices`环境、`MICROSERVICES_DEPLOY_ENABLED=true`及`MICROSERVICES_KUBECONFIG_BASE64`后自动部署。**目前按要求仅保留本地改动，没有推送或运行远端流水线。**

Kubernetes步骤、Secret准备、增量数据库迁移、失败诊断与回滚见[部署指南](docs/微服务部署与回滚.md)。

## 已验证边界

业务服务不共享Entity/Repository，各自数据库运行账号只有本库DML权限，跨域通过签名HTTP及事务outbox通信。网络结果未知的库存操作使用持久化状态机、固定业务键、租约和退避恢复；接收通知按来源与事件号去重。

本仓库完成原项目功能的微服务迁移，不代表真实支付/物流渠道、生产容量、安全审计或现网历史数据切换已经验收。原项目的模拟支付、物流模式保留；真实资金渠道需要单独接入，生产默认不启用模拟支付。

改造前：`monolith-before-microservices`标签（fa88c48）；改造后：当前本地工作树及交付源码快照。未自动提交Git。

## 本地初始数据导入

本次已按确认覆盖导入用户提供的旧SQL数据；账号、商品、订单及兼容记录验证结果、备份位置和图片缺失情况见[导入报告](reports/初始数据覆盖导入报告.md)。这不代表现网数据迁移已验收。日常保留当前数据请使用Compose的stop/start；部署脚本默认冒烟会新增测试数据。
