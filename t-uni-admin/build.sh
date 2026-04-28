#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)

MODULE_PATH="t-uni-admin/admin-api"
JAR_PATH="$PROJECT_ROOT/$MODULE_PATH/target/t-uni-admin.jar"

MAVEN_IMAGE="${MAVEN_IMAGE:-maven:3.9.6-eclipse-temurin-21}"
BUILD_PROFILE="${BUILD_PROFILE:-prod}"
DEFAULT_MAVEN_CACHE_DIR="$HOME/.m2"
if [ -d "/data" ]; then
  DEFAULT_MAVEN_CACHE_DIR="/data/maven/.m2"
fi
MAVEN_CACHE_DIR="${MAVEN_CACHE_DIR:-$DEFAULT_MAVEN_CACHE_DIR}"

echo "开始使用 Maven 镜像打包管理端..."
echo "项目根目录: $PROJECT_ROOT"
echo "构建模块: $MODULE_PATH"
echo "构建环境: $BUILD_PROFILE"
echo "Maven 本地仓库: $MAVEN_CACHE_DIR"
echo "Maven 镜像: $MAVEN_IMAGE"

if ! command -v docker >/dev/null 2>&1; then
  echo "错误: 未找到 docker 命令" >&2
  exit 1
fi

mkdir -p "$MAVEN_CACHE_DIR"
docker run --rm \
  -v "$PROJECT_ROOT":/app \
  -v "$MAVEN_CACHE_DIR":/root/.m2 \
  -w /app \
  "$MAVEN_IMAGE" \
  mvn clean package -DskipTests "-P$BUILD_PROFILE" -pl "$MODULE_PATH" -am

echo "打包完成"
echo "检查产物: $JAR_PATH"
if [ -f "$JAR_PATH" ]; then
  ls -lh "$JAR_PATH"
else
  echo "错误: 未找到可执行 jar: $JAR_PATH" >&2
  echo "target 目录内容如下:" >&2
  ls -lah "$PROJECT_ROOT/$MODULE_PATH/target" >&2 || true
  exit 1
fi
