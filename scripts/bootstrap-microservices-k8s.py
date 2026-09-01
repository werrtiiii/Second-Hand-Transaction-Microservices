"""仅初始化全新的隔离开发命名空间；不覆盖已有环境，不输出凭证。"""
from pathlib import Path
import subprocess,json,secrets,sys,argparse
root=Path(__file__).resolve().parents[1];namespace='secondhand-microservices'
parser=argparse.ArgumentParser();parser.add_argument('--confirm-new-namespace',action='store_true');parser.add_argument('--enable-mock-payments',action='store_true');args=parser.parse_args()
if not args.confirm_new_namespace:sys.exit('Require --confirm-new-namespace; development only')
def run(*cmd,data=None):
    return subprocess.run(cmd,input=data,text=True,encoding='utf8',capture_output=True,check=True).stdout
def create(kind,name,data):
    payload={'apiVersion':'v1','kind':kind,'metadata':{'name':name,'namespace':namespace},'stringData' if kind=='Secret' else 'data':data}
    run('kubectl','create','-f','-',data=json.dumps(payload))
existing=run('kubectl','get','namespace',namespace,'--ignore-not-found','-o','name').strip()
if existing:sys.exit('Namespace already exists; refusing to overwrite credentials or data')
values={line.split('=',1)[0]:line.split('=',1)[1].strip() for line in (root/'.env').read_text().splitlines() if '=' in line and not line.startswith('#')}
for key in ['MYSQL_ROOT_PASSWORD']+[s+'_'+k for s in ['USER','PRODUCT','TRADE'] for k in ['DB_PASSWORD','PRIVATE_KEY','PUBLIC_KEY']]:
    if not values.get(key):sys.exit('Missing configuration: '+key)
run('kubectl','create','namespace',namespace)
public={s+'_PUBLIC_KEY':values[s+'_PUBLIC_KEY'] for s in ['USER','PRODUCT','TRADE']}
public.update({s.upper()+'_SERVICE_URL':f'http://{s}-service:8080' for s in ['user','product','trade']})
create('ConfigMap','service-public-config',public)
bootstrap={'MYSQL_ROOT_PASSWORD':values['MYSQL_ROOT_PASSWORD']}
for s in ['user','product','trade']:
    password=secrets.token_hex(24);bootstrap[s.upper()+'_MIGRATION_PASSWORD']=password;bootstrap[s.upper()+'_DB_PASSWORD']=values[s.upper()+'_DB_PASSWORD']
    create('Secret',s+'-service-secrets',{'DB_PASSWORD':values[s.upper()+'_DB_PASSWORD'],'SERVICE_PRIVATE_KEY':values[s.upper()+'_PRIVATE_KEY'],'MOCK_PAYMENTS_ENABLED':str(args.enable_mock_payments).lower()})
    create('Secret',s+'-migration',{'DB_HOST':'mysql','DB_NAME':'secondhand_'+s,'DB_USERNAME':s+'_migration','DB_PASSWORD':password})
create('Secret','mysql-bootstrap',bootstrap)
run('kubectl','apply','-f',str(root/'deploy/kubernetes/mysql-development.yaml'))
run('kubectl','-n',namespace,'rollout','status','deployment/mysql','--timeout=180s')
script=(root/'db/kubernetes-bootstrap.sh').read_text(encoding='utf8')
run('kubectl','-n',namespace,'exec','-i','deployment/mysql','--','bash','-s',data=script)
print('Created isolated development databases and service credentials; no application images deployed')
