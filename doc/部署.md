# GIS警务指挥系统部署文档

## 目录

- [环境要求](#环境要求)
- [数据库配置](#数据库配置)
- [后端部署](#后端部署)
- [前端部署](#前端部署)
- [视频服务配置](#视频服务配置)
- [生产环境部署](#生产环境部署)
- [常见问题](#常见问题)

## 环境要求

### 服务器配置

| 配置项 | 最低要求 | 推荐配置 |
|--------|----------|----------|
| CPU | 2核 | 4核+ |
| 内存 | 4GB | 8GB+ |
| 硬盘 | 50GB | 100GB+ |

### 软件环境

| 软件 | 版本要求 |
|------|----------|
| JDK | 1.8+ |
| Node.js | 16+ |
| PostgreSQL | 12+ |
| Maven | 3.6+ |
| ffmpeg | 4.0+（视频播放功能需要）|

## 数据库配置

### 1. 安装PostgreSQL

**Windows:**
下载安装包：https://www.postgresql.org/download/windows/

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**Linux (CentOS/RHEL):**
```bash
sudo yum install postgresql-server postgresql-contrib
sudo postgresql-setup initdb
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 2. 创建数据库和用户

```bash
# 切换到 postgres 用户
sudo -u postgres psql

# 创建数据库
CREATE DATABASE gis_system WITH ENCODING = 'UTF8';

# 创建用户（可选）
CREATE USER gis_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE gis_system TO gis_user;

# 退出
\q
```

### 3. 配置远程访问（可选）

编辑 `postgresql.conf`：
```
listen_addresses = '*'
```

编辑 `pg_hba.conf`，添加：
```
host    all             all             0.0.0.0/0               md5
```

重启服务：
```bash
sudo systemctl restart postgresql
```

## 后端部署

### 开发环境

```bash
cd backend

# 配置数据库连接
# 编辑 src/main/resources/application.properties

# 启动
mvn spring-boot:run
```

### 生产环境

#### 方式一：JAR包部署

```bash
# 打包
cd backend
mvn clean package -DskipTests

# 运行
java -jar target/gis-backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8081
```

#### 方式二：系统服务（Linux）

创建服务文件 `/etc/systemd/system/gis-backend.service`：

```ini
[Unit]
Description=GIS Backend Service
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/gis/backend
ExecStart=/usr/bin/java -jar /opt/gis/backend/gis-backend.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl start gis-backend
sudo systemctl enable gis-backend
```

### 配置文件说明

`application-prod.properties`：
```properties
# 服务端口
server.port=8081

# 数据库配置
spring.datasource.url=jdbc:postgresql://localhost:5432/gis_system
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false

# 日志配置
logging.level.root=INFO
logging.file.name=/var/log/gis/backend.log
```

## 前端部署

### 开发环境

```bash
cd frontend

# 安装依赖
npm install

# 配置高德地图Key
# 创建 .env.local 文件
echo "VITE_AMAP_KEY=your_amap_key" > .env.local

# 启动开发服务器
npm run dev
```

### 生产环境

```bash
# 构建
cd frontend
npm run build

# 构建产物在 dist/ 目录
```

#### Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /opt/gis/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理
    location /api/ {
        proxy_pass http://127.0.0.1:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # HLS视频流代理
    location /hls/ {
        proxy_pass http://127.0.0.1:8081/hls/;
        proxy_set_header Host $host;
        add_header Access-Control-Allow-Origin *;
    }
}
```

## 视频服务配置

### 安装ffmpeg

**Windows:**
1. 下载：https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip
2. 解压到 `C:\ffmpeg`
3. 添加 `C:\ffmpeg\bin` 到系统环境变量 PATH

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install ffmpeg
```

**Linux (CentOS/RHEL):**
```bash
sudo yum install epel-release
sudo yum install ffmpeg
```

### 验证安装

```bash
ffmpeg -version
```

### 视频流原理

```
RTSP URL → ffmpeg转码 → HLS文件(.m3u8) → HTTP → 前端hls.js播放
```

后端通过 `VideoStreamService` 管理ffmpeg进程，将RTSP流转换为HLS格式。

## 生产环境部署

### 完整部署架构

```
                    ┌─────────────┐
                    │   Nginx     │
                    │   (80/443)  │
                    └──────┬──────┘
                           │
           ┌───────────────┼───────────────┐
           │               │               │
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │  Frontend   │ │   Backend   │ │  PostgreSQL │
    │  (静态文件)  │ │   (8081)    │ │   (5432)    │
    └─────────────┘ └─────────────┘ └─────────────┘
```

### 部署检查清单

- [ ] PostgreSQL已安装并创建数据库
- [ ] 后端配置文件已正确配置数据库连接
- [ ] 前端已构建，高德地图Key已配置
- [ ] Nginx已配置，前端和API代理正常
- [ ] ffmpeg已安装（如需视频播放功能）
- [ ] 防火墙已开放相应端口
- [ ] SSL证书已配置（生产环境推荐）

### 性能优化建议

**后端：**
- 调整JVM参数：`-Xms512m -Xmx1024m`
- 开启GZIP压缩
- 配置数据库连接池

**前端：**
- 开启Nginx GZIP压缩
- 配置静态资源缓存
- 使用CDN加速静态资源

**数据库：**
- 定期备份数据
- 创建必要的索引
- 配置连接池

## 常见问题

### Q: 后端启动失败，提示数据库连接错误

A: 检查以下项：
1. PostgreSQL服务是否启动
2. 数据库是否创建
3. 用户名密码是否正确
4. 防火墙是否开放5432端口

### Q: 前端地图不显示

A: 检查以下项：
1. 高德地图Key是否正确配置
2. 浏览器控制台是否有错误
3. 网络是否能访问高德地图API

### Q: 视频播放失败

A: 检查以下项：
1. ffmpeg是否正确安装
2. 监控点的RTSP URL是否正确
3. 网络是否能访问RTSP流
4. 后端日志是否有错误信息

### Q: 跨域问题

A: 后端已配置CORS，如仍有问题：
1. 检查Nginx代理配置
2. 确认前端请求的API地址正确

## 联系支持

如有问题，请提交 Issue：https://github.com/lengyeguxin/gis-system-trae/issues