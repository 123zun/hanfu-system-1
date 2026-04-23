-- =============================================
-- 双塔模型推荐系统 - 数据库表结构
-- =============================================

-- 用户 Embedding 表
CREATE TABLE IF NOT EXISTS `user_embedding` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    `embedding_vector` TEXT NOT NULL COMMENT '用户向量，384维，逗号分隔',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户Embedding表';

-- 作品 Embedding 表
CREATE TABLE IF NOT EXISTS `work_embedding` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `work_id` BIGINT NOT NULL UNIQUE COMMENT '作品ID',
    `embedding_vector` TEXT NOT NULL COMMENT '作品向量，384维，逗号分隔',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_work_id` (`work_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品Embedding表';

-- =============================================
-- 如果是更新现有数据库，执行以下 ALTER 语句
-- =============================================
-- ALTER TABLE `user_embedding` ADD COLUMN `id` BIGINT PRIMARY KEY AUTO_INCREMENT FIRST;
-- ALTER TABLE `work_embedding` ADD COLUMN `id` BIGINT PRIMARY KEY AUTO_INCREMENT FIRST;
