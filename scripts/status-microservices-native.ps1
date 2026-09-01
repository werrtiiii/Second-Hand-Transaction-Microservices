[CmdletBinding()]
param()

$ErrorActionPreference = "Continue"
$checks = @(
    @{ Name = "user-service"; Url = "http://127.0.0.1:18081/actuator/health/readiness" },
    @{ Name = "product-service"; Url = "http://127.0.0.1:18082/actuator/health/readiness" },
    @{ Name = "trade-service"; Url = "http://127.0.0.1:18083/actuator/health/readiness" },
    @{ Name = "local-gateway"; Url = "http://127.0.0.1:18080/healthz" },
    @{ Name = "frontend"; Url = "http://127.0.0.1:5173/" }
)
$results = foreach ($check in $checks) {
    try {
        $response = Invoke-WebRequest -Uri $check.Url -UseBasicParsing -TimeoutSec 3
        [pscustomobject]@{ Service = $check.Name; Status = "UP"; Http = $response.StatusCode; Url = $check.Url }
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "-" }
        [pscustomobject]@{ Service = $check.Name; Status = "DOWN"; Http = $status; Url = $check.Url }
    }
}
$results | Format-Table -AutoSize
