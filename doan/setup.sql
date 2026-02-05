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
    so_dien_thoai VARCHAR(20),
    so_du DECIMAL(15, 2) DEFAULT 0, -- Số dư tài khoản
    vai_tro VARCHAR(20) DEFAULT 'nguoi_dung', -- 'quan_ly' hoặc 'nguoi_dung'
    trang_thai VARCHAR(20) DEFAULT 'hoat_dong', -- 'hoat_dong' hoặc 'bi_khoa'
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
    mau_sac VARCHAR(7) DEFAULT '#3498db',  -- Màu hex
    so_tai_khoan VARCHAR(10) DEFAULT NULL,  -- NULL = danh mục mặc định, khác NULL = danh mục riêng
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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
    danh_muc_id INT DEFAULT NULL, -- Danh mục của giao dịch
    ngay_giao_dich DATETIME DEFAULT CURRENT_TIMESTAMP,
    trang_thai VARCHAR(20) DEFAULT 'thanh_cong', -- 'thanh_cong', 'that_bai'
    FOREIGN KEY (so_tai_khoan_gui) REFERENCES nguoi_dung(so_tai_khoan),
    FOREIGN KEY (so_tai_khoan_nhan) REFERENCES nguoi_dung(so_tai_khoan),
    FOREIGN KEY (danh_muc_id) REFERENCES danh_muc(id) ON DELETE SET NULL
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
CREATE INDEX idx_giao_dich_ngay ON giao_dich(ngay_giao_dich);
CREATE INDEX idx_ngan_sach_thang_nam ON ngan_sach(thang, nam);

-- =====================================================
-- DỮ LIỆU MẶC ĐỊNH
-- =====================================================

-- 1. Tài khoản (Admin + User)
-- Admin - Username: admin | Password: admin123 (MD5: 0192023a7bbd73250516f069df18b500)
INSERT INTO nguoi_dung (so_tai_khoan, ten_dang_nhap, email, mat_khau, ho_ten, so_du, vai_tro, trang_thai) 
VALUES ('101', 'admin', 'admin@qlchitieu.com', '0192023a7bbd73250516f069df18b500', 'Quản Trị Viên', 100000000, 'quan_ly', 'hoat_dong');

-- User mẫu
-- nguyenvana - Password: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
-- tranthib - Password: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO nguoi_dung (so_tai_khoan, ten_dang_nhap, email, mat_khau, ho_ten, so_dien_thoai, so_du, vai_tro, trang_thai) 
VALUES 
('102', 'nguyenvana', 'nguyenvana@email.com', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyễn Văn A', '0901234567', 10000000, 'nguoi_dung', 'hoat_dong'),
('103', 'tranthib', 'tranthib@email.com', 'e10adc3949ba59abbe56e057f20f883e', 'Trần Thị B', '0912345678', 8000000, 'nguoi_dung', 'hoat_dong');

-- 2. Danh mục mặc định (8 loại - Mọi người dùng đều có)
INSERT INTO danh_muc (ten_danh_muc, mo_ta, mau_sac, so_tai_khoan) VALUES
('Ăn uống', 'Chi phí ăn uống, nhà hàng, cafe', '#e74c3c', NULL),
('Di chuyển', 'Xe bus, taxi, xăng xe', '#3498db', NULL),
('Mua sắm', 'Quần áo, đồ dùng cá nhân', '#9b59b6', NULL),
('Giải trí', 'Xem phim, du lịch, sở thích', '#f39c12', NULL),
('Học tập', 'Sách, khóa học, văn phòng phẩm', '#27ae60', NULL),
('Sức khỏe', 'Thuốc men, khám bệnh', '#e67e22', NULL),
('Hóa đơn', 'Điện, nước, internet, điện thoại', '#95a5a6', NULL),
('Khác', 'Các khoản chi khác', '#34495e', NULL);

-- 3. Giao dịch mẫu (Chuyển tiền giữa các tài khoản với danh mục)
INSERT INTO giao_dich (so_tai_khoan_gui, so_tai_khoan_nhan, so_tien, noi_dung, danh_muc_id, ngay_giao_dich) VALUES
('101', '102', 5000000, 'Chuyển tiền hỗ trợ', 8, '2026-01-05 10:30:00'),
('102', '103', 2000000, 'Trả nợ', 8, '2026-01-06 14:20:00'),
('103', '102', 1000000, 'Tiền ăn', 1, '2026-01-08 09:15:00');

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
