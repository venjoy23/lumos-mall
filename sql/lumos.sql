-- =============================================================
-- Lumos 基于 SpringBoot 的网络商品贸易平台 - 数据库初始化脚本
-- 数据库：MySQL 8.x（兼容 5.7）
-- =============================================================

CREATE DATABASE IF NOT EXISTS lumos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE lumos;

-- ----------------------------
-- 1. 用户信息表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '用户账号',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码（MD5加盐）',
  `name`        VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '头像',
  `balance`     DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- ----------------------------
-- 2. 管理员信息表
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '管理员账号',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码（MD5加盐）',
  `name`        VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '头像',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员信息表';

-- ----------------------------
-- 3. 商品种类表
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(50) NOT NULL COMMENT '分类名称',
  `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `sort`        INT         DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品种类表';

-- ----------------------------
-- 4. 商品基础信息表
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`         VARCHAR(100)  NOT NULL COMMENT '商品名称',
  `category_id`  BIGINT        DEFAULT NULL COMMENT '商品分类',
  `price`        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
  `stock`        INT           NOT NULL DEFAULT 0 COMMENT '库存',
  `brand`        VARCHAR(100)  DEFAULT NULL COMMENT '品牌',
  `spec`         VARCHAR(100)  DEFAULT NULL COMMENT '规格',
  `image`        VARCHAR(500)  DEFAULT NULL COMMENT '商品图片',
  `detail`       TEXT          COMMENT '商品详情',
  `limit_num`    INT           DEFAULT NULL COMMENT '单限（每人限购）',
  `click_count`  INT           DEFAULT 0 COMMENT '点击次数',
  `status`       INT           DEFAULT 1 COMMENT '状态：0下架 1上架',
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted`      INT           DEFAULT 0 COMMENT '逻辑删除：0正常 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品基础信息表';

-- ----------------------------
-- 5. 商品入库信息表
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_record`;
CREATE TABLE `warehouse_record` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id`  BIGINT   NOT NULL COMMENT '商品ID',
  `quantity`    INT      NOT NULL COMMENT '入库数量',
  `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品入库信息表';

-- ----------------------------
-- 6. 收货地址表
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `name`        VARCHAR(50) NOT NULL COMMENT '收货人',
  `phone`       VARCHAR(20) NOT NULL COMMENT '联系电话',
  `detail`      VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default`  INT         DEFAULT 0 COMMENT '是否默认：0否 1是',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ----------------------------
-- 7. 订单信息表
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`         VARCHAR(50)   NOT NULL COMMENT '订单编号',
  `user_id`          BIGINT        NOT NULL COMMENT '用户ID',
  `total_price`      DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `status`           INT           DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已发货 3已完成 4已取消 5已退款',
  `receiver_name`    VARCHAR(50)   DEFAULT NULL COMMENT '收货人',
  `receiver_phone`   VARCHAR(20)   DEFAULT NULL COMMENT '收货电话',
  `receiver_address` VARCHAR(255)  DEFAULT NULL COMMENT '收货地址',
  `remark`           VARCHAR(255)  DEFAULT NULL COMMENT '订单备注',
  `pay_time`         DATETIME      DEFAULT NULL COMMENT '支付时间',
  `ship_time`        DATETIME      DEFAULT NULL COMMENT '发货时间',
  `finish_time`      DATETIME      DEFAULT NULL COMMENT '完成时间',
  `create_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

-- ----------------------------
-- 8. 订单明细表
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id`      BIGINT        NOT NULL COMMENT '订单ID',
  `product_id`    BIGINT        NOT NULL COMMENT '商品ID',
  `product_name`  VARCHAR(100)  DEFAULT NULL COMMENT '商品名称（快照）',
  `product_image` VARCHAR(500)  DEFAULT NULL COMMENT '商品图片（快照）',
  `price`         DECIMAL(10,2) NOT NULL COMMENT '成交单价',
  `quantity`      INT           NOT NULL COMMENT '数量',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ----------------------------
-- 9. 商城情报分类表
-- ----------------------------
DROP TABLE IF EXISTS `news_type`;
CREATE TABLE `news_type` (
  `id`     BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`   VARCHAR(50) NOT NULL COMMENT '分类名称',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城情报分类表';

-- ----------------------------
-- 10. 商城情报表（商城资讯）
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type_id`     BIGINT       DEFAULT NULL COMMENT '分类ID',
  `title`       VARCHAR(100) NOT NULL COMMENT '标题',
  `intro`       VARCHAR(255) DEFAULT NULL COMMENT '简介',
  `content`     TEXT COMMENT '正文',
  `image`       VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商城情报表';

-- ----------------------------
-- 11. 商品信息评论表
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
  `product_id`  BIGINT       NOT NULL COMMENT '商品ID',
  `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
  `reply`       VARCHAR(500) DEFAULT NULL COMMENT '管理员回复',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息评论表';

-- ----------------------------
-- 12. 配置文件表（系统参数）
-- ----------------------------
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
  `id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`  VARCHAR(100) NOT NULL COMMENT '配置项名称',
  `value` VARCHAR(500) DEFAULT NULL COMMENT '配置项值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配置文件表';

-- ----------------------------
-- 13. 购物车表
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `product_id`  BIGINT   NOT NULL COMMENT '商品ID',
  `quantity`    INT      NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ----------------------------
-- 14. 收藏表
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `product_id`  BIGINT   NOT NULL COMMENT '商品ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ----------------------------
-- 15. 充值记录表
-- ----------------------------
DROP TABLE IF EXISTS `recharge`;
CREATE TABLE `recharge` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT        NOT NULL COMMENT '用户ID',
  `amount`      DECIMAL(10,2) NOT NULL COMMENT '充值金额',
  `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- ----------------------------
-- 16. 轮播图表（系统管理-首页轮播）
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(100) DEFAULT NULL COMMENT '轮播图名称',
  `image`       VARCHAR(500) NOT NULL COMMENT '图片地址',
  `url`         VARCHAR(500) DEFAULT NULL COMMENT '跳转链接',
  `sort`        INT          DEFAULT 0 COMMENT '展示顺序',
  `status`      INT          DEFAULT 1 COMMENT '状态：0停用 1启用',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- =============================================================
-- 种子数据
-- 说明：MD5("lumos#2026" + 明文) ，admin / admin123 ，user / 123456
-- =============================================================

INSERT INTO `admin` (`username`, `password`, `name`, `phone`) VALUES
('admin', MD5(CONCAT('lumos#2026', 'admin123')), '系统管理员', '13800000000');

INSERT INTO `user` (`username`, `password`, `name`, `phone`, `email`, `balance`) VALUES
('user', MD5(CONCAT('lumos#2026', '123456')), '测试用户', '13900000000', 'user@lumos.com', 10000.00);

INSERT INTO `category` (`name`, `remark`, `sort`) VALUES
('数码电子', '手机、电脑、耳机等数码产品', 1),
('家用电器', '大家电与生活电器', 2),
('服饰鞋包', '男装女装、鞋靴、箱包', 3),
('食品生鲜', '休闲食品、生鲜果蔬', 4),
('图书文具', '图书、办公文具', 5);

INSERT INTO `product` (`name`, `category_id`, `price`, `stock`, `brand`, `spec`, `image`, `detail`, `limit_num`, `click_count`) VALUES
('Lumos X1 智能手机 8GB+256GB', 1, 3999.00, 100, 'Lumos', '曜石黑 / 8GB+256GB', '/uploads/demo/phone.jpg', '6.7 英寸 OLED 高刷屏，5000mAh 大电池，1 亿像素主摄。', 2, 356),
('Lumos Air 无线蓝牙耳机', 1, 299.00, 500, 'Lumos', '白色 / 半入耳', '/uploads/demo/earphone.jpg', '主动降噪，单次续航 8 小时，支持快充。', 5, 210),
('超薄笔记本电脑 14 英寸', 1, 5699.00, 60, 'StarBook', '16GB+512GB', '/uploads/demo/laptop.jpg', '金属机身，仅重 1.2kg，2.5K 高清屏。', 1, 189),
('变频滚筒洗衣机 10KG', 2, 2299.00, 40, 'HomePro', '10KG / 一级能效', '/uploads/demo/washer.jpg', 'BLDC 变频电机，除菌洗，静音节能。', 2, 98),
('家用空气净化器', 2, 899.00, 80, 'AirPure', '适用 40㎡', '/uploads/demo/purifier.jpg', 'HEPA 滤网，实时空气质量显示。', 3, 76),
('纯棉连帽卫衣 秋冬款', 3, 159.00, 300, 'UrbanFit', 'M/L/XL/XXL', '/uploads/demo/hoodie.jpg', '360g 精梳棉，不易起球，多色可选。', 10, 145),
('轻便运动鞋 男女同款', 3, 269.00, 200, 'RunMax', '36-45 码', '/uploads/demo/shoes.jpg', '缓震回弹中底，透气网面。', 5, 167),
('每日坚果混合装 750g', 4, 99.00, 400, 'NutriDay', '30 包礼盒装', '/uploads/demo/nuts.jpg', '6 种坚果 + 3 种果干，独立小包装。', 20, 132),
('高山乌龙茶 250g', 4, 128.00, 150, 'TeaHill', '250g / 罐装', '/uploads/demo/tea.jpg', '高山茶园直采，炭焙浓香型。', 10, 88),
('算法导论（原书第 3 版）', 5, 128.00, 90, 'Addison', '平装 / 780 页', '/uploads/demo/book.jpg', '计算机经典教材，全面讲解算法设计与分析。', 5, 203);

INSERT INTO `warehouse_record` (`product_id`, `quantity`, `remark`) VALUES
(1, 100, '首批进货'),
(2, 500, '首批进货'),
(3, 60, '首批进货'),
(4, 40, '促销备货'),
(8, 400, '节庆备货');

INSERT INTO `news_type` (`name`, `remark`) VALUES
('平台公告', '官方公告与通知'),
('行业动态', '电商行业新闻'),
('促销活动', '优惠活动信息');

INSERT INTO `news` (`type_id`, `title`, `intro`, `content`, `image`) VALUES
(1, 'Lumos 商城正式上线公告', '平台完成内测，正式对外开放注册与购物。', '尊敬的各位用户：\nLumos 网络商品贸易平台现已完成内部测试，正式对外开放。平台提供商品浏览、在线下单、余额支付、订单跟踪等完整购物体验，祝您购物愉快！', '/uploads/demo/news1.jpg'),
(3, '双十一狂欢节全场满减', '全场满 300 减 50，部分商品限时 5 折。', '双十一期间，全场商品参与满 300 减 50 活动，数码电子、家用电器专区限时 5 折起，先到先得。', '/uploads/demo/news2.jpg'),
(2, '2026 电商行业趋势观察', '直播电商与即时零售持续高速增长。', '报告显示，2026 年直播电商渗透率进一步提升，即时零售成为各大平台新的增长引擎……', '/uploads/demo/news3.jpg');

INSERT INTO `banner` (`name`, `image`, `url`, `sort`, `status`) VALUES
('首页轮播-手机新品', '/uploads/demo/banner1.jpg', '/product/1', 1, 1),
('首页轮播-双十一直播', '/uploads/demo/banner2.jpg', '/news', 2, 1),
('首页轮播-图书专区', '/uploads/demo/banner3.jpg', '/products?categoryId=5', 3, 1);

INSERT INTO `config` (`name`, `value`) VALUES
('site_name', 'Lumos 商城'),
('site_notice', '全场满 300 减 50，新用户注册即送 100 元购物金');
