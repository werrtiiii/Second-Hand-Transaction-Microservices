#!/usr/bin/env bash
set -Eeuo pipefail
# 仅由全新命名空间初始化器调用；迁移账号与运行账号分离。
export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"
for service in user product trade; do
  upper="${service^^}"; app_variable="${upper}_DB_PASSWORD"; migration_variable="${upper}_MIGRATION_PASSWORD"
  app_password="${!app_variable}"; migration_password="${!migration_variable}"
  [[ "$app_password" =~ ^[A-Za-z0-9_]{16,}$ && "$migration_password" =~ ^[A-Za-z0-9_]{16,}$ ]] || exit 2
  mysql -uroot <<SQL
CREATE DATABASE secondhand_$service CHARACTER SET utf8mb4;
CREATE USER '${service}_app'@'%' IDENTIFIED BY '$app_password';
CREATE USER '${service}_migration'@'%' IDENTIFIED BY '$migration_password';
GRANT SELECT,INSERT,UPDATE,DELETE ON secondhand_$service.* TO '${service}_app'@'%';
GRANT ALL PRIVILEGES ON secondhand_$service.* TO '${service}_migration'@'%';
SQL
done
