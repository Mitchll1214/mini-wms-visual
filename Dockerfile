# ============================================================
# MiniWMS Visual - 多阶段 Docker 构建（国内外环境兼容）
# ------------------------------------------------------------
# 可选构建参数（docker build --build-arg 或 docker-compose .env 覆盖，
# 默认值适配国内直用）：
#   REGISTRY_PREFIX  基础镜像仓库前缀，默认空 = Docker Hub 官方镜像。
#                     国内换源示例：m.daocloud.io/docker.io
#                                     或 registry.cn-hangzhou.aliyuncs.com
#   NPM_REGISTRY     npm 依赖源，默认 https://registry.npmmirror.com（国内）
#                     国外切换为 https://registry.npmjs.org
#   MAVEN_MIRROR     Maven 中央仓库镜像，默认阿里云（国内）
#                     国外切换为 https://repo.maven.apache.org/maven2
# 用法：
#   docker compose build                        # 国内默认配置
#   REGISTRY_PREFIX=... docker compose build    # 自定义镜像源
# ============================================================

ARG REGISTRY_PREFIX=

# ---------- 阶段 1：后端构建 ----------
FROM ${REGISTRY_PREFIX}maven:3.9-eclipse-temurin-17-alpine AS backend_builder

# Maven 仓库镜像（在阶段内重新声明 ARG 才能使用）
ARG MAVEN_MIRROR=https://maven.aliyun.com/repository/public

WORKDIR /app/backend

# 应用 Maven 镜像配置到用户级 settings.xml（副本，不改源码）
COPY docker/maven-settings.xml /tmp/maven-settings.xml
RUN mkdir -p /root/.m2 && \
    sed "s|__MAVEN_MIRROR__|${MAVEN_MIRROR}|g" /tmp/maven-settings.xml > /root/.m2/settings.xml

# 先复制 pom.xml 预拉依赖（利用 Docker 层缓存，后续源码变更不重复下载）
COPY mini-wms-backend/pom.xml .
RUN mvn dependency:go-offline -B -q || true

# 复制后端源码（含内置的 static 前端产物）
COPY mini-wms-backend/src ./src

# 构建 Spring Boot 可执行 JAR（跳过测试）
RUN mvn clean package -DskipTests -B -q

# ---------- 阶段 2：前端构建 ----------
FROM ${REGISTRY_PREFIX}node:20-alpine AS frontend_builder

ARG NPM_REGISTRY=https://registry.npmmirror.com

WORKDIR /app/frontend

# 复制前端项目描述与构建配置
COPY mini-wms-frontend/package.json ./
COPY mini-wms-frontend/vite.config.js ./
COPY mini-wms-frontend/index.html ./
COPY mini-wms-frontend/src ./src
COPY mini-wms-frontend/public ./public

# 安装依赖并构建（registry 可配，默认国内 npmmirror）
RUN npm install --legacy-peer-deps --registry=${NPM_REGISTRY}
RUN npm run build

# ---------- 阶段 3：运行时镜像（仅 JRE，轻量安全） ----------
FROM ${REGISTRY_PREFIX}eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制后端可执行 JAR
COPY --from=backend_builder /app/backend/target/mini-wms-visual.jar /app/app.jar

# 复制前端构建产物到外部静态资源目录（Spring Boot 外部资源加载）
COPY --from=frontend_builder /app/frontend/dist /app/public

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=default \
    SERVER_PORT=8080

CMD ["java", "-jar", "/app/app.jar"]