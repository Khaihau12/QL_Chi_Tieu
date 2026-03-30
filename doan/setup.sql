-- =====================================================
-- ĐỒ ÁN CƠ SỞ: HỆ THỐNG QUẢN LÝ CHI TIÊU
-- Database: QLChiTieu
-- Phiên bản: Đơn giản cho sinh viên
-- =====================================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS QLChiTieu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QLChiTieu;

-- =====================================================
-- 1. BẢNG NGƯỜI DÙNG (GỒM CẢ ADMIN VÀ USER)
-- =====================================================
CREATE TABLE nguoi_dung (
    ma_nguoi_dung INT AUTO_INCREMENT PRIMARY KEY,
    so_tai_khoan VARCHAR(10) NOT NULL UNIQUE, -- Số tài khoản ngắn gọn (3 chữ số: 101, 102...)
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    mat_khau VARCHAR(255) NOT NULL,
    ho_ten VARCHAR(100),
    so_du DECIMAL(15, 2) DEFAULT 0, -- Số dư tài khoản ngân hàng
    so_du_tien_mat DECIMAL(15, 2) DEFAULT 0, -- Số dư ví tiền mặt
    vai_tro VARCHAR(20) DEFAULT 'nguoi_dung', -- 'quan_ly' hoặc 'nguoi_dung'
    trang_thai VARCHAR(20) DEFAULT 'hoat_dong', -- 'hoat_dong' hoặc 'bi_khoa'
    ly_do_khoa VARCHAR(500) DEFAULT NULL,       -- Lý do khóa tài khoản (NULL nếu đang hoạt động)
    thoi_gian_mo_khoa DATETIME DEFAULT NULL,    -- Thời điểm tự động mở khóa (NULL = khóa vĩnh viễn)
    lan_dang_nhap_cuoi DATETIME NULL,
    ngay_tao DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================
-- 2. BẢNG DANH MỤC (PHÂN LOẠI GIAO DỊCH)
-- =====================================================
CREATE TABLE danh_muc (
    id INT PRIMARY KEY AUTO_INCREMENT,
    ten_danh_muc VARCHAR(100) NOT NULL,
    mo_ta TEXT,
    loai VARCHAR(10) DEFAULT 'chi',         -- 'chi' = danh mục chi tiêu, 'thu' = danh mục thu nhập
    so_tai_khoan VARCHAR(10) DEFAULT NULL,  -- NULL = danh mục mặc định, khác NULL = danh mục riêng
    parent_id INT DEFAULT NULL,             -- NULL = danh mục cha/gốc, khác NULL = danh mục con
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES danh_muc(id) ON DELETE SET NULL,
    FOREIGN KEY (so_tai_khoan) REFERENCES nguoi_dung(so_tai_khoan) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- 3. BẢNG GIAO DỊCH (CHUYỂN TIỀN)
-- =====================================================
CREATE TABLE giao_dich (
    ma_giao_dich INT AUTO_INCREMENT PRIMARY KEY,
    so_tai_khoan_gui VARCHAR(20) NOT NULL, -- Số tài khoản gửi tiền
    so_tai_khoan_nhan VARCHAR(20) NOT NULL, -- Số tài khoản nhận tiền
    so_tien DECIMAL(15, 2) NOT NULL,
    noi_dung TEXT, -- Nội dung chuyển tiền
    danh_muc_id INT DEFAULT NULL,     -- Danh mục chi tiêu của người gửi
    danh_muc_thu_id INT DEFAULT NULL, -- Danh mục thu nhập của người nhận
    ngay_giao_dich DATETIME DEFAULT CURRENT_TIMESTAMP,
    trang_thai VARCHAR(20) DEFAULT 'thanh_cong', -- 'thanh_cong', 'that_bai'
    FOREIGN KEY (so_tai_khoan_gui) REFERENCES nguoi_dung(so_tai_khoan),
    FOREIGN KEY (so_tai_khoan_nhan) REFERENCES nguoi_dung(so_tai_khoan),
    FOREIGN KEY (danh_muc_id) REFERENCES danh_muc(id) ON DELETE SET NULL,
    FOREIGN KEY (danh_muc_thu_id) REFERENCES danh_muc(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- 4. BẢNG NGÂN SÁCH (QUẢN LÝ CHI TIÊU)
-- =====================================================
CREATE TABLE ngan_sach (
    id INT PRIMARY KEY AUTO_INCREMENT,
    danh_muc_id INT NOT NULL,
    so_tai_khoan VARCHAR(20) NOT NULL,
    gioi_han DECIMAL(15,2) NOT NULL,  -- Giới hạn chi tiêu
    thang INT NOT NULL,                -- 1-12
    nam INT NOT NULL,                  -- 2024, 2025...
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (danh_muc_id) REFERENCES danh_muc(id) ON DELETE CASCADE,
    FOREIGN KEY (so_tai_khoan) REFERENCES nguoi_dung(so_tai_khoan) ON DELETE CASCADE,
    UNIQUE KEY unique_budget (danh_muc_id, so_tai_khoan, thang, nam)
) ENGINE=InnoDB;

-- =====================================================
-- 5. TẠO INDEX ĐỂ TĂNG TỐC QUERY
-- =====================================================
CREATE INDEX idx_giao_dich_danh_muc ON giao_dich(danh_muc_id);
CREATE INDEX idx_giao_dich_danh_muc_thu ON giao_dich(danh_muc_thu_id);
CREATE INDEX idx_giao_dich_ngay ON giao_dich(ngay_giao_dich);
CREATE INDEX idx_ngan_sach_thang_nam ON ngan_sach(thang, nam);
CREATE INDEX idx_danh_muc_parent ON danh_muc(parent_id);

-- =====================================================
-- DỮ LIỆU MẶC ĐỊNH
-- =====================================================

-- 1. Tài khoản (Admin + User)
-- Admin - Username: admin | Password: admin123 (MD5: 0192023a7bbd73250516f069df18b500)
INSERT INTO nguoi_dung (so_tai_khoan, ten_dang_nhap, email, mat_khau, ho_ten, so_du, so_du_tien_mat, vai_tro, trang_thai) 
VALUES ('101', 'admin', 'admin@qlchitieu.com', '0192023a7bbd73250516f069df18b500', 'Quản Trị Viên',  0, 0, 'quan_ly', 'hoat_dong');

-- User mẫu
-- nguyenvana - Password: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
-- tranthib - Password: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO nguoi_dung (so_tai_khoan, ten_dang_nhap, email, mat_khau, ho_ten, so_du, so_du_tien_mat, vai_tro, trang_thai) 
VALUES 
('102', 'nguyenvana', 'nguyenvana@email.com', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyễn Văn A', 10000000, 0, 'nguoi_dung', 'hoat_dong'),
('103', 'tranthib', 'tranthib@email.com', 'e10adc3949ba59abbe56e057f20f883e', 'Trần Thị B', 8000000, 0, 'nguoi_dung', 'hoat_dong');

-- 2a. Danh mục chi mặc định theo nhóm (cha/con) - loai='chi'
-- Nhóm cha
INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id) VALUES
('Chi tiêu - sinh hoạt', 'Nhóm chi tiêu sinh hoạt hằng ngày', 'chi', NULL, NULL),
('Chi phí phát sinh',    'Nhóm chi phí phát sinh theo nhu cầu', 'chi', NULL, NULL),
('Chi phí cố định',      'Nhóm chi phí cố định định kỳ', 'chi', NULL, NULL),
('Đầu tư - tiết kiệm',   'Nhóm đầu tư và tiết kiệm', 'chi', NULL, NULL);

-- Danh mục con của "Chi tiêu - sinh hoạt"
INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Chợ, siêu thị', 'Đi chợ, mua thực phẩm, đồ gia dụng', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi tiêu - sinh hoạt' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Ăn uống', 'Ăn uống, nhà hàng, cafe', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi tiêu - sinh hoạt' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Di chuyển', 'Xe bus, taxi, xăng xe', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi tiêu - sinh hoạt' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

-- Danh mục con của "Chi phí phát sinh"
INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Mua sắm', 'Mua sắm hàng hóa, đồ dùng cá nhân', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi phí phát sinh' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Giải trí', 'Xem phim, du lịch, sở thích', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi phí phát sinh' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Làm đẹp', 'Mỹ phẩm, làm tóc, chăm sóc cá nhân', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi phí phát sinh' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Sức khỏe', 'Khám bệnh, thuốc men, chăm sóc sức khỏe', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi phí phát sinh' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Từ thiện', 'Ủng hộ, quyên góp, hoạt động cộng đồng', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi phí phát sinh' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

-- Danh mục con của "Chi phí cố định"
INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Hóa đơn', 'Điện, nước, internet, điện thoại', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi phí cố định' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Nhà cửa', 'Chi phí nhà cửa, sửa chữa, thuê nhà', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi phí cố định' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Người thân', 'Chu cấp, hỗ trợ người thân', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Chi phí cố định' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Đầu tư', 'Chi phí góp vốn, tích lũy đầu tư', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Đầu tư - tiết kiệm' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT 'Học tập', 'Chi phí học tập, khóa học, kỹ năng', 'chi', NULL, id
FROM danh_muc WHERE ten_danh_muc = 'Đầu tư - tiết kiệm' AND loai = 'chi' AND so_tai_khoan IS NULL LIMIT 1;

-- 2b. Danh mục thu mặc định theo hình - loai='thu'
-- LƯU Ý: giữ "Thu khác" để gán tự động khi nhận tiền chuyển khoản
INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
VALUES ('Thu', 'Nhóm danh mục thu nhập', 'thu', NULL, NULL);

INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan, parent_id)
SELECT x.ten_danh_muc, x.mo_ta, 'thu', NULL, p.id
FROM (
    SELECT 'Thu hồi nợ' AS ten_danh_muc, 'Nhận lại tiền đã cho vay' AS mo_ta
    UNION ALL SELECT 'Kinh doanh', 'Thu nhập từ kinh doanh'
    UNION ALL SELECT 'Lợi nhuận', 'Lợi nhuận, cổ tức, đầu tư'
    UNION ALL SELECT 'Thưởng', 'Tiền thưởng, bonus'
    UNION ALL SELECT 'Trợ cấp', 'Trợ cấp, hỗ trợ tài chính'
    UNION ALL SELECT 'Lương', 'Thu nhập từ lương, thù lao'
    UNION ALL SELECT 'Thu khác', 'Các khoản thu nhập khác (mặc định nhận tiền)'
) x
JOIN (
    SELECT id
    FROM danh_muc
    WHERE ten_danh_muc = 'Thu' AND loai = 'thu' AND so_tai_khoan IS NULL AND parent_id IS NULL
    ORDER BY id DESC
    LIMIT 1
) p;

-- 3. Giao dịch mẫu (Chuyển tiền giữa các tài khoản với danh mục)
INSERT INTO giao_dich (so_tai_khoan_gui, so_tai_khoan_nhan, so_tien, noi_dung, danh_muc_id, ngay_giao_dich) VALUES
('101', '102', 5000000, 'Chuyển tiền hỗ trợ', 8, '2026-01-05 10:30:00'),
('102', '103', 2000000, 'Trả nợ', 8, '2026-01-06 14:20:00'),
('103', '102', 1000000, 'Tiền ăn', 1, '2026-01-08 09:15:00');

-- =====================================================
-- NÂNG CẤP DATABASE (Chạy nếu đã có DB cũ, bỏ qua nếu tạo mới)
-- =====================================================
-- Giao dịch: bổ sung cột nếu DB cũ còn thiếu
-- ALTER TABLE giao_dich ADD COLUMN IF NOT EXISTS ngay_giao_dich DATETIME DEFAULT CURRENT_TIMESTAMP;
-- ALTER TABLE giao_dich ADD COLUMN IF NOT EXISTS trang_thai VARCHAR(20) DEFAULT 'thanh_cong';

-- Ngân sách: bổ sung cột thời gian nếu DB cũ còn thiếu
-- ALTER TABLE ngan_sach ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
-- ALTER TABLE ngan_sach ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Ví tiền mặt: bổ sung cột cho DB cũ nếu chưa có
-- ALTER TABLE nguoi_dung ADD COLUMN IF NOT EXISTS so_du_tien_mat DECIMAL(15,2) DEFAULT 0 AFTER so_du;
-- UPDATE nguoi_dung SET so_du_tien_mat = 0 WHERE so_du_tien_mat IS NULL;

-- ALTER TABLE danh_muc ADD COLUMN IF NOT EXISTS loai VARCHAR(10) DEFAULT 'chi';
-- ALTER TABLE danh_muc ADD COLUMN IF NOT EXISTS parent_id INT DEFAULT NULL;
-- ALTER TABLE danh_muc ADD CONSTRAINT fk_danh_muc_parent FOREIGN KEY (parent_id) REFERENCES danh_muc(id) ON DELETE SET NULL;
-- ALTER TABLE giao_dich ADD COLUMN IF NOT EXISTS danh_muc_thu_id INT DEFAULT NULL;
-- ALTER TABLE giao_dich ADD CONSTRAINT fk_dmthu FOREIGN KEY (danh_muc_thu_id) REFERENCES danh_muc(id) ON DELETE SET NULL;
-- UPDATE danh_muc SET loai='chi' WHERE loai IS NULL;
-- Thêm danh mục thu mặc định nếu chưa có:
-- INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan) VALUES ('Lương','Thu từ lương','thu',NULL),('Thưởng','Thưởng bonus','thu',NULL),('Hoàn tiền','Hoàn tiền','thu',NULL),('Đầu tư','Lợi nhuận','thu',NULL),('Thu khác','Thu nhập khác','thu',NULL);

-- =====================================================
-- HOÀN TẤT
-- =====================================================

SELECT 'Database QLChiTieu đã tạo thành công!' AS Thong_Bao;
SELECT CONCAT('Số người dùng: ', COUNT(*)) AS Tong_Nguoi_Dung FROM nguoi_dung;
SELECT CONCAT('Số danh mục: ', COUNT(*)) AS Tong_Danh_Muc FROM danh_muc;
SELECT CONCAT('Số giao dịch: ', COUNT(*)) AS Tong_Giao_Dich FROM giao_dich;

-- =====================================================
-- THÔNG TIN ĐĂNG NHẬP
-- =====================================================
-- ADMIN (so_tai_khoan=101):
--   User: admin
--   Pass: admin123
--   MD5: 0192023a7bbd73250516f069df18b500
--   Số dư: 100.000.000 đ
--   Vai trò: quan_ly
--
-- USER 1 (so_tai_khoan=102):
--   User: nguyenvana
--   Pass: 123456
--   MD5: e10adc3949ba59abbe56e057f20f883e
--   Số dư: 10.000.000 đ
--   Vai trò: nguoi_dung
--
-- USER 2 (so_tai_khoan=103):
--   User: tranthib
--   Pass: 123456
--   MD5: e10adc3949ba59abbe56e057f20f883e
--   Số dư: 8.000.000 đ
--   Vai trò: nguoi_dung
-- =====================================================

-- LƯU Ý: 
-- - Mật khẩu đã mã hóa bằng MD5
-- - Số tài khoản ngắn gọn 3 chữ số (101, 102, 103...)
-- - Danh mục mặc định (so_tai_khoan=NULL): Mọi người dùng đều thấy
-- - Danh mục riêng (so_tai_khoan='102'): Chỉ người dùng đó thấy
-- - Giao dịch là chuyển tiền giữa các tài khoản
-- - Database đơn giản, dễ hiểu cho đồ án
