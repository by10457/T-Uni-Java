#!/usr/bin/env bash

set -euo pipefail

echo "========================================="
echo "  T-Uni Admin Docker 部署脚本"
echo "========================================="

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)

IMAGE_NAME="${IMAGE_NAME:-t-uni-admin}"
CONTAINER_NAME="${CONTAINER_NAME:-t-uni-admin}"
MODULE_PATH="t-uni-admin/admin-api"
JAR_FILE="$PROJECT_ROOT/$MODULE_PATH/target/t-uni-admin.jar"
DOCKERFILE="$SCRIPT_DIR/Dockerfile"
PORT="${PORT:-7840}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
ENV_FILE="${ENV_FILE:-$PROJECT_ROOT/.env}"
HOST_LOG_DIR="${HOST_LOG_DIR:-$PROJECT_ROOT/logs/t-uni-admin}"
HOST_DATA_DIR="${HOST_DATA_DIR:-$PROJECT_ROOT/data/t-uni-admin}"
CONTAINER_LOG_DIR="${CONTAINER_LOG_DIR:-/app/logs}"
CONTAINER_DATA_DIR="${CONTAINER_DATA_DIR:-/app/data/t-uni-admin}"
CONTAINER_LOG_MAX_SIZE="${CONTAINER_LOG_MAX_SIZE:-100m}"
CONTAINER_LOG_MAX_FILE="${CONTAINER_LOG_MAX_FILE:-3}"

echo "项目根目录: $PROJECT_ROOT"
echo "镜像名称: $IMAGE_NAME"
echo "容器名称: $CONTAINER_NAME"
echo "运行端口: $PORT"
echo "运行环境: $SPRING_PROFILE"

if ! command -v docker >/dev/null 2>&1; then
  echo "错误: 未找到 docker 命令" >&2
  exit 1
fi

echo ""
echo "[1/6] 检查 jar 包..."
if [ ! -f "$JAR_FILE" ]; then
  echo "错误: 未找到 jar 包，请先执行 ./t-uni-admin/build.sh" >&2
  echo "期望路径: $JAR_FILE" >&2
  exit 1
fi
echo "jar 包存在: $JAR_FILE"

echo ""
echo "[2/6] 停止旧容器..."
if [ -n "$(docker ps -aq -f name="^${CONTAINER_NAME}$")" ]; then
  docker stop "$CONTAINER_NAME"
  docker rm "$CONTAINER_NAME"
  echo "旧容器已删除"
else
  echo "无旧容器需要删除"
fi

echo ""
echo "[3/6] 删除旧镜像..."
if [ -n "$(docker images -q "$IMAGE_NAME")" ]; then
  docker rmi "$IMAGE_NAME"
  echo "旧镜像已删除"
else
  echo "无旧镜像需要删除"
fi

echo ""
echo "[4/6] 构建 Docker 镜像..."
docker build -f "$DOCKERFILE" -t "$IMAGE_NAME" "$PROJECT_ROOT"
echo "镜像构建成功"

echo ""
echo "[5/6] 启动容器..."
mkdir -p "$HOST_LOG_DIR" "$HOST_DATA_DIR"

ENV_ARGS=()
if [ -f "$ENV_FILE" ]; then
  ENV_ARGS+=(--env-file "$ENV_FILE")
  echo "使用环境变量文件: $ENV_FILE"
else
  echo "未发现环境变量文件: $ENV_FILE，将仅使用当前 shell 环境和脚本参数"
fi

docker run -d \
  --name "$CONTAINER_NAME" \
  --add-host=host.docker.internal:host-gateway \
  -p "$PORT:$PORT" \
  "${ENV_ARGS[@]}" \
  -e SPRING_PROFILES_ACTIVE="$SPRING_PROFILE" \
  -e SERVER_PORT="$PORT" \
  -e T_UNI_ADMIN_LOCAL_STORAGE_PATH="$CONTAINER_DATA_DIR/" \
  --restart unless-stopped \
  --log-opt max-size="$CONTAINER_LOG_MAX_SIZE" \
  --log-opt max-file="$CONTAINER_LOG_MAX_FILE" \
  -v "$HOST_LOG_DIR:$CONTAINER_LOG_DIR" \
  -v "$HOST_DATA_DIR:$CONTAINER_DATA_DIR" \
  "$IMAGE_NAME"
echo "容器启动成功"

echo ""
echo "[6/6] 查看容器状态..."
sleep 3
docker ps -f name="$CONTAINER_NAME"

echo ""
echo "========================================="
echo "  部署完成"
echo "========================================="
echo "容器名称: $CONTAINER_NAME"
echo "访问地址: http://localhost:$PORT"
echo "运行环境: $SPRING_PROFILE"
echo "日志映射: $HOST_LOG_DIR -> $CONTAINER_LOG_DIR"
echo "数据映射: $HOST_DATA_DIR -> $CONTAINER_DATA_DIR"
echo "容器日志轮转: max-size=$CONTAINER_LOG_MAX_SIZE, max-file=$CONTAINER_LOG_MAX_FILE"
echo ""
echo "常用命令:"
echo "  查看日志: docker logs -f $CONTAINER_NAME"
echo "  停止容器: docker stop $CONTAINER_NAME"
echo "  启动容器: docker start $CONTAINER_NAME"
echo "  重启容器: docker restart $CONTAINER_NAME"
echo "========================================="
