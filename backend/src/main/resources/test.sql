-- =============================================
-- GIS警务指挥系统测试数据
-- =============================================

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
-- status: 0-未处理 1-已处理
-- alarm_level: 1-高 2-中 3-低
INSERT INTO t_alarm (alarm_id, alarm_phone, alarm_time, alarm_location, case_description, handling_result, lon, lat, alarm_type, alarm_level, status) VALUES
('AJ202403270001', '13800138000', CURRENT_TIMESTAMP, '东城区某某路口', '发生交通事故，两车相撞', NULL, 116.407428, 39.91423, '交通事故', 1, 0),
('AJ202403270002', '13900139000', CURRENT_TIMESTAMP, '西城区某某小区', '邻里之间发生纠纷', '已调解，双方达成和解', 116.417428, 39.92423, '治安纠纷', 2, 1),
('AJ202403270003', '13700137000', CURRENT_TIMESTAMP, '朝阳区某某街道', '发现可疑人员', NULL, 116.437428, 39.93423, '治安事件', 3, 0);

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

-- 插入测试操作日志（log表）
INSERT INTO log (username, operation, ip_address, details) VALUES
('admin', '登录', '127.0.0.1', '管理员登录系统'),
('admin', '添加用户', '127.0.0.1', '添加了用户 user1'),
('admin', '添加警务点', '127.0.0.1', '添加了警务点 东城分局派出所'),
('user1', '登录', '127.0.0.1', '用户 user1 登录系统'),
('admin', '添加警情', '127.0.0.1', '添加了警情 AJ202403270001');