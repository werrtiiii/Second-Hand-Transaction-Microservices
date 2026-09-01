# 微服务集成/API与端到端测试报告

生成时间：2026-08-29T10:01:33.423808+08:00

## 结论与范围

后端：**0 项，0 通过，0 失败，0 错误，0 跳过**。前端：**25 项，25 通过，0 未通过/跳过**。
公开接口成功HTTP证据：**0/104**；同时验证接口注册在方案指定的服务中。一次HTTP请求不等于一条测试，下面以JUnit用例为计数单位。

验证使用真实 Spring Boot HTTP 服务和临时 MySQL 8.0，不启动旧单体、不用H2或MockMvc替代业务链路。接口已覆盖不等于穷举全部参数组合；主流程、备选和异常分支见逐项明细。

远端流水线/生产部署不属于本地测试结果。本报告来自本轮本地执行或CI自身生成；实际镜像与部署证据另见交付说明。支付退款使用明确开启的模拟账本，未调用真实资金渠道。

## 运行环境与命令

- Java编译目标17；MySQL容器版本8.0；Testcontainers 1.21.4；Spring Boot 3.3.2。
- 三个服务分别用user_app、product_app、trade_app，只具备本库DML；测试准备建表使用独立的临时容器管理员。
- 普通批量回归关闭实例限流，限流专项在真实HTTP实例中单独开启并验证第21次登录返回429。
- 每个用例创建独立账号/商品/订单；数据库断言按归属使用对应服务账号，测试完成后销毁临时容器。
- 命令：`mvn --batch-mode --no-transfer-progress clean verify`；`npm test -- --reporter=default --reporter=junit --outputFile=../reports/frontend-junit.xml`。

## 数量统计

| 模块 | 总数 | 通过 | 失败 | 错误 | 跳过 |
|---|---:|---:|---:|---:|---:|
| services/user-service | 0 | 0 | 0 | 0 | 0 |
| services/product-service | 0 | 0 | 0 | 0 | 0 |
| services/trade-service | 0 | 0 | 0 | 0 | 0 |
| tests/system-tests | 0 | 0 | 0 | 0 | 0 |
| 后端合计 | 0 | 0 | 0 | 0 | 0 |
| 前端 | 25 | 25 | 0 | 0 | 0 |

## 104个公开接口证据索引

| 编号 | 服务 | 方法与路径 | 业务用途 | 成功请求数 | 异常请求数 |
|---|---|---|---|---:|---:|
| API-001 | user-service | `DELETE /api/users/addresses/{id}` | 删除收货地址 | 0 | 0 |
| API-002 | user-service | `GET /api/users/addresses` | 我的收货地址 | 0 | 0 |
| API-003 | user-service | `POST /api/users/addresses` | 新增收货地址 | 0 | 0 |
| API-004 | user-service | `PUT /api/users/addresses/{id}` | 修改收货地址 | 0 | 0 |
| API-005 | user-service | `PUT /api/users/addresses/{id}/default` | 设置默认地址 | 0 | 0 |
| API-006 | user-service | `GET /api/admin/dashboard` | 汇总用户商品交易统计 | 0 | 0 |
| API-007 | user-service | `GET /api/admin/users` | 管理端用户分页查询 | 0 | 0 |
| API-008 | user-service | `GET /api/admin/users/online` | 查询在线用户 | 0 | 0 |
| API-009 | user-service | `GET /api/admin/users/{id}` | 管理端用户详情 | 0 | 0 |
| API-010 | user-service | `POST /api/admin/users/{id}/kick` | 强制用户下线 | 0 | 0 |
| API-011 | user-service | `PUT /api/admin/users/{id}/disable` | 启用或禁用账号 | 0 | 0 |
| API-012 | user-service | `GET /api/auth/me` | 当前登录用户信息 | 0 | 0 |
| API-013 | user-service | `POST /api/auth/heartbeat` | 更新在线心跳 | 0 | 0 |
| API-014 | user-service | `POST /api/auth/login` | 登录并签发令牌 | 0 | 0 |
| API-015 | user-service | `POST /api/auth/password/change` | 修改登录密码 | 0 | 0 |
| API-016 | user-service | `POST /api/auth/register` | 用户注册 | 0 | 0 |
| API-017 | user-service | `GET /api/products/{productId}/chat` | 读取双方对话 | 0 | 0 |
| API-018 | user-service | `GET /api/users/messages` | 我的会话摘要 | 0 | 0 |
| API-019 | user-service | `POST /api/products/{productId}/chat` | 发送商品相关私聊 | 0 | 0 |
| API-020 | user-service | `PUT /api/messages/read` | 标记指定会话已读 | 0 | 0 |
| API-021 | user-service | `GET /api/messages/comments` | 商品评论通知 | 0 | 0 |
| API-022 | user-service | `GET /api/messages/system` | 订单和举报系统通知 | 0 | 0 |
| API-023 | user-service | `GET /api/regions` | 省市区静态数据 | 0 | 0 |
| API-024 | user-service | `GET /api/users/notifications` | 未读消息及待办数量 | 0 | 0 |
| API-025 | user-service | `GET /api/users/profile` | 我的个人资料 | 0 | 0 |
| API-026 | user-service | `GET /api/users/{id}/public` | 卖家公开资料和评分摘要 | 0 | 0 |
| API-027 | user-service | `PUT /api/users/avatar` | 上传头像 | 0 | 0 |
| API-028 | user-service | `PUT /api/users/profile` | 修改个人资料 | 0 | 0 |
| API-029 | product-service | `DELETE /api/admin/products/{id}` | 管理端删除违规商品 | 0 | 0 |
| API-030 | product-service | `GET /api/admin/products` | 管理端商品检索 | 0 | 0 |
| API-031 | product-service | `PUT /api/admin/products/{id}/off-shelf` | 管理端强制下架 | 0 | 0 |
| API-032 | product-service | `PUT /api/admin/products/{id}/on-shelf` | 管理端重新上架 | 0 | 0 |
| API-033 | product-service | `GET /api/admin/reports` | 管理端举报列表 | 0 | 0 |
| API-034 | product-service | `PUT /api/admin/reports/{id}/dismiss` | 驳回举报 | 0 | 0 |
| API-035 | product-service | `PUT /api/admin/reports/{id}/handle` | 办结举报 | 0 | 0 |
| API-036 | product-service | `GET /api/categories` | 商品分类树 | 0 | 0 |
| API-037 | product-service | `GET /api/products/{productId}/comments` | 商品评论列表 | 0 | 0 |
| API-038 | product-service | `POST /api/products/{productId}/comments` | 发表评论 | 0 | 0 |
| API-039 | product-service | `DELETE /api/products/{productId}/favorite` | 取消收藏 | 0 | 0 |
| API-040 | product-service | `GET /api/products/{productId}/favorite/status` | 我的收藏状态 | 0 | 0 |
| API-041 | product-service | `GET /api/users/favorites` | 我的收藏列表 | 0 | 0 |
| API-042 | product-service | `POST /api/products/{productId}/favorite` | 收藏商品 | 0 | 0 |
| API-043 | product-service | `GET /api/my-products` | 我的商品列表 | 0 | 0 |
| API-044 | product-service | `GET /api/products` | 商品分页分类搜索推荐 | 0 | 0 |
| API-045 | product-service | `GET /api/products/{id}` | 商品详情 | 0 | 0 |
| API-046 | product-service | `POST /api/products` | 发布商品 | 0 | 0 |
| API-047 | product-service | `PUT /api/products/{id}` | 编辑商品或上下架 | 0 | 0 |
| API-048 | product-service | `DELETE /api/products/{productId}/images/{imageId}` | 删除商品图片 | 0 | 0 |
| API-049 | product-service | `GET /api/products/{productId}/images` | 商品图片列表 | 0 | 0 |
| API-050 | product-service | `POST /api/products/{productId}/images` | 上传商品图片 | 0 | 0 |
| API-051 | product-service | `PUT /api/products/{productId}/images/{imageId}/cover` | 设置商品封面 | 0 | 0 |
| API-052 | product-service | `POST /api/products/{productId}/report` | 举报商品 | 0 | 0 |
| API-053 | product-service | `GET /api/users/{sellerId}/products` | 卖家在售商品 | 0 | 0 |
| API-054 | trade-service | `GET /api/admin/after-sale` | 管理端售后分页筛选 | 0 | 0 |
| API-055 | trade-service | `GET /api/admin/after-sale/{id}` | 管理端售后详情 | 0 | 0 |
| API-056 | trade-service | `POST /api/admin/after-sale/process-timeouts` | 管理员触发售后超时处理 | 0 | 0 |
| API-057 | trade-service | `POST /api/admin/after-sale/{id}/arbitrate` | 责任运费及退款仲裁 | 0 | 0 |
| API-058 | trade-service | `GET /api/admin/orders` | 管理端订单分页 | 0 | 0 |
| API-059 | trade-service | `GET /api/admin/orders/{id}` | 管理端订单详情 | 0 | 0 |
| API-060 | trade-service | `POST /api/admin/orders/{id}/cancel` | 管理员取消订单 | 0 | 0 |
| API-061 | trade-service | `POST /api/admin/orders/{id}/mark-paid` | 管理员标记已支付 | 0 | 0 |
| API-062 | trade-service | `GET /api/after-sale/all` | 旧版管理员售后列表 | 0 | 0 |
| API-063 | trade-service | `GET /api/after-sale/by-order/{orderId}` | 订单售后记录 | 0 | 0 |
| API-064 | trade-service | `GET /api/after-sale/my-received` | 我收到的售后 | 0 | 0 |
| API-065 | trade-service | `GET /api/after-sale/my-requests` | 我申请的售后 | 0 | 0 |
| API-066 | trade-service | `GET /api/after-sale/{id}` | 售后单详情 | 0 | 0 |
| API-067 | trade-service | `POST /api/after-sale` | 买家发起售后 | 0 | 0 |
| API-068 | trade-service | `POST /api/after-sale/process-timeouts` | 触发售后超时处理 | 0 | 0 |
| API-069 | trade-service | `POST /api/after-sale/{id}/approve` | 卖家同意售后 | 0 | 0 |
| API-070 | trade-service | `POST /api/after-sale/{id}/arbitrate` | 旧版平台仲裁入口 | 0 | 0 |
| API-071 | trade-service | `POST /api/after-sale/{id}/buyer-evidence` | 买家补充举证 | 0 | 0 |
| API-072 | trade-service | `POST /api/after-sale/{id}/cancel` | 买家取消售后 | 0 | 0 |
| API-073 | trade-service | `POST /api/after-sale/{id}/confirm-return` | 卖家确认收到退货 | 0 | 0 |
| API-074 | trade-service | `POST /api/after-sale/{id}/escalate` | 买家申请平台介入 | 0 | 0 |
| API-075 | trade-service | `POST /api/after-sale/{id}/reject` | 卖家拒绝售后 | 0 | 0 |
| API-076 | trade-service | `POST /api/after-sale/{id}/reject-return` | 卖家拒收退货 | 0 | 0 |
| API-077 | trade-service | `POST /api/after-sale/{id}/return-ship` | 买家填写退货运单 | 0 | 0 |
| API-078 | trade-service | `POST /api/after-sale/{id}/seller-evidence` | 卖家提交举证 | 0 | 0 |
| API-079 | trade-service | `GET /api/shipments/{orderId}/track` | 查询订单物流轨迹 | 0 | 0 |
| API-080 | trade-service | `GET /api/my-offers` | 我发出的报价 | 0 | 0 |
| API-081 | trade-service | `GET /api/products/{productId}/offers` | 卖家查看商品报价 | 0 | 0 |
| API-082 | trade-service | `GET /api/seller-offers` | 我收到的报价 | 0 | 0 |
| API-083 | trade-service | `POST /api/offers/{id}/accept` | 接受报价并创建订单 | 0 | 0 |
| API-084 | trade-service | `POST /api/offers/{id}/cancel` | 撤回报价 | 0 | 0 |
| API-085 | trade-service | `POST /api/offers/{id}/reject` | 拒绝报价 | 0 | 0 |
| API-086 | trade-service | `POST /api/products/{productId}/offers` | 发起议价 | 0 | 0 |
| API-087 | trade-service | `GET /api/orders/bought` | 我买到的订单 | 0 | 0 |
| API-088 | trade-service | `GET /api/orders/sold` | 我卖出的订单 | 0 | 0 |
| API-089 | trade-service | `GET /api/orders/{id}` | 订单详情及可用操作 | 0 | 0 |
| API-090 | trade-service | `POST /api/orders` | 按商品原价创建订单 | 0 | 0 |
| API-091 | trade-service | `POST /api/orders/process-settlements` | 触发到期结算 | 0 | 0 |
| API-092 | trade-service | `POST /api/orders/{id}/cancel` | 买家取消订单 | 0 | 0 |
| API-093 | trade-service | `POST /api/orders/{id}/confirm` | 买家确认收货 | 0 | 0 |
| API-094 | trade-service | `POST /api/orders/{id}/mark-paid` | 旧版支付兼容入口 | 0 | 0 |
| API-095 | trade-service | `POST /api/orders/{id}/pay` | 支付订单 | 0 | 0 |
| API-096 | trade-service | `POST /api/orders/{id}/ship` | 卖家发货 | 0 | 0 |
| API-097 | trade-service | `PUT /api/orders/{id}/receiver` | 补填或更新收货信息 | 0 | 0 |
| API-098 | trade-service | `GET /api/payments/{paymentNo}` | 查询支付状态 | 0 | 0 |
| API-099 | trade-service | `POST /api/payments` | 创建模拟支付单 | 0 | 0 |
| API-100 | trade-service | `POST /api/payments/{paymentNo}/mock-pay` | 开发测试模拟支付成功 | 0 | 0 |
| API-101 | trade-service | `GET /api/orders/{orderId}/rating` | 查询订单评价 | 0 | 0 |
| API-102 | trade-service | `GET /api/users/{userId}/rating` | 卖家评分摘要 | 0 | 0 |
| API-103 | trade-service | `POST /api/orders/{orderId}/rate` | 对已完成交易评分 | 0 | 0 |
| API-104 | trade-service | `GET /api/users/{sellerId}/sold` | 卖家已售订单及评价 | 0 | 0 |

## 测试用例逐项明细

编号MS-xxx对应本轮原始XML里的独立testcase。参数化测试的每个参数组分别统计；同方法的HTTP步骤合并展示其所有参数组。接口步骤中的状态码是实测结果，期望与业务断言来自测试代码。
所有账号、商品和订单均为测试创建的数据；实际请求体和完整数据库断言可点击代码链接查看。登录凭证、密码和图片正文不写入HTTP证据。

## 失败判定及原始证据

本轮失败原因以各testcase的failure/error为准；全部通过时不存在未解决的测试失败。开发中发现并修复的问题另记于交付说明，不能混入本轮失败数。
- 原始后端报告：`services/*/target/failsafe-reports/TEST-*.xml`、`tests/system-tests/target/failsafe-reports/TEST-*.xml`。
- 逐请求证据：`tests/system-tests/target/api-coverage/requests.json`。
- 前端原始报告：`reports/frontend-junit.xml`。
- CI任务verify非零退出时，images与deploy均被needs门禁阻断；原始报告仍由always步骤归档。
- 完整流水线部署还需远端仓库、镜像库与microservices环境凭证；本地完成不代表远端任务已经运行。

缺失接口：API-001, API-002, API-003, API-004, API-005, API-006, API-007, API-008, API-009, API-010, API-011, API-012, API-013, API-014, API-015, API-016, API-017, API-018, API-019, API-020, API-021, API-022, API-023, API-024, API-025, API-026, API-027, API-028, API-029, API-030, API-031, API-032, API-033, API-034, API-035, API-036, API-037, API-038, API-039, API-040, API-041, API-042, API-043, API-044, API-045, API-046, API-047, API-048, API-049, API-050, API-051, API-052, API-053, API-054, API-055, API-056, API-057, API-058, API-059, API-060, API-061, API-062, API-063, API-064, API-065, API-066, API-067, API-068, API-069, API-070, API-071, API-072, API-073, API-074, API-075, API-076, API-077, API-078, API-079, API-080, API-081, API-082, API-083, API-084, API-085, API-086, API-087, API-088, API-089, API-090, API-091, API-092, API-093, API-094, API-095, API-096, API-097, API-098, API-099, API-100, API-101, API-102, API-103, API-104
