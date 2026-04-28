#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/../.." && pwd)

DISPLAY_NAME=""
REPO_SLUG=""
GROUP_ID=""
ROOT_PACKAGE=""
CONFIG_PREFIX=""
ENV_PREFIX=""
SERVER_DB_NAME=""
ADMIN_DB_NAME=""
COMMON_MODULE=""
SERVER_MODULE=""
ADMIN_MODULE=""
RENAME_ADMIN="false"
DRY_RUN="false"
CONFIG_FILE=""

usage() {
  cat <<'EOF'
Usage: rename-project.sh [options]

Options:
  --config <file>              Load KEY=VALUE pairs before CLI options
  --display-name <value>       New display name, e.g. "Wxy Server"
  --repo-slug <value>          New repository slug, e.g. wxy-server
  --group-id <value>           New Maven groupId, e.g. com.wxy
  --root-package <value>       New Java root package, e.g. com.wxy
  --config-prefix <value>      New Spring config prefix, e.g. wxy
  --env-prefix <value>         New env prefix, e.g. WXY
  --server-db-name <value>     New server database name, e.g. wxy
  --admin-db-name <value>      New admin database name, e.g. wxy_admin
  --common-module <value>      New common module directory/artifactId
  --server-module <value>      New server module directory/artifactId
  --admin-module <value>       New admin module directory/artifactId
  --rename-admin true|false    Whether to rename admin module, default false
  --dry-run                    Print planned changes without writing files
  -h, --help                   Show help
EOF
}

load_config() {
  local file="$1"
  if [ ! -f "$file" ]; then
    echo "错误: 配置文件不存在: $file" >&2
    exit 1
  fi

  while IFS='=' read -r key value || [ -n "${key:-}" ]; do
    case "$key" in
      ''|'#'*) continue ;;
    esac
    value=${value%$'\r'}
    value=${value%\"}
    value=${value#\"}
    case "$key" in
      DISPLAY_NAME) DISPLAY_NAME="$value" ;;
      REPO_SLUG) REPO_SLUG="$value" ;;
      GROUP_ID) GROUP_ID="$value" ;;
      ROOT_PACKAGE) ROOT_PACKAGE="$value" ;;
      CONFIG_PREFIX) CONFIG_PREFIX="$value" ;;
      ENV_PREFIX) ENV_PREFIX="$value" ;;
      SERVER_DB_NAME) SERVER_DB_NAME="$value" ;;
      ADMIN_DB_NAME) ADMIN_DB_NAME="$value" ;;
      COMMON_MODULE) COMMON_MODULE="$value" ;;
      SERVER_MODULE) SERVER_MODULE="$value" ;;
      ADMIN_MODULE) ADMIN_MODULE="$value" ;;
      RENAME_ADMIN) RENAME_ADMIN="$value" ;;
    esac
  done < "$file"
}

while [ $# -gt 0 ]; do
  case "$1" in
    --config) CONFIG_FILE="$2"; shift 2 ;;
    --display-name) DISPLAY_NAME="$2"; shift 2 ;;
    --repo-slug) REPO_SLUG="$2"; shift 2 ;;
    --group-id) GROUP_ID="$2"; shift 2 ;;
    --root-package) ROOT_PACKAGE="$2"; shift 2 ;;
    --config-prefix) CONFIG_PREFIX="$2"; shift 2 ;;
    --env-prefix) ENV_PREFIX="$2"; shift 2 ;;
    --server-db-name) SERVER_DB_NAME="$2"; shift 2 ;;
    --admin-db-name) ADMIN_DB_NAME="$2"; shift 2 ;;
    --common-module) COMMON_MODULE="$2"; shift 2 ;;
    --server-module) SERVER_MODULE="$2"; shift 2 ;;
    --admin-module) ADMIN_MODULE="$2"; shift 2 ;;
    --rename-admin) RENAME_ADMIN="$2"; shift 2 ;;
    --dry-run) DRY_RUN="true"; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "错误: 未知参数 $1" >&2; usage; exit 1 ;;
  esac
done

if [ -n "$CONFIG_FILE" ]; then
  load_config "$CONFIG_FILE"
fi

RENAME_ADMIN=$(printf '%s' "$RENAME_ADMIN" | tr '[:upper:]' '[:lower:]')
if [ "$RENAME_ADMIN" != "true" ] && [ "$RENAME_ADMIN" != "false" ]; then
  echo "错误: --rename-admin 只能是 true 或 false" >&2
  exit 1
fi

require_value() {
  local name="$1"
  local value="$2"
  if [ -z "$value" ]; then
    echo "错误: 缺少必填参数 $name" >&2
    exit 1
  fi
}

require_value "--display-name" "$DISPLAY_NAME"
require_value "--repo-slug" "$REPO_SLUG"
require_value "--group-id" "$GROUP_ID"
require_value "--root-package" "$ROOT_PACKAGE"
require_value "--config-prefix" "$CONFIG_PREFIX"
require_value "--env-prefix" "$ENV_PREFIX"
require_value "--server-db-name" "$SERVER_DB_NAME"
require_value "--admin-db-name" "$ADMIN_DB_NAME"
require_value "--common-module" "$COMMON_MODULE"
require_value "--server-module" "$SERVER_MODULE"
if [ "$RENAME_ADMIN" = "true" ]; then
  require_value "--admin-module" "$ADMIN_MODULE"
fi

ROOT_PACKAGE_PATH=${ROOT_PACKAGE//./\/}
export DISPLAY_NAME REPO_SLUG GROUP_ID ROOT_PACKAGE CONFIG_PREFIX ENV_PREFIX SERVER_DB_NAME ADMIN_DB_NAME COMMON_MODULE SERVER_MODULE ADMIN_MODULE RENAME_ADMIN ROOT_PACKAGE_PATH

cd "$PROJECT_ROOT"

if [ ! -f "pom.xml" ]; then
  echo "错误: 未找到仓库根目录 pom.xml: $PROJECT_ROOT" >&2
  exit 1
fi

echo "========================================="
echo "  T-Uni Java 项目改名脚本"
echo "========================================="
echo "仓库根目录: $PROJECT_ROOT"
echo "模式: $([ "$DRY_RUN" = "true" ] && echo dry-run || echo apply)"
echo "rename_admin: $RENAME_ADMIN"
echo "新项目名: $DISPLAY_NAME"
echo "新仓库名: $REPO_SLUG"
echo "新包名: $ROOT_PACKAGE"
echo "新配置前缀: $CONFIG_PREFIX"
echo "新环境变量前缀: $ENV_PREFIX"

should_skip_file() {
  local file="$1"
  local base
  base=$(basename "$file")

  case "$file" in
    ./.git/*|./target/*|*/target/*|./node_modules/*|*/node_modules/*|./.idea/*|*/.idea/*|./.claude/*|*/.claude/*|./scripts/rename-project/*) return 0 ;;
  esac

  if [ "$RENAME_ADMIN" = "false" ]; then
    case "$file" in
      ./t-uni-admin|./t-uni-admin/*) return 0 ;;
    esac
  fi

  case "$base" in
    .env) return 0 ;;
    .env.*) [ "$base" = ".env.example" ] && return 1 || return 0 ;;
  esac

  return 1
}

is_text_file() {
  local file="$1"
  grep -Iq . "$file" 2>/dev/null
}

transform_file() {
  perl -0pe '
    my $rename_admin = $ENV{"RENAME_ADMIN"} eq "true";
    my %keep;
    sub protect {
      my ($text_ref, $needle, $token) = @_;
      $$text_ref =~ s/\Q$needle\E/$token/g;
      $keep{$token} = $needle;
    }

    if (!$rename_admin) {
      protect(\$_, "t-uni-admin", "__KEEP_ADMIN_DASH_MODULE__");
      protect(\$_, "tuni-admin", "__KEEP_ADMIN_DASH_RESOURCE__");
      protect(\$_, "tuni_admin", "__KEEP_ADMIN_UNDERSCORE_DB__");
      protect(\$_, "t_uni_admin", "__KEEP_ADMIN_SPLIT_DB__");
      protect(\$_, "T_UNI_ADMIN", "__KEEP_ADMIN_ENV_PREFIX__");
    }

    s/\Q<groupId>t.uni<\/groupId>\E/<groupId>$ENV{"GROUP_ID"}<\/groupId>/g;
    s/\Q<groupId>t.uni\E/<groupId>$ENV{"GROUP_ID"}/g;
    s/\Qprefix = "t.uni.\E/prefix = "$ENV{"CONFIG_PREFIX"}./g;
    s/\Qprefix="t.uni.\E/prefix="$ENV{"CONFIG_PREFIX"}./g;
    s/\Qprefix = "t.uni"\E/prefix = "$ENV{"CONFIG_PREFIX"}/g;
    s/\Qprefix="t.uni"\E/prefix="$ENV{"CONFIG_PREFIX"}/g;
    s/\bt\.uni\b(?=\s*:)/$ENV{"CONFIG_PREFIX"}/g;
    s/\bt\.uni\.(jwt|master|redis|qiniu|wx|payment)\b/$ENV{"CONFIG_PREFIX"}.$1/g;
    s/\bt\.uni\b/$ENV{"ROOT_PACKAGE"}/g;
    s#\bt/uni\b#$ENV{"ROOT_PACKAGE_PATH"}#g;

    s/\Qt-uni-common\E/$ENV{"COMMON_MODULE"}/g;
    s/\Qt-uni-server\E/$ENV{"SERVER_MODULE"}/g;
    if ($rename_admin) {
      s/\Qt-uni-admin\E/$ENV{"ADMIN_MODULE"}/g;
      s/\Qtuni-admin\E/$ENV{"ADMIN_MODULE"}/g;
      s/\Qtuni_admin\E/$ENV{"ADMIN_DB_NAME"}/g;
      s/\Qt_uni_admin\E/$ENV{"ADMIN_DB_NAME"}/g;
      s/\QT_UNI_ADMIN\E/$ENV{"ENV_PREFIX"} . "_ADMIN"/ge;
    }

    s/\QT-Uni-Java\E/$ENV{"DISPLAY_NAME"}/g;
    s/\QT-Uni\E/$ENV{"DISPLAY_NAME"}/g;
    s/\Qt-uni\E/$ENV{"REPO_SLUG"}/g;
    s/\Qtuni\E/$ENV{"SERVER_DB_NAME"}/g;
    s/\Qt_uni\E/$ENV{"SERVER_DB_NAME"}/g;
    s/\QT_UNI_DOCKER\E/$ENV{"ENV_PREFIX"} . "_DOCKER"/ge;
    s/\QT_UNI_\E/$ENV{"ENV_PREFIX"} . "_"/ge;
    s/\QT_UNI\E/$ENV{"ENV_PREFIX"}/g;

    if (!$rename_admin) {
      for my $token (keys %keep) {
        my $needle = $keep{$token};
        s/\Q$token\E/$needle/g;
      }
    }
  '
}

updated_files=0
renamed_paths=0
skipped_files=0
manual_items=0

move_path() {
  local from="$1"
  local to="$2"
  if [ ! -e "$from" ]; then
    return 0
  fi
  if [ "$from" = "$to" ]; then
    return 0
  fi
  if [ -e "$to" ]; then
    echo "跳过重命名，目标已存在: $from -> $to"
    manual_items=$((manual_items + 1))
    return 0
  fi
  if [ "$DRY_RUN" = "true" ]; then
    echo "[dry-run] 重命名: $from -> $to"
  else
    mkdir -p "$(dirname "$to")"
    mv "$from" "$to"
    echo "已重命名: $from -> $to"
  fi
  renamed_paths=$((renamed_paths + 1))
}

rename_package_dirs() {
  local roots
  roots=$(find . \( -path './.git' -o -path './target' -o -path '*/target' -o -path './node_modules' -o -path '*/node_modules' -o -path './.idea' -o -path '*/.idea' -o -path './.claude' -o -path '*/.claude' -o -path './scripts/rename-project' \) -prune -o -type d \( -path '*/src/main/java/t/uni' -o -path '*/src/test/java/t/uni' \) -print)
  if [ -z "$roots" ]; then
    return 0
  fi

  printf '%s\n' "$roots" | while IFS= read -r old_dir; do
    [ -n "$old_dir" ] || continue
    if [ "$RENAME_ADMIN" = "false" ]; then
      case "$old_dir" in
        ./t-uni-admin/*) continue ;;
      esac
    fi
    local java_root new_dir
    java_root=${old_dir%/t/uni}
    new_dir="$java_root/$ROOT_PACKAGE_PATH"
    move_path "$old_dir" "$new_dir"

    if [ "$DRY_RUN" != "true" ]; then
      local parent
      parent=$(dirname "$old_dir")
      while [ "$parent" != "$java_root" ] && [ -d "$parent" ]; do
        rmdir "$parent" 2>/dev/null || break
        parent=$(dirname "$parent")
      done
    fi
  done
}

rename_module_dirs() {
  move_path "./t-uni-common" "./$COMMON_MODULE"
  move_path "./t-uni-server" "./$SERVER_MODULE"
  if [ "$RENAME_ADMIN" = "true" ]; then
    move_path "./t-uni-admin" "./$ADMIN_MODULE"
  fi
}

apply_text_replacements() {
  while IFS= read -r file; do
    [ -n "$file" ] || continue
    if should_skip_file "$file"; then
      skipped_files=$((skipped_files + 1))
      continue
    fi
    if ! is_text_file "$file"; then
      skipped_files=$((skipped_files + 1))
      continue
    fi

    local tmp
    tmp=$(mktemp)
    transform_file < "$file" > "$tmp"
    if cmp -s "$file" "$tmp"; then
      rm -f "$tmp"
      continue
    fi

    if [ "$DRY_RUN" = "true" ]; then
      echo "[dry-run] 修改文本: $file"
      rm -f "$tmp"
    else
      mv "$tmp" "$file"
      echo "已修改文本: $file"
    fi
    updated_files=$((updated_files + 1))
  done < <(find . \( -path './.git' -o -path './target' -o -path '*/target' -o -path './node_modules' -o -path '*/node_modules' -o -path './.idea' -o -path '*/.idea' -o -path './.claude' -o -path '*/.claude' -o -path './scripts/rename-project' \) -prune -o -type f -print)
}

echo ""
echo "[1/3] 规划目录重命名..."
rename_package_dirs
rename_module_dirs

echo ""
echo "[2/3] 扫描并替换文本..."
apply_text_replacements

echo ""
echo "[3/3] 人工确认提示..."
echo "- 真实 .env / .env.* 不会自动修改，请按新的环境变量前缀人工同步。"
echo "- 已有 Docker volume 不会迁移，如需保留数据请手工规划迁移。"
echo "- OpenIM userID / MinIO bucket / 线上回调域名不会迁移，请按部署环境人工确认。"
echo "- LICENSE holder 是否改名需要人工确认。"
manual_items=$((manual_items + 4))

echo ""
echo "========================================="
echo "  改名脚本摘要"
echo "========================================="
echo "文本文件变更数: $updated_files"
echo "路径重命名数: $renamed_paths"
echo "跳过文件数: $skipped_files"
echo "人工确认项: $manual_items"
if [ "$DRY_RUN" = "true" ]; then
  echo "当前是 dry-run，未写入任何文件。确认摘要后去掉 --dry-run 再执行。"
else
  echo "已写入文件。请继续运行 scripts/rename-project/verify-rename.sh。"
fi
