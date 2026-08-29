"""仅对secondhand-microservices开发项目进行可复现的坏网关版本/回滚演练。"""
from pathlib import Path
import subprocess,os,json,urllib.request,urllib.error,time
root=Path(__file__).resolve().parents[1];out=root/'reports/local-runtime';out.mkdir(parents=True,exist_ok=True)
good='phase2-20260828';bad='failure-drill';prefix='secondhand-microservices'
env=dict(os.environ,APP_VERSION=bad,IMAGE_PREFIX=prefix)
def command(args,name,environment=None,check=True):
    p=subprocess.run(args,cwd=root,env=environment or os.environ,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    (out/name).write_bytes(p.stdout)
    if check and p.returncode:raise RuntimeError(f'{name}: exit={p.returncode}')
    return p.returncode
def get(path):
    with urllib.request.urlopen('http://127.0.0.1:18080'+path,timeout=3) as r:return r.status,r.read()
original=json.loads(get('/api/products/1')[1])['data']
for service in ['user-service','product-service','trade-service']:
    command(['docker','tag',f'{prefix}/{service}:{good}',f'{prefix}/{service}:{bad}'],f'tag-{service}.log')
command(['docker','build','-f','tests/deployment/failure-gateway.Dockerfile','--build-arg',f'BASE_IMAGE={prefix}/gateway:{good}','-t',f'{prefix}/gateway:{bad}','.'],'failure-image-build.log')
try:
    command(['docker','compose','-f','compose.microservices.yml','up','-d','--no-build','--pull','never'],'failure-deploy.log',env)
    # 有限等待观察新网关停止就绪，随后必须回滚；不保持故障环境。
    deadline=time.monotonic()+20;unhealthy=False
    while time.monotonic()<deadline:
        try:healthy=get('/healthz')[0]==200
        except Exception:healthy=False
        if not healthy:unhealthy=True;break
        time.sleep(1)
    command(['docker','compose','-f','compose.microservices.yml','logs','--tail','60','gateway'],'failure-gateway.log',env)
    assert unhealthy,'Bad gateway unexpectedly healthy'
    assert 'intentional_invalid_directive' in (out/'failure-gateway.log').read_text(encoding='utf8',errors='replace')
finally:
    powershell=str(Path(os.environ['SystemRoot'])/'System32/WindowsPowerShell/v1.0/powershell.exe')
    command([powershell,'-NoProfile','-ExecutionPolicy','Bypass','-File','scripts/rollback-microservices-local.ps1','-Version',good],'rollback-final.log')
current=json.loads(get('/api/products/1')[1])['data']
assert current['id']==original['id'] and current['quantity']==original['quantity']
(out/'rollback-result.json').write_text(json.dumps({'failureVersion':bad,'restoredVersion':good,'gatewayFailureDetected':True,'rollbackPassed':True,'existingProductAndStockRetained':True},indent=2),encoding='utf8')
print('PASS: invalid gateway config detected; verified version restored; existing data retained')
