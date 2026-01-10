-- MyBatis学习演示数据库初始化脚本
-- 数据库: H2 (内存数据库，用于演示)

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    age INT COMMENT '年龄',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- 创建权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    resource_type VARCHAR(50) COMMENT '资源类型：menu,button,api',
    resource_url VARCHAR(255) COMMENT '资源URL',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
);

-- 创建用户详细信息表（一对一关联）
CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '详细信息ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    avatar VARCHAR(255) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    address VARCHAR(255) COMMENT '地址',
    birthday DATE COMMENT '生日',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- 创建订单表（演示一对多关联）
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态：PENDING,PAID,SHIPPED,COMPLETED,CANCELLED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- 创建订单项表（演示多层关联）
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 插入测试数据

-- 插入用户数据
INSERT INTO user (username, email, password, real_name, phone, age, gender, status) VALUES
('zhangsan', 'zhangsan@example.com', 'encrypted_password_123', '张三', '13812345678', 28, 1, 1),
('lisi', 'lisi@example.com', 'encrypted_password_456', '李四', '13987654321', 25, 2, 1),
('wangwu', 'wangwu@example.com', 'encrypted_password_789', '王五', '13611111111', 30, 1, 1),
('zhaoliu', 'zhaoliu@example.com', 'encrypted_password_000', '赵六', '13622222222', 22, 2, 0),
('admin', 'admin@example.com', 'encrypted_admin_password', '管理员', '13800000000', 35, 1, 1);

-- 插入角色数据
INSERT INTO role (role_code, role_name, description) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限'),
('USER', '普通用户', '基础用户权限'),
('GUEST', '访客', '只读权限'),
('MODERATOR', '版主', '内容管理权限');

-- 插入用户角色关联数据
INSERT INTO user_role (user_id, role_id) VALUES
(1, 2), -- 张三 -> 普通用户
(2, 2), -- 李四 -> 普通用户
(3, 4), -- 王五 -> 版主
(4, 3), -- 赵六 -> 访客
(5, 1), -- 管理员 -> 系统管理员
(5, 2); -- 管理员 -> 普通用户（多角色）

-- 插入权限数据
INSERT INTO permission (permission_code, permission_name, resource_type, resource_url, parent_id) VALUES
('system', '系统管理', 'menu', '/system', 0),
('system:user', '用户管理', 'menu', '/system/user', 1),
('system:user:list', '用户列表', 'button', '/system/user/list', 2),
('system:user:add', '添加用户', 'button', '/system/user/add', 2),
('system:user:edit', '编辑用户', 'button', '/system/user/edit', 2),
('system:user:delete', '删除用户', 'button', '/system/user/delete', 2),
('system:role', '角色管理', 'menu', '/system/role', 1),
('content', '内容管理', 'menu', '/content', 0),
('content:article', '文章管理', 'menu', '/content/article', 8),
('content:comment', '评论管理', 'menu', '/content/comment', 8);

-- 插入角色权限关联数据
INSERT INTO role_permission (role_id, permission_id) VALUES
-- 系统管理员拥有所有权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
-- 普通用户基础权限
(2, 8), (2, 9),
-- 访客只读权限
(3, 3), (3, 9),
-- 版主内容管理权限
(4, 8), (4, 9), (4, 10);

-- 插入用户详细信息
INSERT INTO user_profile (user_id, avatar, bio, address, birthday) VALUES
(1, '/avatars/zhangsan.jpg', '热爱技术的Java开发者', '北京市朝阳区', '1995-03-15'),
(2, '/avatars/lisi.jpg', '前端工程师，喜欢设计', '上海市浦东新区', '1998-07-22'),
(3, '/avatars/wangwu.jpg', '全栈开发者', '深圳市南山区', '1993-11-08'),
(5, '/avatars/admin.jpg', '系统管理员', '广州市天河区', '1988-01-01');

-- 插入订单数据
INSERT INTO orders (order_no, user_id, product_name, quantity, price, total_amount, status) VALUES
('ORD20240101001', 1, 'MacBook Pro 16寸', 1, 19999.00, 19999.00, 'COMPLETED'),
('ORD20240101002', 1, 'iPhone 15 Pro', 1, 8999.00, 8999.00, 'SHIPPED'),
('ORD20240101003', 2, 'iPad Air', 1, 4599.00, 4599.00, 'PAID'),
('ORD20240101004', 2, 'AirPods Pro', 2, 1999.00, 3998.00, 'PENDING'),
('ORD20240101005', 3, 'Apple Watch', 1, 2999.00, 2999.00, 'COMPLETED');

-- 插入订单项数据
INSERT INTO order_item (order_id, product_id, product_name, quantity, price, total_price) VALUES
(1, 1001, 'MacBook Pro 16寸 M3 Max', 1, 19999.00, 19999.00),
(2, 1002, 'iPhone 15 Pro 256GB', 1, 8999.00, 8999.00),
(3, 1003, 'iPad Air 256GB', 1, 4599.00, 4599.00),
(4, 1004, 'AirPods Pro 第二代', 2, 1999.00, 3998.00),
(5, 1005, 'Apple Watch Series 9', 1, 2999.00, 2999.00);

-- 创建索引以提高查询性能
CREATE INDEX idx_user_username ON user(username);
CREATE INDEX idx_user_email ON user(email);
CREATE INDEX idx_user_status ON user(status);
CREATE INDEX idx_user_create_time ON user(create_time);
CREATE INDEX idx_role_code ON role(role_code);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_create_time ON orders(create_time);

-- 创建视图（演示复杂查询）
CREATE VIEW user_role_view AS
SELECT 
    u.id as user_id,
    u.username,
    u.email,
    u.real_name,
    u.status as user_status,
    r.id as role_id,
    r.role_code,
    r.role_name,
    r.description as role_description
FROM user u
LEFT JOIN user_role ur ON u.id = ur.user_id
LEFT JOIN role r ON ur.role_id = r.id;

-- 插入更多测试数据用于分页和搜索演示
INSERT INTO user (username, email, password, real_name, phone, age, gender, status) VALUES
('test001', 'test001@example.com', 'password', '测试用户001', '13700000001', 20, 1, 1),
('test002', 'test002@example.com', 'password', '测试用户002', '13700000002', 21, 2, 1),
('test003', 'test003@example.com', 'password', '测试用户003', '13700000003', 22, 1, 1),
('test004', 'test004@example.com', 'password', '测试用户004', '13700000004', 23, 2, 0),
('test005', 'test005@example.com', 'password', '测试用户005', '13700000005', 24, 1, 1),
('test006', 'test006@example.com', 'password', '测试用户006', '13700000006', 25, 2, 1),
('test007', 'test007@example.com', 'password', '测试用户007', '13700000007', 26, 1, 1),
('test008', 'test008@example.com', 'password', '测试用户008', '13700000008', 27, 2, 0),
('test009', 'test009@example.com', 'password', '测试用户009', '13700000009', 28, 1, 1),
('test010', 'test010@example.com', 'password', '测试用户010', '13700000010', 29, 2, 1);

-- 提交事务
COMMIT;