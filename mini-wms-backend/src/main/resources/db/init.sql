-- ============================================================
-- MiniWMS Visual 数据库初始化脚本
-- 小仓图管 v1.0 - 面向小仓库的货位可视化库存管理系统
-- ============================================================
-- 数据库引擎：SQLite (WAL 模式)
-- 初始化时间：2024-01
-- ============================================================

-- 开启 WAL 模式，避免并发写锁
PRAGMA journal_mode = WAL;

-- 会话持久化配置（主程序进程内）
PRAGMA cache_size = 10000;
PRAGMA synchronous = NORMAL;
PRAGMA temp_store = MEMORY;

-- ============================================================
-- 一、系统表
-- ============================================================

-- 用户表（账号 + 角色）
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'OPERATOR',
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 默认管理员账号（密码：admin123）SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
INSERT INTO sys_user (username, password_hash, role, enabled) VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'ADMIN', 1)
    ON CONFLICT(username) DO NOTHING;

-- 会话表
CREATE TABLE IF NOT EXISTS sys_session (
    id VARCHAR(64) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    client_type VARCHAR(16) NOT NULL,
    permission_mode VARCHAR(16) NOT NULL,
    device_info VARCHAR(128),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expire_at DATETIME NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sys_session_expire ON sys_session(expire_at);

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    config_key VARCHAR(64) PRIMARY KEY,
    config_value TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module VARCHAR(32) NOT NULL,
    action VARCHAR(32) NOT NULL,
    detail TEXT,
    operator VARCHAR(64),
    device_info VARCHAR(32),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_operation_log_created ON operation_log(created_at);

-- ============================================================
-- 二、仓储表
-- ============================================================

-- 仓库
CREATE TABLE IF NOT EXISTS warehouse (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(64) NOT NULL,
    address VARCHAR(255),
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_warehouse_name ON warehouse(name);

-- 库区
CREATE TABLE IF NOT EXISTS warehouse_area (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    warehouse_id INTEGER NOT NULL,
    name VARCHAR(64) NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_warehouse_area_warehouse_id ON warehouse_area(warehouse_id);

-- 货架
CREATE TABLE IF NOT EXISTS shelf (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    area_id INTEGER NOT NULL,
    name VARCHAR(64) NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (area_id) REFERENCES warehouse_area(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_shelf_area_id ON shelf(area_id);

-- 储位
CREATE TABLE IF NOT EXISTS bin (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    shelf_id INTEGER NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(64),
    status VARCHAR(16) DEFAULT 'FREE',
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shelf_id) REFERENCES shelf(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_bin_shelf_id ON bin(shelf_id);
CREATE INDEX IF NOT EXISTS idx_bin_status ON bin(status);

-- ============================================================
-- 三、商品表
-- ============================================================

-- 类别（树形）
CREATE TABLE IF NOT EXISTS category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    parent_id INTEGER DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_category_parent_id ON category(parent_id);

-- 商品
CREATE TABLE IF NOT EXISTS goods (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    category_id INTEGER,
    spec VARCHAR(128),
    unit VARCHAR(16),
    barcode VARCHAR(64),
    status VARCHAR(16) DEFAULT 'ENABLED',
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_goods_code ON goods(code);
CREATE INDEX IF NOT EXISTS idx_goods_category_id ON goods(category_id);
CREATE INDEX IF NOT EXISTS idx_goods_name ON goods(name);
CREATE INDEX IF NOT EXISTS idx_goods_barcode ON goods(barcode);
CREATE INDEX IF NOT EXISTS idx_goods_status ON goods(status);

-- 商品图片
CREATE TABLE IF NOT EXISTS goods_image (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_id INTEGER NOT NULL,
    image_type VARCHAR(16) NOT NULL,
    bin_id INTEGER,
    relative_path VARCHAR(255) NOT NULL,
    thumb_path VARCHAR(255),
    mime_type VARCHAR(64),
    file_size BIGINT,
    width INTEGER,
    height INTEGER,
    sha256 CHAR(64),
    deleted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE,
    FOREIGN KEY (bin_id) REFERENCES bin(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_goods_image_goods_id ON goods_image(goods_id, image_type, deleted_at);
CREATE INDEX IF NOT EXISTS idx_goods_image_bin_id ON goods_image(bin_id);
CREATE INDEX IF NOT EXISTS idx_goods_image_deleted ON goods_image(deleted_at);
CREATE INDEX IF NOT EXISTS idx_goods_image_sha256 ON goods_image(sha256);

-- 图片标记
CREATE TABLE IF NOT EXISTS image_marker (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    image_id INTEGER NOT NULL,
    bin_id INTEGER,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    w INTEGER NOT NULL,
    h INTEGER NOT NULL,
    label VARCHAR(64),
    note VARCHAR(255),
    deleted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (image_id) REFERENCES goods_image(id) ON DELETE CASCADE,
    FOREIGN KEY (bin_id) REFERENCES bin(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_image_marker_image_id ON image_marker(image_id);

-- 商品与储位关联
CREATE TABLE IF NOT EXISTS goods_bin (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_id INTEGER NOT NULL,
    bin_id INTEGER NOT NULL,
    is_primary TINYINT DEFAULT 0,
    deleted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE,
    FOREIGN KEY (bin_id) REFERENCES bin(id) ON DELETE CASCADE,
    UNIQUE(goods_id, bin_id)
);
CREATE INDEX IF NOT EXISTS idx_goods_bin_bin_id ON goods_bin(bin_id);

-- 供应商
CREATE TABLE IF NOT EXISTS supplier (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(128) NOT NULL,
    contact VARCHAR(64),
    phone VARCHAR(32),
    address VARCHAR(255),
    status VARCHAR(16) DEFAULT 'ENABLED',
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_supplier_name ON supplier(name);
CREATE INDEX IF NOT EXISTS idx_supplier_status ON supplier(status);

-- 商品与供应商关联
CREATE TABLE IF NOT EXISTS goods_supplier (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_id INTEGER NOT NULL,
    supplier_id INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES supplier(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_goods_supplier_goods_id ON goods_supplier(goods_id);
CREATE INDEX IF NOT EXISTS idx_goods_supplier_supplier_id ON goods_supplier(supplier_id);

-- ============================================================
-- 四、库存表
-- ============================================================

-- 库存（商品 + 储位维度）
CREATE TABLE IF NOT EXISTS inventory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_id INTEGER NOT NULL,
    bin_id INTEGER NOT NULL,
    qty INTEGER NOT NULL DEFAULT 0,
    deleted_at DATETIME,
    deleted_expire_at DATETIME,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE,
    FOREIGN KEY (bin_id) REFERENCES bin(id) ON DELETE CASCADE,
    UNIQUE(goods_id, bin_id)
);
CREATE INDEX IF NOT EXISTS idx_inventory_goods_bin ON inventory(goods_id, bin_id);
CREATE INDEX IF NOT EXISTS idx_inventory_deleted ON inventory(deleted_at);

-- 库存流水（永久保留）
CREATE TABLE IF NOT EXISTS stock_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_id INTEGER NOT NULL,
    bin_id INTEGER,
    change_type VARCHAR(16) NOT NULL,
    change_qty INTEGER NOT NULL,
    before_qty INTEGER,
    after_qty INTEGER,
    adjust_reason VARCHAR(64),
    ref_order_id INTEGER,
    operator VARCHAR(64),
    device_info VARCHAR(32),
    remark VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE,
    FOREIGN KEY (bin_id) REFERENCES bin(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_stock_log_goods_id ON stock_log(goods_id);
CREATE INDEX IF NOT EXISTS idx_stock_log_created ON stock_log(created_at);
CREATE INDEX IF NOT EXISTS idx_stock_log_change_type ON stock_log(change_type);

-- 入库单主表
CREATE TABLE IF NOT EXISTS stock_in_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    in_type VARCHAR(32) NOT NULL,
    supplier_id INTEGER,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    audit_at DATETIME,
    audit_by VARCHAR(64),
    remark VARCHAR(255),
    operator VARCHAR(64),
    device_info VARCHAR(32),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES supplier(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_stock_in_order_no ON stock_in_order(order_no);
CREATE INDEX IF NOT EXISTS idx_stock_in_order_created ON stock_in_order(created_at);

-- 入库单明细
CREATE TABLE IF NOT EXISTS stock_in_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    goods_id INTEGER NOT NULL,
    bin_id INTEGER NOT NULL,
    qty INTEGER NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES stock_in_order(id) ON DELETE CASCADE,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE,
    FOREIGN KEY (bin_id) REFERENCES bin(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_stock_in_item_order_id ON stock_in_item(order_id);

-- 出库单主表
CREATE TABLE IF NOT EXISTS stock_out_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    out_type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    audit_at DATETIME,
    audit_by VARCHAR(64),
    remark VARCHAR(255),
    operator VARCHAR(64),
    device_info VARCHAR(32),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_stock_out_order_no ON stock_out_order(order_no);
CREATE INDEX IF NOT EXISTS idx_stock_out_order_created ON stock_out_order(created_at);

-- 出库单明细（含拆分）
CREATE TABLE IF NOT EXISTS stock_out_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    goods_id INTEGER NOT NULL,
    bin_id INTEGER NOT NULL,
    qty INTEGER NOT NULL,
    is_suggested TINYINT DEFAULT 0,
    suggest_order INTEGER,
    FOREIGN KEY (order_id) REFERENCES stock_out_order(id) ON DELETE CASCADE,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE,
    FOREIGN KEY (bin_id) REFERENCES bin(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_stock_out_item_order_id ON stock_out_item(order_id);

-- 入出库类型字典
CREATE TABLE IF NOT EXISTS stock_type_dict (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type VARCHAR(16) NOT NULL,
    name VARCHAR(32) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    enabled TINYINT DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_stock_type_dict_type ON stock_type_dict(type);
CREATE INDEX IF NOT EXISTS idx_stock_type_dict_enabled ON stock_type_dict(enabled);

-- 供应商资质图片
CREATE TABLE IF NOT EXISTS supplier_image (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    supplier_id INTEGER NOT NULL,
    image_type VARCHAR(32) DEFAULT 'qualification',
    relative_path VARCHAR(255) NOT NULL,
    thumb_path VARCHAR(255),
    mime_type VARCHAR(64),
    file_size BIGINT,
    sha256 CHAR(64),
    deleted_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES supplier(id) ON DELETE CASCADE
);

-- 回收站
CREATE TABLE IF NOT EXISTS recycle_bin (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    biz_type VARCHAR(32) NOT NULL,
    biz_id INTEGER NOT NULL,
    original_data TEXT NOT NULL,
    deleted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expire_at DATETIME NOT NULL,
    restored TINYINT DEFAULT 0,
    restored_at DATETIME,
    UNIQUE(biz_type, biz_id)
);
CREATE INDEX IF NOT EXISTS idx_recycle_bin_expire ON recycle_bin(expire_at);

-- ============================================================
-- 五、初始化字典数据
-- ============================================================

-- 字典为系统固定数据，每次启动先清空再按脚本重建，保证幂等不膨胀
DELETE FROM stock_type_dict;

-- 入库类型
INSERT INTO stock_type_dict (type, name, sort_order) VALUES
('in', '采购入库', 1),
('in', '退货入库', 2),
('in', '调拨入库', 3),
('in', '盘盈入库', 4),
('in', '赠品入库', 5),
('in', '其他入库', 6);

-- 出库类型
INSERT INTO stock_type_dict (type, name, sort_order) VALUES
('out', '销售出库', 1),
('out', '领用出库', 2),
('out', '调拨出库', 3),
('out', '报损出库', 4),
('out', '盘亏出库', 5),
('out', '其他出库', 6);

-- 库存调整原因
INSERT INTO stock_type_dict (type, name, sort_order) VALUES
('adjust', '盘点差异', 1),
('adjust', '录入错误', 2),
('adjust', '损耗', 3),
('adjust', '丢失', 4),
('adjust', '赠品补录', 5),
('adjust', '其他', 6);
