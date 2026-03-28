package com.gis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 删除旧表（如果存在）
        dropOldTables();
        
        // 创建新表
        createTables();
    }
    
    private void dropOldTables() {
        try {
            // 删除旧表（按照依赖关系的逆序）
            String[] dropTables = {
                "DROP TABLE IF EXISTS t_operation_log",
                "DROP TABLE IF EXISTS t_alarm",
                "DROP TABLE IF EXISTS t_camera",
                "DROP TABLE IF EXISTS t_police_point",
                "DROP TABLE IF EXISTS t_address",
                "DROP TABLE IF EXISTS t_icon",
                "DROP TABLE IF EXISTS t_user",
                "DROP TABLE IF EXISTS gis_data",
                "DROP TABLE IF EXISTS sys_user",
                "DROP TABLE IF EXISTS log"
            };
            
            for (String sql : dropTables) {
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception e) {
                    // 忽略不存在的表
                }
            }
            System.out.println("旧表清理完成");
        } catch (Exception e) {
            System.err.println("清理旧表失败: " + e.getMessage());
        }
    }

    private void createTables() {
        try {
            // 创建图标表
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_icon (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "file_path VARCHAR(500), " +
                "category VARCHAR(50), " +
                "upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT chk_icon_category CHECK (category IN ('police', 'camera'))" +
                ")"
            );
            System.out.println("t_icon 表创建成功");

            // 创建用户表
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_user (" +
                "id SERIAL PRIMARY KEY, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "real_name VARCHAR(100), " +
                "certificate_dn VARCHAR(200) UNIQUE, " +
                "role VARCHAR(50), " +
                "password VARCHAR(255), " +
                "last_login_time TIMESTAMP, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT chk_user_role CHECK (role IN ('admin', 'operator', 'viewer'))" +
                ")"
            );
            System.out.println("t_user 表创建成功");

            // 创建地址表（不使用PostGIS）
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_address (" +
                "id SERIAL PRIMARY KEY, " +
                "address_full VARCHAR(500) NOT NULL, " +
                "admin_code VARCHAR(20), " +
                "street VARCHAR(200), " +
                "house_number VARCHAR(50), " +
                "lon DOUBLE PRECISION, " +
                "lat DOUBLE PRECISION, " +
                "status INTEGER DEFAULT 1, " +
                "source VARCHAR(50), " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT chk_status CHECK (status IN (0, 1, 2))" +
                ")"
            );
            System.out.println("t_address 表创建成功");

            // 创建警务点表（不使用PostGIS）
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_police_point (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(200) NOT NULL, " +
                "type VARCHAR(50), " +
                "lon DOUBLE PRECISION, " +
                "lat DOUBLE PRECISION, " +
                "contact_person VARCHAR(100), " +
                "contact_phone VARCHAR(50), " +
                "district VARCHAR(200), " +
                "icon_id INTEGER, " +
                "create_by VARCHAR(50), " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT chk_police_type CHECK (type IN ('派出所', '警务站', '执勤点'))" +
                ")"
            );
            System.out.println("t_police_point 表创建成功");

            // 创建监控点表（不使用PostGIS）
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_camera (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(200) NOT NULL, " +
                "camera_no VARCHAR(100) UNIQUE, " +
                "rtsp_url VARCHAR(500), " +
                "camera_type VARCHAR(50), " +
                "lon DOUBLE PRECISION, " +
                "lat DOUBLE PRECISION, " +
                "online_status BOOLEAN DEFAULT true, " +
                "district VARCHAR(200), " +
                "icon_id INTEGER, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT chk_camera_type CHECK (camera_type IN ('球机', '枪机'))" +
                ")"
            );
            System.out.println("t_camera 表创建成功");

            // 创建警情表（不使用PostGIS）
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_alarm (" +
                "id SERIAL PRIMARY KEY, " +
                "alarm_id VARCHAR(100) UNIQUE, " +
                "alarm_phone VARCHAR(50), " +
                "alarm_time TIMESTAMP, " +
                "address_text VARCHAR(500), " +
                "lon DOUBLE PRECISION, " +
                "lat DOUBLE PRECISION, " +
                "alarm_type VARCHAR(100), " +
                "alarm_level INTEGER, " +
                "status VARCHAR(50), " +
                "police_point_id INTEGER, " +
                "camera_id INTEGER, " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT chk_alarm_level CHECK (alarm_level BETWEEN 1 AND 5)" +
                ")"
            );
            System.out.println("t_alarm 表创建成功");

            // 创建操作日志表
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS t_operation_log (" +
                "id SERIAL PRIMARY KEY, " +
                "user_id INTEGER, " +
                "module VARCHAR(100), " +
                "operation VARCHAR(100), " +
                "request_params TEXT, " +
                "response_result TEXT, " +
                "cost_time INTEGER, " +
                "client_ip VARCHAR(50), " +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            System.out.println("t_operation_log 表创建成功");

            // 创建索引
            createIndexes();

            // 插入初始化数据
            insertInitialData();

            System.out.println("数据库初始化完成");

        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createIndexes() {
        // 地址表索引
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_address_admin_code ON t_address(admin_code)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_address_status ON t_address(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_address_lon_lat ON t_address(lon, lat)");

        // 警务点表索引
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_police_type ON t_police_point(type)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_police_district ON t_police_point(district)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_police_lon_lat ON t_police_point(lon, lat)");

        // 监控点表索引
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_camera_no ON t_camera(camera_no)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_camera_online ON t_camera(online_status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_camera_district ON t_camera(district)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_camera_lon_lat ON t_camera(lon, lat)");

        // 警情表索引
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_alarm_alarm_id ON t_alarm(alarm_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_alarm_time ON t_alarm(alarm_time)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_alarm_type ON t_alarm(alarm_type)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_alarm_status ON t_alarm(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_alarm_lon_lat ON t_alarm(lon, lat)");

        // 用户表索引
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_username ON t_user(username)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_role ON t_user(role)");

        // 日志表索引
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_log_user ON t_operation_log(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_log_module ON t_operation_log(module)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_log_time ON t_operation_log(create_time)");

        System.out.println("索引创建完成");
    }

    private void insertInitialData() {
        try {
            // 插入默认管理员用户
            jdbcTemplate.execute(
                "INSERT INTO t_user (username, real_name, role, password) VALUES " +
                "('admin', '系统管理员', 'admin', 'admin123')"
            );
            System.out.println("默认管理员用户创建成功");

            // 插入默认图标
            jdbcTemplate.execute(
                "INSERT INTO t_icon (name, file_path, category) VALUES " +
                "('police_station', '/icons/police_station.png', 'police'), " +
                "('police_car', '/icons/police_car.png', 'police'), " +
                "('camera_dome', '/icons/camera_dome.png', 'camera'), " +
                "('camera_gun', '/icons/camera_gun.png', 'camera')"
            );
            System.out.println("默认图标插入成功");

            // 插入测试警务点数据
            jdbcTemplate.execute(
                "INSERT INTO t_police_point (name, type, lon, lat, contact_person, contact_phone, district) VALUES " +
                "('东城分局派出所', '派出所', 116.397428, 39.90923, '张警官', '010-12345678', '东城区'), " +
                "('西城分局警务站', '警务站', 116.407428, 39.91923, '李警官', '010-87654321', '西城区'), " +
                "('朝阳执勤点', '执勤点', 116.437428, 39.92923, '王警官', '010-11223344', '朝阳区')"
            );
            System.out.println("测试警务点数据插入成功");

            // 插入测试监控点数据
            jdbcTemplate.execute(
                "INSERT INTO t_camera (name, camera_no, rtsp_url, camera_type, lon, lat, district, online_status) VALUES " +
                "('路口监控01', 'CAM001', 'rtsp://192.168.1.1/stream1', '枪机', 116.417428, 39.90923, '东城区', true), " +
                "('小区监控02', 'CAM002', 'rtsp://192.168.1.2/stream1', '球机', 116.397428, 39.92923, '西城区', true), " +
                "('商场监控03', 'CAM003', 'rtsp://192.168.1.3/stream1', '球机', 116.427428, 39.91923, '朝阳区', false)"
            );
            System.out.println("测试监控点数据插入成功");

            // 插入测试警情数据
            jdbcTemplate.execute(
                "INSERT INTO t_alarm (alarm_id, alarm_phone, alarm_time, address_text, lon, lat, alarm_type, alarm_level, status) VALUES " +
                "('AJ202403270001', '13800138000', CURRENT_TIMESTAMP, '东城区某某路口发生交通事故', 116.407428, 39.91423, '交通事故', 2, '处置中'), " +
                "('AJ202403270002', '13900139000', CURRENT_TIMESTAMP, '西城区某某小区发生纠纷', 116.417428, 39.92423, '治安纠纷', 1, '已处置')"
            );
            System.out.println("测试警情数据插入成功");

            // 插入测试地址数据
            jdbcTemplate.execute(
                "INSERT INTO t_address (address_full, admin_code, street, house_number, lon, lat, status, source) VALUES " +
                "('北京市东城区某某街道1号', '110101', '某某街道', '1号', 116.397428, 39.90923, 1, 'manual'), " +
                "('北京市西城区某某路2号', '110102', '某某路', '2号', 116.407428, 39.91923, 1, 'manual'), " +
                "('北京市朝阳区某某街3号', '110105', '某某街', '3号', 116.437428, 39.92923, 1, 'import')"
            );
            System.out.println("测试地址数据插入成功");

            // 插入测试日志数据
            jdbcTemplate.execute(
                "INSERT INTO t_operation_log (user_id, module, operation, request_params, cost_time, client_ip) VALUES " +
                "(1, '用户管理', '登录', '{\"username\":\"admin\"}', 150, '127.0.0.1'), " +
                "(1, '警务点管理', '新增', '{\"name\":\"东城分局派出所\"}', 230, '127.0.0.1'), " +
                "(1, '监控管理', '查询', '{\"district\":\"东城区\"}', 89, '127.0.0.1')"
            );
            System.out.println("测试日志数据插入成功");

        } catch (Exception e) {
            System.err.println("插入初始化数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
