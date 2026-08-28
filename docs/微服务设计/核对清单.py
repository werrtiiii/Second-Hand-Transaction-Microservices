"""只读核对设计清单与单体源码；不代表微服务已实现或接口测试已通过。"""
import collections
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent


def without_comments(text):
    # 保留换行，使定位仍然对应源文件行号。
    return re.sub(r'/\*.*?\*/|//[^\n]*', lambda m: '\n' * m.group().count('\n'), text, flags=re.S)


def scan_routes():
    routes = []
    for file in sorted((ROOT / 'backend/src/main/java').rglob('*Controller.java')):
        source = file.read_text(encoding='utf-8-sig')
        clean = without_comments(source)
        cls = re.search(r'public\s+class\s+(\w+)', clean)
        if not cls:
            continue
        prefix = re.search(r'@RequestMapping\s*\(\s*"([^"]*)"\s*\)', clean[:cls.start()])
        prefix = prefix.group(1) if prefix else ''
        mappings = list(re.finditer(r'^\s*@(Get|Post|Put|Delete|Patch)Mapping(?:\(([^\n]*)\))?\s*$', clean, re.M))
        for mapping in mappings:
            suffixes = re.findall(r'"([^"]*)"', mapping.group(2) or '') or ['']
            method = re.search(r'public\s+([\w<>., ?]+?)\s+(\w+)\s*\(', clean[mapping.end():])
            assert method, (file, mapping.group())
            start = mapping.end() + method.end()
            level, end = 1, start
            while level:
                char = clean[end]
                level += (char == '(') - (char == ')')
                end += 1
            parameters = re.sub(r'\s+', ' ', clean[start:end-1]).strip()
            for suffix in suffixes:
                path = prefix + suffix
                if not path.startswith('/api/'):
                    continue
                routes.append({'http_method': mapping.group(1).upper(), 'path': path,
                               'controller': cls.group(1), 'handler': method.group(2),
                               'parameters': parameters, 'return_type': method.group(1),
                               'source': file.relative_to(ROOT).as_posix(),
                               'line': clean.count('\n', 0, clean.index('@', mapping.start())) + 1})
    return routes


def scan_tables():
    sql = (ROOT / 'db/init.sql').read_text(encoding='utf-8-sig')
    ddl = sorted(re.findall(r'^CREATE TABLE `([^`]+)`', sql, re.M))
    entities = []
    for file in (ROOT / 'backend/src/main/java').rglob('*.java'):
        entities += re.findall(r'@Table\(name\s*=\s*"([^"]+)"', file.read_text(encoding='utf-8-sig'))
    return ddl, sorted(entities)


def verify():
    inventory = json.loads((HERE / '接口与表归属清单.json').read_text(encoding='utf-8'))
    actual = scan_routes()
    key = lambda row: (row['http_method'], row['path'])
    planned = inventory['existing_public_routes']
    assert len(set(map(key, actual))) == len(actual), '源码存在重复 HTTP 方法/路径'
    assert len(set(map(key, planned))) == len(planned), '文档存在重复 HTTP 方法/路径'
    assert set(map(key, actual)) == set(map(key, planned)), '接口有遗漏、增加或路径变化'
    by_key = {key(row): row for row in actual}
    for row in planned:
        current = by_key[key(row)]
        for field in ['source', 'controller', 'handler', 'parameters', 'return_type']:
            assert current[field] == row[field], f'{key(row)} 的 {field} 已变化'
        assert row['target_service'] in inventory['services']
        assert row['purpose'] and row['target_auth']
    ddl, entities = scan_tables()
    tables = inventory['existing_tables']
    assert len({row['table'] for row in tables}) == len(tables), '表存在重复归属'
    assert sorted(row['table'] for row in tables) == ddl == entities, 'DDL、Entity 或归属清单不一致'
    assert all(row['target_service'] in inventory['services'] for row in tables)
    sql = (ROOT / 'db/init.sql').read_text(encoding='utf-8-sig')
    blocks = dict(re.findall(r'CREATE TABLE `([^`]+)` \((.*?)\) ENGINE', sql, re.S))
    for row in tables:
        assert re.findall(r'^  `([^`]+)`', blocks[row['table']], re.M) == row['existing_columns'], '表字段与基线清单不一致'
    internal = inventory['proposed_internal_routes']
    assert len({key(row) for row in internal}) == len(internal), '拟新增内部接口重复'
    assert all(row['path'].startswith('/internal/v1/') for row in internal)
    assert all(row['target_service'] in inventory['services'] for row in internal)
    markdown = (HERE / '02_服务接口清单.md').read_text(encoding='utf-8')
    listed = re.findall(r'^\| (API-\d{3}) \|', markdown, re.M)
    assert collections.Counter(listed) == collections.Counter(row['id'] for row in planned), '公开接口文档条目与清单不一致'
    for row in planned + internal:
        assert f"`{row['path']}`" in markdown, f"文档未列出路径 {row['path']}"
        assert f"| {row['id']} | {row['http_method']} | `{row['path']}` |" in markdown, '接口编号、方法与路径不一致'
    listed_internal = re.findall(r'^\| (INT-[UPT]-\d{2}) \|', markdown, re.M)
    assert collections.Counter(listed_internal) == collections.Counter(row['id'] for row in internal), '内部接口文档条目与清单不一致'
    table_doc = (HERE / '03_数据表归属方案.md').read_text(encoding='utf-8')
    for row in tables:
        table_line = next(line for line in table_doc.splitlines() if line.startswith(f"| `{row['table']}` |"))
        assert row['target_service'] in table_line and row['database'] in table_line, '表的目标数据库或服务不一致'
    mmd = (HERE / '微服务划分图.mmd').read_text(encoding='utf-8').strip()
    diagram_doc = (HERE / '01_微服务划分图与说明.md').read_text(encoding='utf-8')
    embedded = re.search(r'```mermaid\n(.*?)```', diagram_doc, re.S).group(1).strip()
    compact = lambda text: '\n'.join(line.strip() for line in text.splitlines() if line.strip())
    assert compact(mmd) == compact(embedded), '独立图源与正文图不一致'
    for file in HERE.glob('*.md'):
        content = file.read_text(encoding='utf-8')
        assert len(re.findall(r'^```', content, re.M)) % 2 == 0, f'{file.name} 代码块未闭合'
        for link in re.findall(r'\]\(([^)]+)\)', content):
            if not link.startswith(('http://', 'https://', '#')):
                assert (file.parent / link.split('#', 1)[0]).exists(), f'{file.name} 本地链接失效：{link}'
    result = {'result': 'PASS', 'existing_public_api_count': len(actual),
              'public_api_by_service': dict(collections.Counter(row['target_service'] for row in planned)),
              'existing_business_table_count': len(tables),
              'tables_by_service': dict(collections.Counter(row['target_service'] for row in tables)),
              'proposed_internal_api_count': len(internal),
              'note': '仅核对静态清单完整性；没有执行 API 测试、DDL 或微服务部署。'}
    print(json.dumps(result, ensure_ascii=False, indent=2))
    if '--report' in sys.argv:
        (HERE / '清单校验结果.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    sys.stdout.reconfigure(encoding='utf-8')
    if '--scan' in sys.argv:
        print(json.dumps(scan_routes(), ensure_ascii=False, indent=2))
    else:
        verify()
