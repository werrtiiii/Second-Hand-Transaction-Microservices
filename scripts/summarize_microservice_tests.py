"""依据原始XML及HTTP证据生成逐项报告；缺失、失败、跳过或接口不全均失败。"""
from datetime import datetime,timezone
from pathlib import Path
from collections import Counter,defaultdict
import sys,re,json,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
groups=['services/user-service','services/product-service','services/trade-service','tests/system-tests']
catalog=json.loads((root/'docs/微服务设计/接口与表归属清单.json').read_text(encoding='utf8'))['existing_public_routes']
routes={r['id']:r for r in catalog}
ep=root/'tests/system-tests/target/api-coverage/requests.json'
evidence=json.loads(ep.read_text(encoding='utf8')) if ep.exists() else []
covered={e['id'] for e in evidence if e['success']}
bycase=defaultdict(list)
for e in evidence:bycase[e.get('case','fixture')].append(e)
failed=False;details=[];totals=[0]*4;environment={};rows=[]
for group in groups:
    counts=[0]*4
    files=sorted((root/group/'target/failsafe-reports').glob('TEST-*.xml'))
    if not files:failed=True
    for report in files:
        suite=ET.parse(report).getroot()
        environment.update({p.get('name'):p.get('value') for p in suite.findall('properties/property') if p.get('name') in ['java.version','java.vendor','os.name','os.version','os.arch']})
        for i,key in enumerate(['tests','failures','errors','skipped']):counts[i]+=int(suite.get(key,0))
        for case in suite.findall('testcase'):
            problem=case.find('failure')
            if problem is None:problem=case.find('error')
            if problem is None:problem=case.find('skipped')
            details.append((group,case,problem))
    n,f,e,s=counts;failed|=n==0 or f+e+s>0;totals=[a+b for a,b in zip(totals,counts)]
    rows.append(f'| {group} | {n} | {n-f-e-s} | {f} | {e} | {s} |')
front=root/'reports/frontend-junit.xml';fc=[0]*4
if front.exists():
    node=ET.parse(front).getroot()
    suites=[node] if node.tag=='testsuite' else list(node.iter('testsuite'))
    for suite in suites:
        for i,k in enumerate(['tests','failures','errors','skipped']):fc[i]+=int(suite.get(k,0))
failed|=fc[0]==0 or sum(fc[1:])>0 or covered!=set(routes)
n,f,e,s=totals
lines=['# 微服务集成/API与端到端测试报告','',f'生成时间：{datetime.now().astimezone().isoformat()}','',
'## 结论与范围','',f'后端：**{n} 项，{n-f-e-s} 通过，{f} 失败，{e} 错误，{s} 跳过**。前端：**{fc[0]} 项，{fc[0]-sum(fc[1:])} 通过，{sum(fc[1:])} 未通过/跳过**。',
f'公开接口成功HTTP证据：**{len(covered)}/{len(routes)}**；同时验证接口注册在方案指定的服务中。一次HTTP请求不等于一条测试，下面以JUnit用例为计数单位。','',
'验证使用真实 Spring Boot HTTP 服务和临时 MySQL 8.0，不启动旧单体、不用H2或MockMvc替代业务链路。接口已覆盖不等于穷举全部参数组合；主流程、备选和异常分支见逐项明细。',
'', '远端流水线/生产部署不属于本地测试结果。本报告来自本轮本地执行或CI自身生成；实际镜像与部署证据另见交付说明。支付退款使用明确开启的模拟账本，未调用真实资金渠道。','',
'## 运行环境与命令','',*(f'- {k}：{v}' for k,v in environment.items()),
'- Java编译目标17；MySQL容器版本8.0；Testcontainers 1.21.4；Spring Boot 3.3.2。',
'- 三个服务分别用user_app、product_app、trade_app，只具备本库DML；测试准备建表使用独立的临时容器管理员。',
'- 普通批量回归关闭实例限流，限流专项在真实HTTP实例中单独开启并验证第21次登录返回429。',
'- 每个用例创建独立账号/商品/订单；数据库断言按归属使用对应服务账号，测试完成后销毁临时容器。',
'- 命令：`mvn --batch-mode --no-transfer-progress clean verify`；`npm test -- --reporter=default --reporter=junit --outputFile=../reports/frontend-junit.xml`。','',
'## 数量统计','', '| 模块 | 总数 | 通过 | 失败 | 错误 | 跳过 |','|---|---:|---:|---:|---:|---:|',*rows,
f'| 后端合计 | {n} | {n-f-e-s} | {f} | {e} | {s} |',f'| 前端 | {fc[0]} | {fc[0]-sum(fc[1:])} | {fc[1]} | {fc[2]} | {fc[3]} |','',
'## 104个公开接口证据索引','', '| 编号 | 服务 | 方法与路径 | 业务用途 | 成功请求数 | 异常请求数 |','|---|---|---|---|---:|---:|']
for r in catalog:
    items=[e for e in evidence if e['id']==r['id']]
    lines.append(f'| {r["id"]} | {r["target_service"]} | `{r["http_method"]} {r["path"]}` | {r["purpose"]} | {sum(e["success"] for e in items)} | {sum(not e["success"] for e in items)} |')
lines+=['','## 测试用例逐项明细','',
'编号MS-xxx对应本轮原始XML里的独立testcase。参数化测试的每个参数组分别统计；同方法的HTTP步骤合并展示其所有参数组。接口步骤中的状态码是实测结果，期望与业务断言来自测试代码。',
'所有账号、商品和订单均为测试创建的数据；实际请求体和完整数据库断言可点击代码链接查看。登录凭证、密码和图片正文不写入HTTP证据。','']
manual={
'wrongPasswordAndMissingTokenAreRejected':'错误密码与缺失令牌分别返回401',
'invalidInputAndUnauthenticatedInternalRequestAreRejected':'非法邮箱参数400及未认证内部请求401，无权限绕过',
'revokedVersionInvalidatesPreviouslyIssuedToken':'持久化令牌版本更新后，旧凭证内省结果失效',
'duplicateRegistrationRollsBackUser':'重复邮箱注册409，用户行数保持不变',
'concurrentBuyersCannotOversell':'多买家并发抢购最后一件商品，库存不超卖',
'secondOperationCannotClaimSameOrder':'同一订单不能绑定两个不同库存操作',
'bindingAndQuantityAreValidated':'库存预占校验卖家、数量及请求绑定关系',
'sameKeyWithDifferentPayloadIsConflict':'同一幂等键携带不同内容返回冲突',
'internalApiRequiresCorrectServiceAndAudience':'内部库存接口拒绝无凭证、错误服务及错误受众',
'duplicateReserveAndReleaseChangeStockOnlyOnce':'重复预占和释放请求仅改变一次库存',
'releaseDoesNotUndoAdministrativeOffShelf':'库存补偿不撤销管理员审核下架',
'internalOrderStateRequiresProductService':'库存核对接口只允许商品服务身份',
'anonymousMalformedOrderIsRejectedBeforeSideEffects':'匿名或畸形下单在产生业务副作用前被拒绝',

'profilesAddressesAndAddressSnapshot':'个人资料、头像、收货地址生命周期与订单地址快照；修改密码使旧令牌失效',
'commentsFavoritesChatAndNotificationProjection':'评论收藏、聊天已读与跨服务评论通知投影',
'productImagesAreBoundToOwnedProduct':'商品图片上传、设封面与删除；禁止跨商品删除图片',
'adminQueriesAndModerationRespectRoles':'后台聚合查询、商品审核下架/恢复、踢出用户与管理员权限',
'paymentOwnershipPersistenceAndOrderLists':'支付记录持久化、支付归属校验、重复模拟回调及买卖订单列表',
'ratingsAndSettlements':'完成订单评价、卖家评分聚合与七天后结算',
'offerQueriesAndReceiverCompletion':'议价查询、接受幂等、订单收件信息补充',
'afterSaleEvidenceQueriesReturnDisputeAndLegacyAdminRoutes':'售后查询、双方举证、退货争议、平台仲裁与退款账本',
'everyPublicRouteIsOwnedAndHasSuccessfulHttpEvidence':'核验104个公开路由归属与成功HTTP证据完整性',
'loginRateLimitRejectsExcessRequests':'前20次无效登录返回401，第21次请求返回429及RATE_LIMITED',
'notificationLostReplyRetriesAfterSenderRecreationWithoutDuplicates':'通知落库后响应丢失，重新创建发送器后重试并验证只保留一份通知'}
for index,(group,case,problem) in enumerate(details,1):
    classname=case.get('classname');method=re.split(r'\(|\[',case.get('name'))[0]
    path=root/group/'src/test/java'/Path(*classname.split('.')).with_suffix('.java')
    source=path.read_text(encoding='utf8') if path.exists() else ''
    m=re.search(r'\bvoid\s+'+re.escape(method)+r'\s*\([^)]*\)[^{]*\{',source)
    body='';title=manual.get(method,method)
    if m:
        start=m.end();nextmethod=re.search(r'\n\s*(?:@Test|@ParameterizedTest|(?:public |private |protected |static )*void\s+)',source[start:])
        end=start+nextmethod.start() if nextmethod else len(source)
        body=source[start:end]
        comments=re.findall(r'//\s*([^\n]+)',body)
        before=source[max(0,m.start()-250):m.start()]
        display=re.findall(r'@DisplayName\("([^"\n]+)"\)',before)
        if method not in manual:
            if display:title=display[-1]
            elif comments:title=comments[0]
    state='通过' if problem is None else '失败/错误/跳过：'+problem.get('message','未提供原因')
    escaped=state.replace('|','/').replace('\n',' ')
    lines += [f'### MS-{index:03d} {title}','',f'- 测试标识：`{classname}#{case.get("name")}`',f'- 流程类型：{"端到端业务场景" if ".e2e." in classname else "集成/API或可靠性边界"}；结果：**{escaped}**；耗时：{case.get("time","0")} 秒。',
              '- 前置条件：服务及所属数据库就绪，使用本例创建的参与者和业务记录；管理员场景使用测试夹具显式建立的管理员。',
              f'- 代码与数据准备：[测试源码](../{path.relative_to(root).as_posix()})。','', '**操作步骤与接口实测：**','']
    items=bycase.get(classname+'#'+method,[])
    if items:
        sequence=[]
        for item in items:
            value=(item['method'],item['path'],item['status'],routes[item['id']]['purpose'])
            if not sequence or sequence[-1]!=value:sequence.append(value)
        for i,(verb,url,code,purpose) in enumerate(sequence,1):lines.append(f'{i}. {purpose}：`{verb} {url}` → HTTP {code}。')
    else:
        comments=re.findall(r'//\s*([^\n]+)',body)
        if comments:
            for i,c in enumerate(comments,1):lines.append(f'{i}. {c}')
        else:lines.append('1. 按下方源码断言建立数据、发送HTTP请求并检查返回值/数据库记录；接口总覆盖检查则核对路由及前序请求证据。')
    # 保留实际断言表达式，不把没有检查的数据写成“验证通过”。
    assertion_lines=[line.strip() for line in body.splitlines() if re.search(r'assert\w*\(|\berror\(|\bstock\(|\borderStatus\(',line)]
    lines += ['','**预期结果与关键业务断言（从本例源码提取）：**','','```java',*(assertion_lines or ['// 断言封装在测试辅助方法中，完整定义见上方源码链接。']),'```','']
    if problem is not None:lines+=['失败原因：'+(problem.text or problem.get('message','')).replace('```',''), '']
lines += ['## 失败判定及原始证据','',
'本轮失败原因以各testcase的failure/error为准；全部通过时不存在未解决的测试失败。开发中发现并修复的问题另记于交付说明，不能混入本轮失败数。',
'- 原始后端报告：`services/*/target/failsafe-reports/TEST-*.xml`、`tests/system-tests/target/failsafe-reports/TEST-*.xml`。',
'- 逐请求证据：`tests/system-tests/target/api-coverage/requests.json`。',
'- 前端原始报告：`reports/frontend-junit.xml`。',
'- CI任务verify非零退出时，images与deploy均被needs门禁阻断；原始报告仍由always步骤归档。',
'- 完整流水线部署还需远端仓库、镜像库与microservices环境凭证；本地完成不代表远端任务已经运行。','']
if covered!=set(routes):lines+=['缺失接口：'+', '.join(sorted(set(routes)-covered))]
out=root/'reports/microservices-test-report.md';out.parent.mkdir(exist_ok=True);out.write_text('\n'.join(lines)+'\n',encoding='utf8')
print(f'Backend={n}, failed={f+e+s}; frontend={fc[0]}, failed={sum(fc[1:])}; API={len(covered)}/{len(routes)}')
sys.exit(1 if failed else 0)
