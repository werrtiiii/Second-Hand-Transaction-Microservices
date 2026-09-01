"""在同机、同数据、同脚本条件下对比单体和微服务性能。"""
from __future__ import annotations
import argparse,csv,ctypes,getpass,hashlib,json,math,os,platform,shutil,statistics,subprocess,sys,time
from ctypes import wintypes
from datetime import datetime
from pathlib import Path
from urllib.request import urlopen
import import_legacy_data as legacy

ROOT=Path(__file__).resolve().parents[1]
MONOLITH=Path(r"D:\OneDrive\Desktop\Second-Hand-Transaction")
LOAD_DIR=ROOT/"tests"/"load"
REPORT_ROOT=ROOT/"reports"/"performance-comparison"
PS=Path(r"C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe")
JAVA=shutil.which("java.exe") or shutil.which("java")
DOCKER=shutil.which("docker.exe") or shutil.which("docker")
MYSQL=Path(r"E:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe")
MONOLITH_JAR=MONOLITH/"backend"/"target"/"secondhand-backend-0.1.0-SNAPSHOT.jar"
STATE_FILE=ROOT/"reports"/"local-native"/"processes.json"
OWNER={
    "users":"user","user_identities":"user","user_addresses":"user","chat_messages":"user",
    "categories":"product","products":"product","product_images":"product","comments":"product",
    "favorites":"product","reports":"product","orders":"trade","order_events":"trade",
    "offers":"trade","shipments":"trade","after_sale_requests":"trade","ratings":"trade",
}
ENDPOINTS=(
    "/api/products?page=0&size=20",
    "/api/regions",
    "/api/users/2/rating",
)

kernel32=ctypes.WinDLL("kernel32",use_last_error=True)
psapi=ctypes.WinDLL("psapi",use_last_error=True)
PROCESS_QUERY_LIMITED_INFORMATION=0x1000
PROCESS_VM_READ=0x0010

class FILETIME(ctypes.Structure):
    _fields_=(("low",wintypes.DWORD),("high",wintypes.DWORD))
class PROCESS_MEMORY_COUNTERS(ctypes.Structure):
    _fields_=(
        ("cb",wintypes.DWORD),("PageFaultCount",wintypes.DWORD),
        ("PeakWorkingSetSize",ctypes.c_size_t),("WorkingSetSize",ctypes.c_size_t),
        ("QuotaPeakPagedPoolUsage",ctypes.c_size_t),("QuotaPagedPoolUsage",ctypes.c_size_t),
        ("QuotaPeakNonPagedPoolUsage",ctypes.c_size_t),("QuotaNonPagedPoolUsage",ctypes.c_size_t),
        ("PagefileUsage",ctypes.c_size_t),("PeakPagefileUsage",ctypes.c_size_t),
    )
kernel32.OpenProcess.argtypes=(wintypes.DWORD,wintypes.BOOL,wintypes.DWORD)
kernel32.OpenProcess.restype=wintypes.HANDLE

RUN_DIR=REPORT_ROOT/("run-"+datetime.now().strftime("%Y%m%d-%H%M%S"))
RAW_DIR=RUN_DIR/"raw"
RUN_DIR.mkdir(parents=True)
RAW_DIR.mkdir()
PROGRESS=RUN_DIR/"progress.log"

def log(message):
    line=datetime.now().strftime("%H:%M:%S")+" "+message
    print(line,flush=True)
    with PROGRESS.open("a",encoding="utf8") as stream:stream.write(line+"\n")

def ps(script,*arguments,check=True):
    log_path=RAW_DIR/("powershell-"+script.stem+"-"+datetime.now().strftime("%H%M%S%f")+".log")
    with log_path.open("wb") as output:
        process=subprocess.run(
            [str(PS),"-NoProfile","-ExecutionPolicy","Bypass","-File",str(script),*map(str,arguments)],
            cwd=ROOT,stdout=output,stderr=subprocess.STDOUT
        )
    if process.returncode:
        details=log_path.read_bytes().decode("utf8",errors="replace").strip()
        if details:
            for line in details.splitlines()[-8:]:log(line)
        if check:raise RuntimeError(script.name+" 执行失败")
    else:
        log(script.name+" 执行完成")
    return process.returncode

def stop_micro():
    if STATE_FILE.exists():
        log("停止微服务版")
        ps(ROOT/"scripts"/"stop-microservices-native.ps1")
        time.sleep(3)

def start_micro():
    if STATE_FILE.exists():stop_micro()
    log("启动微服务版")
    ps(ROOT/"scripts"/"start-microservices-native.ps1","-SkipBuild","-SkipNpmInstall","-Version","performance-comparison")
    wait_endpoints("http://127.0.0.1:18080")

def wait_endpoints(base,timeout=120):
    deadline=time.time()+timeout
    last=None
    while time.time()<deadline:
        try:
            for path in ENDPOINTS:
                with urlopen(base+path,timeout=5) as response:
                    body=json.loads(response.read())
                    if response.status!=200 or body.get("success") is not True:
                        raise RuntimeError("接口业务响应失败")
            log("接口预检通过："+base)
            return
        except Exception as error:
            last=error
            time.sleep(2)
    raise RuntimeError("接口未就绪："+base+" "+repr(last))

def mysql_query(password,statement):
    env=os.environ.copy()
    env["MYSQL_PWD"]=password
    process=subprocess.run([
        str(MYSQL),"--protocol=tcp","--host=127.0.0.1","--port=3306","--user=root",
        "--default-character-set=utf8mb4","--batch","--skip-column-names"
    ],input=statement.encode("utf8"),stdout=subprocess.PIPE,stderr=subprocess.PIPE,env=env)
    if process.returncode:
        raise RuntimeError(process.stderr.decode("utf8",errors="replace")[:500])
    return process.stdout.decode("utf8")

def prepare_benchmark_database(password,database,dump_path):
    # 先用白名单解析器校验附件，再只导入隔离的临时库。
    source=legacy.parse_dump(dump_path)
    legacy.validate(source)
    if not database.startswith("secondhand_benchmark_perf_") or not database.replace("_","").isalnum():
        raise ValueError("临时数据库名不安全")
    mysql_query(password,"CREATE DATABASE "+database+" CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    env=os.environ.copy()
    env["MYSQL_PWD"]=password
    process=subprocess.run([
        str(MYSQL),"--protocol=tcp","--host=127.0.0.1","--port=3306","--user=root",
        "--default-character-set=utf8mb4","--database="+database
    ],input=dump_path.read_bytes(),stdout=subprocess.PIPE,stderr=subprocess.PIPE,env=env)
    if process.returncode:
        raise RuntimeError("临时库导入失败："+process.stderr.decode("utf8",errors="replace")[:500])
    log("已从同一 dump 创建隔离单体测试库："+database)
    return hashlib.sha256(dump_path.read_bytes()).hexdigest()

def verify_same_data(password,database):
    mysql_query(password,"SELECT VERSION();")
    rows={}
    for table,owner in OWNER.items():
        source=int(mysql_query(password,"SELECT COUNT(*) FROM "+database+"."+table+";").strip())
        target=int(mysql_query(password,"SELECT COUNT(*) FROM secondhand_"+owner+"."+table+";").strip())
        rows[table]={"monolith":source,"microservices":target}
        if source!=target:
            (RUN_DIR/"data-counts.json").write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding="utf8")
            raise ValueError(table+" 两版行数不一致："+str(source)+" != "+str(target))
    (RUN_DIR/"data-counts.json").write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding="utf8")
    log("16 张业务表行数一致，数据条件校验通过")
    return rows

def process_sample(pids):
    cpu=0.0
    memory=0
    alive=0
    for pid in pids:
        handle=kernel32.OpenProcess(PROCESS_QUERY_LIMITED_INFORMATION|PROCESS_VM_READ,False,int(pid))
        if not handle:continue
        try:
            creation=FILETIME();exit_time=FILETIME();kernel=FILETIME();user=FILETIME()
            if kernel32.GetProcessTimes(handle,ctypes.byref(creation),ctypes.byref(exit_time),ctypes.byref(kernel),ctypes.byref(user)):
                cpu+=(kernel.high<<32|kernel.low)/10_000_000+(user.high<<32|user.low)/10_000_000
            counters=PROCESS_MEMORY_COUNTERS()
            counters.cb=ctypes.sizeof(counters)
            if psapi.GetProcessMemoryInfo(handle,ctypes.byref(counters),counters.cb):
                memory+=int(counters.WorkingSetSize)
            alive+=1
        finally:kernel32.CloseHandle(handle)
    return cpu,memory,alive

def k6_command(base_url,summary_name,vus,duration):
    arguments=[
        DOCKER,"run","--rm","-e","BASE_URL="+base_url,"-e","VUS="+str(vus),
        "-e","DURATION="+duration,
        "--mount","type=bind,source="+str(LOAD_DIR)+",target=/scripts,readonly",
        "--mount","type=bind,source="+str(RAW_DIR)+",target=/results",
        "grafana/k6:latest","run",
    ]
    if summary_name:arguments.extend(["--summary-export","/results/"+summary_name])
    arguments.append("/scripts/performance-comparison.js")
    return arguments

def warmup(variant,base_url):
    log(variant+" 预热：10 VU，10 秒")
    path=RAW_DIR/(variant+"-warmup.log")
    with path.open("wb") as stream:
        result=subprocess.run(k6_command(base_url,None,10,"10s"),stdout=stream,stderr=subprocess.STDOUT)
    if result.returncode:raise RuntimeError(variant+" 预热失败")

def metric(summary,name,key,default=0):
    item=summary.get("metrics",{}).get(name,{})
    values=item.get("values",item)
    return values.get(key,default)

def formal_run(variant,run_number,base_url,pids):
    summary_name=variant+"-run"+str(run_number)+"-summary.json"
    stdout_path=RAW_DIR/(variant+"-run"+str(run_number)+"-k6.log")
    resource_path=RAW_DIR/(variant+"-run"+str(run_number)+"-resources.csv")
    log(variant+" 第 "+str(run_number)+" 次正式压测：50 VU，45 秒")
    with stdout_path.open("wb") as output:
        process=subprocess.Popen(k6_command(base_url,summary_name,50,"45s"),stdout=output,stderr=subprocess.STDOUT)
        samples=[]
        previous_cpu,_,_=process_sample(pids)
        previous_time=time.monotonic()
        while process.poll() is None:
            time.sleep(1)
            current_time=time.monotonic()
            current_cpu,memory,alive=process_sample(pids)
            elapsed=current_time-previous_time
            cpu_percent=max(0.0,(current_cpu-previous_cpu)/elapsed*100.0)
            samples.append({
                "elapsedSeconds":round(current_time-previous_time+len(samples),3),
                "cpuCorePercent":round(cpu_percent,3),
                "workingSetMiB":round(memory/1024/1024,3),
                "aliveProcesses":alive,
            })
            previous_cpu=current_cpu
            previous_time=current_time
    if process.returncode:raise RuntimeError(variant+" 第 "+str(run_number)+" 次 k6 失败")
    with resource_path.open("w",newline="",encoding="utf8") as stream:
        writer=csv.DictWriter(stream,fieldnames=samples[0].keys())
        writer.writeheader();writer.writerows(samples)
    summary=json.loads((RAW_DIR/summary_name).read_text(encoding="utf8"))
    cpu_values=[row["cpuCorePercent"] for row in samples]
    memory_values=[row["workingSetMiB"] for row in samples]
    result={
        "variant":variant,"run":run_number,
        "concurrency":50,"durationSeconds":45,
        "requests":int(metric(summary,"http_reqs","count")),
        "throughputRps":metric(summary,"http_reqs","rate"),
        "averageResponseMs":metric(summary,"http_req_duration","avg"),
        "p95ResponseMs":metric(summary,"http_req_duration","p(95)"),
        "errorRatePercent":metric(summary,"http_req_failed","rate",metric(summary,"http_req_failed","value"))*100,
        "checkRatePercent":metric(summary,"checks","rate",metric(summary,"checks","value"))*100,
        "averageCpuCorePercent":statistics.fmean(cpu_values),
        "peakCpuCorePercent":max(cpu_values),
        "averageWorkingSetMiB":statistics.fmean(memory_values),
        "peakWorkingSetMiB":max(memory_values),
        "sampleCount":len(samples),
    }
    (RAW_DIR/(variant+"-run"+str(run_number)+"-result.json")).write_text(
        json.dumps(result,ensure_ascii=False,indent=2),encoding="utf8"
    )
    log(variant+" 第 "+str(run_number)+" 次完成："+
        f"{result['throughputRps']:.2f} req/s，P95 {result['p95ResponseMs']:.2f} ms，错误率 {result['errorRatePercent']:.4f}%")
    return result

def start_monolith(password,run_number,database):
    log("启动单体版")
    env=os.environ.copy()
    env.update({
        "SPRING_DATASOURCE_URL":"jdbc:mysql://127.0.0.1:3306/"+database+"?characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
        "SPRING_DATASOURCE_USERNAME":"root",
        "SPRING_DATASOURCE_PASSWORD":password,
        "SPRING_JPA_HIBERNATE_DDL_AUTO":"validate",
        "SERVER_PORT":"8088",
    })
    output=(RAW_DIR/("monolith-run"+str(run_number)+"-app.log")).open("wb")
    process=subprocess.Popen(
        [JAVA,"-Xms128m","-Xmx512m","-jar",str(MONOLITH_JAR)],
        cwd=MONOLITH/"backend",env=env,stdout=output,stderr=subprocess.STDOUT,
        creationflags=getattr(subprocess,"CREATE_NO_WINDOW",0)
    )
    try:wait_endpoints("http://127.0.0.1:8088")
    except Exception:
        process.terminate();process.wait(timeout=15);output.close();raise
    return process,output

def stop_monolith(process,output):
    if process and process.poll() is None:
        process.terminate()
        try:process.wait(timeout=15)
        except subprocess.TimeoutExpired:
            process.kill();process.wait()
    if output:output.close()
    time.sleep(3)

def micro_pids():
    state=json.loads(STATE_FILE.read_text(encoding="utf-8-sig"))
    if isinstance(state,dict):state=[state]
    names={"user-service","product-service","trade-service","local-gateway"}
    return [int(item["Pid"]) for item in state if item["Name"] in names]

def aggregate(results,data_counts):
    grouped={}
    fields=(
        "requests","throughputRps","averageResponseMs","p95ResponseMs",
        "errorRatePercent","averageCpuCorePercent","peakCpuCorePercent",
        "averageWorkingSetMiB","peakWorkingSetMiB",
    )
    for variant in ("monolith","microservices"):
        runs=[row for row in results if row["variant"]==variant]
        grouped[variant]={
            field:{
                "mean":statistics.fmean(row[field] for row in runs),
                "sampleStdDev":statistics.stdev(row[field] for row in runs),
            } for field in fields
        }
    payload={
        "status":"COMPLETED",
        "generatedAt":datetime.now().astimezone().isoformat(),
        "conditions":{
            "machine":platform.platform(),
            "processor":os.environ.get("PROCESSOR_IDENTIFIER",""),
            "logicalCpuCount":os.cpu_count(),
            "concurrency":50,"durationPerRunSeconds":45,"runsPerVariant":3,
            "warmup":"10 VU / 10 seconds before each run",
            "endpoints":list(ENDPOINTS),
            "applicationMemoryScope":{
                "monolith":"one Java backend process",
                "microservices":"three Java service processes plus Node gateway; frontend excluded",
            },
            "database":"same local MySQL instance; source and split schemas have equal row counts",
        },
        "dataCounts":data_counts,"runs":results,"summary":grouped,
    }
    (RUN_DIR/"experiment-result.json").write_text(json.dumps(payload,ensure_ascii=False,indent=2),encoding="utf8")
    return payload

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("--dump",type=Path,required=True)
    parser.add_argument("--cleanup-orphan-db")
    args=parser.parse_args()
    if not JAVA or not DOCKER or not MYSQL.exists() or not MONOLITH_JAR.exists():
        raise RuntimeError("缺少 Java、Docker、mysql.exe 或单体可执行 JAR")
    log("性能对比实验目录："+str(RUN_DIR))
    password=getpass.getpass("请输入本地 MySQL root 密码（仅保留在本进程内存中）：")
    benchmark_db="secondhand_benchmark_perf_"+datetime.now().strftime("%Y%m%d%H%M%S")
    benchmark_created=False
    results=[]
    monolith_process=None;monolith_output=None
    source_hash=None
    try:
        if args.cleanup_orphan_db:
            orphan=args.cleanup_orphan_db
            if not orphan.startswith("secondhand_benchmark_perf_") or not orphan.replace("_","").isalnum():
                raise ValueError("待清理临时数据库名不安全")
            mysql_query(password,"DROP DATABASE "+orphan+";")
            log("已清理上次中断实验创建的临时库："+orphan)
        source_hash=prepare_benchmark_database(password,benchmark_db,args.dump)
        benchmark_created=True
        data_counts=verify_same_data(password,benchmark_db)
        (RUN_DIR/"source.json").write_text(json.dumps({
            "path":str(args.dump),"sha256":source_hash,"temporaryDatabase":benchmark_db
        },ensure_ascii=False,indent=2),encoding="utf8")
        stop_micro()
        order={
            1:("monolith","microservices"),
            2:("microservices","monolith"),
            3:("monolith","microservices"),
        }
        for run_number in (1,2,3):
            for variant in order[run_number]:
                if variant=="monolith":
                    stop_micro()
                    monolith_process,monolith_output=start_monolith(password,run_number,benchmark_db)
                    warmup(variant,"http://host.docker.internal:8088")
                    results.append(formal_run(variant,run_number,"http://host.docker.internal:8088",[monolith_process.pid]))
                    stop_monolith(monolith_process,monolith_output)
                    monolith_process=None;monolith_output=None
                else:
                    start_micro()
                    warmup(variant,"http://host.docker.internal:18080")
                    results.append(formal_run(variant,run_number,"http://host.docker.internal:18080",micro_pids()))
                    stop_micro()
                time.sleep(5)
        aggregate(results,data_counts)
        log("六次正式压测全部完成")
    finally:
        stop_monolith(monolith_process,monolith_output)
        try:start_micro()
        except Exception as error:log("警告：微服务复原失败："+repr(error))
        dropped=False
        try:
            if benchmark_created:
                mysql_query(password,"DROP DATABASE "+benchmark_db+";")
                dropped=True
                log("已删除本次新建的临时单体测试库："+benchmark_db)
        except Exception as error:log("警告：临时库删除失败："+repr(error))
        (RUN_DIR/"restoration.json").write_text(json.dumps({
            "monolithStopped":True,"microservicesStartAttempted":True,
            "temporaryDatabaseDropped":dropped,"mysqlKeptRunning":True
        },ensure_ascii=False,indent=2),encoding="utf8")
        password=None
    log("环境已复原：单体停止，微服务启动，MySQL 保持运行")
    log("结果文件："+str(RUN_DIR/"experiment-result.json"))

if __name__=="__main__":
    try:main()
    except Exception as error:
        log("实验失败："+type(error).__name__+" "+str(error))
        raise
