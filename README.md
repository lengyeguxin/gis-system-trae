# GIS警务指挥系统

基于 Spring Boot + Vue 3 的高德地图警务指挥管理系统。

## 功能特性

### 核心功能

- **地图展示**：基于高德地图API，支持多种地图控件和交互操作
- **警务点管理**：警务点的增删改查，地图标记展示
- **监控点管理**：监控设备管理，支持RTSP实时视频播放
- **警情信息管理**：警情录入、处理、状态跟踪
- **GIS图层地址库**：标准地址库管理，行政区划级联选择
- **系统管理**：用户管理、操作日志

### 特色功能

- **警情边界范围**：自动绘制警情范围圆圈，范围内警务点/监控点高亮显示
- **实时视频播放**：基于ffmpeg + hls.js实现RTSP视频流播放
- **地址去重**：智能判断地址名称和经纬度重复

## 技术栈

### 后端

- Java 8
- Spring Boot 2.7
- PostgreSQL + PostGIS
- Maven

### 前端

- Vue 3
- Vite
- Element Plus
- 高德地图 API

## 项目结构

```
gis/
├── backend/                    # 后端项目
│   ├── src/main/java/
│   │   └── com/gis/
│   │       ├── config/         # 配置类
│   │       ├── controller/     # 控制器
│   │       ├── entity/         # 实体类
│   │       ├── repository/     # 数据访问层
│   │       └── service/        # 服务层
│   └── src/main/resources/
│       ├── application.properties
│       ├── schema.sql          # 数据库表结构
│       └── test.sql            # 测试数据
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── assets/             # 静态资源
│   │   ├── components/         # 组件
│   │   ├── stores/             # 状态管理
│   │   └── views/              # 页面
│   └── vite.config.js
│
└── docs/                       # 文档
    └── DEPLOYMENT.md           # 部署文档
```

## 快速开始

### 环境要求

- JDK 8+
- Node.js 16+
- PostgreSQL 12+
- Maven 3.6+

### 本地开发

1. **克隆项目**

```bash
git clone https://github.com/lengyeguxin/gis-system-trae.git
cd gis-system-trae
```

2. **创建数据库**

```sql
CREATE DATABASE gis_system WITH ENCODING = 'UTF8';
```

3. **配置后端**

修改 `backend/src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gis_system
spring.datasource.username=postgres
spring.datasource.password=your_password
```

4. **启动后端**

```bash
cd backend
mvn spring-boot:run
```

5. **安装前端依赖**

```bash
cd frontend
npm install
```

6. **配置高德地图Key**

创建 `frontend/.env.local`：

```
VITE_AMAP_KEY=your_amap_key
```

7. **启动前端**

```bash
npm run dev
```

8. **访问系统**

打开浏览器访问 http://localhost:8082

默认账号：admin / admin123

## 部署文档

详细部署说明请参考 [部署文档](docs/DEPLOYMENT.md)。

## 主要页面

| 页面 | 路径 | 说明 |
|------|------|------|
| 首页 | /home | 地图总览，展示警务点、监控点、警情 |
| 警务点管理 | /gis-police | 警务点CRUD |
| 监控点管理 | /gis-monitor | 监控点CRUD |
| 警情信息 | /alarm-info | 警情管理 |
| 地址库 | /gis-address | 地址库管理 |
| 系统管理 | /system | 用户管理、日志管理 |

## API接口

### 警务点

- `GET /api/police` - 获取警务点列表
- `POST /api/police` - 新增警务点
- `PUT /api/police/{id}` - 更新警务点
- `DELETE /api/police/{id}` - 删除警务点

### 监控点

- `GET /api/camera` - 获取监控点列表
- `POST /api/camera` - 新增监控点
- `PUT /api/camera/{id}` - 更新监控点
- `DELETE /api/camera/{id}` - 删除监控点

### 警情

- `GET /api/alarm` - 获取警情列表
- `POST /api/alarm` - 新增警情
- `PUT /api/alarm/{id}` - 更新警情
- `DELETE /api/alarm/{id}` - 删除警情

### 视频流

- `POST /api/video/stream/{cameraId}` - 启动视频流
- `DELETE /api/video/stream/{cameraId}` - 停止视频流

## License

MIT
