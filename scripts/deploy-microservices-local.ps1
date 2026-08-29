param(
  [Parameter(Mandatory=$true)][ValidatePattern('^[a-z0-9][a-z0-9._-]+$')][string]$Version,
  [switch]$SkipBuild
)
$ErrorActionPreference = 'Stop'
function Invoke-Docker {
  # Docker将正常进度写到stderr；按退出码判定，兼容PowerShell 5.1重定向。
  $ErrorActionPreference = 'Continue'
  & docker @args 2>&1 | ForEach-Object { "$_" }
  if ($LASTEXITCODE -ne 0) { throw "Docker命令失败，退出码：$LASTEXITCODE" }
}

$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
  # 固定 Compose 项目与端口，不操作旧单体的容器或数据卷。
  $env:APP_VERSION = $Version
  if (-not $SkipBuild) {
    Invoke-Docker compose -f compose.microservices.yml build
    if ($LASTEXITCODE -ne 0) { throw '镜像构建失败，停止部署' }
  }
  # 发布前检查所有版本镜像，缺失时保留正在运行的版本。
  $imagePrefix = if ($env:IMAGE_PREFIX) { $env:IMAGE_PREFIX } else { 'secondhand-microservices' }
  foreach ($service in @('user-service','product-service','trade-service','gateway')) {
    Invoke-Docker image inspect "$imagePrefix/${service}:$Version" --format '{{.Id}}' | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "缺少已验证镜像：${service}:$Version" }
  }
  Invoke-Docker compose -f compose.microservices.yml up -d --no-build --pull never
  if ($LASTEXITCODE -ne 0) { throw '容器启动失败' }
  $deadline = (Get-Date).AddSeconds(120)
  do {
    $ready = $true
    foreach ($port in @(18081,18082,18083)) {
      try { if ((Invoke-RestMethod "http://127.0.0.1:$port/actuator/health/readiness" -TimeoutSec 3).status -ne 'UP') { $ready=$false } }
      catch { $ready=$false }
    }
    if (-not $ready) { Start-Sleep -Seconds 2 }
  } while (-not $ready -and (Get-Date) -lt $deadline)
  if (-not $ready) { throw '就绪检查未通过' }
  python scripts/smoke-microservices.py
  if ($LASTEXITCODE -ne 0) { throw '网关业务验证未通过' }
  New-Item -ItemType Directory -Force reports/local-runtime | Out-Null
  Invoke-Docker compose -f compose.microservices.yml ps > reports/local-runtime/containers.txt
  foreach ($port in @(18081,18082,18083)) {
    $info=Invoke-RestMethod "http://127.0.0.1:$port/actuator/info"
    if ($info.app.version -ne $Version) { throw "服务 $port 版本不一致" }
  }
  Write-Output "部署及版本验证通过：$Version"
} catch {
  New-Item -ItemType Directory -Force reports/local-runtime | Out-Null
  Invoke-Docker compose -f compose.microservices.yml logs --tail 200 > reports/local-runtime/deployment-failure.log
  throw
} finally { Pop-Location }
