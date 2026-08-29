# 微服务集成/API与端到端测试报告

生成时间：2026-08-28T17:32:27.600981+08:00

## 结论与范围

后端：**115 项，115 通过，0 失败，0 错误，0 跳过**。前端：**25 项，25 通过，0 未通过/跳过**。
公开接口成功HTTP证据：**104/104**；同时验证接口注册在方案指定的服务中。一次HTTP请求不等于一条测试，下面以JUnit用例为计数单位。

验证使用真实 Spring Boot HTTP 服务和临时 MySQL 8.0，不启动旧单体、不用H2或MockMvc替代业务链路。接口已覆盖不等于穷举全部参数组合；主流程、备选和异常分支见逐项明细。

远端流水线/生产部署不属于本地测试结果。本报告来自本轮本地执行或CI自身生成；实际镜像与部署证据另见交付说明。支付退款使用明确开启的模拟账本，未调用真实资金渠道。

## 运行环境与命令

- os.name：Windows 11
- os.version：10.0
- java.version：21.0.6
- os.arch：amd64
- java.vendor：Microsoft
- Java编译目标17；MySQL容器版本8.0；Testcontainers 1.21.4；Spring Boot 3.3.2。
- 三个服务分别用user_app、product_app、trade_app，只具备本库DML；测试准备建表使用独立的临时容器管理员。
- 普通批量回归关闭实例限流，限流专项在真实HTTP实例中单独开启并验证第21次登录返回429。
- 每个用例创建独立账号/商品/订单；数据库断言按归属使用对应服务账号，测试完成后销毁临时容器。
- 命令：`mvn --batch-mode --no-transfer-progress clean verify`；`npm test -- --reporter=default --reporter=junit --outputFile=../reports/frontend-junit.xml`。

## 数量统计

| 模块 | 总数 | 通过 | 失败 | 错误 | 跳过 |
|---|---:|---:|---:|---:|---:|
| services/user-service | 6 | 6 | 0 | 0 | 0 |
| services/product-service | 9 | 9 | 0 | 0 | 0 |
| services/trade-service | 3 | 3 | 0 | 0 | 0 |
| tests/system-tests | 97 | 97 | 0 | 0 | 0 |
| 后端合计 | 115 | 115 | 0 | 0 | 0 |
| 前端 | 25 | 25 | 0 | 0 | 0 |

## 104个公开接口证据索引

| 编号 | 服务 | 方法与路径 | 业务用途 | 成功请求数 | 异常请求数 |
|---|---|---|---|---:|---:|
| API-001 | user-service | `DELETE /api/users/addresses/{id}` | 删除收货地址 | 1 | 0 |
| API-002 | user-service | `GET /api/users/addresses` | 我的收货地址 | 2 | 0 |
| API-003 | user-service | `POST /api/users/addresses` | 新增收货地址 | 1 | 0 |
| API-004 | user-service | `PUT /api/users/addresses/{id}` | 修改收货地址 | 1 | 0 |
| API-005 | user-service | `PUT /api/users/addresses/{id}/default` | 设置默认地址 | 1 | 0 |
| API-006 | user-service | `GET /api/admin/dashboard` | 汇总用户商品交易统计 | 1 | 1 |
| API-007 | user-service | `GET /api/admin/users` | 管理端用户分页查询 | 1 | 0 |
| API-008 | user-service | `GET /api/admin/users/online` | 查询在线用户 | 1 | 0 |
| API-009 | user-service | `GET /api/admin/users/{id}` | 管理端用户详情 | 1 | 0 |
| API-010 | user-service | `POST /api/admin/users/{id}/kick` | 强制用户下线 | 1 | 0 |
| API-011 | user-service | `PUT /api/admin/users/{id}/disable` | 启用或禁用账号 | 3 | 0 |
| API-012 | user-service | `GET /api/auth/me` | 当前登录用户信息 | 1 | 0 |
| API-013 | user-service | `POST /api/auth/heartbeat` | 更新在线心跳 | 1 | 0 |
| API-014 | user-service | `POST /api/auth/login` | 登录并签发令牌 | 22 | 5 |
| API-015 | user-service | `POST /api/auth/password/change` | 修改登录密码 | 1 | 0 |
| API-016 | user-service | `POST /api/auth/register` | 用户注册 | 171 | 6 |
| API-017 | user-service | `GET /api/products/{productId}/chat` | 读取双方对话 | 1 | 0 |
| API-018 | user-service | `GET /api/users/messages` | 我的会话摘要 | 1 | 0 |
| API-019 | user-service | `POST /api/products/{productId}/chat` | 发送商品相关私聊 | 1 | 0 |
| API-020 | user-service | `PUT /api/messages/read` | 标记指定会话已读 | 1 | 0 |
| API-021 | user-service | `GET /api/messages/comments` | 商品评论通知 | 1 | 0 |
| API-022 | user-service | `GET /api/messages/system` | 订单和举报系统通知 | 11 | 0 |
| API-023 | user-service | `GET /api/regions` | 省市区静态数据 | 1 | 0 |
| API-024 | user-service | `GET /api/users/notifications` | 未读消息及待办数量 | 1 | 0 |
| API-025 | user-service | `GET /api/users/profile` | 我的个人资料 | 1 | 2 |
| API-026 | user-service | `GET /api/users/{id}/public` | 卖家公开资料和评分摘要 | 1 | 0 |
| API-027 | user-service | `PUT /api/users/avatar` | 上传头像 | 1 | 0 |
| API-028 | user-service | `PUT /api/users/profile` | 修改个人资料 | 1 | 0 |
| API-029 | product-service | `DELETE /api/admin/products/{id}` | 管理端删除违规商品 | 1 | 0 |
| API-030 | product-service | `GET /api/admin/products` | 管理端商品检索 | 1 | 0 |
| API-031 | product-service | `PUT /api/admin/products/{id}/off-shelf` | 管理端强制下架 | 1 | 0 |
| API-032 | product-service | `PUT /api/admin/products/{id}/on-shelf` | 管理端重新上架 | 1 | 0 |
| API-033 | product-service | `GET /api/admin/reports` | 管理端举报列表 | 1 | 0 |
| API-034 | product-service | `PUT /api/admin/reports/{id}/dismiss` | 驳回举报 | 2 | 2 |
| API-035 | product-service | `PUT /api/admin/reports/{id}/handle` | 办结举报 | 2 | 2 |
| API-036 | product-service | `GET /api/categories` | 商品分类树 | 1 | 0 |
| API-037 | product-service | `GET /api/products/{productId}/comments` | 商品评论列表 | 1 | 0 |
| API-038 | product-service | `POST /api/products/{productId}/comments` | 发表评论 | 1 | 0 |
| API-039 | product-service | `DELETE /api/products/{productId}/favorite` | 取消收藏 | 1 | 0 |
| API-040 | product-service | `GET /api/products/{productId}/favorite/status` | 我的收藏状态 | 1 | 0 |
| API-041 | product-service | `GET /api/users/favorites` | 我的收藏列表 | 1 | 0 |
| API-042 | product-service | `POST /api/products/{productId}/favorite` | 收藏商品 | 1 | 0 |
| API-043 | product-service | `GET /api/my-products` | 我的商品列表 | 1 | 0 |
| API-044 | product-service | `GET /api/products` | 商品分页分类搜索推荐 | 2 | 0 |
| API-045 | product-service | `GET /api/products/{id}` | 商品详情 | 1 | 2 |
| API-046 | product-service | `POST /api/products` | 发布商品 | 75 | 7 |
| API-047 | product-service | `PUT /api/products/{id}` | 编辑商品或上下架 | 4 | 4 |
| API-048 | product-service | `DELETE /api/products/{productId}/images/{imageId}` | 删除商品图片 | 1 | 1 |
| API-049 | product-service | `GET /api/products/{productId}/images` | 商品图片列表 | 3 | 0 |
| API-050 | product-service | `POST /api/products/{productId}/images` | 上传商品图片 | 1 | 0 |
| API-051 | product-service | `PUT /api/products/{productId}/images/{imageId}/cover` | 设置商品封面 | 1 | 0 |
| API-052 | product-service | `POST /api/products/{productId}/report` | 举报商品 | 6 | 5 |
| API-053 | product-service | `GET /api/users/{sellerId}/products` | 卖家在售商品 | 1 | 0 |
| API-054 | trade-service | `GET /api/admin/after-sale` | 管理端售后分页筛选 | 1 | 0 |
| API-055 | trade-service | `GET /api/admin/after-sale/{id}` | 管理端售后详情 | 1 | 0 |
| API-056 | trade-service | `POST /api/admin/after-sale/process-timeouts` | 管理员触发售后超时处理 | 2 | 0 |
| API-057 | trade-service | `POST /api/admin/after-sale/{id}/arbitrate` | 责任运费及退款仲裁 | 5 | 2 |
| API-058 | trade-service | `GET /api/admin/orders` | 管理端订单分页 | 1 | 0 |
| API-059 | trade-service | `GET /api/admin/orders/{id}` | 管理端订单详情 | 1 | 0 |
| API-060 | trade-service | `POST /api/admin/orders/{id}/cancel` | 管理员取消订单 | 1 | 0 |
| API-061 | trade-service | `POST /api/admin/orders/{id}/mark-paid` | 管理员标记已支付 | 1 | 0 |
| API-062 | trade-service | `GET /api/after-sale/all` | 旧版管理员售后列表 | 1 | 1 |
| API-063 | trade-service | `GET /api/after-sale/by-order/{orderId}` | 订单售后记录 | 1 | 0 |
| API-064 | trade-service | `GET /api/after-sale/my-received` | 我收到的售后 | 1 | 0 |
| API-065 | trade-service | `GET /api/after-sale/my-requests` | 我申请的售后 | 1 | 0 |
| API-066 | trade-service | `GET /api/after-sale/{id}` | 售后单详情 | 2 | 1 |
| API-067 | trade-service | `POST /api/after-sale` | 买家发起售后 | 18 | 4 |
| API-068 | trade-service | `POST /api/after-sale/process-timeouts` | 触发售后超时处理 | 1 | 0 |
| API-069 | trade-service | `POST /api/after-sale/{id}/approve` | 卖家同意售后 | 5 | 2 |
| API-070 | trade-service | `POST /api/after-sale/{id}/arbitrate` | 旧版平台仲裁入口 | 1 | 0 |
| API-071 | trade-service | `POST /api/after-sale/{id}/buyer-evidence` | 买家补充举证 | 1 | 0 |
| API-072 | trade-service | `POST /api/after-sale/{id}/cancel` | 买家取消售后 | 1 | 0 |
| API-073 | trade-service | `POST /api/after-sale/{id}/confirm-return` | 卖家确认收到退货 | 3 | 1 |
| API-074 | trade-service | `POST /api/after-sale/{id}/escalate` | 买家申请平台介入 | 7 | 1 |
| API-075 | trade-service | `POST /api/after-sale/{id}/reject` | 卖家拒绝售后 | 6 | 0 |
| API-076 | trade-service | `POST /api/after-sale/{id}/reject-return` | 卖家拒收退货 | 1 | 0 |
| API-077 | trade-service | `POST /api/after-sale/{id}/return-ship` | 买家填写退货运单 | 4 | 1 |
| API-078 | trade-service | `POST /api/after-sale/{id}/seller-evidence` | 卖家提交举证 | 1 | 0 |
| API-079 | trade-service | `GET /api/shipments/{orderId}/track` | 查询订单物流轨迹 | 1 | 2 |
| API-080 | trade-service | `GET /api/my-offers` | 我发出的报价 | 1 | 0 |
| API-081 | trade-service | `GET /api/products/{productId}/offers` | 卖家查看商品报价 | 1 | 0 |
| API-082 | trade-service | `GET /api/seller-offers` | 我收到的报价 | 1 | 0 |
| API-083 | trade-service | `POST /api/offers/{id}/accept` | 接受报价并创建订单 | 5 | 4 |
| API-084 | trade-service | `POST /api/offers/{id}/cancel` | 撤回报价 | 2 | 1 |
| API-085 | trade-service | `POST /api/offers/{id}/reject` | 拒绝报价 | 2 | 1 |
| API-086 | trade-service | `POST /api/products/{productId}/offers` | 发起议价 | 11 | 4 |
| API-087 | trade-service | `GET /api/orders/bought` | 我买到的订单 | 1 | 0 |
| API-088 | trade-service | `GET /api/orders/sold` | 我卖出的订单 | 1 | 0 |
| API-089 | trade-service | `GET /api/orders/{id}` | 订单详情及可用操作 | 3 | 1 |
| API-090 | trade-service | `POST /api/orders` | 按商品原价创建订单 | 45 | 6 |
| API-091 | trade-service | `POST /api/orders/process-settlements` | 触发到期结算 | 1 | 0 |
| API-092 | trade-service | `POST /api/orders/{id}/cancel` | 买家取消订单 | 2 | 2 |
| API-093 | trade-service | `POST /api/orders/{id}/confirm` | 买家确认收货 | 21 | 1 |
| API-094 | trade-service | `POST /api/orders/{id}/mark-paid` | 旧版支付兼容入口 | 1 | 0 |
| API-095 | trade-service | `POST /api/orders/{id}/pay` | 支付订单 | 31 | 4 |
| API-096 | trade-service | `POST /api/orders/{id}/ship` | 卖家发货 | 23 | 5 |
| API-097 | trade-service | `PUT /api/orders/{id}/receiver` | 补填或更新收货信息 | 3 | 0 |
| API-098 | trade-service | `GET /api/payments/{paymentNo}` | 查询支付状态 | 2 | 0 |
| API-099 | trade-service | `POST /api/payments` | 创建模拟支付单 | 1 | 0 |
| API-100 | trade-service | `POST /api/payments/{paymentNo}/mock-pay` | 开发测试模拟支付成功 | 2 | 1 |
| API-101 | trade-service | `GET /api/orders/{orderId}/rating` | 查询订单评价 | 1 | 0 |
| API-102 | trade-service | `GET /api/users/{userId}/rating` | 卖家评分摘要 | 1 | 0 |
| API-103 | trade-service | `POST /api/orders/{orderId}/rate` | 对已完成交易评分 | 1 | 0 |
| API-104 | trade-service | `GET /api/users/{sellerId}/sold` | 卖家已售订单及评价 | 1 | 0 |

## 测试用例逐项明细

编号MS-xxx对应本轮原始XML里的独立testcase。参数化测试的每个参数组分别统计；同方法的HTTP步骤合并展示其所有参数组。接口步骤中的状态码是实测结果，期望与业务断言来自测试代码。
所有账号、商品和订单均为测试创建的数据；实际请求体和完整数据库断言可点击代码链接查看。登录凭证、密码和图片正文不写入HTTP证据。

### MS-001 错误密码与缺失令牌分别返回401

- 测试标识：`com.secondhand.micro.user.UserApiIT#wrongPasswordAndMissingTokenAreRejected`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.455 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/user-service/src/test/java/com/secondhand/micro/user/UserApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(401,Http.call(url,"POST","/api/auth/login",Map.of("identityType","EMAIL","identifier","wrong@example.com","password","wrongpass"),null,null).status());
assertEquals(401,Http.call(url,"GET","/api/auth/me",null,null,null).status());
```

### MS-002 通过真实 HTTP 和 MySQL 验证身份签发及当前用户查询。

- 测试标识：`com.secondhand.micro.user.UserApiIT#registerLoginAndMe`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.18 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/user-service/src/test/java/com/secondhand/micro/user/UserApiIT.java)。

**操作步骤与接口实测：**

1. 通过真实 HTTP 和 MySQL 验证身份签发及当前用户查询。

**预期结果与关键业务断言（从本例源码提取）：**

```java
var registered=Http.call(url,"POST","/api/auth/register",credentials("one@example.com"),null,null);assertEquals(201,registered.status());
var login=Http.call(url,"POST","/api/auth/login",credentials("one@example.com"),null,null);assertEquals(200,login.status());
var me=Http.call(url,"GET","/api/auth/me",null,login.data().path("accessToken").asText(),null);assertEquals(registered.data().path("userId"),me.data().path("userId"));
```

### MS-003 非法邮箱参数400及未认证内部请求401，无权限绕过

- 测试标识：`com.secondhand.micro.user.UserApiIT#invalidInputAndUnauthenticatedInternalRequestAreRejected`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.01 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/user-service/src/test/java/com/secondhand/micro/user/UserApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(400,Http.call(url,"POST","/api/auth/register",credentials("not-an-email"),null,null).status());
assertEquals(401,Http.call(url,"POST","/internal/v1/users/batch",Map.of("userIds",java.util.List.of(1)),null,null).status());
```

### MS-004 前20次无效登录返回401，第21次请求返回429及RATE_LIMITED

- 测试标识：`com.secondhand.micro.user.UserApiIT#loginRateLimitRejectsExcessRequests`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：1.018 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/user-service/src/test/java/com/secondhand/micro/user/UserApiIT.java)。

**操作步骤与接口实测：**

1. 独立实例启用默认安全限制，避免批量业务数据准备消耗此窗口。

**预期结果与关键业务断言（从本例源码提取）：**

```java
for(int i=0;i<20;i++)assertEquals(401,Http.call(endpoint,"POST","/api/auth/login",credentials("missing@example.com"),null,null).status());
assertEquals(429,rejected.status());assertEquals("RATE_LIMITED",rejected.body().path("error").path("code").asText());
```

### MS-005 持久化令牌版本更新后，旧凭证内省结果失效

- 测试标识：`com.secondhand.micro.user.UserApiIT#revokedVersionInvalidatesPreviouslyIssuedToken`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.123 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/user-service/src/test/java/com/secondhand/micro/user/UserApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(200,introspect.status());assertFalse(introspect.data().path("active").asBoolean());
```

### MS-006 重复邮箱注册409，用户行数保持不变

- 测试标识：`com.secondhand.micro.user.UserApiIT#duplicateRegistrationRollsBackUser`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.099 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/user-service/src/test/java/com/secondhand/micro/user/UserApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(409,Http.call(url,"POST","/api/auth/register",credentials("duplicate@example.com"),null,null).status());
assertEquals(before,env.db("user").queryForObject("SELECT COUNT(*) FROM users",Long.class));
```

### MS-007 多个线程同时重放同一业务键，全部成功且库存只扣一次。

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#concurrentDuplicateReservationsAllReturnSameResult`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.463 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 多个线程同时重放同一业务键，全部成功且库存只扣一次。

**预期结果与关键业务断言（从本例源码提取）：**

```java
for(var future:executor.invokeAll(work)){var result=future.get();assertEquals(200,result.status());assertEquals(key,result.data().path("reservationId").asText());}
assertEquals(1,stock(id));assertEquals(1,env.db("product").queryForObject("SELECT COUNT(*) FROM inventory_reservations WHERE operation_id=?",Integer.class,key));
```

### MS-008 多买家并发抢购最后一件商品，库存不超卖

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#concurrentBuyersCannotOversell`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.155 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
for(var result:results){int status=result.get();assertTrue(status==200||status==409);if(status==200)successes++;}
assertEquals(1,successes);assertEquals(0,stock(id));
```

### MS-009 同一订单不能绑定两个不同库存操作

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#secondOperationCannotClaimSameOrder`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.085 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(409,call("POST","",reserve("second-"+id,id,id),"second-"+id).status());assertEquals(1,stock(id));
```

### MS-010 库存预占校验卖家、数量及请求绑定关系

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#bindingAndQuantityAreValidated`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.115 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(409,call("POST","/"+key+"/release",Map.of("orderId",id+999),null).status());
assertEquals(400,call("POST","",invalid,"invalid-"+id).status());assertEquals(0,stock(id));
```

### MS-011 同一幂等键携带不同内容返回冲突

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#sameKeyWithDifferentPayloadIsConflict`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.071 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(409,call("POST","",altered,key).status());assertEquals(1,stock(id));
```

### MS-012 内部库存接口拒绝无凭证、错误服务及错误受众

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#internalApiRequiresCorrectServiceAndAudience`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.018 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(401,Http.call(url,"POST","/internal/v1/inventory/reservations",payload,null,"denied").status());
assertEquals(403,Http.call(url,"POST","/internal/v1/inventory/reservations",payload,env.tokens("user").serviceToken("product-service"),"denied").status());
assertEquals(401,Http.call(url,"POST","/internal/v1/inventory/reservations",payload,env.tokens("trade").serviceToken("user-service"),"denied").status());
```

### MS-013 重复预占和释放请求仅改变一次库存

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#duplicateReserveAndReleaseChangeStockOnlyOnce`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.146 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(200,call("POST","",payload,key).status());assertEquals(200,call("POST","",payload,key).status());assertEquals(1,stock(id));
assertEquals(200,call("POST","/"+key+"/confirm",Map.of("orderId",id),null).status());assertEquals(1,stock(id));
call("POST","/"+key+"/release",Map.of("orderId",id),null);call("POST","/"+key+"/release",Map.of("orderId",id),null);assertEquals(2,stock(id));
```

### MS-014 库存补偿不撤销管理员审核下架

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#releaseDoesNotUndoAdministrativeOffShelf`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.108 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(1,stock(id));assertEquals("OFF_SALE",env.db("product").queryForObject("SELECT status FROM products WHERE id=?",String.class,id));
```

### MS-015 模拟取消先到、预占请求后到的网络乱序。

- 测试标识：`com.secondhand.micro.product.InventoryApiIT#releaseBeforeReserveLeavesTerminalTombstone`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.062 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/product-service/src/test/java/com/secondhand/micro/product/InventoryApiIT.java)。

**操作步骤与接口实测：**

1. 模拟取消先到、预占请求后到的网络乱序。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(200,call("POST","/"+key+"/release",Map.of("orderId",id),null).status());
assertEquals(409,call("POST","",reserve(key,id,id),key).status());assertEquals(1,stock(id));
```

### MS-016 库存核对接口只允许商品服务身份

- 测试标识：`com.secondhand.micro.trade.TradeApiIT#internalOrderStateRequiresProductService`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.514 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/trade-service/src/test/java/com/secondhand/micro/trade/TradeApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(401,Http.call(url,"GET","/internal/v1/orders/1/inventory-state",null,null,null).status());
assertEquals(403,Http.call(url,"GET","/internal/v1/orders/1/inventory-state",null,env.tokens("user").serviceToken("trade-service"),null).status());
```

### MS-017 匿名或畸形下单在产生业务副作用前被拒绝

- 测试标识：`com.secondhand.micro.trade.TradeApiIT#anonymousMalformedOrderIsRejectedBeforeSideEffects`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.018 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/trade-service/src/test/java/com/secondhand/micro/trade/TradeApiIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(401,Http.call(url,"POST","/api/orders",Map.of("productId",1,"receiverName","","receiverPhone","","receiverAddress",""),null,"invalid").status());}
```

### MS-018 不连接用户服务也不能放行匿名写入。

- 测试标识：`com.secondhand.micro.trade.TradeApiIT#anonymousOrderCreationIsRejected`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.029 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../services/trade-service/src/test/java/com/secondhand/micro/trade/TradeApiIT.java)。

**操作步骤与接口实测：**

1. 不连接用户服务也不能放行匿名写入。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(401,Http.call(url,"POST","/api/orders",Map.of("productId",1,"receiverName","测试","receiverPhone","13800000000","receiverAddress","测试地址"),null,"anonymous").status());
assertEquals(0,env.db("trade").queryForObject("SELECT COUNT(*) FROM orders",Integer.class));
```

### MS-019 6.4～6.7：分别验证四种仲裁结果、责任及运费落库，并完成裁定退货后的退款流程。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#adminArbitrationAlternativesPersistDecisionAndRelatedOrder(String, String, String, int)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：41.19 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/1/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/1/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/1/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家拒绝售后：`POST /api/after-sale/1/reject` → HTTP 200。
9. 买家申请平台介入：`POST /api/after-sale/1/escalate` → HTTP 200。
10. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
11. 责任运费及退款仲裁：`POST /api/admin/after-sale/1/arbitrate` → HTTP 200。
12. 用户注册：`POST /api/auth/register` → HTTP 201。
13. 发布商品：`POST /api/products` → HTTP 200。
14. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
15. 支付订单：`POST /api/orders/2/pay` → HTTP 200。
16. 卖家发货：`POST /api/orders/2/ship` → HTTP 200。
17. 买家确认收货：`POST /api/orders/2/confirm` → HTTP 200。
18. 买家发起售后：`POST /api/after-sale` → HTTP 200。
19. 卖家拒绝售后：`POST /api/after-sale/2/reject` → HTTP 200。
20. 买家申请平台介入：`POST /api/after-sale/2/escalate` → HTTP 200。
21. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
22. 责任运费及退款仲裁：`POST /api/admin/after-sale/2/arbitrate` → HTTP 200。
23. 用户注册：`POST /api/auth/register` → HTTP 201。
24. 发布商品：`POST /api/products` → HTTP 200。
25. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
26. 支付订单：`POST /api/orders/3/pay` → HTTP 200。
27. 卖家发货：`POST /api/orders/3/ship` → HTTP 200。
28. 买家确认收货：`POST /api/orders/3/confirm` → HTTP 200。
29. 买家发起售后：`POST /api/after-sale` → HTTP 200。
30. 卖家拒绝售后：`POST /api/after-sale/3/reject` → HTTP 200。
31. 买家申请平台介入：`POST /api/after-sale/3/escalate` → HTTP 200。
32. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
33. 责任运费及退款仲裁：`POST /api/admin/after-sale/3/arbitrate` → HTTP 200。
34. 用户注册：`POST /api/auth/register` → HTTP 201。
35. 发布商品：`POST /api/products` → HTTP 200。
36. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
37. 支付订单：`POST /api/orders/4/pay` → HTTP 200。
38. 卖家发货：`POST /api/orders/4/ship` → HTTP 200。
39. 买家确认收货：`POST /api/orders/4/confirm` → HTTP 200。
40. 买家发起售后：`POST /api/after-sale` → HTTP 200。
41. 卖家拒绝售后：`POST /api/after-sale/4/reject` → HTTP 200。
42. 买家申请平台介入：`POST /api/after-sale/4/escalate` → HTTP 200。
43. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
44. 责任运费及退款仲裁：`POST /api/admin/after-sale/4/arbitrate` → HTTP 200。
45. 买家填写退货运单：`POST /api/after-sale/4/return-ship` → HTTP 200。
46. 卖家确认收到退货：`POST /api/after-sale/4/confirm-return` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
state(id, requestState); orderStatus(t.orderId(), orderState);
assertThat(count("select count(*) from after_sale_requests where id=? and refund_amount_cent=? and responsibility='SELLER' and shipping_paid_by='SELLER' and shipping_cost_cent=100", id, refund)).isEqualTo(1);
state(id, "REFUNDED"); orderStatus(t.orderId(), "CANCELLED");
```

### MS-020 6.4～6.7：分别验证四种仲裁结果、责任及运费落库，并完成裁定退货后的退款流程。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#adminArbitrationAlternativesPersistDecisionAndRelatedOrder(String, String, String, int)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.754 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/1/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/1/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/1/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家拒绝售后：`POST /api/after-sale/1/reject` → HTTP 200。
9. 买家申请平台介入：`POST /api/after-sale/1/escalate` → HTTP 200。
10. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
11. 责任运费及退款仲裁：`POST /api/admin/after-sale/1/arbitrate` → HTTP 200。
12. 用户注册：`POST /api/auth/register` → HTTP 201。
13. 发布商品：`POST /api/products` → HTTP 200。
14. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
15. 支付订单：`POST /api/orders/2/pay` → HTTP 200。
16. 卖家发货：`POST /api/orders/2/ship` → HTTP 200。
17. 买家确认收货：`POST /api/orders/2/confirm` → HTTP 200。
18. 买家发起售后：`POST /api/after-sale` → HTTP 200。
19. 卖家拒绝售后：`POST /api/after-sale/2/reject` → HTTP 200。
20. 买家申请平台介入：`POST /api/after-sale/2/escalate` → HTTP 200。
21. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
22. 责任运费及退款仲裁：`POST /api/admin/after-sale/2/arbitrate` → HTTP 200。
23. 用户注册：`POST /api/auth/register` → HTTP 201。
24. 发布商品：`POST /api/products` → HTTP 200。
25. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
26. 支付订单：`POST /api/orders/3/pay` → HTTP 200。
27. 卖家发货：`POST /api/orders/3/ship` → HTTP 200。
28. 买家确认收货：`POST /api/orders/3/confirm` → HTTP 200。
29. 买家发起售后：`POST /api/after-sale` → HTTP 200。
30. 卖家拒绝售后：`POST /api/after-sale/3/reject` → HTTP 200。
31. 买家申请平台介入：`POST /api/after-sale/3/escalate` → HTTP 200。
32. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
33. 责任运费及退款仲裁：`POST /api/admin/after-sale/3/arbitrate` → HTTP 200。
34. 用户注册：`POST /api/auth/register` → HTTP 201。
35. 发布商品：`POST /api/products` → HTTP 200。
36. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
37. 支付订单：`POST /api/orders/4/pay` → HTTP 200。
38. 卖家发货：`POST /api/orders/4/ship` → HTTP 200。
39. 买家确认收货：`POST /api/orders/4/confirm` → HTTP 200。
40. 买家发起售后：`POST /api/after-sale` → HTTP 200。
41. 卖家拒绝售后：`POST /api/after-sale/4/reject` → HTTP 200。
42. 买家申请平台介入：`POST /api/after-sale/4/escalate` → HTTP 200。
43. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
44. 责任运费及退款仲裁：`POST /api/admin/after-sale/4/arbitrate` → HTTP 200。
45. 买家填写退货运单：`POST /api/after-sale/4/return-ship` → HTTP 200。
46. 卖家确认收到退货：`POST /api/after-sale/4/confirm-return` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
state(id, requestState); orderStatus(t.orderId(), orderState);
assertThat(count("select count(*) from after_sale_requests where id=? and refund_amount_cent=? and responsibility='SELLER' and shipping_paid_by='SELLER' and shipping_cost_cent=100", id, refund)).isEqualTo(1);
state(id, "REFUNDED"); orderStatus(t.orderId(), "CANCELLED");
```

### MS-021 6.4～6.7：分别验证四种仲裁结果、责任及运费落库，并完成裁定退货后的退款流程。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#adminArbitrationAlternativesPersistDecisionAndRelatedOrder(String, String, String, int)[3]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.706 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/1/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/1/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/1/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家拒绝售后：`POST /api/after-sale/1/reject` → HTTP 200。
9. 买家申请平台介入：`POST /api/after-sale/1/escalate` → HTTP 200。
10. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
11. 责任运费及退款仲裁：`POST /api/admin/after-sale/1/arbitrate` → HTTP 200。
12. 用户注册：`POST /api/auth/register` → HTTP 201。
13. 发布商品：`POST /api/products` → HTTP 200。
14. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
15. 支付订单：`POST /api/orders/2/pay` → HTTP 200。
16. 卖家发货：`POST /api/orders/2/ship` → HTTP 200。
17. 买家确认收货：`POST /api/orders/2/confirm` → HTTP 200。
18. 买家发起售后：`POST /api/after-sale` → HTTP 200。
19. 卖家拒绝售后：`POST /api/after-sale/2/reject` → HTTP 200。
20. 买家申请平台介入：`POST /api/after-sale/2/escalate` → HTTP 200。
21. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
22. 责任运费及退款仲裁：`POST /api/admin/after-sale/2/arbitrate` → HTTP 200。
23. 用户注册：`POST /api/auth/register` → HTTP 201。
24. 发布商品：`POST /api/products` → HTTP 200。
25. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
26. 支付订单：`POST /api/orders/3/pay` → HTTP 200。
27. 卖家发货：`POST /api/orders/3/ship` → HTTP 200。
28. 买家确认收货：`POST /api/orders/3/confirm` → HTTP 200。
29. 买家发起售后：`POST /api/after-sale` → HTTP 200。
30. 卖家拒绝售后：`POST /api/after-sale/3/reject` → HTTP 200。
31. 买家申请平台介入：`POST /api/after-sale/3/escalate` → HTTP 200。
32. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
33. 责任运费及退款仲裁：`POST /api/admin/after-sale/3/arbitrate` → HTTP 200。
34. 用户注册：`POST /api/auth/register` → HTTP 201。
35. 发布商品：`POST /api/products` → HTTP 200。
36. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
37. 支付订单：`POST /api/orders/4/pay` → HTTP 200。
38. 卖家发货：`POST /api/orders/4/ship` → HTTP 200。
39. 买家确认收货：`POST /api/orders/4/confirm` → HTTP 200。
40. 买家发起售后：`POST /api/after-sale` → HTTP 200。
41. 卖家拒绝售后：`POST /api/after-sale/4/reject` → HTTP 200。
42. 买家申请平台介入：`POST /api/after-sale/4/escalate` → HTTP 200。
43. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
44. 责任运费及退款仲裁：`POST /api/admin/after-sale/4/arbitrate` → HTTP 200。
45. 买家填写退货运单：`POST /api/after-sale/4/return-ship` → HTTP 200。
46. 卖家确认收到退货：`POST /api/after-sale/4/confirm-return` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
state(id, requestState); orderStatus(t.orderId(), orderState);
assertThat(count("select count(*) from after_sale_requests where id=? and refund_amount_cent=? and responsibility='SELLER' and shipping_paid_by='SELLER' and shipping_cost_cent=100", id, refund)).isEqualTo(1);
state(id, "REFUNDED"); orderStatus(t.orderId(), "CANCELLED");
```

### MS-022 6.4～6.7：分别验证四种仲裁结果、责任及运费落库，并完成裁定退货后的退款流程。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#adminArbitrationAlternativesPersistDecisionAndRelatedOrder(String, String, String, int)[4]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.8 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/1/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/1/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/1/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家拒绝售后：`POST /api/after-sale/1/reject` → HTTP 200。
9. 买家申请平台介入：`POST /api/after-sale/1/escalate` → HTTP 200。
10. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
11. 责任运费及退款仲裁：`POST /api/admin/after-sale/1/arbitrate` → HTTP 200。
12. 用户注册：`POST /api/auth/register` → HTTP 201。
13. 发布商品：`POST /api/products` → HTTP 200。
14. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
15. 支付订单：`POST /api/orders/2/pay` → HTTP 200。
16. 卖家发货：`POST /api/orders/2/ship` → HTTP 200。
17. 买家确认收货：`POST /api/orders/2/confirm` → HTTP 200。
18. 买家发起售后：`POST /api/after-sale` → HTTP 200。
19. 卖家拒绝售后：`POST /api/after-sale/2/reject` → HTTP 200。
20. 买家申请平台介入：`POST /api/after-sale/2/escalate` → HTTP 200。
21. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
22. 责任运费及退款仲裁：`POST /api/admin/after-sale/2/arbitrate` → HTTP 200。
23. 用户注册：`POST /api/auth/register` → HTTP 201。
24. 发布商品：`POST /api/products` → HTTP 200。
25. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
26. 支付订单：`POST /api/orders/3/pay` → HTTP 200。
27. 卖家发货：`POST /api/orders/3/ship` → HTTP 200。
28. 买家确认收货：`POST /api/orders/3/confirm` → HTTP 200。
29. 买家发起售后：`POST /api/after-sale` → HTTP 200。
30. 卖家拒绝售后：`POST /api/after-sale/3/reject` → HTTP 200。
31. 买家申请平台介入：`POST /api/after-sale/3/escalate` → HTTP 200。
32. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
33. 责任运费及退款仲裁：`POST /api/admin/after-sale/3/arbitrate` → HTTP 200。
34. 用户注册：`POST /api/auth/register` → HTTP 201。
35. 发布商品：`POST /api/products` → HTTP 200。
36. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
37. 支付订单：`POST /api/orders/4/pay` → HTTP 200。
38. 卖家发货：`POST /api/orders/4/ship` → HTTP 200。
39. 买家确认收货：`POST /api/orders/4/confirm` → HTTP 200。
40. 买家发起售后：`POST /api/after-sale` → HTTP 200。
41. 卖家拒绝售后：`POST /api/after-sale/4/reject` → HTTP 200。
42. 买家申请平台介入：`POST /api/after-sale/4/escalate` → HTTP 200。
43. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
44. 责任运费及退款仲裁：`POST /api/admin/after-sale/4/arbitrate` → HTTP 200。
45. 买家填写退货运单：`POST /api/after-sale/4/return-ship` → HTTP 200。
46. 卖家确认收到退货：`POST /api/after-sale/4/confirm-return` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
state(id, requestState); orderStatus(t.orderId(), orderState);
assertThat(count("select count(*) from after_sale_requests where id=? and refund_amount_cent=? and responsibility='SELLER' and shipping_paid_by='SELLER' and shipping_cost_cent=100", id, refund)).isEqualTo(1);
state(id, "REFUNDED"); orderStatus(t.orderId(), "CANCELLED");
```

### MS-023 6.11：无关用户不能申请、查看或审批他人的售后。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#unrelatedUserCannotApplyReadOrApprove`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.804 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/5/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/5/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/5/confirm` → HTTP 200。
7. 用户注册：`POST /api/auth/register` → HTTP 201。
8. 买家发起售后：`POST /api/after-sale` → HTTP 403。
9. 买家发起售后：`POST /api/after-sale` → HTTP 200。
10. 售后单详情：`GET /api/after-sale/5` → HTTP 403。
11. 卖家同意售后：`POST /api/after-sale/5/approve` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/after-sale", application(t, "REFUND_RECEIVED", 1000), other, 403, "FORBIDDEN");
error("GET", "/api/after-sale/" + id, null, other, 403, "FORBIDDEN");
error("POST", "/api/after-sale/" + id + "/approve", null, other, 403, "FORBIDDEN");
state(id, "REQUESTED"); orderStatus(t.orderId(), "AFTER_SALE");
```

### MS-024 6.8：买家取消售后后恢复原订单，卖家不能继续审批已关闭的申请。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#buyerCancellationClosesRequestAndRestoresOrder`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.564 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/6/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/6/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/6/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 买家取消售后：`POST /api/after-sale/6/cancel` → HTTP 200。
9. 卖家同意售后：`POST /api/after-sale/6/approve` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
ok("POST", "/api/after-sale/" + id + "/cancel", null, t.buyer()); state(id, "CLOSED"); orderStatus(t.orderId(), "COMPLETED");
error("POST", "/api/after-sale/" + id + "/approve", null, t.seller(), 409, "CONFLICT");
orderStatus(t.orderId(), "COMPLETED");
```

### MS-025 6.10：分别验证未确认收货和超过 7 天窗口的申请失败，订单不变。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#unconfirmedAndExpiredOrdersRejectAfterSaleWithoutMutation`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.882 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 买家发起售后：`POST /api/after-sale` → HTTP 403。
5. 用户注册：`POST /api/auth/register` → HTTP 201。
6. 发布商品：`POST /api/products` → HTTP 200。
7. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
8. 支付订单：`POST /api/orders/8/pay` → HTTP 200。
9. 卖家发货：`POST /api/orders/8/ship` → HTTP 200。
10. 买家确认收货：`POST /api/orders/8/confirm` → HTTP 200。
11. 买家发起售后：`POST /api/after-sale` → HTTP 410。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/after-sale", application(unpaid, "REFUND_RECEIVED", 1000), unpaid.buyer(), 403, "FORBIDDEN");
orderStatus(unpaid.orderId(), "WAIT_PAY");
assertThat(count("select count(*) from after_sale_requests where order_id=?", unpaid.orderId())).isZero();
error("POST", "/api/after-sale", application(expired, "REFUND_RECEIVED", 1000), expired.buyer(), 410, "CLOSED");
orderStatus(expired.orderId(), "COMPLETED");
assertThat(count("select count(*) from after_sale_requests where order_id=?", expired.orderId())).isZero();
```

### MS-026 6.15：验证管理员权限，并检查无效裁决是否回滚已修改的责任和裁决字段。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#nonAdminCannotArbitrateAndInvalidDecisionRollsBack`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.7 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/9/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/9/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/9/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家拒绝售后：`POST /api/after-sale/7/reject` → HTTP 200。
9. 买家申请平台介入：`POST /api/after-sale/7/escalate` → HTTP 200。
10. 责任运费及退款仲裁：`POST /api/admin/after-sale/7/arbitrate` → HTTP 403。
11. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
12. 责任运费及退款仲裁：`POST /api/admin/after-sale/7/arbitrate` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(call("POST", "/api/admin/after-sale/" + id + "/arbitrate", Map.of("result", "FULL_REFUND"), t.buyer())
error("POST", "/api/admin/after-sale/" + id + "/arbitrate", Map.of("result", "INVALID", "responsibility", "BUYER"), admin(), 400, "BAD_REQUEST");
state(id, "PLATFORM_ARBITRATION"); orderStatus(t.orderId(), "AFTER_SALE");
assertThat(count("select count(*) from after_sale_requests where id=? and responsibility is null and arbitration_result is null", id)).isEqualTo(1);
```

### MS-027 6.16：将处理截止时间设为过去，验证超时任务自动同意退货而非立即退款。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#sellerTimeoutAutomaticallyApprovesReturn`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.615 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/10/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/10/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/10/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
9. 管理员触发售后超时处理：`POST /api/admin/after-sale/process-timeouts` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
state(id, "APPROVED"); orderStatus(t.orderId(), "AFTER_SALE");
```

### MS-028 6.2～6.3：全额退款关闭订单，部分退款则保留订单完成状态。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#directRefundAlternativeUpdatesOrderAccordingToAmount(String, int, String)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.519 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/11/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/11/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/11/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家同意售后：`POST /api/after-sale/9/approve` → HTTP 200。
9. 用户注册：`POST /api/auth/register` → HTTP 201。
10. 发布商品：`POST /api/products` → HTTP 200。
11. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
12. 支付订单：`POST /api/orders/12/pay` → HTTP 200。
13. 卖家发货：`POST /api/orders/12/ship` → HTTP 200。
14. 买家确认收货：`POST /api/orders/12/confirm` → HTTP 200。
15. 买家发起售后：`POST /api/after-sale` → HTTP 200。
16. 卖家同意售后：`POST /api/after-sale/10/approve` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
state(id, "REFUNDED"); orderStatus(t.orderId(), orderState);
assertThat(databaseFor("select refund_amount_cent from after_sale_requests where id=?").queryForObject("select refund_amount_cent from after_sale_requests where id=?", Integer.class, id)).isEqualTo(amount);
```

### MS-029 6.2～6.3：全额退款关闭订单，部分退款则保留订单完成状态。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#directRefundAlternativeUpdatesOrderAccordingToAmount(String, int, String)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.718 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/11/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/11/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/11/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家同意售后：`POST /api/after-sale/9/approve` → HTTP 200。
9. 用户注册：`POST /api/auth/register` → HTTP 201。
10. 发布商品：`POST /api/products` → HTTP 200。
11. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
12. 支付订单：`POST /api/orders/12/pay` → HTTP 200。
13. 卖家发货：`POST /api/orders/12/ship` → HTTP 200。
14. 买家确认收货：`POST /api/orders/12/confirm` → HTTP 200。
15. 买家发起售后：`POST /api/after-sale` → HTTP 200。
16. 卖家同意售后：`POST /api/after-sale/10/approve` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
state(id, "REFUNDED"); orderStatus(t.orderId(), orderState);
assertThat(databaseFor("select refund_amount_cent from after_sale_requests where id=?").queryForObject("select refund_amount_cent from after_sale_requests where id=?", Integer.class, id)).isEqualTo(amount);
```

### MS-030 6.9：重复申请不能生成第二张售后单，也不能改写原申请状态。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#duplicateApplicationDoesNotCreateSecondRequest`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.537 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/13/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/13/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/13/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 买家发起售后：`POST /api/after-sale` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/after-sale", application(t, "RETURN_REFUND", 1000), t.buyer(), 409, "CONFLICT");
assertThat(count("select count(*) from after_sale_requests where order_id=?", t.orderId())).isEqualTo(1);
state(id, "REQUESTED"); orderStatus(t.orderId(), "AFTER_SALE");
```

### MS-031 6.12～6.14：在未审批阶段尝试介入、寄回或确认退货，均不得推进状态。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#illegalStageDoesNotChangeRequestOrOrder(String)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.511 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/14/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/14/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/14/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 买家申请平台介入：`POST /api/after-sale/12/escalate` → HTTP 409。
9. 用户注册：`POST /api/auth/register` → HTTP 201。
10. 发布商品：`POST /api/products` → HTTP 200。
11. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
12. 支付订单：`POST /api/orders/15/pay` → HTTP 200。
13. 卖家发货：`POST /api/orders/15/ship` → HTTP 200。
14. 买家确认收货：`POST /api/orders/15/confirm` → HTTP 200。
15. 买家发起售后：`POST /api/after-sale` → HTTP 200。
16. 买家填写退货运单：`POST /api/after-sale/13/return-ship` → HTTP 409。
17. 用户注册：`POST /api/auth/register` → HTTP 201。
18. 发布商品：`POST /api/products` → HTTP 200。
19. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
20. 支付订单：`POST /api/orders/16/pay` → HTTP 200。
21. 卖家发货：`POST /api/orders/16/ship` → HTTP 200。
22. 买家确认收货：`POST /api/orders/16/confirm` → HTTP 200。
23. 买家发起售后：`POST /api/after-sale` → HTTP 200。
24. 卖家确认收到退货：`POST /api/after-sale/14/confirm-return` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/after-sale/" + id + "/" + action, shipping(t.orderId()),
state(id, "REQUESTED"); orderStatus(t.orderId(), "AFTER_SALE");
```

### MS-032 6.12～6.14：在未审批阶段尝试介入、寄回或确认退货，均不得推进状态。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#illegalStageDoesNotChangeRequestOrOrder(String)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.522 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/14/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/14/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/14/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 买家申请平台介入：`POST /api/after-sale/12/escalate` → HTTP 409。
9. 用户注册：`POST /api/auth/register` → HTTP 201。
10. 发布商品：`POST /api/products` → HTTP 200。
11. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
12. 支付订单：`POST /api/orders/15/pay` → HTTP 200。
13. 卖家发货：`POST /api/orders/15/ship` → HTTP 200。
14. 买家确认收货：`POST /api/orders/15/confirm` → HTTP 200。
15. 买家发起售后：`POST /api/after-sale` → HTTP 200。
16. 买家填写退货运单：`POST /api/after-sale/13/return-ship` → HTTP 409。
17. 用户注册：`POST /api/auth/register` → HTTP 201。
18. 发布商品：`POST /api/products` → HTTP 200。
19. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
20. 支付订单：`POST /api/orders/16/pay` → HTTP 200。
21. 卖家发货：`POST /api/orders/16/ship` → HTTP 200。
22. 买家确认收货：`POST /api/orders/16/confirm` → HTTP 200。
23. 买家发起售后：`POST /api/after-sale` → HTTP 200。
24. 卖家确认收到退货：`POST /api/after-sale/14/confirm-return` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/after-sale/" + id + "/" + action, shipping(t.orderId()),
state(id, "REQUESTED"); orderStatus(t.orderId(), "AFTER_SALE");
```

### MS-033 6.12～6.14：在未审批阶段尝试介入、寄回或确认退货，均不得推进状态。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#illegalStageDoesNotChangeRequestOrOrder(String)[3]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.507 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/14/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/14/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/14/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 买家申请平台介入：`POST /api/after-sale/12/escalate` → HTTP 409。
9. 用户注册：`POST /api/auth/register` → HTTP 201。
10. 发布商品：`POST /api/products` → HTTP 200。
11. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
12. 支付订单：`POST /api/orders/15/pay` → HTTP 200。
13. 卖家发货：`POST /api/orders/15/ship` → HTTP 200。
14. 买家确认收货：`POST /api/orders/15/confirm` → HTTP 200。
15. 买家发起售后：`POST /api/after-sale` → HTTP 200。
16. 买家填写退货运单：`POST /api/after-sale/13/return-ship` → HTTP 409。
17. 用户注册：`POST /api/auth/register` → HTTP 201。
18. 发布商品：`POST /api/products` → HTTP 200。
19. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
20. 支付订单：`POST /api/orders/16/pay` → HTTP 200。
21. 卖家发货：`POST /api/orders/16/ship` → HTTP 200。
22. 买家确认收货：`POST /api/orders/16/confirm` → HTTP 200。
23. 买家发起售后：`POST /api/after-sale` → HTTP 200。
24. 卖家确认收到退货：`POST /api/after-sale/14/confirm-return` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/after-sale/" + id + "/" + action, shipping(t.orderId()),
state(id, "REQUESTED"); orderStatus(t.orderId(), "AFTER_SALE");
```

### MS-034 6.1：核对退货退款每个阶段的落库状态，全额退款后订单作废。

- 测试标识：`com.secondhand.aftersale.AfterSaleApiIT#returnRefundPersistsEveryStageAndCancelsOrder`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.724 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/aftersale/AfterSaleApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/17/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/17/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/17/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家同意售后：`POST /api/after-sale/15/approve` → HTTP 200。
9. 买家填写退货运单：`POST /api/after-sale/15/return-ship` → HTTP 200。
10. 卖家确认收到退货：`POST /api/after-sale/15/confirm-return` → HTTP 200。
11. 售后单详情：`GET /api/after-sale/15` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(databaseFor("select return_tracking_no from after_sale_requests where id=?").queryForObject("select return_tracking_no from after_sale_requests where id=?", String.class, id)).isEqualTo("RETURN-" + id);
state(id, "REFUNDED"); orderStatus(t.orderId(), "CANCELLED");
assertThat(count("select count(*) from after_sale_requests where id=? and refunded_at is not null", id)).isEqualTo(1);
assertThat(ok("GET", "/api/after-sale/" + id, null, t.buyer()).path("refundAmountCent").asInt()).isEqualTo(1000);
```

### MS-035 1.3：重复手机号注册应被拒绝，用户表和身份表都不能新增记录。

- 测试标识：`com.secondhand.auth.AuthFlowIT#duplicatePhoneDoesNotCreateExtraUserOrIdentity`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.114 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/auth/AuthFlowIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 用户注册：`POST /api/auth/register` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/auth/register", credentials("PHONE", u.identifier(), PASSWORD), null, 409, "IDENTITY_EXISTS");
assertThat(count("select count(*) from users")).isEqualTo(users);
assertThat(count("select count(*) from user_identities")).isEqualTo(identities);
```

### MS-036 1.6：错误密码和未知账号都不能登录，也不能改变现有账号状态。

- 测试标识：`com.secondhand.auth.AuthFlowIT#wrongPasswordAndUnknownIdentityCannotLogin`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.147 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/auth/AuthFlowIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 登录并签发令牌：`POST /api/auth/login` → HTTP 401。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/auth/login", credentials("PHONE", u.identifier(), "wrong-password"), null, 401, "INVALID_CREDENTIALS");
error("POST", "/api/auth/login", credentials("PHONE", nextPhone(), PASSWORD), null, 401, "INVALID_CREDENTIALS");
assertThat(count("select count(*) from users where id=? and status='ACTIVE'", u.id())).isEqualTo(1);
```

### MS-037 1.7：验证管理员禁用、重新启用账号后，数据库状态与登录结果一致。

- 测试标识：`com.secondhand.auth.AuthFlowIT#adminDisableAndEnableControlsLogin`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.359 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/auth/AuthFlowIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
3. 启用或禁用账号：`PUT /api/admin/users/39/disable?disabled=true` → HTTP 200。
4. 登录并签发令牌：`POST /api/auth/login` → HTTP 403。
5. 启用或禁用账号：`PUT /api/admin/users/39/disable?disabled=false` → HTTP 200。
6. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(count("select count(*) from users where id=? and status='DISABLED'", u.id())).isEqualTo(1);
error("POST", "/api/auth/login", credentials("PHONE", u.identifier(), PASSWORD), null, 403, "FORBIDDEN");
assertThat(ok("POST", "/api/auth/login", credentials("PHONE", u.identifier(), PASSWORD), null).path("userId").asLong()).isEqualTo(u.id());
```

### MS-038 1.2：验证邮箱归一化和重复注册回滚，避免产生孤立用户。

- 测试标识：`com.secondhand.auth.AuthFlowIT#emailAlternativeNormalizesAndRejectsDuplicateWithoutOrphanUser`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.17 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/auth/AuthFlowIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
3. 用户注册：`POST /api/auth/register` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(databaseFor("select email from users where id=?").queryForObject("select email from users where id=?", String.class, u.id())).isEqualTo(email.toLowerCase());
assertThat(ok("POST", "/api/auth/login", credentials("EMAIL", email.toLowerCase(), PASSWORD), null).path("userId").asLong()).isEqualTo(u.id());
error("POST", "/api/auth/register", credentials("EMAIL", email.toLowerCase(), PASSWORD), null, 409, "IDENTITY_EXISTS");
assertThat(count("select count(*) from users")).isEqualTo(users);
assertThat(count("select count(*) from user_identities")).isEqualTo(identities);
```

### MS-039 1.1：验证注册身份、密码哈希落库，以及登录令牌可访问当前用户接口。

- 测试标识：`com.secondhand.auth.AuthFlowIT#registerPersistsIdentityAndHashedPasswordThenLoginAuthorizesApi`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.208 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/auth/AuthFlowIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
3. 当前登录用户信息：`GET /api/auth/me` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(count("select count(*) from user_identities where user_id=? and identifier=?", u.id(), u.identifier())).isEqualTo(1);
assertThat(hash).isNotEqualTo(PASSWORD);
assertThat(passwords.matches(PASSWORD, hash)).isTrue();
assertThat(loggedIn.id()).isEqualTo(u.id());
assertThat(ok("GET", "/api/auth/me", null, loggedIn).path("userId").asLong()).isEqualTo(u.id());
```

### MS-040 1.4～1.5：分别验证短密码、非法身份类型在参数校验阶段被拦截。

- 测试标识：`com.secondhand.auth.AuthFlowIT#invalidRegistrationDoesNotWriteDatabase(String, String)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.045 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/auth/AuthFlowIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/auth/register", credentials(type, phone, password), null, 400, "VALIDATION_ERROR");
assertThat(count("select count(*) from users")).isEqualTo(users);
assertThat(count("select count(*) from user_identities where identifier=?", phone)).isZero();
```

### MS-041 1.4～1.5：分别验证短密码、非法身份类型在参数校验阶段被拦截。

- 测试标识：`com.secondhand.auth.AuthFlowIT#invalidRegistrationDoesNotWriteDatabase(String, String)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.023 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/auth/AuthFlowIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/auth/register", credentials(type, phone, password), null, 400, "VALIDATION_ERROR");
assertThat(count("select count(*) from users")).isEqualTo(users);
assertThat(count("select count(*) from user_identities where identifier=?", phone)).isZero();
```

### MS-042 1.8：验证匿名和非法 JWT 请求被安全过滤器拒绝，商品表无副作用。

- 测试标识：`com.secondhand.auth.AuthFlowIT#anonymousAndInvalidJwtCannotCreateProduct`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.024 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/auth/AuthFlowIT.java)。

**操作步骤与接口实测：**

1. 发布商品：`POST /api/products` → HTTP 401。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(call("POST", "/api/products", productBody(1), null).getResponse().getStatus()).isEqualTo(401);
assertThat(call("POST","/api/products",productBody(1),new Actor(0,"invalid-token","invalid")).getResponse().getStatus()).isEqualTo(401);
assertThat(count("select count(*) from products")).isEqualTo(products);
```

### MS-043 退货退款：申请→卖家同意→寄回→确认收货→退款

- 测试标识：`com.secondhand.e2e.AfterSaleE2eIT#afterSaleReturnRefundApproved`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.571 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/AfterSaleE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/18/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/18/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/18/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家同意售后：`POST /api/after-sale/16/approve` → HTTP 200。
9. 买家填写退货运单：`POST /api/after-sale/16/return-ship` → HTTP 200。
10. 卖家确认收到退货：`POST /api/after-sale/16/confirm-return` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals("REQUESTED", req.get("status").asText(), "申请后应为待审核");
assertEquals("APPROVED", data(post("/api/after-sale/" + rid + "/approve", null, seller))
assertEquals("RETURN_SHIPPED", data(post("/api/after-sale/" + rid + "/return-ship",
assertEquals("REFUNDED", data(post("/api/after-sale/" + rid + "/confirm-return", null, seller))
```

### MS-044 售后被拒→买家申请平台介入→管理员仲裁全额退款

- 测试标识：`com.secondhand.e2e.AfterSaleE2eIT#afterSaleArbitration`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.602 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/AfterSaleE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/19/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/19/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/19/confirm` → HTTP 200。
7. 买家发起售后：`POST /api/after-sale` → HTTP 200。
8. 卖家拒绝售后：`POST /api/after-sale/17/reject` → HTTP 200。
9. 买家申请平台介入：`POST /api/after-sale/17/escalate` → HTTP 200。
10. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
11. 责任运费及退款仲裁：`POST /api/admin/after-sale/17/arbitrate` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals("REJECTED", data(post("/api/after-sale/" + rid + "/reject",
assertEquals("PLATFORM_ARBITRATION", data(post("/api/after-sale/" + rid + "/escalate",
assertEquals("REFUNDED", arbitrated.get("status").asText(), "仲裁全额退款后应退款完成");
```

### MS-045 密码错误登录应返回 401 INVALID_CREDENTIALS

- 测试标识：`com.secondhand.e2e.AuthE2eIT#loginWrongPassword`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.134 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/AuthE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 登录并签发令牌：`POST /api/auth/login` → HTTP 401。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode(), "密码错误应返回 401");
assertEquals("INVALID_CREDENTIALS", errorCode(resp));
```

### MS-046 注册成功返回身份凭证（JWT）

- 测试标识：`com.secondhand.e2e.AuthE2eIT#registerSuccess`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.078 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/AuthE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.CREATED, resp.getStatusCode(), "注册应返回 201");
assertTrue(data.get("accessToken").asText().length() > 10, "注册成功应返回 JWT");
assertEquals("USER", data.get("role").asText(), "普通注册用户角色应为 USER");
```

### MS-047 重复注册应返回 409 IDENTITY_EXISTS

- 测试标识：`com.secondhand.e2e.AuthE2eIT#registerDuplicate`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.083 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/AuthE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 用户注册：`POST /api/auth/register` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.CONFLICT, resp.getStatusCode(), "重复注册应返回 409");
assertEquals("IDENTITY_EXISTS", errorCode(resp));
```

### MS-048 注册校验失败应返回 400 VALIDATION_ERROR

- 测试标识：`com.secondhand.e2e.AuthE2eIT#registerValidationFails`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.005 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/AuthE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode(), "非法参数应返回 400");
assertEquals("VALIDATION_ERROR", errorCode(resp));
```

### MS-049 账号禁用后登录应返回 403 FORBIDDEN

- 测试标识：`com.secondhand.e2e.AuthE2eIT#loginDisabledAccount`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.248 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/AuthE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
3. 启用或禁用账号：`PUT /api/admin/users/49/disable?disabled=true` → HTTP 200。
4. 登录并签发令牌：`POST /api/auth/login` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.OK, dis.getStatusCode(), "管理员禁用应成功");
assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode(), "禁用账号登录应返回 403");
assertEquals("FORBIDDEN", errorCode(resp));
```

### MS-050 买家出价、卖家接受，按议价生成订单并支付

- 测试标识：`com.secondhand.e2e.OfferE2eIT#offerAcceptSuccess`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.463 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/OfferE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/20/offers` → HTTP 200。
4. 接受报价并创建订单：`POST /api/offers/1/accept` → HTTP 200。
5. 补填或更新收货信息：`PUT /api/orders/20/receiver` → HTTP 200。
6. 支付订单：`POST /api/orders/20/pay` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals("PENDING", offer.get("status").asText(), "出价后应为待回复");
assertEquals(3500, order.get("amountCent").asInt(), "订单金额应为议价金额");
assertEquals("WAIT_PAY", order.get("status").asText());
assertEquals("WAIT_DELIVER", paid.get("status").asText(), "补填收货信息并支付后应待发货");
```

### MS-051 买家撤销报价

- 测试标识：`com.secondhand.e2e.OfferE2eIT#offerCancel`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.244 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/OfferE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/21/offers` → HTTP 200。
4. 撤回报价：`POST /api/offers/2/cancel` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals("CANCELLED", cancelled.get("status").asText(), "撤销后报价应变为已取消");
```

### MS-052 卖家拒绝报价

- 测试标识：`com.secondhand.e2e.OfferE2eIT#offerReject`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.274 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/OfferE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/22/offers` → HTTP 200。
4. 拒绝报价：`POST /api/offers/3/reject` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals("REJECTED", rejected.get("status").asText(), "拒绝后报价应变为已拒绝");
```

### MS-053 卖家编辑自己的商品成功

- 测试标识：`com.secondhand.e2e.ProductE2eIT#editOwnProductSuccess`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.157 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ProductE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 编辑商品或上下架：`PUT /api/products/23` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.OK, resp.getStatusCode());
assertEquals("更新后的标题", data(resp).get("title").asText());
assertEquals(2500, data(resp).get("priceCent").asInt());
```

### MS-054 编辑他人商品应返回 403 FORBIDDEN

- 测试标识：`com.secondhand.e2e.ProductE2eIT#editOthersProductForbidden`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.209 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ProductE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 编辑商品或上下架：`PUT /api/products/24` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode(), "编辑他人商品应返回 403");
assertEquals("FORBIDDEN", errorCode(resp));
```

### MS-055 发布商品校验失败应返回 400

- 测试标识：`com.secondhand.e2e.ProductE2eIT#publishProductValidationFails`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.102 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ProductE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
assertEquals("VALIDATION_ERROR", errorCode(resp));
```

### MS-056 卖家发布商品成功并进入在售状态

- 测试标识：`com.secondhand.e2e.ProductE2eIT#publishProductSuccess`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.105 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ProductE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.OK, resp.getStatusCode(), "发布商品应成功");
assertEquals("ON_SALE", data.get("status").asText(), "商品应处于在售状态");
assertTrue(data.get("id").asLong() > 0, "商品应返回有效 id");
assertEquals("测试商品-九成新手机", data.get("title").asText());
```

### MS-057 购买自己的商品应返回 403

- 测试标识：`com.secondhand.e2e.PurchaseE2eIT#purchaseOwnProductForbidden`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.131 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/PurchaseE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
assertEquals("FORBIDDEN", errorCode(resp));
```

### MS-058 重复支付（状态不符）应返回 409

- 测试标识：`com.secondhand.e2e.PurchaseE2eIT#payTwiceConflict`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.365 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/PurchaseE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/21/pay` → HTTP 200。
5. 支付订单：`POST /api/orders/21/pay` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.CONFLICT, resp.getStatusCode(), "重复支付应返回 409");
assertEquals("CONFLICT", errorCode(resp));
```

### MS-059 买家下单并支付成功，订单进入待发货

- 测试标识：`com.secondhand.e2e.PurchaseE2eIT#purchaseSuccess`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.344 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/PurchaseE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/22/pay` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals("WAIT_PAY", order.get("status").asText(), "下单后应为待支付");
assertEquals("WAIT_DELIVER", paid.get("status").asText(), "支付后应进入待发货");
```

### MS-060 举报违规商品→管理员办结

- 测试标识：`com.secondhand.e2e.ReportE2eIT#reportHandled`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.308 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ReportE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 举报商品：`POST /api/products/29/report` → HTTP 200。
4. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
5. 办结举报：`PUT /api/admin/reports/1/handle` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals("PENDING", report.get("status").asText(), "提交后举报应为待处理");
assertEquals("HANDLED", handled.get("status").asText(), "管理员办结后应为已处理");
```

### MS-061 举报违规商品→管理员驳回

- 测试标识：`com.secondhand.e2e.ReportE2eIT#reportDismissed`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.296 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ReportE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 举报商品：`POST /api/products/30/report` → HTTP 200。
4. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
5. 驳回举报：`PUT /api/admin/reports/2/dismiss` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals("DISMISSED", dismissed.get("status").asText(), "管理员驳回后应为已驳回");
```

### MS-062 举报自己的商品应返回 403

- 测试标识：`com.secondhand.e2e.ReportE2eIT#reportOwnProductForbidden`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.12 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ReportE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 举报商品：`POST /api/products/31/report` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
assertEquals("FORBIDDEN", errorCode(resp));
```

### MS-063 未支付订单发货应返回 409（状态不符）

- 测试标识：`com.secondhand.e2e.ShipE2eIT#shipWithoutPayConflict`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.318 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ShipE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 卖家发货：`POST /api/orders/23/ship` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(HttpStatus.CONFLICT, resp.getStatusCode(), "未支付发货应返回 409");
assertEquals("CONFLICT", errorCode(resp));
```

### MS-064 卖家发货成功，生成运单并进入待收货

- 测试标识：`com.secondhand.e2e.ShipE2eIT#shipSuccess`
- 流程类型：端到端业务场景；结果：**通过**；耗时：0.421 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/e2e/ShipE2eIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/24/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/24/ship` → HTTP 200。
6. 订单详情及可用操作：`GET /api/orders/24` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertNotNull(shipment.get("trackingNo"), "发货应生成运单");
assertEquals("SF123456789", shipment.get("trackingNo").asText());
assertEquals("WAIT_RECEIVE", order.get("order").get("status").asText(), "发货后应进入待收货");
```

### MS-065 registerPublishOrderAndCancelAcrossThreeServices

- 测试标识：`com.secondhand.micro.system.BusinessFlowIT#registerPublishOrderAndCancelAcrossThreeServices`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：1.035 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/micro/system/BusinessFlowIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
var created=Http.call(trade,"POST","/api/orders",order(productId),buyer,key);assertEquals(200,created.status());long id=created.data().path("id").asLong();assertEquals("WAIT_PAY",created.data().path("status").asText());assertEquals(1,quantity(productId));
var repeated=Http.call(trade,"POST","/api/orders",order(productId),buyer,key);assertEquals(id,repeated.data().path("id").asLong());assertEquals(1,quantity(productId));
assertEquals(200,Http.call(trade,"POST","/api/orders/"+id+"/cancel",Map.of(),buyer,null).status());
Http.call(trade,"POST","/api/orders/"+id+"/cancel",Map.of(),buyer,null);assertEquals(2,quantity(productId));
assertEquals(1,env.db("trade").queryForObject("SELECT COUNT(*) FROM order_events WHERE order_id=? AND to_status='CANCELLED'",Integer.class,id));
```

### MS-066 databaseAccountsCannotReadOrWriteOtherServices

- 测试标识：`com.secondhand.micro.system.BusinessFlowIT#databaseAccountsCannotReadOrWriteOtherServices`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.241 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/micro/system/BusinessFlowIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThrows(DataAccessException.class,()->env.db(caller).queryForList("SELECT * FROM secondhand_"+owner+"."+table.get(owner)+" LIMIT 1"));
assertThrows(DataAccessException.class,()->env.db(caller).update("DELETE FROM secondhand_"+owner+"."+table.get(owner)+" WHERE 1=0"));
assertThrows(DataAccessException.class,()->env.db(caller).update("UPDATE secondhand_"+owner+"."+table.get(owner)+" SET id=id WHERE 1=0"));
assertThrows(DataAccessException.class,()->env.db(caller).update("INSERT INTO secondhand_"+owner+"."+table.get(owner)+"(id) SELECT -1 WHERE 1=0"));
```

### MS-067 foreignBuyerCannotReadOrCancelOrder

- 测试标识：`com.secondhand.micro.system.BusinessFlowIT#foreignBuyerCannotReadOrCancelOrder`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.652 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/micro/system/BusinessFlowIT.java)。

**操作步骤与接口实测：**

1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(403,Http.call(trade,"GET","/api/orders/"+id,null,stranger,null).status());
assertEquals(404,Http.call(trade,"POST","/api/orders/"+id+"/cancel",Map.of(),stranger,null).status());assertEquals(0,quantity(pid));
```

### MS-068 就绪检查包含本服务数据库，存活检查不依赖下游服务。

- 测试标识：`com.secondhand.micro.system.BusinessFlowIT#allServicesExposeDatabaseAwareReadiness`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.06 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/micro/system/BusinessFlowIT.java)。

**操作步骤与接口实测：**

1. 就绪检查包含本服务数据库，存活检查不依赖下游服务。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(200,result.status());assertEquals("UP",result.body().path("status").asText());
```

### MS-069 恢复任务来自交易数据库，不靠原 JVM 内存继续执行。

- 测试标识：`com.secondhand.micro.system.BusinessFlowIT#lostReplyIsRecoveredAfterTradeServiceRestart`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：2.541 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/micro/system/BusinessFlowIT.java)。

**操作步骤与接口实测：**

1. 恢复任务来自交易数据库，不靠原 JVM 内存继续执行。
2. 恢复采用最终一致性；允许瞬时冲突重试，但必须在期限内收敛到确认状态。

**预期结果与关键业务断言（从本例源码提取）：**

```java
try{var result=Http.call(trade,"POST","/api/orders",order(pid),buyer,"lost-"+pid);assertEquals(202,result.status());id=result.data().path("id").asLong();assertEquals(1,quantity(pid));}
assertEquals("WAIT_PAY",result.data().path("order").path("status").asText());assertEquals("CONFIRMED",env.db("trade").queryForObject("SELECT phase FROM trade_operations WHERE order_id=?",String.class,id));assertEquals(1,quantity(pid));
```

### MS-070 通知落库后响应丢失，重新创建发送器后重试并验证只保留一份通知

- 测试标识：`com.secondhand.micro.system.BusinessFlowIT#notificationLostReplyRetriesAfterSenderRecreationWithoutDuplicates`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.787 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/micro/system/BusinessFlowIT.java)。

**操作步骤与接口实测：**

1. 模拟真正接收成功后网络断开；所有发送状态与去重标识都持久化在所属库。
2. 测试推进持久化重试时间，不在用例中等待真实退避窗口。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertEquals(1,db.queryForObject("SELECT attempts FROM outbox_events WHERE id=?",Integer.class,event));
assertNull(db.queryForObject("SELECT published_at FROM outbox_events WHERE id=?",java.sql.Timestamp.class,event));
assertEquals(1,env.db("user").queryForObject("SELECT COUNT(*) FROM notifications WHERE id=?",Integer.class,"trade-service:"+event));
assertTrue(db.queryForObject("SELECT next_attempt_at>NOW() FROM outbox_events WHERE id=?",Boolean.class,event));
assertNotNull(db.queryForObject("SELECT published_at FROM outbox_events WHERE id=?",java.sql.Timestamp.class,event));
assertEquals(1,env.db("user").queryForObject("SELECT COUNT(*) FROM notifications WHERE id=?",Integer.class,"trade-service:"+event));
```

### MS-071 支付记录持久化、支付归属校验、重复模拟回调及买卖订单列表

- 测试标识：`com.secondhand.migration.ComplementaryApiIT#paymentOwnershipPersistenceAndOrderLists`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：2.666 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/migration/ComplementaryApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 创建模拟支付单：`POST /api/payments` → HTTP 200。
6. 查询支付状态：`GET /api/payments/PAY-19b77511-2949-41dc-bdb2-2d71358b0e19` → HTTP 200。
7. 开发测试模拟支付成功：`POST /api/payments/PAY-19b77511-2949-41dc-bdb2-2d71358b0e19/mock-pay?orderId=25` → HTTP 404。
8. 开发测试模拟支付成功：`POST /api/payments/PAY-19b77511-2949-41dc-bdb2-2d71358b0e19/mock-pay?orderId=25` → HTTP 200。
9. 查询支付状态：`GET /api/payments/PAY-19b77511-2949-41dc-bdb2-2d71358b0e19` → HTTP 200。
10. 我买到的订单：`GET /api/orders/bought` → HTTP 200。
11. 我卖出的订单：`GET /api/orders/sold` → HTTP 200。
12. 用户注册：`POST /api/auth/register` → HTTP 201。
13. 发布商品：`POST /api/products` → HTTP 200。
14. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
15. 旧版支付兼容入口：`POST /api/orders/26/mark-paid` → HTTP 200。
16. 用户注册：`POST /api/auth/register` → HTTP 201。
17. 发布商品：`POST /api/products` → HTTP 200。
18. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
19. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
20. 管理员标记已支付：`POST /api/admin/orders/27/mark-paid` → HTTP 200。
21. 管理端订单详情：`GET /api/admin/orders/27` → HTTP 200。
22. 用户注册：`POST /api/auth/register` → HTTP 201。
23. 发布商品：`POST /api/products` → HTTP 200。
24. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
25. 管理员取消订单：`POST /api/admin/orders/28/cancel` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
String no=ok("POST","/api/payments",Map.of("orderId",t.orderId(),"method","ALIPAY"),t.buyer()).path("paymentNo").asText();assertThat(no).startsWith("PAY-");
assertThat(ok("GET","/api/payments/"+no,null,t.buyer()).asText()).isEqualTo("WAIT_PAY");
assertThat(call("POST","/api/payments/"+no+"/mock-pay?orderId="+t.orderId(),null,stranger).getResponse().getStatus()).isEqualTo(404);
assertThat(ok("GET","/api/payments/"+no,null,t.buyer()).asText()).isEqualTo("PAID");orderStatus(t.orderId(),"WAIT_DELIVER");
assertThat(count("SELECT COUNT(*) FROM payments WHERE order_id=?",t.orderId())).isEqualTo(1);
assertThat(ok("GET","/api/orders/bought",null,t.buyer()).path("totalElements").asLong()).isEqualTo(1);
assertThat(ok("GET","/api/orders/sold",null,t.seller()).path("totalElements").asLong()).isEqualTo(1);
Trade legacy=trade();ok("POST","/api/orders/"+legacy.orderId()+"/mark-paid",null,legacy.buyer());orderStatus(legacy.orderId(),"WAIT_DELIVER");
assertThat(ok("GET","/api/admin/orders/"+manual.orderId(),null,admin).path("order").path("status").asText()).isEqualTo("WAIT_DELIVER");
Trade cancelled=trade();ok("POST","/api/admin/orders/"+cancelled.orderId()+"/cancel",null,admin);stock(cancelled.productId(),1,"ON_SALE");
```

### MS-072 议价查询、接受幂等、订单收件信息补充

- 测试标识：`com.secondhand.migration.ComplementaryApiIT#offerQueriesAndReceiverCompletion`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.786 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/migration/ComplementaryApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/38/offers` → HTTP 200。
4. 卖家查看商品报价：`GET /api/products/38/offers` → HTTP 200。
5. 我发出的报价：`GET /api/my-offers` → HTTP 200。
6. 我收到的报价：`GET /api/seller-offers` → HTTP 200。
7. 接受报价并创建订单：`POST /api/offers/4/accept` → HTTP 200。
8. 补填或更新收货信息：`PUT /api/orders/29/receiver` → HTTP 200。
9. 接受报价并创建订单：`POST /api/offers/4/accept` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(ok("GET","/api/products/"+pid+"/offers",null,seller).size()).isEqualTo(1);
assertThat(ok("GET","/api/my-offers",null,buyer).size()).isEqualTo(1);assertThat(ok("GET","/api/seller-offers",null,seller).size()).isEqualTo(1);
assertThat(ok("PUT","/api/orders/"+order+"/receiver",receiver(),buyer).path("receiverName").asText()).isEqualTo("测试买家");
assertThat(ok("POST","/api/offers/"+offer+"/accept",null,seller).path("id").asLong()).isEqualTo(order);
assertThat(count("SELECT COUNT(*) FROM trade_operations WHERE idempotency_key=?","offer-accept-"+offer)).isEqualTo(1);
```

### MS-073 完成订单评价、卖家评分聚合与七天后结算

- 测试标识：`com.secondhand.migration.ComplementaryApiIT#ratingsAndSettlements`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：1.079 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/migration/ComplementaryApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/30/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/30/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/30/confirm` → HTTP 200。
7. 对已完成交易评分：`POST /api/orders/30/rate` → HTTP 200。
8. 查询订单评价：`GET /api/orders/30/rating` → HTTP 200。
9. 卖家评分摘要：`GET /api/users/86/rating` → HTTP 200。
10. 卖家已售订单及评价：`GET /api/users/86/sold` → HTTP 200。
11. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
12. 触发到期结算：`POST /api/orders/process-settlements` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(ok("POST","/api/orders/"+t.orderId()+"/rate",Map.of("score",5,"comment","物品符合描述"),t.buyer()).path("score").asInt()).isEqualTo(5);
assertThat(ok("GET","/api/orders/"+t.orderId()+"/rating",null,t.buyer()).path("score").asInt()).isEqualTo(5);
assertThat(ok("GET","/api/users/"+t.seller().id()+"/rating",null,null).path("averageScore").asDouble()).isEqualTo(5);
assertThat(ok("GET","/api/users/"+t.seller().id()+"/sold",null,null).size()).isEqualTo(1);
assertThat(ok("POST","/api/orders/process-settlements",null,admin()).asInt()).isGreaterThanOrEqualTo(1);orderStatus(t.orderId(),"SETTLED");
```

### MS-074 评论收藏、聊天已读与跨服务评论通知投影

- 测试标识：`com.secondhand.migration.ComplementaryApiIT#commentsFavoritesChatAndNotificationProjection`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：6.006 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/migration/ComplementaryApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 商品分类树：`GET /api/categories` → HTTP 200。
4. 我的商品列表：`GET /api/my-products` → HTTP 200。
5. 卖家在售商品：`GET /api/users/88/products` → HTTP 200。
6. 发表评论：`POST /api/products/40/comments` → HTTP 200。
7. 商品评论列表：`GET /api/products/40/comments` → HTTP 200。
8. 收藏商品：`POST /api/products/40/favorite` → HTTP 200。
9. 我的收藏状态：`GET /api/products/40/favorite/status` → HTTP 200。
10. 我的收藏列表：`GET /api/users/favorites` → HTTP 200。
11. 取消收藏：`DELETE /api/products/40/favorite` → HTTP 200。
12. 发送商品相关私聊：`POST /api/products/40/chat` → HTTP 200。
13. 读取双方对话：`GET /api/products/40/chat?with=89` → HTTP 200。
14. 我的会话摘要：`GET /api/users/messages` → HTTP 200。
15. 标记指定会话已读：`PUT /api/messages/read` → HTTP 200。
16. 未读消息及待办数量：`GET /api/users/notifications` → HTTP 200。
17. 商品评论通知：`GET /api/messages/comments` → HTTP 200。
18. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(ok("GET","/api/categories",null,null).size()).isPositive();
assertThat(ok("GET","/api/my-products",null,seller).path("totalElements").asInt()).isEqualTo(1);
assertThat(ok("GET","/api/users/"+seller.id()+"/products",null,null).path("totalElements").asInt()).isEqualTo(1);
long comment=ok("POST","/api/products/"+pid+"/comments",Map.of("content","还能优惠吗"),buyer).path("id").asLong();assertThat(comment).isPositive();
assertThat(ok("GET","/api/products/"+pid+"/comments",null,null).size()).isEqualTo(1);
assertThat(ok("GET","/api/products/"+pid+"/favorite/status",null,buyer).toString()).contains("true");
assertThat(ok("GET","/api/users/favorites",null,buyer).size()).isPositive();
assertThat(ok("POST","/api/products/"+pid+"/chat",Map.of("receiverId",seller.id(),"content","准备购买"),buyer).path("id").asLong()).isPositive();
assertThat(ok("GET","/api/products/"+pid+"/chat?with="+buyer.id(),null,seller).size()).isEqualTo(1);
assertThat(ok("GET","/api/users/messages",null,seller).size()).isEqualTo(1);
assertThat(ok("GET","/api/users/notifications",null,seller).path("unreadMessages").asLong()).isZero();
Suite.INSTANCE.flushEvents();assertThat(ok("GET","/api/messages/comments",null,seller).get(0).path("content").asText()).isEqualTo("还能优惠吗");
```

### MS-075 商品图片上传、设封面与删除；禁止跨商品删除图片

- 测试标识：`com.secondhand.migration.ComplementaryApiIT#productImagesAreBoundToOwnedProduct`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.86 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/migration/ComplementaryApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 上传商品图片：`POST /api/products/41/images` → HTTP 200。
4. 商品图片列表：`GET /api/products/41/images` → HTTP 200。
5. 设置商品封面：`PUT /api/products/41/images/1/cover` → HTTP 200。
6. 删除商品图片：`DELETE /api/products/42/images/1` → HTTP 403。
7. 商品图片列表：`GET /api/products/41/images` → HTTP 200。
8. 删除商品图片：`DELETE /api/products/41/images/1` → HTTP 200。
9. 商品图片列表：`GET /api/products/41/images` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
long image=upload("POST","/api/products/"+pid+"/images",seller).path("id").asLong();assertThat(image).isPositive();
assertThat(ok("GET","/api/products/"+pid+"/images",null,seller).size()).isEqualTo(1);
assertThat(call("DELETE","/api/products/"+foreign+"/images/"+image,null,other).getResponse().getStatus()).isIn(403,404);
assertThat(ok("GET","/api/products/"+pid+"/images",null,seller).size()).isEqualTo(1);
assertThat(ok("GET","/api/products/"+pid+"/images",null,seller)).isEmpty();
```

### MS-076 售后查询、双方举证、退货争议、平台仲裁与退款账本

- 测试标识：`com.secondhand.migration.ComplementaryApiIT#afterSaleEvidenceQueriesReturnDisputeAndLegacyAdminRoutes`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：1.967 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/migration/ComplementaryApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/31/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/31/ship` → HTTP 200。
6. 买家确认收货：`POST /api/orders/31/confirm` → HTTP 200。
7. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
8. 买家发起售后：`POST /api/after-sale` → HTTP 200。
9. 我申请的售后：`GET /api/after-sale/my-requests` → HTTP 200。
10. 订单售后记录：`GET /api/after-sale/by-order/31` → HTTP 200。
11. 售后单详情：`GET /api/after-sale/18` → HTTP 200。
12. 我收到的售后：`GET /api/after-sale/my-received` → HTTP 200。
13. 旧版管理员售后列表：`GET /api/after-sale/all` → HTTP 200。
14. 管理端售后详情：`GET /api/admin/after-sale/18` → HTTP 200。
15. 买家补充举证：`POST /api/after-sale/18/buyer-evidence` → HTTP 200。
16. 卖家提交举证：`POST /api/after-sale/18/seller-evidence` → HTTP 200。
17. 卖家同意售后：`POST /api/after-sale/18/approve` → HTTP 200。
18. 买家填写退货运单：`POST /api/after-sale/18/return-ship` → HTTP 200。
19. 卖家拒收退货：`POST /api/after-sale/18/reject-return` → HTTP 200。
20. 买家申请平台介入：`POST /api/after-sale/18/escalate` → HTTP 200。
21. 旧版平台仲裁入口：`POST /api/after-sale/18/arbitrate` → HTTP 200。
22. 触发售后超时处理：`POST /api/after-sale/process-timeouts` → HTTP 200。
23. 管理员触发售后超时处理：`POST /api/admin/after-sale/process-timeouts` → HTTP 200。
24. 旧版管理员售后列表：`GET /api/after-sale/all` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(ok("POST","/api/after-sale/"+id+"/buyer-evidence",Map.of("evidence","开箱图片"),t.buyer()).path("buyerEvidence").asText()).contains("开箱图片");
assertThat(ok("POST","/api/after-sale/"+id+"/seller-evidence",Map.of("evidence","出库录像"),t.seller()).path("sellerEvidence").asText()).contains("出库录像");
assertThat(ok("POST","/api/after-sale/"+id+"/reject-return",Map.of("note","货物破损"),t.seller()).path("status").asText()).isEqualTo("REJECTED");
assertThat(ok("POST","/api/after-sale/"+id+"/escalate",Map.of("evidence","退货争议"),t.buyer()).path("status").asText()).isEqualTo("PLATFORM_ARBITRATION");
assertThat(ok("POST","/api/after-sale/"+id+"/arbitrate",Map.of("result","FULL_REFUND","note","裁定退款"),admin).path("status").asText()).isEqualTo("REFUNDED");
assertThat(count("SELECT COUNT(*) FROM refunds WHERE after_sale_id=?",id)).isEqualTo(1);
assertThat(call("GET","/api/after-sale/all",null,t.buyer()).getResponse().getStatus()).isEqualTo(403);
assertThat(res.statusCode()).as(res.body()).isEqualTo(200);var json=mapper.readTree(res.body());assertThat(json.path("success").asBoolean()).as(res.body()).isTrue();return json.path("data");
```

### MS-077 个人资料、头像、收货地址生命周期与订单地址快照；修改密码使旧令牌失效

- 测试标识：`com.secondhand.migration.ComplementaryApiIT#profilesAddressesAndAddressSnapshot`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：1.392 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/migration/ComplementaryApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 更新在线心跳：`POST /api/auth/heartbeat` → HTTP 200。
3. 我的个人资料：`GET /api/users/profile` → HTTP 200。
4. 修改个人资料：`PUT /api/users/profile` → HTTP 200。
5. 卖家公开资料和评分摘要：`GET /api/users/94/public` → HTTP 200。
6. 新增收货地址：`POST /api/users/addresses` → HTTP 200。
7. 我的收货地址：`GET /api/users/addresses` → HTTP 200。
8. 修改收货地址：`PUT /api/users/addresses/1` → HTTP 200。
9. 设置默认地址：`PUT /api/users/addresses/1/default` → HTTP 200。
10. 发布商品：`POST /api/products` → HTTP 200。
11. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
12. 订单详情及可用操作：`GET /api/orders/32` → HTTP 200。
13. 按商品原价创建订单：`POST /api/orders` → HTTP 403。
14. 删除收货地址：`DELETE /api/users/addresses/1` → HTTP 200。
15. 我的收货地址：`GET /api/users/addresses` → HTTP 200。
16. 省市区静态数据：`GET /api/regions` → HTTP 200。
17. 上传头像：`PUT /api/users/avatar` → HTTP 200。
18. 修改登录密码：`POST /api/auth/password/change` → HTTP 200。
19. 我的个人资料：`GET /api/users/profile` → HTTP 401。
20. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(ok("GET","/api/users/profile",null,buyer).path("userId").asLong()).isEqualTo(buyer.id());
assertThat(ok("PUT","/api/users/profile",Map.of("nickname","迁移测试用户"),buyer).path("nickname").asText()).isEqualTo("迁移测试用户");
assertThat(ok("GET","/api/users/"+buyer.id()+"/public",null,null).path("nickname").asText()).isEqualTo("迁移测试用户");
long id=ok("POST","/api/users/addresses",address,buyer).path("id").asLong();assertThat(id).isPositive();
assertThat(ok("GET","/api/users/addresses",null,buyer).size()).isEqualTo(1);
assertThat(ok("PUT","/api/users/addresses/"+id,address,buyer).path("receiverName").asText()).isEqualTo("地址收件人");
assertThat(ok("PUT","/api/users/addresses/"+id+"/default",null,buyer).path("isDefault").asBoolean()).isTrue();
assertThat(ok("GET","/api/orders/"+order,null,buyer).path("order").path("receiverAddress").asText()).contains("测试路1号");
assertThat(call("POST","/api/orders",Map.of("productId",pid,"addressId",id),seller).getResponse().getStatus()).isIn(403,404);
ok("DELETE","/api/users/addresses/"+id,null,buyer);assertThat(ok("GET","/api/users/addresses",null,buyer)).isEmpty();
assertThat(ok("GET","/api/regions",null,null).size()).isGreaterThan(0);
String avatar=upload("PUT","/api/users/avatar",buyer).asText();assertThat(avatar).startsWith("/uploads/avatars/");
assertThat(call("GET","/api/users/profile",null,buyer).getResponse().getStatus()).isEqualTo(401);
assertThat(ok("POST","/api/auth/login",credentials("PHONE",buyer.identifier(),"changed-password123"),null).path("userId").asLong()).isEqualTo(buyer.id());
```

### MS-078 后台聚合查询、商品审核下架/恢复、踢出用户与管理员权限

- 测试标识：`com.secondhand.migration.ComplementaryApiIT#adminQueriesAndModerationRespectRoles`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.504 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/migration/ComplementaryApiIT.java)。

**操作步骤与接口实测：**

1. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
2. 用户注册：`POST /api/auth/register` → HTTP 201。
3. 发布商品：`POST /api/products` → HTTP 200。
4. 汇总用户商品交易统计：`GET /api/admin/dashboard` → HTTP 200。
5. 管理端用户分页查询：`GET /api/admin/users` → HTTP 200。
6. 查询在线用户：`GET /api/admin/users/online` → HTTP 200。
7. 管理端用户详情：`GET /api/admin/users/96` → HTTP 200。
8. 管理端商品检索：`GET /api/admin/products` → HTTP 200。
9. 管理端举报列表：`GET /api/admin/reports` → HTTP 200。
10. 管理端订单分页：`GET /api/admin/orders` → HTTP 200。
11. 管理端售后分页筛选：`GET /api/admin/after-sale` → HTTP 200。
12. 管理端强制下架：`PUT /api/admin/products/45/off-shelf` → HTTP 200。
13. 管理端重新上架：`PUT /api/admin/products/45/on-shelf` → HTTP 200。
14. 管理端删除违规商品：`DELETE /api/admin/products/45` → HTTP 200。
15. 商品详情：`GET /api/products/45` → HTTP 404。
16. 强制用户下线：`POST /api/admin/users/97/kick` → HTTP 200。
17. 我的个人资料：`GET /api/users/profile` → HTTP 401。
18. 汇总用户商品交易统计：`GET /api/admin/dashboard` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(ok("GET","/api/admin/dashboard",null,admin).path("totalUsers").asLong()).isPositive();
assertThat(ok("PUT","/api/admin/products/"+pid+"/off-shelf",null,admin).path("status").asText()).isEqualTo("OFF_SALE");
assertThat(ok("PUT","/api/admin/products/"+pid+"/on-shelf",null,admin).path("status").asText()).isEqualTo("ON_SALE");
assertThat(call("GET","/api/products/"+pid,null,null).getResponse().getStatus()).isEqualTo(404);
ok("POST","/api/admin/users/"+buyer.id()+"/kick",null,admin);assertThat(call("GET","/api/users/profile",null,buyer).getResponse().getStatus()).isEqualTo(401);
assertThat(call("GET","/api/admin/dashboard",null,seller).getResponse().getStatus()).isEqualTo(403);
```

### MS-079 5.2～5.3：分别验证卖家拒绝、买家撤回，不生成订单或占用库存。

- 测试标识：`com.secondhand.offer.OfferApiIT#rejectOrWithdrawDoesNotCreateOrderOrConsumeStock(String, String)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.298 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/46/offers` → HTTP 200。
4. 拒绝报价：`POST /api/offers/5/reject` → HTTP 200。
5. 接受报价并创建订单：`POST /api/offers/5/accept` → HTTP 409。
6. 用户注册：`POST /api/auth/register` → HTTP 201。
7. 发布商品：`POST /api/products` → HTTP 200。
8. 发起议价：`POST /api/products/47/offers` → HTTP 200。
9. 撤回报价：`POST /api/offers/6/cancel` → HTTP 200。
10. 接受报价并创建订单：`POST /api/offers/6/accept` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(databaseFor("select status from offers where id=?").queryForObject("select status from offers where id=?", String.class, offerId)).isEqualTo(status);
error("POST", "/api/offers/" + offerId + "/accept", null, seller, 409, "CONFLICT");
stock(productId, 1, "ON_SALE");
assertThat(count("select count(*) from orders where product_id=?", productId)).isZero();
```

### MS-080 5.2～5.3：分别验证卖家拒绝、买家撤回，不生成订单或占用库存。

- 测试标识：`com.secondhand.offer.OfferApiIT#rejectOrWithdrawDoesNotCreateOrderOrConsumeStock(String, String)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.268 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/46/offers` → HTTP 200。
4. 拒绝报价：`POST /api/offers/5/reject` → HTTP 200。
5. 接受报价并创建订单：`POST /api/offers/5/accept` → HTTP 409。
6. 用户注册：`POST /api/auth/register` → HTTP 201。
7. 发布商品：`POST /api/products` → HTTP 200。
8. 发起议价：`POST /api/products/47/offers` → HTTP 200。
9. 撤回报价：`POST /api/offers/6/cancel` → HTTP 200。
10. 接受报价并创建订单：`POST /api/offers/6/accept` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(databaseFor("select status from offers where id=?").queryForObject("select status from offers where id=?", String.class, offerId)).isEqualTo(status);
error("POST", "/api/offers/" + offerId + "/accept", null, seller, 409, "CONFLICT");
stock(productId, 1, "ON_SALE");
assertThat(count("select count(*) from orders where product_id=?", productId)).isZero();
```

### MS-081 5.7：自有商品及售罄商品不能收到新的报价。

- 测试标识：`com.secondhand.offer.OfferApiIT#ownOrSoldOutProductCannotReceiveOffer`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.507 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/48/offers` → HTTP 403。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
6. 用户注册：`POST /api/auth/register` → HTTP 201。
7. 发起议价：`POST /api/products/48/offers` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/products/" + productId + "/offers", Map.of("offeredPriceCent", 800), seller, 403, "FORBIDDEN");
error("POST", "/api/products/" + productId + "/offers", Map.of("offeredPriceCent", 800), user(), 409, "CONFLICT");
assertThat(count("select count(*) from offers where product_id=?", productId)).isZero();
```

### MS-082 5.1：接受报价按议价生成订单；补齐地址才能付款，重复接受不能重复下单。

- 测试标识：`com.secondhand.offer.OfferApiIT#acceptedOfferLinksDiscountedOrderAndRequiresReceiverBeforePayment`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.56 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/49/offers` → HTTP 200。
4. 接受报价并创建订单：`POST /api/offers/7/accept` → HTTP 200。
5. 支付订单：`POST /api/orders/34/pay` → HTTP 400。
6. 补填或更新收货信息：`PUT /api/orders/34/receiver` → HTTP 200。
7. 支付订单：`POST /api/orders/34/pay` → HTTP 200。
8. 接受报价并创建订单：`POST /api/offers/7/accept` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(count("select count(*) from offers where id=? and status='PENDING' and buyer_id=?", offerId, buyer.id())).isEqualTo(1);
assertThat(count("select count(*) from offers where id=? and status='ACCEPTED' and order_id=?", offerId, orderId)).isEqualTo(1);
assertThat(databaseFor("select amount_cent from orders where id=?").queryForObject("select amount_cent from orders where id=?", Integer.class, orderId)).isEqualTo(800);
stock(productId, 0, "OFF_SALE");
error("POST", "/api/orders/" + orderId + "/pay", null, buyer, 400, "BAD_REQUEST");
orderStatus(orderId, "WAIT_PAY");
assertThat(count("select count(*) from order_events where order_id=?", orderId)).isEqualTo(1);
ok("POST", "/api/orders/" + orderId + "/pay", null, buyer); orderStatus(orderId, "WAIT_DELIVER");
assertThat(ok("POST", "/api/offers/" + offerId + "/accept", null, seller).path("id").asLong()).isEqualTo(orderId);
assertThat(count("select count(*) from orders where product_id=?", productId)).isEqualTo(1);
```

### MS-083 5.5～5.6：零价和负价报价均被拒绝，数据库不能产生报价记录。

- 测试标识：`com.secondhand.offer.OfferApiIT#invalidPriceDoesNotPersistOffer(int)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.227 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/50/offers` → HTTP 400。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 发布商品：`POST /api/products` → HTTP 200。
6. 发起议价：`POST /api/products/51/offers` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/products/" + productId + "/offers", Map.of("offeredPriceCent", price), buyer, 400, "BAD_REQUEST");
assertThat(count("select count(*) from offers where product_id=?", productId)).isZero(); stock(productId, 1, "ON_SALE");
```

### MS-084 5.5～5.6：零价和负价报价均被拒绝，数据库不能产生报价记录。

- 测试标识：`com.secondhand.offer.OfferApiIT#invalidPriceDoesNotPersistOffer(int)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.228 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/50/offers` → HTTP 400。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 发布商品：`POST /api/products` → HTTP 200。
6. 发起议价：`POST /api/products/51/offers` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/products/" + productId + "/offers", Map.of("offeredPriceCent", price), buyer, 400, "BAD_REQUEST");
assertThat(count("select count(*) from offers where product_id=?", productId)).isZero(); stock(productId, 1, "ON_SALE");
```

### MS-085 5.4：商品被其他买家买走后，接受报价失败必须回滚此前的报价状态修改。

- 测试标识：`com.secondhand.offer.OfferApiIT#soldOutAcceptanceDoesNotMutateOfferOrCreateOrder`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.539 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/52/offers` → HTTP 200。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
6. 接受报价并创建订单：`POST /api/offers/8/accept` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/offers/" + offerId + "/accept", null, seller, 409, "CONFLICT");
assertThat(count("select count(*) from offers where id=? and status='PENDING' and order_id is null", offerId)).isEqualTo(1);
assertThat(count("select count(*) from orders where product_id=?", productId)).isEqualTo(1);
stock(productId, 0, "OFF_SALE");
```

### MS-086 5.8～5.10：分别验证无关用户不能接受、拒绝或撤回报价。

- 测试标识：`com.secondhand.offer.OfferApiIT#unrelatedUserCannotMutateOffer(String)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.359 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/53/offers` → HTTP 200。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 接受报价并创建订单：`POST /api/offers/9/accept` → HTTP 403。
6. 用户注册：`POST /api/auth/register` → HTTP 201。
7. 发布商品：`POST /api/products` → HTTP 200。
8. 发起议价：`POST /api/products/54/offers` → HTTP 200。
9. 用户注册：`POST /api/auth/register` → HTTP 201。
10. 拒绝报价：`POST /api/offers/10/reject` → HTTP 403。
11. 用户注册：`POST /api/auth/register` → HTTP 201。
12. 发布商品：`POST /api/products` → HTTP 200。
13. 发起议价：`POST /api/products/55/offers` → HTTP 200。
14. 用户注册：`POST /api/auth/register` → HTTP 201。
15. 撤回报价：`POST /api/offers/11/cancel` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/offers/" + offerId + "/" + action, null, user(), 403, "FORBIDDEN");
assertThat(databaseFor("select status from offers where id=?").queryForObject("select status from offers where id=?", String.class, offerId)).isEqualTo("PENDING");
assertThat(count("select count(*) from orders where product_id=?", productId)).isZero();
```

### MS-087 5.8～5.10：分别验证无关用户不能接受、拒绝或撤回报价。

- 测试标识：`com.secondhand.offer.OfferApiIT#unrelatedUserCannotMutateOffer(String)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.328 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/53/offers` → HTTP 200。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 接受报价并创建订单：`POST /api/offers/9/accept` → HTTP 403。
6. 用户注册：`POST /api/auth/register` → HTTP 201。
7. 发布商品：`POST /api/products` → HTTP 200。
8. 发起议价：`POST /api/products/54/offers` → HTTP 200。
9. 用户注册：`POST /api/auth/register` → HTTP 201。
10. 拒绝报价：`POST /api/offers/10/reject` → HTTP 403。
11. 用户注册：`POST /api/auth/register` → HTTP 201。
12. 发布商品：`POST /api/products` → HTTP 200。
13. 发起议价：`POST /api/products/55/offers` → HTTP 200。
14. 用户注册：`POST /api/auth/register` → HTTP 201。
15. 撤回报价：`POST /api/offers/11/cancel` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/offers/" + offerId + "/" + action, null, user(), 403, "FORBIDDEN");
assertThat(databaseFor("select status from offers where id=?").queryForObject("select status from offers where id=?", String.class, offerId)).isEqualTo("PENDING");
assertThat(count("select count(*) from orders where product_id=?", productId)).isZero();
```

### MS-088 5.8～5.10：分别验证无关用户不能接受、拒绝或撤回报价。

- 测试标识：`com.secondhand.offer.OfferApiIT#unrelatedUserCannotMutateOffer(String)[3]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.322 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/offer/OfferApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 发起议价：`POST /api/products/53/offers` → HTTP 200。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 接受报价并创建订单：`POST /api/offers/9/accept` → HTTP 403。
6. 用户注册：`POST /api/auth/register` → HTTP 201。
7. 发布商品：`POST /api/products` → HTTP 200。
8. 发起议价：`POST /api/products/54/offers` → HTTP 200。
9. 用户注册：`POST /api/auth/register` → HTTP 201。
10. 拒绝报价：`POST /api/offers/10/reject` → HTTP 403。
11. 用户注册：`POST /api/auth/register` → HTTP 201。
12. 发布商品：`POST /api/products` → HTTP 200。
13. 发起议价：`POST /api/products/55/offers` → HTTP 200。
14. 用户注册：`POST /api/auth/register` → HTTP 201。
15. 撤回报价：`POST /api/offers/11/cancel` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/offers/" + offerId + "/" + action, null, user(), 403, "FORBIDDEN");
assertThat(databaseFor("select status from offers where id=?").queryForObject("select status from offers where id=?", String.class, offerId)).isEqualTo("PENDING");
assertThat(count("select count(*) from orders where product_id=?", productId)).isZero();
```

### MS-089 3.7：库存为 2 时逐次购买，只有最后一件售出后才自动下架。

- 测试标识：`com.secondhand.order.PurchaseApiIT#multiQuantityPurchaseKeepsRemainingProductOnSale`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.648 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/PurchaseApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 用户注册：`POST /api/auth/register` → HTTP 201。
4. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
5. 用户注册：`POST /api/auth/register` → HTTP 201。
6. 按商品原价创建订单：`POST /api/orders` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
ok("POST", "/api/orders", orderBody(id), user()); stock(id, 1, "ON_SALE");
ok("POST", "/api/orders", orderBody(id), user()); stock(id, 0, "OFF_SALE");
assertThat(count("select count(*) from orders where product_id=?", id)).isEqualTo(2);
```

### MS-090 3.5：其他买家不能支付、取消或查看该订单，原状态和事件不变。

- 测试标识：`com.secondhand.order.PurchaseApiIT#otherBuyerCannotPayCancelOrReadOrder`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.5 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/PurchaseApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 支付订单：`POST /api/orders/38/pay` → HTTP 404。
6. 买家取消订单：`POST /api/orders/38/cancel` → HTTP 404。
7. 订单详情及可用操作：`GET /api/orders/38` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/orders/" + t.orderId() + "/" + action, null, other, 404, "NOT_FOUND");
error("GET", "/api/orders/" + t.orderId(), null, other, 403, "FORBIDDEN");
orderStatus(t.orderId(), "WAIT_PAY"); stock(t.productId(), 0, "OFF_SALE");
assertThat(count("select count(*) from order_events where order_id=?", t.orderId())).isEqualTo(1);
```

### MS-091 3.4：验证售罄和不存在商品的下单失败分支，不能留下新订单。

- 测试标识：`com.secondhand.order.PurchaseApiIT#soldOutAndMissingProductCannotCreateOrder`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.538 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/PurchaseApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 用户注册：`POST /api/auth/register` → HTTP 201。
5. 按商品原价创建订单：`POST /api/orders` → HTTP 409。
6. 按商品原价创建订单：`POST /api/orders` → HTTP 404。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/orders", orderBody(t.productId()), other, 409, "CONFLICT");
error("POST", "/api/orders", orderBody(Long.MAX_VALUE), other, 404, "NOT_FOUND");
assertThat(count("select count(*) from orders where buyer_id=?", other.id())).isZero(); stock(t.productId(), 0, "OFF_SALE");
```

### MS-092 3.6：已支付订单拒绝重复支付和取消，不能重复写事件或恢复库存。

- 测试标识：`com.secondhand.order.PurchaseApiIT#repeatPaymentAndPaidCancellationDoNotWriteExtraEvents`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.425 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/PurchaseApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/40/pay` → HTTP 200。
5. 支付订单：`POST /api/orders/40/pay` → HTTP 409。
6. 买家取消订单：`POST /api/orders/40/cancel` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/orders/" + t.orderId() + "/pay", null, t.buyer(), 409, "CONFLICT");
error("POST", "/api/orders/" + t.orderId() + "/cancel", null, t.buyer(), 409, "CONFLICT");
orderStatus(t.orderId(), "WAIT_DELIVER"); stock(t.productId(), 0, "OFF_SALE");
assertThat(count("select count(*) from order_events where order_id=?", t.orderId())).isEqualTo(2);
```

### MS-093 3.3：收货信息缺失或购买自有商品时，不生成订单、不扣库存。

- 测试标识：`com.secondhand.order.PurchaseApiIT#invalidReceiverAndOwnProductDoNotConsumeStock`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.221 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/PurchaseApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 400。
4. 按商品原价创建订单：`POST /api/orders` → HTTP 403。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/orders", Map.of("productId", id), buyer, 400, "VALIDATION_ERROR");
error("POST", "/api/orders", orderBody(id), seller, 403, "FORBIDDEN");
stock(id, 1, "ON_SALE");
assertThat(count("select count(*) from orders where product_id=?", id)).isZero();
```

### MS-094 3.1：核对下单扣库存、支付状态及事件记录在数据库中一致提交。

- 测试标识：`com.secondhand.order.PurchaseApiIT#purchaseAndPayCommitStockOrderAndEvents`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.373 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/PurchaseApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/41/pay` → HTTP 200。
5. 订单详情及可用操作：`GET /api/orders/41` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
Trade t = trade(); stock(t.productId(), 0, "OFF_SALE"); orderStatus(t.orderId(), "WAIT_PAY");
assertThat(count("select count(*) from orders where id=? and buyer_id=? and seller_id=? and amount_cent=1000",
orderStatus(t.orderId(), "WAIT_DELIVER");
assertThat(count("select count(*) from orders where id=? and paid_at is not null", t.orderId())).isEqualTo(1);
assertThat(databaseFor("select to_status from order_events where order_id=? order by id").queryForList("select to_status from order_events where order_id=? order by id", String.class, t.orderId()))
assertThat(ok("GET", "/api/orders/" + t.orderId(), null, t.seller()).path("canShip").asBoolean()).isTrue();
```

### MS-095 3.2：取消只恢复一次库存，商品随后可被其他买家重新购买。

- 测试标识：`com.secondhand.order.PurchaseApiIT#cancelRestoresStockAndAllowsAnotherBuyerToOrder`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.624 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/PurchaseApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 买家取消订单：`POST /api/orders/42/cancel` → HTTP 200。
5. 用户注册：`POST /api/auth/register` → HTTP 201。
6. 按商品原价创建订单：`POST /api/orders` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
orderStatus(t.orderId(), "CANCELLED"); stock(t.productId(), 1, "ON_SALE");
assertThat(ok("POST", "/api/orders/" + t.orderId() + "/cancel", null, t.buyer()).path("status").asText()).isEqualTo("CANCELLED");
stock(t.productId(), 1, "ON_SALE");
assertThat(count("select count(*) from order_events where order_id=?", t.orderId())).isEqualTo(2);
orderStatus(second, "WAIT_PAY"); stock(t.productId(), 0, "OFF_SALE");
```

### MS-096 4.4：非卖家操作或空运单号均不得生成物流记录或推进订单状态。

- 测试标识：`com.secondhand.order.ShippingApiIT#otherSellerAndInvalidWaybillCannotShip`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.456 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/ShippingApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/44/pay` → HTTP 200。
5. 用户注册：`POST /api/auth/register` → HTTP 201。
6. 卖家发货：`POST /api/orders/44/ship` → HTTP 404。
7. 卖家发货：`POST /api/orders/44/ship` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/orders/" + t.orderId() + "/ship", shipping(t.orderId()), user(), 404, "NOT_FOUND");
error("POST", "/api/orders/" + t.orderId() + "/ship", Map.of("carrierCode", "SF", "trackingNo", ""), t.seller(), 400, "VALIDATION_ERROR");
assertThat(count("select count(*) from shipments where order_id=?", t.orderId())).isZero();
orderStatus(t.orderId(), "WAIT_DELIVER");
```

### MS-097 4.2：重复发货不能覆盖原运单，也不能重复追加发货事件。

- 测试标识：`com.secondhand.order.ShippingApiIT#repeatShippingCannotOverwriteWaybill`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.469 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/ShippingApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/45/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/45/ship` → HTTP 200。
6. 卖家发货：`POST /api/orders/45/ship` → HTTP 409。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/orders/" + t.orderId() + "/ship", Map.of("carrierCode", "YTO", "trackingNo", "WRONG"), t.seller(), 409, "CONFLICT");
assertThat(count("select count(*) from shipments where order_id=?", t.orderId())).isEqualTo(1);
assertThat(databaseFor("select tracking_no from shipments where order_id=?").queryForObject("select tracking_no from shipments where order_id=?", String.class, t.orderId())).isEqualTo("IT-SF-" + t.orderId());
assertThat(count("select count(*) from order_events where order_id=?", t.orderId())).isEqualTo(3);
```

### MS-098 4.3：未付款订单不能发货，物流查询应提示尚无运单。

- 测试标识：`com.secondhand.order.ShippingApiIT#unpaidOrderHasNoWaybillAndCannotShip`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.569 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/ShippingApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 卖家发货：`POST /api/orders/46/ship` → HTTP 409。
5. 查询订单物流轨迹：`GET /api/shipments/46/track` → HTTP 404。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/orders/" + t.orderId() + "/ship", shipping(t.orderId()), t.seller(), 409, "CONFLICT");
error("GET", "/api/shipments/" + t.orderId() + "/track", null, null, 404, "NOT_FOUND");
assertThat(count("select count(*) from shipments where order_id=?", t.orderId())).isZero();
orderStatus(t.orderId(), "WAIT_PAY");
```

### MS-099 4.5：未发货不能确认收货，不存在订单的物流查询返回未找到。

- 测试标识：`com.secondhand.order.ShippingApiIT#unshippedOrderCannotBeConfirmedAndMissingTrackingIsNotFound`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.374 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/ShippingApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/47/pay` → HTTP 200。
5. 买家确认收货：`POST /api/orders/47/confirm` → HTTP 409。
6. 查询订单物流轨迹：`GET /api/shipments/9223372036854775807/track` → HTTP 404。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/orders/" + t.orderId() + "/confirm", null, t.buyer(), 409, "CONFLICT");
error("GET", "/api/shipments/9223372036854775807/track", null, null, 404, "NOT_FOUND");
orderStatus(t.orderId(), "WAIT_DELIVER");
```

### MS-100 4.1：验证运单落库、物流参数传递和确认收货，串联订单与物流模块。

- 测试标识：`com.secondhand.order.ShippingApiIT#shipPersistsWaybillAndTrackingUsesStoredCarrierAndNumber`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.459 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/order/ShippingApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 按商品原价创建订单：`POST /api/orders` → HTTP 200。
4. 支付订单：`POST /api/orders/48/pay` → HTTP 200。
5. 卖家发货：`POST /api/orders/48/ship` → HTTP 200。
6. 查询订单物流轨迹：`GET /api/shipments/48/track` → HTTP 200。
7. 买家确认收货：`POST /api/orders/48/confirm` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(shipment.path("trackingNo").asText()).isEqualTo("IT-SF-" + t.orderId());
assertThat(count("select count(*) from shipments where order_id=? and carrier_code='SF' and tracking_no=?",
orderStatus(t.orderId(), "WAIT_RECEIVE");
assertThat(databaseFor("select to_status from order_events where order_id=? order by id").queryForList("select to_status from order_events where order_id=? order by id", String.class, t.orderId()))
assertThat(track.path("carrierCode").asText()).isEqualTo("SF");
assertThat(track.path("trackingNo").asText()).isEqualTo(shipment.path("trackingNo").asText());
assertThat(track.path("points").size()).isEqualTo(4);
orderStatus(t.orderId(), "COMPLETED");
```

### MS-101 2.1：核对商品发布后的库存、归属及免邮设置，再验证编辑结果可被公开接口读取。

- 测试标识：`com.secondhand.product.ProductApiIT#publishAndEditPersistsAndPublicApiReturnsNewValues`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.17 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/product/ProductApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 编辑商品或上下架：`PUT /api/products/68` → HTTP 200。
4. 商品详情：`GET /api/products/68` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
stock(id, 2, "ON_SALE");
assertThat(count("select count(*) from products where id=? and seller_id=? and shipping_fee_cent=0", id, seller.id())).isEqualTo(1);
assertThat(databaseFor("select price_cent from products where id=?").queryForObject("select price_cent from products where id=?", Integer.class, id)).isEqualTo(1500);
assertThat(ok("GET", "/api/products/" + id, null, null).path("title").asText()).isEqualTo("更新商品");
```

### MS-102 2.8：查询、编辑不存在的商品都应返回明确的未找到错误。

- 测试标识：`com.secondhand.product.ProductApiIT#missingProductReturnsNotFound`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.1 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/product/ProductApiIT.java)。

**操作步骤与接口实测：**

1. 商品详情：`GET /api/products/9223372036854775807` → HTTP 404。
2. 用户注册：`POST /api/auth/register` → HTTP 201。
3. 编辑商品或上下架：`PUT /api/products/9223372036854775807` → HTTP 404。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("GET", "/api/products/9223372036854775807", null, null, 404, "NOT_FOUND");
error("PUT", "/api/products/9223372036854775807", Map.of("title", "missing"), user(), 404, "NOT_FOUND");
```

### MS-103 2.6：缺少描述时发布失败，卖家不能留下无效商品记录。

- 测试标识：`com.secondhand.product.ProductApiIT#missingDescriptionDoesNotPublish`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.099 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/product/ProductApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/products", body, seller, 400, "VALIDATION_ERROR");
assertThat(count("select count(*) from products where seller_id=?", seller.id())).isZero();
```

### MS-104 2.2：验证上下架状态会影响搜索结果，使用唯一标题隔离其他测试数据。

- 测试标识：`com.secondhand.product.ProductApiIT#offSaleAndRelistChangesSearchVisibility`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.2 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/product/ProductApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 编辑商品或上下架：`PUT /api/products/69` → HTTP 200。
4. 商品分页分类搜索推荐：`GET /api/products?keyword=unique13910000180` → HTTP 200。
5. 编辑商品或上下架：`PUT /api/products/69` → HTTP 200。
6. 商品分页分类搜索推荐：`GET /api/products?keyword=unique13910000180` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
stock(id, 1, "OFF_SALE");
assertThat(ok("GET", "/api/products?keyword=" + title, null, null).path("totalElements").asLong()).isZero();
assertThat(ok("GET", "/api/products?keyword=" + title, null, null).at("/content/0/id").asLong()).isEqualTo(id);
```

### MS-105 2.3～2.5：分别用价格 0、库存 0、运费 -1 验证发布参数边界。

- 测试标识：`com.secondhand.product.ProductApiIT#invalidNumericFieldDoesNotPublish(String, int)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.117 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/product/ProductApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 400。
3. 用户注册：`POST /api/auth/register` → HTTP 201。
4. 发布商品：`POST /api/products` → HTTP 400。
5. 用户注册：`POST /api/auth/register` → HTTP 201。
6. 发布商品：`POST /api/products` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/products", body, seller, 400, "VALIDATION_ERROR");
assertThat(count("select count(*) from products where seller_id=?", seller.id())).isZero();
```

### MS-106 2.3～2.5：分别用价格 0、库存 0、运费 -1 验证发布参数边界。

- 测试标识：`com.secondhand.product.ProductApiIT#invalidNumericFieldDoesNotPublish(String, int)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.093 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/product/ProductApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 400。
3. 用户注册：`POST /api/auth/register` → HTTP 201。
4. 发布商品：`POST /api/products` → HTTP 400。
5. 用户注册：`POST /api/auth/register` → HTTP 201。
6. 发布商品：`POST /api/products` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/products", body, seller, 400, "VALIDATION_ERROR");
assertThat(count("select count(*) from products where seller_id=?", seller.id())).isZero();
```

### MS-107 2.3～2.5：分别用价格 0、库存 0、运费 -1 验证发布参数边界。

- 测试标识：`com.secondhand.product.ProductApiIT#invalidNumericFieldDoesNotPublish(String, int)[3]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.1 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/product/ProductApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 400。
3. 用户注册：`POST /api/auth/register` → HTTP 201。
4. 发布商品：`POST /api/products` → HTTP 400。
5. 用户注册：`POST /api/auth/register` → HTTP 201。
6. 发布商品：`POST /api/products` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/products", body, seller, 400, "VALIDATION_ERROR");
assertThat(count("select count(*) from products where seller_id=?", seller.id())).isZero();
```

### MS-108 2.7：越权或非法价格编辑均失败，原商品价格与库存保持不变。

- 测试标识：`com.secondhand.product.ProductApiIT#foreignSellerAndInvalidEditLeaveOriginalData`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.215 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/product/ProductApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 用户注册：`POST /api/auth/register` → HTTP 201。
4. 编辑商品或上下架：`PUT /api/products/70` → HTTP 403。
5. 编辑商品或上下架：`PUT /api/products/70` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("PUT", "/api/products/" + id, Map.of("priceCent", 2000), user(), 403, "FORBIDDEN");
error("PUT", "/api/products/" + id, Map.of("priceCent", 0), seller, 400, "VALIDATION_ERROR");
assertThat(databaseFor("select price_cent from products where id=?").queryForObject("select price_cent from products where id=?", Integer.class, id)).isEqualTo(1000);
stock(id, 1, "ON_SALE");
```

### MS-109 7.4～7.5：普通用户不能办结或驳回举报，也不能触发审核通知。

- 测试标识：`com.secondhand.report.ReportApiIT#ordinaryUserCannotReviewOrTriggerNotification(String)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.225 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/report/ReportApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 举报商品：`POST /api/products/71/report` → HTTP 200。
4. 办结举报：`PUT /api/admin/reports/3/handle` → HTTP 403。
5. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。
6. 用户注册：`POST /api/auth/register` → HTTP 201。
7. 发布商品：`POST /api/products` → HTTP 200。
8. 举报商品：`POST /api/products/72/report` → HTTP 200。
9. 驳回举报：`PUT /api/admin/reports/4/dismiss` → HTTP 403。
10. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(call("PUT", "/api/admin/reports/" + id + "/" + action, Map.of("handleNote", "unauthorized"), reporter)
assertThat(count("select count(*) from reports where id=? and status='PENDING' and handled_by is null", id)).isEqualTo(1);
assertThat(ok("GET", "/api/messages/system", null, reporter)).isEmpty();
```

### MS-110 7.4～7.5：普通用户不能办结或驳回举报，也不能触发审核通知。

- 测试标识：`com.secondhand.report.ReportApiIT#ordinaryUserCannotReviewOrTriggerNotification(String)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.237 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/report/ReportApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 举报商品：`POST /api/products/71/report` → HTTP 200。
4. 办结举报：`PUT /api/admin/reports/3/handle` → HTTP 403。
5. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。
6. 用户注册：`POST /api/auth/register` → HTTP 201。
7. 发布商品：`POST /api/products` → HTTP 200。
8. 举报商品：`POST /api/products/72/report` → HTTP 200。
9. 驳回举报：`PUT /api/admin/reports/4/dismiss` → HTTP 403。
10. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(call("PUT", "/api/admin/reports/" + id + "/" + action, Map.of("handleNote", "unauthorized"), reporter)
assertThat(count("select count(*) from reports where id=? and status='PENDING' and handled_by is null", id)).isEqualTo(1);
assertThat(ok("GET", "/api/messages/system", null, reporter)).isEmpty();
```

### MS-111 7.6：管理员处理不存在的举报时返回未找到，举报表记录数不变。

- 测试标识：`com.secondhand.report.ReportApiIT#missingReportCannotBeReviewed`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.092 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/report/ReportApiIT.java)。

**操作步骤与接口实测：**

1. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
2. 办结举报：`PUT /api/admin/reports/9223372036854775807/handle` → HTTP 404。
3. 驳回举报：`PUT /api/admin/reports/9223372036854775807/dismiss` → HTTP 404。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("PUT", "/api/admin/reports/9223372036854775807/handle", null, admin, 404, "NOT_FOUND");
error("PUT", "/api/admin/reports/9223372036854775807/dismiss", null, admin, 404, "NOT_FOUND");
assertThat(count("select count(*) from reports")).isEqualTo(reports);
```

### MS-112 7.3：自报、目标不存在、原因缺失或非法时，均不能新增举报。

- 测试标识：`com.secondhand.report.ReportApiIT#ownMissingProductAndInvalidReasonDoNotCreateReport`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.243 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/report/ReportApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 发布商品：`POST /api/products` → HTTP 200。
3. 举报商品：`POST /api/products/73/report` → HTTP 403。
4. 举报商品：`POST /api/products/9223372036854775807/report` → HTTP 404。
5. 举报商品：`POST /api/products/73/report` → HTTP 400。

**预期结果与关键业务断言（从本例源码提取）：**

```java
error("POST", "/api/products/" + productId + "/report", reportBody(), seller, 403, "FORBIDDEN");
error("POST", "/api/products/9223372036854775807/report", reportBody(), reporter, 404, "NOT_FOUND");
error("POST", "/api/products/" + productId + "/report", Map.of("description", "missing reason"), reporter, 400, "VALIDATION_ERROR");
error("POST", "/api/products/" + productId + "/report", Map.of("reasonType", "INVALID"), reporter, 400, "BAD_REQUEST");
assertThat(count("select count(*) from reports")).isEqualTo(reports);
```

### MS-113 7.1～7.2：分别验证办结、驳回的审核记录及通知，不向无关用户泄漏信息。

- 测试标识：`com.secondhand.report.ReportApiIT#reviewPersistsAuditAndNotifiesOnlyRelevantUsers(String, String, String)[1]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：3.115 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/report/ReportApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
3. 发布商品：`POST /api/products` → HTTP 200。
4. 举报商品：`POST /api/products/74/report` → HTTP 200。
5. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。
6. 办结举报：`PUT /api/admin/reports/5/handle` → HTTP 200。
7. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。
8. 用户注册：`POST /api/auth/register` → HTTP 201。
9. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
10. 发布商品：`POST /api/products` → HTTP 200。
11. 举报商品：`POST /api/products/75/report` → HTTP 200。
12. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。
13. 驳回举报：`PUT /api/admin/reports/6/dismiss` → HTTP 200。
14. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(count("select count(*) from reports where id=? and status='PENDING' and reporter_id=? and product_id=?", id, reporter.id(), productId)).isEqualTo(1);
assertThat(ok("GET", "/api/messages/system", null, reporter)).isEmpty();
assertThat(count("select count(*) from reports where id=? and status=? and handled_by=? and handled_at is not null and handle_note=?",
assertThat(matching).hasSize(1);
assertThat(matching.get(0).path("type").asText()).isEqualTo(messageType);
assertThat(matching.get(0).path("relatedId").asText()).isEqualTo(String.valueOf(productId));
assertThat(matching.get(0).path("content").asText()).contains("核查结论");
assertThat(ok("GET", "/api/messages/system", null, other)).isEmpty();
assertThat(sellerMessages.size()).isEqualTo(action.equals("handle") ? 1 : 0);
if (action.equals("handle")) assertThat(sellerMessages.get(0).path("type").asText()).isEqualTo("report_product_handled");
```

### MS-114 7.1～7.2：分别验证办结、驳回的审核记录及通知，不向无关用户泄漏信息。

- 测试标识：`com.secondhand.report.ReportApiIT#reviewPersistsAuditAndNotifiesOnlyRelevantUsers(String, String, String)[2]`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：3.144 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/report/ReportApiIT.java)。

**操作步骤与接口实测：**

1. 用户注册：`POST /api/auth/register` → HTTP 201。
2. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
3. 发布商品：`POST /api/products` → HTTP 200。
4. 举报商品：`POST /api/products/74/report` → HTTP 200。
5. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。
6. 办结举报：`PUT /api/admin/reports/5/handle` → HTTP 200。
7. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。
8. 用户注册：`POST /api/auth/register` → HTTP 201。
9. 登录并签发令牌：`POST /api/auth/login` → HTTP 200。
10. 发布商品：`POST /api/products` → HTTP 200。
11. 举报商品：`POST /api/products/75/report` → HTTP 200。
12. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。
13. 驳回举报：`PUT /api/admin/reports/6/dismiss` → HTTP 200。
14. 订单和举报系统通知：`GET /api/messages/system` → HTTP 200。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertThat(count("select count(*) from reports where id=? and status='PENDING' and reporter_id=? and product_id=?", id, reporter.id(), productId)).isEqualTo(1);
assertThat(ok("GET", "/api/messages/system", null, reporter)).isEmpty();
assertThat(count("select count(*) from reports where id=? and status=? and handled_by=? and handled_at is not null and handle_note=?",
assertThat(matching).hasSize(1);
assertThat(matching.get(0).path("type").asText()).isEqualTo(messageType);
assertThat(matching.get(0).path("relatedId").asText()).isEqualTo(String.valueOf(productId));
assertThat(matching.get(0).path("content").asText()).contains("核查结论");
assertThat(ok("GET", "/api/messages/system", null, other)).isEmpty();
assertThat(sellerMessages.size()).isEqualTo(action.equals("handle") ? 1 : 0);
if (action.equals("handle")) assertThat(sellerMessages.get(0).path("type").asText()).isEqualTo("report_product_handled");
```

### MS-115 核验104个公开路由归属与成功HTTP证据完整性

- 测试标识：`com.secondhand.zzcoverage.ZCoverageIT#everyPublicRouteIsOwnedAndHasSuccessfulHttpEvidence`
- 流程类型：集成/API或可靠性边界；结果：**通过**；耗时：0.01 秒。
- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。
- 代码与数据准备：[测试源码](../tests/system-tests/src/test/java/com/secondhand/zzcoverage/ZCoverageIT.java)。

**操作步骤与接口实测：**

1. 不把路由存在、401 或参数错误算成成功业务覆盖。

**预期结果与关键业务断言（从本例源码提取）：**

```java
assertTrue(mappings.stream().anyMatch(m->m.getPatternValues().stream().anyMatch(p->p.replaceAll("[{][^}]+[}]","{}").equals(expected))&&m.getMethodsCondition().getMethods().stream().anyMatch(v->v.name().equals(method))),route.toString());
assertEquals(List.of(),missing,"缺少成功 HTTP 用例的公开接口");assertEquals(104,suite.covered.size());
```

## 失败判定及原始证据

本轮失败原因以各testcase的failure/error为准；全部通过时不存在未解决的测试失败。开发中发现并修复的问题另记于交付说明，不能混入本轮失败数。
- 原始后端报告：`services/*/target/failsafe-reports/TEST-*.xml`、`tests/system-tests/target/failsafe-reports/TEST-*.xml`。
- 逐请求证据：`tests/system-tests/target/api-coverage/requests.json`。
- 前端原始报告：`reports/frontend-junit.xml`。
- CI任务verify非零退出时，images与deploy均被needs门禁阻断；原始报告仍由always步骤归档。
- 完整流水线部署还需远端仓库、镜像库与microservices环境凭证；本地完成不代表远端任务已经运行。
