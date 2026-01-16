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
    so_tai_khoan VARCHAR(20) NOT NULL UNIQUE, -- Số tài khoản ngân hàng (tự động tạo)
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
-- 2. BẢNG LỊCH SỪgD (TÙY CHỌN - ĐỂ XEM LẠI SAU)
-- =====================================================
-- Xóa bảng phan_loai vì không cần phân loại thu/chi

-- =====================================================
-- 3. BẢNG GIAO DỊCH (CHUYỂN TIỀN) (CHUYỂN TIỀN)
-- =====================================================
CREATE TABLE giao_dich (
    ma_giao_dich INT AUTO_INCREMENT PRIMARY KEY,
    so_tai_khoan_gui VARCHAR(20) NOT NULL, -- Số tài khoản gửi tiền
    so_tai_khoan_nhan VARCHAR(20) NOT NULL, -- Số tài khoản nhận tiền
    so_tien DECIMAL(15, 2) NOT NULL,
    noi_dung TEXT, -- Nội dung chuyển tiền
    ngay_giao_dich DATETIME DEFAULT CURRENT_TIMESTAMP,
    trang_thai VARCHAR(20) DEFAULT 'thanh_cong', -- 'thanh_cong', 'that_bai'
    FOREIGN KEY (so_tai_khoan_gui) REFERENCES nguoi_dung(so_tai_khoan),
    FOREIGN KEY (so_tai_khoan_nhan) REFERENCES nguoi_dung(so_tai_khoan)
) ENGINE=InnoDB;

-- =====================================================
-- DỮ LIỆU MẶC ĐỊNH
-- =====================================================

-- 1. Tài khoản (Admin + User)
-- Admin - Username: admin | Password: admin123 (MD5: 0192023a7bbd73250516f069df18b500)
INSERT INTO nguoi_dung (so_tai_khoan, ten_dang_nhap, email, mat_khau, ho_ten, so_du, vai_tro, trang_thai) 
VALUES ('1000000001', 'admin', 'admin@qlchitieu.com', '0192023a7bbd73250516f069df18b500', 'Quản Trị Viên', 100000000, 'quan_ly', 'hoat_dong');

-- User mẫu
-- nguyenvana - Password: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
-- tranthib - Password: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO nguoi_dung (so_tai_khoan, ten_dang_nhap, email, mat_khau, ho_ten, so_dien_thoai, so_du, vai_tro, trang_thai) 
VALUES 
('1000000002', 'nguyenvana', 'nguyenvana@email.com', 'e10adc3949ba59abbe56e057f20f883e', 'Nguyễn Văn A', '0901234567', 10000000, 'nguoi_dung', 'hoat_dong'),
('1000000003', 'tranthib', 'tranthib@email.com', 'e10adc3949ba59abbe56e057f20f883e', 'Trần Thị B', '0912345678', 8000000, 'nguoi_dung', 'hoat_dong');

-- 2. Giao dịch mẫu (Chuyển tiền giữa các tài khoản)
INSERT INTO giao_dich (so_tai_khoan_gui, so_tai_khoan_nhan, so_tien, noi_dung, ngay_giao_dich) VALUES
('1000000001', '1000000002', 5000000, 'Chuyển tiền hỗ trợ', '2026-01-05 10:30:00'),
('1000000002', '1000000003', 2000000, 'Trả nợ', '2026-01-06 14:20:00'),
('1000000003', '1000000002', 1000000, 'Tiền ăn', '2026-01-08 09:15:00');

-- =====================================================
-- HOÀN TẤT
-- =====================================================

SELECT 'Database QLChiTieu đã tạo thành công!' AS Thong_Bao;
SELECT CONCAT('Số người dùng: ', COUNT(*)) AS Tong_Nguoi_Dung FROM nguoi_dung;
SELECT CONCAT('Số giao dịch: ', COUNT(*)) AS Tong_Giao_Dich FROM giao_dich;

-- =====================================================
-- THÔNG TIN ĐĂNG NHẬP
-- =====================================================
-- ADMIN (so_tai_khoan=1000000001):
--   User: admin
--   Pass: admin123
--   MD5: 0192023a7bbd73250516f069df18b500
--   Số dư: 100.000.000 đ
--   Vai trò: quan_ly
--
-- USER 1 (so_tai_khoan=1000000002):
--   User: nguyenvana
--   Pass: 123456
--   MD5: e10adc3949ba59abbe56e057f20f883e
--   Số dư: 10.000.000 đ
--   Vai trò: nguoi_dung
--
-- USER 2 (so_tai_khoan=1000000003):
--   User: tranthib
--   Pass: 123456
--   MD5: e10adc3949ba59abbe56e057f20f883e
--   Số dư: 8.000.000 đ
--   Vai trò: nguoi_dung
-- =====================================================

-- LƯU Ý: 
-- - Mật khẩu đã mã hóa bằng MD5
-- - Số tài khoản tự động tạo khi đăng ký (bắt đầu từ 1000000001)
-- - Giao dịch là chuyển tiền giữa các tài khoản
-- - Database đơn giản, dễ hiểu cho đồ án
