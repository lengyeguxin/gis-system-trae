-- =============================================
-- GIS警务指挥系统数据库初始化脚本
-- 基于04数据库设计说明书
-- =============================================

-- 创建数据库
CREATE DATABASE gis_system WITH ENCODING = 'UTF8';

-- 连接到数据库
-- \c gis_system;

-- 启用PostGIS扩展（用于空间数据存储）
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- =============================================
-- 1. 图标表 (t_icon) - 无依赖，最先创建
-- =============================================
CREATE TABLE t_icon (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    file_path VARCHAR(500),
    category VARCHAR(50),
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_icon_category CHECK (category IN ('police', 'camera'))
);

-- 创建索引
CREATE INDEX idx_icon_category ON t_icon(category);

-- 表和列注释
COMMENT ON TABLE t_icon IS '图标资源表';
COMMENT ON COLUMN t_icon.name IS '图标名称';
COMMENT ON COLUMN t_icon.file_path IS '存储路径（相对路径或URL）';
COMMENT ON COLUMN t_icon.category IS '所属类别：police/camera';

-- =============================================
-- 2. 地址库表 (t_address) - 无依赖
-- =============================================
CREATE TABLE t_address (
    id SERIAL PRIMARY KEY,
    address_full VARCHAR(500) NOT NULL,
    admin_code VARCHAR(20),
    admin_name VARCHAR(100),
    street_code VARCHAR(20),
    street VARCHAR(200),
    house_number VARCHAR(50),
    lon DOUBLE PRECISION,
    lat DOUBLE PRECISION,
    status INTEGER DEFAULT 1,
    source VARCHAR(50),
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_status CHECK (status IN (0, 1, 2))
);

-- 创建索引
CREATE INDEX idx_address_admin_code ON t_address(admin_code);
CREATE INDEX idx_address_status ON t_address(status);
CREATE INDEX idx_address_lon_lat ON t_address(lon, lat);

-- 表和列注释
COMMENT ON TABLE t_address IS '标准地址库表';
COMMENT ON COLUMN t_address.address_full IS '标准地址全称';
COMMENT ON COLUMN t_address.admin_code IS '行政区划代码（GB/T 2260）';
COMMENT ON COLUMN t_address.admin_name IS '行政区划名称';
COMMENT ON COLUMN t_address.street_code IS '街道代码';
COMMENT ON COLUMN t_address.street IS '街道名称';
COMMENT ON COLUMN t_address.house_number IS '门牌号';
COMMENT ON COLUMN t_address.lon IS '经度（WGS84）';
COMMENT ON COLUMN t_address.lat IS '纬度（WGS84）';
COMMENT ON COLUMN t_address.status IS '地址状态：0-无效 1-有效 2-待核实';
COMMENT ON COLUMN t_address.source IS '来源：manual/import/interface';
COMMENT ON COLUMN t_address.remark IS '备注信息';

-- =============================================
-- 2.1 区划库字典表 (regions) - 无依赖
-- 数据来源：https://gitee.com/red-jasmine/region
-- =============================================
DROP TABLE IF EXISTS regions;
CREATE TABLE regions (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    name VARCHAR(255),
    pinyin VARCHAR(255),
    pinyin_prefix VARCHAR(1),
    level INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_region_level CHECK (level IN (0, 1, 2, 3, 4))
);

-- 创建索引
CREATE INDEX idx_regions_parent ON regions(parent_id);
CREATE INDEX idx_regions_level ON regions(level);
CREATE INDEX idx_regions_name ON regions(name);

-- 表和列注释
COMMENT ON TABLE regions IS '行政区划表';
COMMENT ON COLUMN regions.id IS '行政区划编码（国家统计局标准编码）';
COMMENT ON COLUMN regions.parent_id IS '父级ID';
COMMENT ON COLUMN regions.name IS '名称';
COMMENT ON COLUMN regions.pinyin IS '拼音';
COMMENT ON COLUMN regions.pinyin_prefix IS '首字母';
COMMENT ON COLUMN regions.level IS '等级：0-国家 1-省 2-市 3-区县 4-街道/乡镇';

-- =============================================
-- 3. 用户表 (t_user) - 无依赖
-- =============================================
CREATE TABLE t_user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    real_name VARCHAR(100),
    certificate_dn VARCHAR(200) UNIQUE,
    role VARCHAR(50),
    password VARCHAR(255),
    last_login_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('admin', 'operator', 'viewer'))
);

-- 创建索引
CREATE INDEX idx_user_username ON t_user(username);
CREATE INDEX idx_user_role ON t_user(role);

-- 表和列注释
COMMENT ON TABLE t_user IS '系统用户表';
COMMENT ON COLUMN t_user.username IS '登录账号，唯一';
COMMENT ON COLUMN t_user.real_name IS '真实姓名';
COMMENT ON COLUMN t_user.certificate_dn IS '数字证书DN，唯一';
COMMENT ON COLUMN t_user.role IS '角色：admin/operator/viewer';
COMMENT ON COLUMN t_user.password IS '登录密码';
COMMENT ON COLUMN t_user.last_login_time IS '最后登录时间';

-- =============================================
-- 4. 警务点表 (t_police_point) - 依赖 t_icon
-- =============================================
CREATE TABLE t_police_point (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50),
    address VARCHAR(500),
    lon DOUBLE PRECISION,
    lat DOUBLE PRECISION,
    contact_person VARCHAR(100),
    contact_phone VARCHAR(50),
    responsibility_unit VARCHAR(200),
    description TEXT,
    icon_id INTEGER,
    create_by VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_police_type CHECK (type IN ('派出所', '警务站', '执勤点')),
    CONSTRAINT fk_police_icon FOREIGN KEY (icon_id) REFERENCES t_icon(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_police_type ON t_police_point(type);
CREATE INDEX idx_police_responsibility_unit ON t_police_point(responsibility_unit);
CREATE INDEX idx_police_lon_lat ON t_police_point(lon, lat);
CREATE INDEX idx_police_icon ON t_police_point(icon_id);

-- 表和列注释
COMMENT ON TABLE t_police_point IS '警务点信息表';
COMMENT ON COLUMN t_police_point.name IS '警务点名称';
COMMENT ON COLUMN t_police_point.type IS '类型：派出所/警务站/执勤点';
COMMENT ON COLUMN t_police_point.address IS '警务点地址';
COMMENT ON COLUMN t_police_point.lon IS '经度';
COMMENT ON COLUMN t_police_point.lat IS '纬度';
COMMENT ON COLUMN t_police_point.contact_person IS '联系人';
COMMENT ON COLUMN t_police_point.contact_phone IS '联系电话';
COMMENT ON COLUMN t_police_point.responsibility_unit IS '责任单位';
COMMENT ON COLUMN t_police_point.description IS '描述';
COMMENT ON COLUMN t_police_point.icon_id IS '外键，关联t_icon.id';
COMMENT ON COLUMN t_police_point.create_by IS '创建人账号';

-- =============================================
-- 5. 监控点表 (t_camera) - 依赖 t_icon
-- =============================================
CREATE TABLE t_camera (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    camera_no VARCHAR(100) UNIQUE,
    camera_type VARCHAR(50),
    address VARCHAR(500),
    ip_address VARCHAR(100),
    rtsp_url VARCHAR(500),
    lon DOUBLE PRECISION,
    lat DOUBLE PRECISION,
    online_status BOOLEAN DEFAULT true,
    responsibility_unit VARCHAR(200),
    icon_id INTEGER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_camera_type CHECK (camera_type IN ('球机', '枪机')),
    CONSTRAINT fk_camera_icon FOREIGN KEY (icon_id) REFERENCES t_icon(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_camera_no ON t_camera(camera_no);
CREATE INDEX idx_camera_online ON t_camera(online_status);
CREATE INDEX idx_camera_responsibility_unit ON t_camera(responsibility_unit);
CREATE INDEX idx_camera_lon_lat ON t_camera(lon, lat);
CREATE INDEX idx_camera_icon ON t_camera(icon_id);

-- 表和列注释
COMMENT ON TABLE t_camera IS '监控摄像头信息表';
COMMENT ON COLUMN t_camera.name IS '摄像头名称';
COMMENT ON COLUMN t_camera.camera_no IS '摄像头编号，唯一';
COMMENT ON COLUMN t_camera.camera_type IS '球机/枪机';
COMMENT ON COLUMN t_camera.address IS '监控点地点';
COMMENT ON COLUMN t_camera.ip_address IS '监控点IP';
COMMENT ON COLUMN t_camera.rtsp_url IS 'RTSP流地址';
COMMENT ON COLUMN t_camera.lon IS '经度';
COMMENT ON COLUMN t_camera.lat IS '纬度';
COMMENT ON COLUMN t_camera.online_status IS '在线状态';
COMMENT ON COLUMN t_camera.responsibility_unit IS '责任单位';
COMMENT ON COLUMN t_camera.icon_id IS '外键，关联t_icon.id';

-- =============================================
-- 6. 警情表 (t_alarm) - 依赖 t_police_point, t_camera
-- =============================================
CREATE TABLE t_alarm (
    id SERIAL PRIMARY KEY,
    alarm_id VARCHAR(100) UNIQUE,
    alarm_phone VARCHAR(50),
    alarm_time TIMESTAMP,
    alarm_location VARCHAR(500),
    case_description TEXT,
    handling_result TEXT,
    lon DOUBLE PRECISION,
    lat DOUBLE PRECISION,
    alarm_type VARCHAR(100),
    alarm_level INTEGER,
    status VARCHAR(50),
    police_point_id INTEGER,
    camera_id INTEGER,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_alarm_level CHECK (alarm_level BETWEEN 1 AND 5),
    CONSTRAINT fk_alarm_police FOREIGN KEY (police_point_id) REFERENCES t_police_point(id) ON DELETE SET NULL,
    CONSTRAINT fk_alarm_camera FOREIGN KEY (camera_id) REFERENCES t_camera(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_alarm_alarm_id ON t_alarm(alarm_id);
CREATE INDEX idx_alarm_time ON t_alarm(alarm_time);
CREATE INDEX idx_alarm_type ON t_alarm(alarm_type);
CREATE INDEX idx_alarm_status ON t_alarm(status);
CREATE INDEX idx_alarm_lon_lat ON t_alarm(lon, lat);
CREATE INDEX idx_alarm_police ON t_alarm(police_point_id);
CREATE INDEX idx_alarm_camera ON t_alarm(camera_id);

-- 表和列注释
COMMENT ON TABLE t_alarm IS '警情信息表';
COMMENT ON COLUMN t_alarm.alarm_id IS '接处警系统警情ID，唯一';
COMMENT ON COLUMN t_alarm.alarm_phone IS '报警电话';
COMMENT ON COLUMN t_alarm.alarm_time IS '报警时间';
COMMENT ON COLUMN t_alarm.alarm_location IS '警情地点';
COMMENT ON COLUMN t_alarm.case_description IS '案件描述';
COMMENT ON COLUMN t_alarm.handling_result IS '处理结果';
COMMENT ON COLUMN t_alarm.lon IS '解析后的经度';
COMMENT ON COLUMN t_alarm.lat IS '解析后的纬度';
COMMENT ON COLUMN t_alarm.alarm_type IS '警情类型（火灾/治安/求助等）';
COMMENT ON COLUMN t_alarm.alarm_level IS '警情等级 1-5';
COMMENT ON COLUMN t_alarm.status IS '处置状态';
COMMENT ON COLUMN t_alarm.police_point_id IS '关联警务点ID';
COMMENT ON COLUMN t_alarm.camera_id IS '关联摄像头ID';

-- =============================================
-- 7. 操作日志表 (t_operation_log) - 依赖 t_user
-- =============================================
CREATE TABLE t_operation_log (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    module VARCHAR(100),
    operation VARCHAR(100),
    request_params TEXT,
    response_result TEXT,
    cost_time INTEGER,
    client_ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_log_user ON t_operation_log(user_id);
CREATE INDEX idx_log_module ON t_operation_log(module);
CREATE INDEX idx_log_time ON t_operation_log(create_time);

-- 表和列注释
COMMENT ON TABLE t_operation_log IS '操作日志表';
COMMENT ON COLUMN t_operation_log.user_id IS '操作用户ID，关联t_user.id';
COMMENT ON COLUMN t_operation_log.module IS '操作模块（地址库/警务点等）';
COMMENT ON COLUMN t_operation_log.operation IS '操作类型（增删改查等）';
COMMENT ON COLUMN t_operation_log.request_params IS '请求参数（JSON格式）';
COMMENT ON COLUMN t_operation_log.response_result IS '响应结果（JSON格式）';
COMMENT ON COLUMN t_operation_log.cost_time IS '耗时（毫秒）';
COMMENT ON COLUMN t_operation_log.client_ip IS '客户端IP';

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认管理员用户
INSERT INTO t_user (username, real_name, role, password) 
VALUES ('admin', '系统管理员', 'admin', 'admin123');

-- 插入默认图标
INSERT INTO t_icon (name, file_path, category) VALUES
('police_station', '/icons/police_station.png', 'police'),
('police_car', '/icons/police_car.png', 'police'),
('camera_dome', '/icons/camera_dome.png', 'camera'),
('camera_gun', '/icons/camera_gun.png', 'camera');

-- 区划数据已移至单独的division_data_full.sql文件

-- 插入测试警务点数据
INSERT INTO t_police_point (name, type, address, lon, lat, contact_person, contact_phone, responsibility_unit, description) VALUES
('东城分局派出所', '派出所', '北京市东城区东直门外大街1号', 116.397428, 39.90923, '张警官', '010-12345678', '东城区公安局', '东城区主要派出所之一，负责东直门外地区的治安管理'),
('西城分局警务站', '警务站', '北京市西城区西长安街12号', 116.407428, 39.91923, '李警官', '010-87654321', '西城区公安局', '西城区中心区域的警务站，提供24小时服务'),
('朝阳执勤点', '执勤点', '北京市朝阳区朝阳公园路15号', 116.437428, 39.92923, '王警官', '010-11223344', '朝阳区公安局', '朝阳区朝阳公园附近的临时执勤点');


-- 插入测试监控点数据
INSERT INTO t_camera (name, camera_no, camera_type, address, ip_address, rtsp_url, lon, lat, online_status, responsibility_unit) VALUES
('路口监控01', 'CAM001', '枪机', '北京市东城区东直门外大街1号', '192.168.1.1', 'rtsp://192.168.1.1/stream1', 116.417428, 39.90923, true, '东城区公安局'),
('小区监控02', 'CAM002', '球机', '北京市西城区西长安街12号', '192.168.1.2', 'rtsp://192.168.1.2/stream1', 116.397428, 39.92923, true, '西城区公安局'),
('商场监控03', 'CAM003', '球机', '北京市朝阳区朝阳公园路15号', '192.168.1.3', 'rtsp://192.168.1.3/stream1', 116.427428, 39.91923, false, '朝阳区公安局');

-- 插入测试警情数据
INSERT INTO t_alarm (alarm_id, alarm_phone, alarm_time, alarm_location, case_description, handling_result, lon, lat, alarm_type, alarm_level, status) VALUES
('AJ202403270001', '13800138000', CURRENT_TIMESTAMP, '东城区某某路口', '发生交通事故，两车相撞', NULL, 116.407428, 39.91423, '交通事故', 2, '处置中'),
('AJ202403270002', '13900139000', CURRENT_TIMESTAMP, '西城区某某小区', '邻里之间发生纠纷', '已调解，双方达成和解', 116.417428, 39.92423, '治安纠纷', 1, '已处置');

-- 插入测试地址数据
INSERT INTO t_address (address_full, admin_code, admin_name, street_code, street, house_number, lon, lat, status, source, remark) VALUES
('北京市东城区东华门街道1号', '110101', '东城区', '110101001', '东华门街道', '1号', 116.397428, 39.90923, 1, 'manual', '测试地址1'),
('北京市西城区西长安街街道2号', '110102', '西城区', '110102001', '西长安街街道', '2号', 116.407428, 39.91923, 1, 'manual', '测试地址2'),
('北京市朝阳区建外街道3号', '110105', '朝阳区', '110105001', '建外街道', '3号', 116.437428, 39.92923, 1, 'import', '测试地址3');

-- 插入测试日志数据
INSERT INTO t_operation_log (user_id, module, operation, request_params, cost_time, client_ip) VALUES
(1, '用户管理', '登录', '{"username":"admin"}', 150, '127.0.0.1'),
(1, '警务点管理', '新增', '{"name":"东城分局派出所"}', 230, '127.0.0.1'),
(1, '监控管理', '查询', '{"district":"东城区"}', 89, '127.0.0.1');

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_update_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为需要自动更新update_time的表创建触发器
CREATE TRIGGER update_t_address_update_time BEFORE UPDATE ON t_address
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_t_police_point_update_time BEFORE UPDATE ON t_police_point
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_t_camera_update_time BEFORE UPDATE ON t_camera
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_t_alarm_update_time BEFORE UPDATE ON t_alarm
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

CREATE TRIGGER update_t_user_update_time BEFORE UPDATE ON t_user
    FOR EACH ROW EXECUTE FUNCTION update_update_time_column();

-- 授权（根据实际部署环境调整）
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO gis_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO gis_user;
