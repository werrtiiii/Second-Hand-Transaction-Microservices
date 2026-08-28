# 第一阶段微服务测试报告

生成时间（UTC）：2026-08-28T08:25:02.455251+00:00

来源：本轮 Maven Failsafe XML。统计的是新增微服务测试，不是原单体 190 项测试。

| 模块 | 总数 | 通过 | 失败 | 错误 | 跳过 |
|---|---:|---:|---:|---:|---:|
| services/user-service | 5 | 5 | 0 | 0 | 0 |
| services/product-service | 9 | 9 | 0 | 0 | 0 |
| services/trade-service | 3 | 3 | 0 | 0 | 0 |
| tests/system-tests | 5 | 5 | 0 | 0 | 0 |
| 合计 | 22 | 22 | 0 | 0 | 0 |

## 用例明细

| 模块 | 测试方法 | 结果 |
|---|---|---|
| services/user-service | wrongPasswordAndMissingTokenAreRejected | 通过 |
| services/user-service | registerLoginAndMe | 通过 |
| services/user-service | invalidInputAndUnauthenticatedInternalRequestAreRejected | 通过 |
| services/user-service | revokedVersionInvalidatesPreviouslyIssuedToken | 通过 |
| services/user-service | duplicateRegistrationRollsBackUser | 通过 |
| services/product-service | concurrentDuplicateReservationsAllReturnSameResult | 通过 |
| services/product-service | concurrentBuyersCannotOversell | 通过 |
| services/product-service | secondOperationCannotClaimSameOrder | 通过 |
| services/product-service | bindingAndQuantityAreValidated | 通过 |
| services/product-service | sameKeyWithDifferentPayloadIsConflict | 通过 |
| services/product-service | internalApiRequiresCorrectServiceAndAudience | 通过 |
| services/product-service | duplicateReserveAndReleaseChangeStockOnlyOnce | 通过 |
| services/product-service | releaseDoesNotUndoAdministrativeOffShelf | 通过 |
| services/product-service | releaseBeforeReserveLeavesTerminalTombstone | 通过 |
| services/trade-service | internalOrderStateRequiresProductService | 通过 |
| services/trade-service | invalidReceiverIsRejectedBeforeSideEffects | 通过 |
| services/trade-service | anonymousOrderCreationIsRejected | 通过 |
| tests/system-tests | registerPublishOrderAndCancelAcrossThreeServices | 通过 |
| tests/system-tests | databaseAccountsCannotReadOrWriteOtherServices | 通过 |
| tests/system-tests | foreignBuyerCannotReadOrCancelOrder | 通过 |
| tests/system-tests | allServicesExposeDatabaseAwareReadiness | 通过 |
| tests/system-tests | lostReplyIsRecoveredAfterTradeServiceRestart | 通过 |

中文操作步骤与断言见 [第一阶段改造记录](../docs/第一阶段改造记录.md#新增测试用例说明)。

环境：Java 17 兼容目标；实际 JVM/操作系统见各 XML 的 properties。临时 MySQL 8.0；三服务通过独立 HTTP 端口通信；数据库账号仅有各自数据库 DML 权限。

当前只覆盖第一阶段链路，不能作为 104 个公开 API、全部七个用例或生产部署验收结果。
