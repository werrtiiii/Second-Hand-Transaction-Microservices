param([Parameter(Mandatory=$true)][ValidatePattern('^[a-z0-9][a-z0-9._-]+$')][string]$Version)
$ErrorActionPreference='Stop'
# 回滚使用本机已经验证过的不可变镜像，保留数据库、上传文件与迁移记录。
& "$PSScriptRoot/deploy-microservices-local.ps1" -Version $Version -SkipBuild
