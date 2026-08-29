#!/usr/bin/env bash
set -Eeuo pipefail
# 仅连接指定服务数据库；应用账号不负责 DDL。失败保留现场，不自动回退数据。
: "${DB_HOST:?}" "${DB_NAME:?}" "${DB_USERNAME:?}" "${DB_PASSWORD:?}" "${MIGRATIONS_DIR:?}"
[[ "$DB_NAME" =~ ^secondhand_(user|product|trade)$ ]] || { echo 'Unexpected database name' >&2; exit 2; }
export MYSQL_PWD="$DB_PASSWORD"
mysql_args=(--protocol=tcp -h "$DB_HOST" -u "$DB_USERNAME" --database="$DB_NAME" --batch --skip-column-names)
mysql "${mysql_args[@]}" -e 'CREATE TABLE IF NOT EXISTS schema_history(version VARCHAR(100) PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at DATETIME NOT NULL)'
shopt -s nullglob
files=("$MIGRATIONS_DIR"/V*.sql)
((${#files[@]})) || { echo 'No migrations found' >&2; exit 3; }
for file in "${files[@]}"; do
  version="$(basename "$file")"
  [[ "$version" =~ ^V[0-9]+__[A-Za-z0-9_]+\.sql$ ]] || exit 4
  checksum="$(tr -d '\r' < "$file" | sha256sum | cut -d' ' -f1)"
  applied="$(mysql "${mysql_args[@]}" -e "SELECT checksum FROM schema_history WHERE version='$version'")"
  if [[ -n "$applied" ]]; then
    [[ "$applied" == "$checksum" ]] || { echo "Applied migration changed: $version" >&2; exit 5; }
    continue
  fi
  mysql "${mysql_args[@]}" < "$file"
  mysql "${mysql_args[@]}" -e "INSERT INTO schema_history VALUES('$version','$checksum',NOW())"
  echo "Applied $DB_NAME/$version"
done
