[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$SkipNpmInstall,
    [string]$Version = "local-native"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot ".env"
$reportDir = Join-Path $projectRoot "reports\local-native"
$logDir = Join-Path $reportDir "logs"
$stateFile = Join-Path $reportDir "processes.json"

function Read-DotEnv {
    param([string]$Path)
    $values = @{}
    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith("#")) { continue }
        $index = $trimmed.IndexOf("=")
        if ($index -lt 1) { continue }
        $name = $trimmed.Substring(0, $index).Trim()
        $value = $trimmed.Substring($index + 1).Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        $values[$name] = $value
    }
    return $values
}

function Resolve-Maven {
    $command = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    foreach ($candidate in @(
        "C:\Users\hyl\.m2\wrapper\dists\apache-maven-3.9.8-bin\337e6d14\apache-maven-3.9.8\bin\mvn.cmd",
        "E:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd"
    )) {
        if (Test-Path -LiteralPath $candidate) { return $candidate }
    }
    throw "未找到 Maven，请安装 Maven 或将 mvn.cmd 加入 PATH。"
}

function Assert-PortFree {
    param([int]$Port)
    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($listener) {
        $owners = ($listener | Select-Object -ExpandProperty OwningProcess -Unique) -join ","
        throw "端口 $Port 已被进程 $owners 占用。请先停止该进程。"
    }
}

function Start-WithEnvironment {
    param(
        [string]$Name, [string]$FilePath, [string[]]$Arguments,
        [hashtable]$Variables, [int]$Order, [string]$WorkingDirectory = $projectRoot
    )
    $original = @{}
    try {
        foreach ($key in $Variables.Keys) {
            $original[$key] = [Environment]::GetEnvironmentVariable($key, "Process")
            [Environment]::SetEnvironmentVariable($key, [string]$Variables[$key], "Process")
        }
        $stdout = Join-Path $logDir ($Name + ".out.log")
        $stderr = Join-Path $logDir ($Name + ".err.log")
        $process = Start-Process -FilePath $FilePath -ArgumentList $Arguments -WorkingDirectory $WorkingDirectory -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru
        return [pscustomobject]@{ Name = $Name; Pid = $process.Id; StartedOrder = $Order; StartedAt = (Get-Date).ToString("o") }
    }
    finally {
        foreach ($key in $Variables.Keys) {
            [Environment]::SetEnvironmentVariable($key, $original[$key], "Process")
        }
    }
}

function Wait-Http {
    param([string]$Name, [string]$Url, [int]$TimeoutSeconds = 90)
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 400) {
                Write-Host "$Name 已就绪：$Url"
                return
            }
        } catch {}
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)
    throw "$Name 在 $TimeoutSeconds 秒内未就绪，请检查 $logDir。"
}

if (-not (Test-Path -LiteralPath $envFile)) { throw "缺少配置文件：$envFile" }
if (Test-Path -LiteralPath $stateFile) { throw "检测到已有本地进程记录。请先运行 .\scripts\stop-microservices-native.ps1" }
foreach ($port in 18080, 18081, 18082, 18083, 5173) { Assert-PortFree -Port $port }

$settings = Read-DotEnv -Path $envFile
foreach ($name in @(
    "USER_DB_PASSWORD", "PRODUCT_DB_PASSWORD", "TRADE_DB_PASSWORD",
    "USER_PRIVATE_KEY", "PRODUCT_PRIVATE_KEY", "TRADE_PRIVATE_KEY",
    "USER_PUBLIC_KEY", "PRODUCT_PUBLIC_KEY", "TRADE_PUBLIC_KEY"
)) {
    if (-not $settings[$name]) { throw ".env 缺少 $name" }
}

New-Item -ItemType Directory -Path $logDir -Force | Out-Null
$uploadRoot = Join-Path $reportDir "uploads"
foreach ($name in "user", "product", "trade") {
    New-Item -ItemType Directory -Path (Join-Path $uploadRoot $name) -Force | Out-Null
}

if (-not $SkipBuild) {
    $maven = Resolve-Maven
    Write-Host "构建后端..."
    & $maven "-DskipTests" "package"
    if ($LASTEXITCODE -ne 0) { throw "Maven 构建失败。" }
}

$frontendDir = Join-Path $projectRoot "frontend"
if (-not $SkipNpmInstall -and -not (Test-Path -LiteralPath (Join-Path $frontendDir "node_modules"))) {
    Write-Host "安装前端依赖..."
    Push-Location $frontendDir
    try {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw "npm ci 失败。" }
    } finally {
        Pop-Location
    }
}

$java = (Get-Command java.exe -ErrorAction Stop).Source
$node = (Get-Command node.exe -ErrorAction Stop).Source
$vite = Join-Path $frontendDir "node_modules\vite\bin\vite.js"
if (-not (Test-Path -LiteralPath $vite)) { throw "未找到 Vite。请去掉 -SkipNpmInstall 后重试。" }

$serviceDefinitions = @(
    @{ Name = "user-service"; Short = "user"; Port = 18081; Db = "secondhand_user"; DbUser = "user_app"; PasswordKey = "USER_DB_PASSWORD"; PrivateKey = "USER_PRIVATE_KEY" },
    @{ Name = "product-service"; Short = "product"; Port = 18082; Db = "secondhand_product"; DbUser = "product_app"; PasswordKey = "PRODUCT_DB_PASSWORD"; PrivateKey = "PRODUCT_PRIVATE_KEY" },
    @{ Name = "trade-service"; Short = "trade"; Port = 18083; Db = "secondhand_trade"; DbUser = "trade_app"; PasswordKey = "TRADE_DB_PASSWORD"; PrivateKey = "TRADE_PRIVATE_KEY" }
)
$started = @()
try {
    $order = 0
    foreach ($service in $serviceDefinitions) {
        $targetDir = Join-Path $projectRoot ("services\" + $service.Short + "-service\target")
        $jar = Get-ChildItem -LiteralPath $targetDir -Filter ($service.Short + "-service-*-exec.jar") -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if (-not $jar) { throw "未找到 $($service.Name) 可执行 JAR，请不要使用 -SkipBuild。" }

        $variables = @{
            PORT = $service.Port
            DB_URL = "jdbc:mysql://127.0.0.1:3306/$($service.Db)?allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&useSSL=false"
            DB_USERNAME = $service.DbUser
            DB_PASSWORD = $settings[$service.PasswordKey]
            SERVICE_PRIVATE_KEY = $settings[$service.PrivateKey]
            USER_PUBLIC_KEY = $settings["USER_PUBLIC_KEY"]
            PRODUCT_PUBLIC_KEY = $settings["PRODUCT_PUBLIC_KEY"]
            TRADE_PUBLIC_KEY = $settings["TRADE_PUBLIC_KEY"]
            USER_SERVICE_URL = "http://127.0.0.1:18081"
            PRODUCT_SERVICE_URL = "http://127.0.0.1:18082"
            TRADE_SERVICE_URL = "http://127.0.0.1:18083"
            UPLOAD_DIR = (Join-Path $uploadRoot $service.Short)
            APP_VERSION = $Version
            MOCK_PAYMENTS_ENABLED = "true"
            ADMIN_BOOTSTRAP_ENABLED = "false"
        }
        $order++
        $started += Start-WithEnvironment -Name $service.Name -FilePath $java -Arguments @("-Xms128m", "-Xmx512m", "-jar", $jar.FullName) -Variables $variables -Order $order
        Write-Host "已启动 $($service.Name)，PID $($started[-1].Pid)"
    }

    $order++
    $gateway = Start-WithEnvironment -Name "local-gateway" -FilePath $node -Arguments @((Join-Path $PSScriptRoot "local-gateway.mjs")) -Variables @{} -Order $order
    $started += $gateway
    Write-Host "已启动 local-gateway，PID $($gateway.Pid)"

    $order++
    $frontend = Start-WithEnvironment -Name "frontend" -FilePath $node -Arguments @($vite, "--host", "127.0.0.1", "--port", "5173") -Variables @{} -Order $order -WorkingDirectory $frontendDir
    $started += $frontend
    Write-Host "已启动 frontend，PID $($frontend.Pid)"

    $started | ConvertTo-Json | Set-Content -LiteralPath $stateFile -Encoding UTF8
    Wait-Http -Name "user-service" -Url "http://127.0.0.1:18081/actuator/health/readiness"
    Wait-Http -Name "product-service" -Url "http://127.0.0.1:18082/actuator/health/readiness"
    Wait-Http -Name "trade-service" -Url "http://127.0.0.1:18083/actuator/health/readiness"
    Wait-Http -Name "local-gateway" -Url "http://127.0.0.1:18080/healthz"
    Wait-Http -Name "frontend" -Url "http://127.0.0.1:5173/"

    Write-Host ""
    Write-Host "本地部署完成。"
    Write-Host "前端：http://127.0.0.1:5173/"
    Write-Host "网关：http://127.0.0.1:18080/"
    Write-Host "日志：$logDir"
}
catch {
    Write-Warning $_.Exception.Message
    foreach ($item in ($started | Sort-Object StartedOrder -Descending)) {
        Stop-Process -Id $item.Pid -Force -ErrorAction SilentlyContinue
    }
    Remove-Item -LiteralPath $stateFile -Force -ErrorAction SilentlyContinue
    throw
}
