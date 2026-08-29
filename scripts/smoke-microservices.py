"""网关部署冒烟：真实 HTTP，断言状态与业务字段；不读取任何业务数据库。"""
import json,sys,uuid,urllib.request,urllib.error,datetime
from pathlib import Path
base=sys.argv[1] if len(sys.argv)>1 else 'http://127.0.0.1:18080'
events=[]
def call(method,path,body=None,token=None,status=200,key=None):
    headers={'Content-Type':'application/json'}
    if token:headers['Authorization']='Bearer '+token
    if path=='/api/orders' and method=='POST':headers['Idempotency-Key']=key or str(uuid.uuid4())
    req=urllib.request.Request(base+path,data=None if body is None else json.dumps(body).encode(),headers=headers,method=method)
    try:
        with urllib.request.urlopen(req,timeout=15) as res:code=res.status;raw=res.read()
    except urllib.error.HTTPError as res:code=res.code;raw=res.read()
    events.append({'method':method,'path':path,'status':code,'expected':status})
    assert code==status,(path,code,raw[:400])
    if status>=400:return
    data=json.loads(raw);assert data.get('success'),(path,data)
    return data.get('data')
def register():
    return call('POST','/api/auth/register',{'identityType':'EMAIL','identifier':str(uuid.uuid4())+'@example.com','password':'local-smoke-pass-123'},status=201)['accessToken']
def product(seller,title):
    return call('POST','/api/products',{'title':title,'description':'微服务部署验证','priceCent':1000,'quantity':2},seller)['id']
def create(buyer,pid,key=None):
    return call('POST','/api/orders',{'productId':pid,'receiverName':'测试收件人','receiverPhone':'13900000000','receiverAddress':'测试地址'},buyer,key=key)
root=Path(__file__).resolve().parents[1];out=root/'reports/local-runtime';out.mkdir(parents=True,exist_ok=True)
try:
    # 场景一：下单幂等→模拟付款→发货→收货→评价。
    seller,buyer=register(),register();pid=product(seller,'部署验证完整交易');key=str(uuid.uuid4())
    order=create(buyer,pid,key);oid=order['id'];assert order['status']=='WAIT_PAY'
    assert create(buyer,pid,key)['id']==oid
    assert call('GET',f'/api/products/{pid}')['quantity']==1
    payment=call('POST','/api/payments',{'orderId':oid,'method':'ALIPAY'},buyer)['paymentNo']
    call('POST',f'/api/payments/{payment}/mock-pay?orderId={oid}',token=buyer)
    shipment=call('POST',f'/api/orders/{oid}/ship',{'carrierCode':'SF','trackingNo':'SMOKE-'+str(oid)},seller)
    assert shipment['trackingNo']=='SMOKE-'+str(oid)
    call('POST',f'/api/orders/{oid}/confirm',token=buyer)
    assert call('GET',f'/api/orders/{oid}',token=buyer)['order']['status']=='COMPLETED'
    assert call('POST',f'/api/orders/{oid}/rate',{'score':5,'comment':'部署验证'},buyer)['score']==5
    # 场景二：议价接受使用固定业务键，重复接受及取消不重复扣减/恢复库存。
    pid2=product(seller,'部署验证议价');offer=call('POST',f'/api/products/{pid2}/offers',{'offeredPriceCent':800,'message':'议价'},buyer)['id']
    accepted=call('POST',f'/api/offers/{offer}/accept',token=seller);assert accepted['amountCent']==800
    assert call('POST',f'/api/offers/{offer}/accept',token=seller)['id']==accepted['id']
    call('POST',f'/api/orders/{accepted["id"]}/cancel',token=buyer)
    call('POST',f'/api/orders/{accepted["id"]}/cancel',token=buyer)
    assert call('GET',f'/api/products/{pid2}')['quantity']==2
    # 场景三：退货退款闭环，校验受保护内部接口没有从网关暴露。
    after=call('POST','/api/after-sale',{'orderId':oid,'type':'RETURN_REFUND','reason':'部署退货验证','refundAmountCent':1000},buyer)['id']
    assert call('POST',f'/api/after-sale/{after}/approve',token=seller)['status']=='APPROVED'
    call('POST',f'/api/after-sale/{after}/return-ship',{'carrierCode':'SF','trackingNo':'RETURN-'+str(oid)},buyer)
    assert call('POST',f'/api/after-sale/{after}/confirm-return',token=seller)['status']=='REFUNDED'
    call('GET','/internal/v1/products/1',status=404)
    call('GET','/actuator/info',status=404)
    result={'result':'PASS','scenarios':3,'time':datetime.datetime.now().astimezone().isoformat(),'requests':events}
    print('PASS: 3 gateway business scenarios, idempotency, refund and internal-route isolation')
except Exception as error:
    result={'result':'FAIL','reason':str(error),'requests':events};raise
finally:
    (out/'smoke.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8')
