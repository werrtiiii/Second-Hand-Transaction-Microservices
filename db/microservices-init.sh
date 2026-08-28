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
USE secondhand_user;
SOURCE /schemas/user.sql;
USE secondhand_product;
SOURCE /schemas/product.sql;
USE secondhand_trade;
SOURCE /schemas/trade.sql;
SQL
