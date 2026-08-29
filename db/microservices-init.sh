#!/usr/bin/env bash
set -Eeuo pipefail
# 仅用于新建的开发数据库卷；密码限制字符集，避免被插入 SQL 语句。
for value in "${USER_DB_PASSWORD}" "${PRODUCT_DB_PASSWORD}" "${TRADE_DB_PASSWORD}"; do
  [[ "$value" =~ ^[A-Za-z0-9_]{16,}$ ]] || { echo "Database password format invalid" >&2; exit 1; }
done
export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"
mysql --protocol=socket -uroot <<SQL
CREATE DATABASE secondhand_user CHARACTER SET utf8mb4;
CREATE DATABASE secondhand_product CHARACTER SET utf8mb4;
CREATE DATABASE secondhand_trade CHARACTER SET utf8mb4;
CREATE USER 'user_app'@'%' IDENTIFIED BY '$USER_DB_PASSWORD';
CREATE USER 'product_app'@'%' IDENTIFIED BY '$PRODUCT_DB_PASSWORD';
CREATE USER 'trade_app'@'%' IDENTIFIED BY '$TRADE_DB_PASSWORD';
GRANT SELECT,INSERT,UPDATE,DELETE ON secondhand_user.* TO 'user_app'@'%';
GRANT SELECT,INSERT,UPDATE,DELETE ON secondhand_product.* TO 'product_app'@'%';
GRANT SELECT,INSERT,UPDATE,DELETE ON secondhand_trade.* TO 'trade_app'@'%';
SQL
# 初始化使用临时 socket；运行期升级使用独立 DDL 账号的 TCP 连接。
for service in user product trade; do
  database="secondhand_$service"
  mysql --protocol=socket -uroot "$database" -e 'CREATE TABLE schema_history(version VARCHAR(100) PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at DATETIME NOT NULL)'
  for file in /schemas/"$service"/V*.sql; do
    mysql --protocol=socket -uroot "$database" < "$file"
    version="$(basename "$file")"; checksum="$(tr -d '\r' < "$file" | sha256sum | cut -d' ' -f1)"
    mysql --protocol=socket -uroot "$database" -e "INSERT INTO schema_history VALUES('$version','$checksum',NOW())"
  done
done
