"""将旧单体 SQL dump 安全覆盖导入本地拆分数据库。"""
import argparse,getpass,hashlib,json,os,shutil,subprocess,sys
from datetime import datetime
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/"scripts"))
import import_legacy_data as legacy

DATABASES=("secondhand_user","secondhand_product","secondhand_trade")
POWERSHELL=Path(r"C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe")

def find_tool(name):
    found=shutil.which(name)
    if found:return found
    for base in (Path(r"E:\Program Files\MySQL\MySQL Server 8.0\bin"),Path(r"C:\Program Files\MySQL\MySQL Server 8.0\bin")):
        candidate=base/name
        if candidate.exists():return str(candidate)
    raise FileNotFoundError("未找到 "+name)

class NativeMySql:
    def __init__(self,user,password):
        self.user=user
        self.env=os.environ.copy()
        self.env["MYSQL_PWD"]=password
        self.mysql=find_tool("mysql.exe")
        self.mysqldump=find_tool("mysqldump.exe")
    def execute(self,statement):
        process=subprocess.run([
            self.mysql,"--protocol=tcp","--host=127.0.0.1","--port=3306",
            "--user="+self.user,"--default-character-set=utf8mb4",
            "--batch","--skip-column-names"
        ],input=statement.encode("utf8"),stdout=subprocess.PIPE,stderr=subprocess.PIPE,env=self.env)
        if process.returncode:
            message=process.stderr.decode("utf8",errors="replace").strip()
            raise RuntimeError("MySQL 执行失败："+message[:500])
        return process.stdout.decode("utf8")
    def backup(self,output):
        process=subprocess.run([
            self.mysqldump,"--protocol=tcp","--host=127.0.0.1","--port=3306",
            "--user="+self.user,"--single-transaction","--hex-blob","--no-tablespaces",
            "--set-gtid-purged=OFF","--default-character-set=utf8mb4","--databases",*DATABASES
        ],stdout=subprocess.PIPE,stderr=subprocess.PIPE,env=self.env)
        if process.returncode:
            raise RuntimeError("备份失败："+process.stderr.decode("utf8",errors="replace")[:500])
        if b"Dump completed" not in process.stdout:raise RuntimeError("备份文件不完整")
        output.write_bytes(process.stdout)
        return hashlib.sha256(process.stdout).hexdigest()

def owned(service):
    return [table for table,owner in legacy.OWNER.items() if owner==service]

def counts(mysql):
    result={}
    for service,extra in legacy.EXTRA.items():
        database="secondhand_"+service
        for table in owned(service)+extra:
            result[database+"."+table]=int(mysql.execute(
                "SELECT COUNT(*) FROM "+database+"."+table+";"
            ).strip())
    return result

def validate_schema(mysql):
    for service,extra in legacy.EXTRA.items():
        database="secondhand_"+service
        expected=set(owned(service))|set(extra)
        output=mysql.execute(
            "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='"
            +database+"' ORDER BY TABLE_NAME;"
        )
        actual={line for line in output.splitlines() if line}
        if actual!=expected:
            raise ValueError(database+" 表结构不匹配，缺少="+str(sorted(expected-actual))+"，多余="+str(sorted(actual-expected)))
        engines=mysql.execute(
            "SELECT DISTINCT ENGINE FROM information_schema.TABLES WHERE TABLE_SCHEMA='"
            +database+"';"
        ).splitlines()
        if engines!=["InnoDB"]:raise ValueError(database+" 存在非 InnoDB 表")

def stop_services():
    state=ROOT/"reports"/"local-native"/"processes.json"
    if not state.exists():return False
    subprocess.run([
        str(POWERSHELL),"-NoProfile","-ExecutionPolicy","Bypass","-File",
        str(ROOT/"scripts"/"stop-microservices-native.ps1")
    ],cwd=ROOT,check=True)
    return True

def start_services():
    subprocess.run([
        str(POWERSHELL),"-NoProfile","-ExecutionPolicy","Bypass","-File",
        str(ROOT/"scripts"/"start-microservices-native.ps1"),
        "-SkipBuild","-SkipNpmInstall","-Version","local-native-imported"
    ],cwd=ROOT,check=True)

def import_sql(imported,extra):
    statements=[
        "SET NAMES utf8mb4;","SET SESSION time_zone='+00:00';",
        "SET FOREIGN_KEY_CHECKS=0;","START TRANSACTION;"
    ]
    # 先删除子表，再删除父表。
    for service,extra_tables in legacy.EXTRA.items():
        database="secondhand_"+service
        for table in reversed(owned(service)+extra_tables):
            statements.append("DELETE FROM "+database+"."+table+";")
    for table,rows in imported.items():
        database="secondhand_"+legacy.OWNER[table]
        statements.extend(legacy.insert(database,table,row) for row in rows)
    for service,tables in extra.items():
        database="secondhand_"+service
        for table,rows in tables.items():
            statements.extend(legacy.insert(database,table,row) for row in rows)
    statements.extend(["COMMIT;","SET FOREIGN_KEY_CHECKS=1;"])
    return "\n".join(statements)

def verify(mysql,source,imported,extra):
    after=counts(mysql)
    for table,rows in imported.items():
        key="secondhand_"+legacy.OWNER[table]+"."+table
        if after[key]!=len(rows):raise AssertionError(key+" 行数不匹配")
    for service,tables in extra.items():
        for table,rows in tables.items():
            key="secondhand_"+service+"."+table
            if after[key]!=len(rows):raise AssertionError(key+" 行数不匹配")
    # 逐列比对原始值，避免只校验数量。
    for table,data in source.items():
        database="secondhand_"+legacy.OWNER[table]
        for row in data["rows"]:
            clauses=[column+" <=> "+legacy.sql_value(value) for column,value in row.items()]
            value=mysql.execute(
                "SELECT COUNT(*) FROM "+database+"."+table+" WHERE "+" AND ".join(clauses)+";"
            ).strip()
            if value!="1":raise AssertionError(database+"."+table+" 导入值不一致")
    return after

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument("dump",type=Path)
    parser.add_argument("--replace",action="store_true")
    parser.add_argument("--admin-user",default="root")
    args=parser.parse_args()
    source=legacy.parse_dump(args.dump)
    legacy.validate(source)
    legacy.build_data(source,{})
    source_counts={table:len(data["rows"]) for table,data in source.items()}
    print("源数据校验通过："+json.dumps(source_counts,ensure_ascii=False))
    if not args.replace:
        print("DRY RUN：未修改数据库")
        return

    password=getpass.getpass("请输入本地 MySQL 管理员 "+args.admin_user+" 的密码：")
    mysql=NativeMySql(args.admin_user,password)
    mysql.execute("SELECT VERSION();")
    validate_schema(mysql)
    directory=ROOT/"reports"/"local-native"/("data-import-"+datetime.now().strftime("%Y%m%d-%H%M%S"))
    directory.mkdir(parents=True)
    report={
        "sourceSha256":hashlib.sha256(args.dump.read_bytes()).hexdigest(),
        "target":"127.0.0.1:3306","sourceCounts":source_counts,"status":"STARTED"
    }
    stopped=False
    committed=False
    try:
        stopped=stop_services()
        report["beforeCounts"]=counts(mysql)
        report["backupSha256"]=mysql.backup(directory/"before.sql")
        print("导入前备份完成："+str(directory/"before.sql"))
        versions={
            int(user):int(version) for user,version in (
                line.split("\t") for line in mysql.execute(
                    "SELECT user_id,token_version FROM secondhand_user.user_security_state;"
                ).splitlines()
            )
        }
        imported,extra=legacy.build_data(source,versions)
        mysql.execute(import_sql(imported,extra))
        committed=True
        report["afterCounts"]=verify(mysql,source,imported,extra)
        report["compatibilityCounts"]={
            service:{table:len(rows) for table,rows in tables.items()}
            for service,tables in extra.items()
        }
        report["status"]="IMPORTED_AND_VERIFIED"
        print("原始数据和兼容数据均已导入并逐行验证。")
    except Exception as error:
        report["status"]="FAILED_AFTER_COMMIT" if committed else "FAILED_WITHOUT_COMMIT"
        report["error"]=type(error).__name__
        raise
    finally:
        (directory/"import-report.json").write_text(
            json.dumps(report,ensure_ascii=False,indent=2),encoding="utf8"
        )
        if stopped and (report["status"]=="IMPORTED_AND_VERIFIED" or not committed):
            start_services()
    print("服务已重新启动。导入报告："+str(directory/"import-report.json"))

if __name__=="__main__":
    main()
