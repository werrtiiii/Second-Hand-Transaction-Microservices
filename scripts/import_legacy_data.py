"""将旧单体导出数据替换到隔离开发库；不执行上传文件中的DDL或其他命令。"""
from pathlib import Path
from datetime import datetime
import argparse,hashlib,json,re,subprocess,sys,time,urllib.request

ROOT=Path(__file__).resolve().parents[1]
OWNER={**dict.fromkeys(['users','user_identities','user_addresses','chat_messages'],'user'),
       **dict.fromkeys(['categories','products','product_images','comments','favorites','reports'],'product'),
       **dict.fromkeys(['orders','order_events','offers','shipments','after_sale_requests','ratings'],'trade')}
EXTRA={'user':['user_security_state','notifications'],'product':['inventory_reservations','outbox_events'],
       'trade':['trade_operations','outbox_events','payments','refunds']}
MYSQL='secondhand-microservices-mysql-1'

def split_values(text):
    result=[];start=0;quoted=False;escape=False;depth=0
    for i,char in enumerate(text):
        if quoted:
            if escape:escape=False
            elif char=='\\':escape=True
            elif char=="'":quoted=False
        elif char=="'":quoted=True
        elif char=='(':depth+=1
        elif char==')':depth-=1
        elif char==',' and depth==0:result.append(text[start:i].strip());start=i+1
        if depth<0:raise ValueError('Invalid tuple nesting')
    if quoted or depth or escape:raise ValueError('Unterminated SQL literal')
    result.append(text[start:].strip());return result

def literal(token):
    token=token.strip();binary=token.startswith('_binary ')
    if binary:token=token[8:].strip()
    if token=='NULL' and not binary:return None
    if re.fullmatch(r'-?\d+',token) and not binary:return int(token)
    if not (token.startswith("'") and token.endswith("'")):raise ValueError('Only literal INSERT values are supported')
    body=token[1:-1];out=[];i=0;escapes={'0':'\0','b':'\b','n':'\n','r':'\r','t':'\t','Z':'\x1a'}
    while i<len(body):
        if body[i]=='\\':
            i+=1
            if i==len(body):raise ValueError('Incomplete escape')
            out.append(escapes.get(body[i],body[i]))
        elif body[i]=="'":
            if i+1<len(body) and body[i+1]=="'":out.append("'");i+=1
            else:raise ValueError('Unexpected quote')
        else:out.append(body[i])
        i+=1
    value=''.join(out);return value.encode('latin1') if binary else value

def parse_dump(path):
    text=path.read_text(encoding='utf-8-sig');tables={}
    for match in re.finditer(r'CREATE TABLE `([a-z_]+)` \((.*?)\n\) ENGINE=',text,re.S):
        table,ddl=match.groups()
        if table not in OWNER or table in tables:raise ValueError('Unexpected or duplicate table: '+table)
        columns=re.findall(r'^  `([a-z_]+)` ',ddl,re.M)
        tables[table]={'columns':columns,'rows':[]}
    if set(tables)!=set(OWNER):raise ValueError('Dump must contain the 16 supported tables')
    for line in text.splitlines():
        if not line.startswith('INSERT INTO'):continue
        match=re.fullmatch(r'INSERT INTO `([a-z_]+)` VALUES (.*);',line)
        if not match or match[1] not in tables:raise ValueError('Unsupported INSERT syntax')
        table=match[1];columns=tables[table]['columns']
        for row in split_values(match[2]):
            if not (row.startswith('(') and row.endswith(')')):raise ValueError('Expected tuple')
            values=[literal(t) for t in split_values(row[1:-1])]
            if len(values)!=len(columns):raise ValueError('Column count mismatch: '+table)
            tables[table]['rows'].append(dict(zip(columns,values)))
    return tables

def validate(tables):
    index={t:{r['id']:r for r in v['rows']} for t,v in tables.items()}
    for t,v in tables.items():
        if len(index[t])!=len(v['rows']):raise ValueError('Duplicate primary key: '+t)
    refs={'user_identities':{'user_id':'users'},'user_addresses':{'user_id':'users'},
      'chat_messages':{'sender_id':'users','receiver_id':'users','product_id':'products'},
      'products':{'seller_id':'users','category_id':'categories'},'categories':{'parent_id':'categories'},
      'product_images':{'product_id':'products'},'comments':{'user_id':'users','product_id':'products'},
      'favorites':{'user_id':'users','product_id':'products'},'reports':{'reporter_id':'users','product_id':'products'},
      'orders':{'buyer_id':'users','seller_id':'users','product_id':'products','address_id':'user_addresses'},
      'order_events':{'order_id':'orders'},'offers':{'buyer_id':'users','seller_id':'users','product_id':'products'},
      'shipments':{'order_id':'orders'},'after_sale_requests':{'buyer_id':'users','seller_id':'users','order_id':'orders'},
      'ratings':{'order_id':'orders','product_id':'products','reviewer_id':'users','seller_id':'users'}}
    for t,columns in refs.items():
        for row in tables[t]['rows']:
            for col,target in columns.items():
                if row.get(col) is not None and row[col] not in index[target]:raise ValueError(f'Orphan reference: {t}.{col}')
    if len({r['order_id'] for r in tables['shipments']['rows']})!=len(tables['shipments']['rows']):raise ValueError('Duplicate shipment per order')
    for order in index['orders'].values():
        if order['status'] not in {'WAIT_PAY','WAIT_DELIVER','WAIT_RECEIVE','COMPLETED','SETTLED','CANCELLED','AFTER_SALE'}:raise ValueError('Unsupported legacy order state')
        if order['amount_cent']<=0:raise ValueError('Invalid amount')
        if index['products'][order['product_id']]['seller_id']!=order['seller_id']:raise ValueError('Order seller differs from product')
    for after in index['after_sale_requests'].values():
        order=index['orders'][after['order_id']]
        if after['buyer_id']!=order['buyer_id'] or after['seller_id']!=order['seller_id']:raise ValueError('After-sale parties differ from order')
    return index

def sql_value(value):
    # 字符串通过UTF8十六进制传输，原始值不能成为可执行SQL。
    if value is None:return 'NULL'
    if isinstance(value,bytes):return "X'"+value.hex()+"'"
    if isinstance(value,int):return str(value)
    return "CONVERT(X'"+str(value).encode('utf8').hex()+"' USING utf8mb4)"

def insert(database,table,row):
    if not re.fullmatch(r'secondhand_(user|product|trade)',database):raise ValueError('Unexpected database')
    for name in [table,*row]:
        if not re.fullmatch('[a-z_]+',name):raise ValueError('Invalid SQL identifier')
    return f"INSERT INTO `{database}`.`{table}` ("+','.join('`'+c+'`' for c in row)+') VALUES ('+','.join(sql_value(v) for v in row.values())+');'

def build_data(tables,old_versions):
    index=validate(tables);result={t:[dict(r) for r in v['rows']] for t,v in tables.items()};extra={s:{t:[] for t in ts} for s,ts in EXTRA.items()}
    for user in index['users'].values():extra['user']['user_security_state'].append({'user_id':user['id'],'token_version':old_versions.get(user['id'],0)+1})
    for order in result['orders']:
        product=index['products'][order['product_id']];oid=order['id'];paid=order['paid_at'] is not None
        if order['status'] not in ['WAIT_PAY','CANCELLED'] and not paid:raise ValueError('Missing paid timestamp for advanced order')
        order.update(product_title=product['title'],product_version=0,list_price_cent=product['price_cent'],version=0)
        released=order['status']=='CANCELLED' and not paid
        phase='RELEASED' if released else 'CONFIRMED'
        digest=hashlib.sha256(f'legacy-import:{oid}'.encode()).hexdigest()
        extra['trade']['trade_operations'].append({'actor_id':order['buyer_id'],'idempotency_key':f'legacy-import-order-{oid}','payload_hash':digest,'order_id':oid,'phase':phase,'created_at':order['created_at'],'updated_at':order['updated_at']})
        snapshot={'id':product['id'],'sellerId':product['seller_id'],'title':product['title'],'description':product['description'],'priceCent':product['price_cent'],'quantity':product['quantity'],'status':product['status'],'coverImageUrl':product['cover_image_url'],'version':0}
        extra['product']['inventory_reservations'].append({'operation_id':f'order-create-{oid}','order_id':oid,'product_id':order['product_id'],'quantity':0 if released else 1,'status':phase,'payload_hash':digest,'product_snapshot':json.dumps(snapshot,ensure_ascii=False),'created_at':order['created_at'],'updated_at':order['updated_at']})
        refunded=[r for r in index['after_sale_requests'].values() if r['order_id']==oid and r['status']=='REFUNDED']
        refund_total=sum(r['refund_amount_cent'] for r in refunded)
        if refund_total>order['amount_cent'] or (refund_total and not paid):raise ValueError('Historical refund inconsistent with payment')
        if paid:
            extra['trade']['payments'].append({'payment_no':f'LEGACY-PAY-{oid}','order_id':oid,'buyer_id':order['buyer_id'],'amount_cent':order['amount_cent'],'status':'REFUNDED' if refund_total==order['amount_cent'] else 'PAID','refunded_cent':refund_total,'method':'LEGACY_IMPORT','created_at':order['paid_at'],'updated_at':order['updated_at']})
        for refund in refunded:
            if refund['refund_amount_cent']<=0:raise ValueError('Invalid historical refund')
            extra['trade']['refunds'].append({'refund_no':f"LEGACY-REF-{refund['id']}",'after_sale_id':refund['id'],'order_id':oid,'amount_cent':refund['refund_amount_cent'],'created_at':refund['refunded_at'] or refund['handled_at'] or refund['created_at']})
    # 重建消息读取投影，不向outbox重复发送历史事件。
    for comment in index['comments'].values():
        product=index['products'][comment['product_id']];user=index['users'][comment['user_id']]
        payload={'id':comment['id'],'productId':product['id'],'productTitle':product['title'],'commenterId':user['id'],'commenterName':user['nickname'],'commenterAvatar':user['avatar_url'],'content':comment['content'],'time':comment['created_at']}
        extra['user']['notifications'].append({'id':f"product-service:legacy-comment-{comment['id']}",'source_service':'product-service','recipient_id':product['seller_id'],'kind':'comment','payload':json.dumps(payload,ensure_ascii=False),'created_at':comment['created_at']})
    for event in index['order_events'].values():
        order=index['orders'][event['order_id']]
        for recipient in {order['buyer_id'],order['seller_id']}:
            payload={'id':f"order-event-{event['id']}",'type':'order_event','title':f"订单 #{order['id']}",'content':event['note'],'relatedId':str(order['id']),'time':event['created_at']}
            extra['user']['notifications'].append({'id':f"trade-service:legacy-event-{event['id']}-{recipient}",'source_service':'trade-service','recipient_id':recipient,'kind':'system','payload':json.dumps(payload,ensure_ascii=False),'created_at':event['created_at']})
    return result,extra

def docker(args,data=None):
    p=subprocess.run(['docker',*args],cwd=ROOT,input=data,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    if p.returncode:raise RuntimeError('Docker operation failed: '+p.stderr.decode('utf8',errors='replace')[:300])
    return p.stdout

def sql(statement):
    # 凭证只在容器内部读取，不传入宿主机参数、不输出原始数据。
    return docker(['exec','-i',MYSQL,'bash','-c','MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --default-character-set=utf8mb4 --batch --skip-column-names'],statement.encode('utf8')).decode('utf8')

def table_counts():
    result={}
    for s in EXTRA:
        database='secondhand_'+s
        for table in [t for t,owner in OWNER.items() if owner==s]+EXTRA[s]+['schema_history']:
            result[database+'.'+table]=int(sql(f'SELECT COUNT(*) FROM `{database}`.`{table}`;').strip())
    return result

def main():
    parser=argparse.ArgumentParser();parser.add_argument('dump',type=Path);parser.add_argument('--replace',action='store_true');args=parser.parse_args()
    tables=parse_dump(args.dump);validate(tables);build_data(tables,{})
    print('Validated source rows:',json.dumps({t:len(v['rows']) for t,v in tables.items()}))
    if not args.replace:print('DRY RUN: no database changes');return
    # 固定容器及Compose标签，避免误连单体/其他数据库。
    info=json.loads(docker(['inspect',MYSQL]))[0]
    if info['Config']['Labels'].get('com.docker.compose.project')!='secondhand-microservices':raise ValueError('Wrong target project')
    directory=ROOT/'reports/local-runtime'/('data-import-'+datetime.now().strftime('%Y%m%d-%H%M%S'));directory.mkdir(parents=True)
    report={'sourceSha256':hashlib.sha256(args.dump.read_bytes()).hexdigest(),'target':MYSQL,'sourceCounts':{t:len(v['rows']) for t,v in tables.items()},'status':'STARTED'}
    stopped=False;committed=False
    try:
        docker(['compose','-f','compose.microservices.yml','stop','gateway','trade-service','product-service','user-service']);stopped=True
        report['beforeCounts']=table_counts()
        dump=docker(['exec',MYSQL,'bash','-c','MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -uroot --single-transaction --hex-blob --no-tablespaces --set-gtid-purged=OFF --default-character-set=utf8mb4 --databases secondhand_user secondhand_product secondhand_trade'])
        if b'Dump completed' not in dump:raise RuntimeError('Backup incomplete')
        (directory/'before.sql').write_bytes(dump);report['backupSha256']=hashlib.sha256(dump).hexdigest()
        versions={int(a):int(b) for a,b in [line.split('\t') for line in sql('SELECT user_id,token_version FROM secondhand_user.user_security_state;').splitlines()]}
        imported,extra=build_data(tables,versions)
        statements=['SET NAMES utf8mb4;','SET SESSION time_zone=\'+00:00\';','START TRANSACTION;']
        # 所有表均为InnoDB，同一连接跨三个本机schema提交，失败会整体回滚。
        for s in EXTRA:
            database='secondhand_'+s
            expected={t for t,owner in OWNER.items() if owner==s}|set(EXTRA[s])|{'schema_history'}
            actual={line.split('\t')[0] for line in sql(f"SELECT TABLE_NAME,ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='{database}';").splitlines()}
            if actual!=expected:raise ValueError('Unexpected target schema tables: '+database)
            engines=sql(f"SELECT DISTINCT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='{database}';").splitlines()
            if engines!=['InnoDB']:raise ValueError('Nontransactional table present')
            for table in reversed([t for t,owner in OWNER.items() if owner==s]+EXTRA[s]):statements.append(f'DELETE FROM `{database}`.`{table}`;')
        # 先用户/分类/商品/订单等父表，再插入身份、订单事件和售后等子表。
        for table in OWNER:
            rows=imported[table]
            for row in rows:statements.append(insert('secondhand_'+OWNER[table],table,row))
        for s,items in extra.items():
            for table,rows in items.items():
                for row in rows:statements.append(insert('secondhand_'+s,table,row))
        statements.append('COMMIT;')
        # 生成的是白名单表与纯字面量DML；源文件DROP/CREATE/SET一律不执行。
        sql('\n'.join(statements));committed=True
        report['afterCounts']=table_counts()
        for t,rows in imported.items():assert report['afterCounts']['secondhand_'+OWNER[t]+'.'+t]==len(rows),t
        for s,items in extra.items():
            for t,rows in items.items():assert report['afterCounts']['secondhand_'+s+'.'+t]==len(rows),t
            assert report['afterCounts']['secondhand_'+s+'.schema_history']==report['beforeCounts']['secondhand_'+s+'.schema_history']
        # 在服务启动前核对导入列的所有值、历史密码哈希、二进制字段和时间，避免只核对数量。
        for table,rows in tables.items():
            for row in rows['rows']:
                clauses=[f'`{column}` <=> {sql_value(value)}' for column,value in row.items()]
                count=sql(f"SELECT COUNT(*) FROM `secondhand_{OWNER[table]}`.`{table}` WHERE "+' AND '.join(clauses)+';')
                assert count.strip()=='1','Imported row differs: '+table
        report['status']='IMPORTED_AND_VERIFIED';report['compatibilityCounts']={s:{t:len(v) for t,v in items.items()} for s,items in extra.items()}
        print('Imported and verified all original values; backup:',directory/'before.sql')
    except Exception as error:
        report['status']='FAILED_AFTER_COMMIT' if committed else 'FAILED_WITHOUT_COMMIT';report['error']=type(error).__name__
        # 验证异常时不把可能不完整的数据继续提供给用户，保留备份及现场。
        raise
    finally:
        (directory/'import-report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf8')
        if stopped and (not committed or report['status']=='IMPORTED_AND_VERIFIED'):
            docker(['compose','-f','compose.microservices.yml','start','user-service','product-service','trade-service','gateway'])
    print('Services restarted; automatic maintenance may process already-expired legacy records.')

if __name__=='__main__':main()
