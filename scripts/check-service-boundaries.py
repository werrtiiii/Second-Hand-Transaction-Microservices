"""阻止重新引入跨服务 Entity/Repository 依赖及嵌入式测试库漂移。"""
from pathlib import Path
import re,sys,json
root=Path(__file__).resolve().parents[1]
errors=[]
owners={}
for service in ['user','product','trade']:
    for p in (root/f'services/{service}-service/src/main/java').rglob('*.java'):
        text=p.read_text(encoding='utf8')
        package=re.search(r'package\s+([\w.]+)',text)
        if package and ('@Entity' in text or 'Repository' in p.name):
            owners[package[1]+'.'+p.stem]=service
for service in ['user','product','trade']:
    base=root/f'services/{service}-service'
    for p in (base/'src/main/java').rglob('*.java'):
        text=p.read_text(encoding='utf8')
        for imported in re.findall(r'import\s+([\w.]+)',text):
            if imported in owners and owners[imported]!=service:errors.append(f'{p}: foreign import {imported}')
        for database in re.findall(r'secondhand_(user|product|trade)\.',text):
            if database!=service:errors.append(f'{p}: foreign database reference')
    migrations=''.join(p.read_text(encoding='utf8') for p in sorted((root/f'db/{service}').glob('V*.sql')))
    embedded=(base/f'src/main/resources/db/{service}/schema.sql').read_text(encoding='utf8')
    # 内嵌测试 Schema 只描述表结构；初始化账号等 DML 种子数据不参与结构漂移比较。
    def ddl_only(sql):
        without_comments=re.sub(r'(?m)^\s*--.*$', '', sql)
        statements=re.split(r';', without_comments)
        return ''.join(statement+';' for statement in statements
                       if re.match(r'^\s*(CREATE|ALTER|DROP|RENAME|TRUNCATE)\b', statement, re.I))
    normalize=lambda s:re.sub(r'\s+','',s)
    if normalize(ddl_only(migrations))!=normalize(ddl_only(embedded)):
        errors.append(f'{service}: test schema differs from versioned migrations')
for p in (root/'platform/src/main/java').rglob('*.java'):
    if re.search(r'@Entity\b|JpaRepository\s*<',p.read_text(encoding='utf8')):errors.append(f'{p}: shared business persistence')
catalog=json.loads((root/'docs/微服务设计/接口与表归属清单.json').read_text(encoding='utf8'))
gateway=(root/'gateway/default.conf.template').read_text(encoding='utf8')
for route in catalog['existing_public_routes']:
    pattern=re.sub(r'[{][^}]+[}]','[^/]+',route['path'])
    if f"~^{pattern}$ {route['target_service']}:8080;" not in gateway:
        errors.append('gateway route mismatch: '+route['id'])
print('\n'.join(errors) if errors else 'PASS: service source/data boundaries and migration schemas')
sys.exit(bool(errors))
