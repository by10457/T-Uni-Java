#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)
RENAME_ADMIN="true"
OLD_ROOT_PACKAGE="t.uni"
OLD_ENV_PREFIX="T_UNI"
OLD_SLUG="t-uni"

usage() {
  cat <<'EOF'
Usage: verify-rename.sh [options]

Options:
  --rename-admin true|false       Whether admin old names should fail, default true
  --old-root-package <value>      Old Java root package, default t.uni
  --old-env-prefix <value>        Old env prefix, default T_UNI
  --old-slug <value>              Old repo/module slug, default t-uni
  -h, --help                      Show help
EOF
}

while [ $# -gt 0 ]; do
  case "$1" in
    --rename-admin) RENAME_ADMIN="$2"; shift 2 ;;
    --old-root-package) OLD_ROOT_PACKAGE="$2"; shift 2 ;;
    --old-env-prefix) OLD_ENV_PREFIX="$2"; shift 2 ;;
    --old-slug) OLD_SLUG="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "错误: 未知参数 $1" >&2; usage; exit 1 ;;
  esac
done

RENAME_ADMIN=$(printf '%s' "$RENAME_ADMIN" | tr '[:upper:]' '[:lower:]')
if [ "$RENAME_ADMIN" != "true" ] && [ "$RENAME_ADMIN" != "false" ]; then
  echo "错误: --rename-admin 只能是 true 或 false" >&2
  exit 1
fi

cd "$PROJECT_ROOT"

if command -v rg >/dev/null 2>&1; then
  SEARCH=(rg -n --hidden --glob '!.git/**' --glob '!target/**' --glob '!**/target/**' --glob '!node_modules/**' --glob '!**/node_modules/**' --glob '!.idea/**' --glob '!**/.idea/**' --glob '!.claude/**' --glob '!**/.claude/**' --glob '!scripts/rename-project/**' --glob '!.env' --glob '!.env.*' --glob '.env.example')
else
  SEARCH=(grep -RIn --exclude-dir=.git --exclude-dir=target --exclude-dir=node_modules --exclude-dir=.idea --exclude-dir=.claude --exclude-dir=scripts/rename-project --exclude=.env --exclude=.env.* --include=.env.example)
fi

failures=0
warnings=0

run_scan() {
  local title="$1"
  local pattern="$2"
  local fail="$3"
  local filter_admin="${4:-false}"
  local output

  set +e
  if [ "${SEARCH[0]}" = "rg" ]; then
    output=$("${SEARCH[@]}" -- "$pattern" . 2>/dev/null)
  else
    output=$("${SEARCH[@]}" -E "$pattern" . 2>/dev/null)
  fi
  local code=$?
  set -e

  if [ "$filter_admin" = "true" ] && [ "$RENAME_ADMIN" = "false" ] && [ -n "$output" ]; then
    output=$(printf '%s\n' "$output" | grep -Ev 't-uni-admin|tuni-admin|tuni_admin|t_uni_admin|T_UNI_ADMIN' || true)
    [ -n "$output" ] || code=1
  fi

  echo ""
  echo "## $title"
  if [ $code -eq 0 ] && [ -n "$output" ]; then
    printf '%s\n' "$output"
    if [ "$fail" = "true" ]; then
      failures=$((failures + 1))
    else
      warnings=$((warnings + 1))
    fi
  else
    echo "未发现残留。"
  fi
}

escaped_root=$(printf '%s' "$OLD_ROOT_PACKAGE" | sed 's/[.[\*^$()+?{}|]/\\&/g')
escaped_env=$(printf '%s' "$OLD_ENV_PREFIX" | sed 's/[.[\*^$()+?{}|]/\\&/g')
escaped_slug=$(printf '%s' "$OLD_SLUG" | sed 's/[.[\*^$()+?{}|]/\\&/g')
old_path=${OLD_ROOT_PACKAGE//./\/}

if [ "$RENAME_ADMIN" = "true" ]; then
  admin_fail="true"
else
  admin_fail="false"
fi

echo "========================================="
echo "  项目改名残留验证"
echo "========================================="
echo "仓库根目录: $PROJECT_ROOT"
echo "rename_admin: $RENAME_ADMIN"

run_scan "Java/package 残留" "\\b${escaped_root}\\b|${old_path}" true true
run_scan "Maven/module 残留" "${escaped_slug}|t-uni-common|t-uni-server" true true
run_scan "YAML/env/config 前缀残留" "${escaped_env}(_|\\b)|\\bt\\.uni\\s*:|\\bt\\.uni\\.(jwt|master|redis|qiniu|wx|payment)" true true
run_scan "Docker/compose/script 残留" "tuni-mysql|tuni-redis|tuni-${escaped_slug#t-}|logs/t-uni|data/t-uni|image: t-uni|container_name: t-uni" true true
run_scan ".env.example 残留" "${escaped_env}|${escaped_slug}|tuni|t_uni" true true
run_scan "OpenIM 默认值残留" "tuni_|tuni_system" true true
run_scan "MinIO / admin 资源残留" "tuni-admin|tuni_admin|t_uni_admin|${escaped_env}_ADMIN" "$admin_fail"
run_scan "README / docs / prompts / skills 展示文案残留" "T-Uni|T-Uni-Java|${escaped_slug}|${escaped_env}" true true

if [ "$RENAME_ADMIN" = "false" ]; then
  run_scan "admin 目录保留项，仅人工复核" "t-uni-admin|tuni_admin|t_uni_admin|${escaped_env}_ADMIN|\\bt\\.uni\\b" false
fi

echo ""
echo "========================================="
echo "  验证摘要"
echo "========================================="
echo "失败分类数: $failures"
echo "人工复核分类数: $warnings"

if [ "$failures" -gt 0 ]; then
  echo "验证未通过：仍有旧命名残留需要处理。" >&2
  exit 1
fi

echo "验证通过：未发现必须失败的旧命名残留。"
