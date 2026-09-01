[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$stateFile = Join-Path $projectRoot "reports\local-native\processes.json"
if (-not (Test-Path -LiteralPath $stateFile)) {
    Write-Host "没有找到本地进程记录：$stateFile"
    exit 0
}
$items = @(Get-Content -LiteralPath $stateFile -Raw -Encoding UTF8 | ConvertFrom-Json)
foreach ($item in ($items | Sort-Object StartedOrder -Descending)) {
    $process = Get-Process -Id $item.Pid -ErrorAction SilentlyContinue
    if (-not $process) {
        Write-Host "$($item.Name) 已停止（PID $($item.Pid)）"
        continue
    }
    try {
        Stop-Process -Id $item.Pid -Force -ErrorAction Stop
        Wait-Process -Id $item.Pid -Timeout 10 -ErrorAction SilentlyContinue
        Write-Host "已停止 $($item.Name)（PID $($item.Pid)）"
    } catch {
        Write-Warning "停止 $($item.Name) 失败：$($_.Exception.Message)"
    }
}
Remove-Item -LiteralPath $stateFile -Force -ErrorAction SilentlyContinue
