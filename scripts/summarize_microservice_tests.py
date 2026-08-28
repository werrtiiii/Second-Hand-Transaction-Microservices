"""从本轮 Maven XML 生成报告；缺少任何服务的结果或失败都返回非零。"""
from datetime import datetime, timezone
from pathlib import Path
import sys
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
groups=['services/user-service','services/product-service','services/trade-service','tests/system-tests']
lines=['# 第一阶段微服务测试报告','',f'生成时间（UTC）：{datetime.now(timezone.utc).isoformat()}','',
       '来源：本轮 Maven Failsafe XML。统计的是新增微服务测试，不是原单体 190 项测试。','',
       '| 模块 | 总数 | 通过 | 失败 | 错误 | 跳过 |','|---|---:|---:|---:|---:|---:|']
failed=False
totals=[0,0,0,0]
details=[]
for group in groups:
    counts=[0,0,0,0]
    reports=list((root/group/'target/failsafe-reports').glob('TEST-*.xml'))
    if not reports: failed=True
    for report in reports:
        suite=ET.parse(report).getroot()
        for i,key in enumerate(['tests','failures','errors','skipped']): counts[i]+=int(suite.get(key,0))
        for case in suite.findall('testcase'):
            problem=next(iter(list(case.findall('failure'))+list(case.findall('error'))+list(case.findall('skipped'))),None)
            details.append((group,case.get('name'), '通过' if problem is None else problem.get('message','失败').replace('|','/').replace('\n',' ')))
    total,failure,error,skipped=counts
    failed |= total==0 or failure+error+skipped>0
    totals=[a+b for a,b in zip(totals,counts)]
    lines.append(f'| {group} | {total} | {total-failure-error-skipped} | {failure} | {error} | {skipped} |')
total,failure,error,skipped=totals
lines += [f'| 合计 | {total} | {total-failure-error-skipped} | {failure} | {error} | {skipped} |','',
          '## 用例明细','', '| 模块 | 测试方法 | 结果 |','|---|---|---|']
lines += [f'| {module} | {case} | {result} |' for module,case,result in details]
lines += ['', '中文操作步骤与断言见 [第一阶段改造记录](../docs/第一阶段改造记录.md#新增测试用例说明)。', '', '环境：Java 17 兼容目标；实际 JVM/操作系统见各 XML 的 properties。临时 MySQL 8.0；三服务通过独立 HTTP 端口通信；数据库账号仅有各自数据库 DML 权限。','',
          '当前只覆盖第一阶段链路，不能作为 104 个公开 API、全部七个用例或生产部署验收结果。']
target=root/'reports/phase1-test-report.md'
target.parent.mkdir(exist_ok=True)
target.write_text('\n'.join(lines)+'\n',encoding='utf-8')
print(f'Tests={total}, failures={failure}, errors={error}, skipped={skipped}')
sys.exit(1 if failed else 0)
