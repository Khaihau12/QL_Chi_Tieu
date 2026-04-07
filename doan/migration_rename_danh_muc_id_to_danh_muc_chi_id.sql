-- =====================================================
-- MIGRATION: giao_dich.danh_muc_id -> giao_dich.danh_muc_chi_id
-- Mục tiêu: đổi tên cột + khóa ngoại + index an toàn cho DB cũ
-- Chạy trên MySQL 8.x
-- =====================================================

USE QLChiTieu;
SET @db := DATABASE();

-- 1) Drop FK cũ nếu đang trỏ từ giao_dich.danh_muc_id -> danh_muc(id)
SET @old_fk := (
    SELECT kcu.CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE kcu
    WHERE kcu.TABLE_SCHEMA = @db
      AND kcu.TABLE_NAME = 'giao_dich'
      AND kcu.COLUMN_NAME = 'danh_muc_id'
      AND kcu.REFERENCED_TABLE_NAME = 'danh_muc'
    LIMIT 1
);
SET @sql := IF(
    @old_fk IS NULL,
    "SELECT 'No old FK to drop'",
    CONCAT('ALTER TABLE giao_dich DROP FOREIGN KEY `', @old_fk, '`')
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Drop index cũ theo tên cũ nếu có
SET @old_idx := (
    SELECT s.INDEX_NAME
    FROM information_schema.STATISTICS s
    WHERE s.TABLE_SCHEMA = @db
      AND s.TABLE_NAME = 'giao_dich'
      AND s.INDEX_NAME = 'idx_giao_dich_danh_muc'
    LIMIT 1
);
SET @sql := IF(
    @old_idx IS NULL,
    "SELECT 'No old index to drop'",
    'DROP INDEX idx_giao_dich_danh_muc ON giao_dich'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) Đổi tên cột nếu DB còn cột cũ và chưa có cột mới
SET @has_old_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS c
    WHERE c.TABLE_SCHEMA = @db
      AND c.TABLE_NAME = 'giao_dich'
      AND c.COLUMN_NAME = 'danh_muc_id'
);
SET @has_new_col := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS c
    WHERE c.TABLE_SCHEMA = @db
      AND c.TABLE_NAME = 'giao_dich'
      AND c.COLUMN_NAME = 'danh_muc_chi_id'
);
SET @sql := IF(
    @has_old_col = 1 AND @has_new_col = 0,
    'ALTER TABLE giao_dich CHANGE COLUMN danh_muc_id danh_muc_chi_id INT DEFAULT NULL',
    "SELECT 'Column rename skipped'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4) Add FK mới nếu chưa có
SET @has_new_fk := (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE kcu
    WHERE kcu.TABLE_SCHEMA = @db
      AND kcu.TABLE_NAME = 'giao_dich'
      AND kcu.COLUMN_NAME = 'danh_muc_chi_id'
      AND kcu.REFERENCED_TABLE_NAME = 'danh_muc'
);
SET @sql := IF(
    @has_new_fk = 0,
    'ALTER TABLE giao_dich ADD CONSTRAINT fk_giao_dich_danh_muc_chi FOREIGN KEY (danh_muc_chi_id) REFERENCES danh_muc(id) ON DELETE SET NULL',
    "SELECT 'New FK already exists'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5) Add index mới nếu chưa có
SET @has_new_idx := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS s
    WHERE s.TABLE_SCHEMA = @db
      AND s.TABLE_NAME = 'giao_dich'
      AND s.INDEX_NAME = 'idx_giao_dich_danh_muc_chi'
);
SET @sql := IF(
    @has_new_idx = 0,
    'CREATE INDEX idx_giao_dich_danh_muc_chi ON giao_dich(danh_muc_chi_id)',
    "SELECT 'New index already exists'"
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6) Kiểm tra nhanh sau migration
SELECT COLUMN_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'giao_dich'
  AND COLUMN_NAME IN ('danh_muc_id', 'danh_muc_chi_id', 'danh_muc_thu_id')
ORDER BY COLUMN_NAME;

SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = @db
  AND TABLE_NAME = 'giao_dich'
  AND COLUMN_NAME IN ('danh_muc_chi_id', 'danh_muc_thu_id')
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY CONSTRAINT_NAME;
