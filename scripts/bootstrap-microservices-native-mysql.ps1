[CmdletBinding()]
param(
    [string]$AdminUser = "root",
    [string]$MySqlExe = ""
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $projectRoot ".env"

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

function Resolve-MySqlExe {
    param([string]$Requested)
    if ($Requested) {
        if (-not (Test-Path -LiteralPath $Requested)) { throw "mysql.exe 不存在：$Requested" }
        return (Resolve-Path -LiteralPath $Requested).Path
    }
    $command = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    foreach ($candidate in @("E:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe", "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe")) {
        if (Test-Path -LiteralPath $candidate) { return $candidate }
    }
    throw "未找到 mysql.exe，请通过 -MySqlExe 指定完整路径。"
}

function Invoke-MySql {
    param([string]$Sql, [string]$Database = "")
    $arguments = @("--protocol=tcp", "--host=127.0.0.1", "--port=3306", "--user=$AdminUser",
        "--default-character-set=utf8mb4", "--batch", "--skip-column-names")
    if ($Database) { $arguments += "--database=$Database" }
    $output = $Sql | & $script:ResolvedMySql @arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw ($output -join [Environment]::NewLine) }
    return $output
}

if (-not (Test-Path -LiteralPath $envFile)) { throw "缺少配置文件：$envFile" }
$settings = Read-DotEnv -Path $envFile
$databases = @(
    @{ Name = "secondhand_user"; User = "user_app"; PasswordKey = "USER_DB_PASSWORD"; Migration = "user" },
    @{ Name = "secondhand_product"; User = "product_app"; PasswordKey = "PRODUCT_DB_PASSWORD"; Migration = "product" },
    @{ Name = "secondhand_trade"; User = "trade_app"; PasswordKey = "TRADE_DB_PASSWORD"; Migration = "trade" }
)
foreach ($item in $databases) {
    $password = $settings[$item.PasswordKey]
    if (-not $password) { throw ".env 缺少 $($item.PasswordKey)" }
    if ($password -notmatch "^[A-Za-z0-9_]{16,}$") { throw "$($item.PasswordKey) 只能包含字母、数字、下划线，且至少 16 位。" }
}
if ($AdminUser -notmatch "^[A-Za-z0-9_]+$") { throw "管理员用户名格式不安全。" }

$script:ResolvedMySql = Resolve-MySqlExe -Requested $MySqlExe
$securePassword = Read-Host "请输入本地 MySQL 管理员 $AdminUser 的密码" -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
    $plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    [Environment]::SetEnvironmentVariable("MYSQL_PWD", $plainPassword, "Process")
    $plainPassword = $null
    Invoke-MySql -Sql "SELECT VERSION();" | Out-Null
    Write-Host "已连接本地 MySQL。"

    foreach ($item in $databases) {
        $database = $item.Name
        $appUser = $item.User
        $appPassword = $settings[$item.PasswordKey]
        $accountSql = @"
CREATE DATABASE IF NOT EXISTS $database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$appUser'@'localhost' IDENTIFIED BY '$appPassword';
ALTER USER '$appUser'@'localhost' IDENTIFIED BY '$appPassword';
CREATE USER IF NOT EXISTS '$appUser'@'127.0.0.1' IDENTIFIED BY '$appPassword';
ALTER USER '$appUser'@'127.0.0.1' IDENTIFIED BY '$appPassword';
GRANT SELECT, INSERT, UPDATE, DELETE ON $database.* TO '$appUser'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON $database.* TO '$appUser'@'127.0.0.1';
FLUSH PRIVILEGES;
"@
        Invoke-MySql -Sql $accountSql | Out-Null
        $countSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$database';"
        $tableCountText = (Invoke-MySql -Sql $countSql | Select-Object -Last 1).ToString().Trim()
        $tableCount = [int]$tableCountText
        if ($tableCount -eq 0) {
            $migrationDir = Join-Path $projectRoot ("db\" + $item.Migration)
            foreach ($migration in (Get-ChildItem -LiteralPath $migrationDir -Filter "V*.sql" | Sort-Object Name)) {
                Write-Host "应用迁移：$database/$($migration.Name)"
                Invoke-MySql -Sql (Get-Content -LiteralPath $migration.FullName -Raw -Encoding UTF8) -Database $database | Out-Null
            }
        } else {
            Write-Host "$database 已有 $tableCount 张表，跳过首次迁移，避免覆盖现有数据。"
        }
    }
    Write-Host "本地微服务数据库初始化完成。"
}
finally {
    [Environment]::SetEnvironmentVariable("MYSQL_PWD", $null, "Process")
    if ($pointer -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
}
