# HƯỚNG DẪN SỬ DỤNG DATABASE QLChiTieu

## 📦 **IMPORT DATABASE VÀO XAMPP**

### Bước 1: Khởi động XAMPP
1. Mở XAMPP Control Panel
2. Click **Start** cho **Apache**  
3. Click **Start** cho **MySQL**

### Bước 2: Mở phpMyAdmin
1. Mở trình duyệt
2. Truy cập: http://localhost/phpmyadmin
3. Click vào tab **SQL** ở menu trên

### Bước 3: Import file setup.sql
**Cách 1: Dùng SQL**
1. Click tab **SQL**
2. Copy toàn bộ nội dung file `setup.sql`
3. Paste vào ô SQL
4. Click **Go** (Thực hiện)

**Cách 2: Dùng Import**
1. Click tab **Import**
2. Click **Choose File** (Chọn tệp)
3. Chọn file `setup.sql`
4. Click **Go**

### Bước 4: Kiểm tra
1. Refresh trang (F5)
2. Bên trái sẽ thấy database **QLChiTieu**
3. Click vào để xem các bảng

---

## 🗂️ **CẤU TRÚC DATABASE**

### Database: `QLChiTieu`
- ✅ 9 bảng dữ liệu
- ✅ 3 triggers tự động
- ✅ 1 stored procedure
- ✅ Charset: utf8mb4_unicode_ci

### Danh sách bảng (Tiếng Việt):
1. **nguoi_dung** - Thông tin người dùng
2. **quan_ly** - Thông tin quản lý hệ thống
3. **phan_loai** - Phân loại thu/chi
4. **giao_dich** - Các giao dịch thu chi
5. **ngan_sach** - Kế hoạch ngân sách
6. **canh_bao** - Cảnh báo tự động
7. **so_du_tai_khoan** - Số dư hiện tại
8. **bao_cao_he_thong** - Báo cáo quản lý
9. **lich_su_truy_cap** - Lịch sử đăng nhập

---

## 👤 **TÀI KHOẢN MẪU**

### Quản lý hệ thống:
- **Tên đăng nhập:** admin
- **Mật khẩu:** admin123
- **Email:** admin@qlchitieu.com

### Người dùng 1:
- **Tên đăng nhập:** nguyenvana
- **Mật khẩu:** 123456
- **Họ tên:** Nguyễn Văn A
- **Số dư:** 5,000,000 VNĐ
- **Giao dịch:** 10+ giao dịch mẫu

### Người dùng 2:
- **Tên đăng nhập:** tranthib
- **Mật khẩu:** 123456
- **Họ tên:** Trần Thị B
- **Số dư:** 3,000,000 VNĐ
- **Giao dịch:** 5+ giao dịch mẫu

---

## 📊 **DỮ LIỆU MẪU CÓ SẴN**

### Phân loại (15 loại):
**Thu nhập (5):**
- Lương
- Thưởng
- Đầu tư
- Kinh doanh
- Thu nhập khác

**Chi tiêu (10):**
- Ăn uống
- Đi lại
- Mua sắm
- Giải trí
- Y tế
- Giáo dục
- Nhà cửa
- Điện thoại
- Tiết kiệm
- Chi tiêu khác

### Giao dịch mẫu:
- Nguyễn Văn A: 10 giao dịch (thu + chi)
- Trần Thị B: 5 giao dịch

### Ngân sách:
- Cả 2 người dùng đều có ngân sách tháng 1/2026
- Ngân sách theo từng danh mục + tổng ngân sách

### Cảnh báo:
- Có 2 cảnh báo mẫu về ngân sách

---

## ⚙️ **CẤU HÌNH CODE**

### File đã được cập nhật:
✅ **database.properties** - Đã trỏ đến database `QLChiTieu`
✅ **DatabaseConfig.java** - Đã cập nhật URL

### Mapping tên cột (Anh → Việt):

| Tên cũ (Anh) | Tên mới (Việt) |
|--------------|----------------|
| user_id | ma_nguoi_dung |
| username | ten_dang_nhap |
| email | email |
| password_hash | mat_khau_hash |
| full_name | ho_ten |
| phone_number | so_dien_thoai |
| created_at | ngay_tao |
| updated_at | ngay_cap_nhat |
| last_login | lan_dang_nhap_cuoi |
| is_active | trang_thai_hoat_dong |
| category_id | ma_phan_loai |
| category_name | ten_phan_loai |
| category_type | loai_phan_loai |
| transaction_id | ma_giao_dich |
| amount | so_tien |
| description | mo_ta |
| transaction_date | ngay_giao_dich |
| budget_id | ma_ngan_sach |
| budget_amount | so_tien_ngan_sach |
| start_date | ngay_bat_dau |
| end_date | ngay_ket_thuc |

---

## 🔧 **SỬ DỤNG TRONG CODE**

### QUAN TRỌNG: 
Các DAO classes cần được cập nhật lại để sử dụng tên cột tiếng Việt.

### Ví dụ SQL mới:
```java
// CŨ
String sql = "SELECT * FROM users WHERE username = ?";

// MỚI  
String sql = "SELECT * FROM nguoi_dung WHERE ten_dang_nhap = ?";
```

```java
// CŨ
user.setUserId(rs.getInt("user_id"));

// MỚI
user.setUserId(rs.getInt("ma_nguoi_dung"));
```

---

## ✨ **TÍNH NĂNG TỰ ĐỘNG**

### 1. Trigger cập nhật số dư:
- Tự động cộng/trừ tiền khi thêm giao dịch
- Cảnh báo khi số dư âm
- Không cần code xử lý thủ công

### 2. Kiểm tra ngân sách:
- Tự động gọi stored procedure
- Cảnh báo khi đạt 80% ngân sách
- Cảnh báo khi vượt 100% ngân sách

### 3. Timestamp tự động:
- `ngay_tao`: Tự động khi thêm mới
- `ngay_cap_nhat`: Tự động khi sửa
- `lan_cap_nhat_cuoi`: Tự động khi có thay đổi

---

## 🧪 **KIỂM TRA DATABASE**

### Truy vấn kiểm tra:

```sql
-- Xem tất cả người dùng
SELECT * FROM nguoi_dung;

-- Xem giao dịch của Nguyễn Văn A
SELECT gd.*, pl.ten_phan_loai 
FROM giao_dich gd
JOIN phan_loai pl ON gd.ma_phan_loai = pl.ma_phan_loai
WHERE gd.ma_nguoi_dung = 1;

-- Xem số dư
SELECT nd.ho_ten, sd.so_du_hien_tai 
FROM nguoi_dung nd
JOIN so_du_tai_khoan sd ON nd.ma_nguoi_dung = sd.ma_nguoi_dung;

-- Xem cảnh báo chưa đọc
SELECT * FROM canh_bao WHERE da_doc = FALSE;

-- Thống kê thu chi tháng 1
SELECT 
    loai_giao_dich,
    COUNT(*) as so_luong,
    SUM(so_tien) as tong_tien
FROM giao_dich
WHERE MONTH(ngay_giao_dich) = 1 AND YEAR(ngay_giao_dich) = 2026
GROUP BY loai_giao_dich;
```

---

## 📝 **GHI CHÚ QUAN TRỌNG**

1. **Mã hóa mật khẩu:** BCrypt - không thể giải mã
2. **Foreign Keys:** Đảm bảo tính toàn vẹn dữ liệu
3. **Charset UTF-8:** Hỗ trợ tiếng Việt có dấu
4. **Triggers:** Tự động, không cần gọi từ code
5. **Enum values:** 
   - `THU_NHAP` / `CHI_TIEU`
   - `THEO_THANG` / `THEO_QUY` / `THEO_NAM` / `TUY_CHINH`

---

## 🚀 **BƯỚC TIẾP THEO**

1. ✅ Import setup.sql vào phpMyAdmin
2. ⏭️ Kiểm tra database đã tạo đúng chưa
3. ⏭️ Test đăng nhập với tài khoản mẫu
4. ⏭️ Cập nhật DAO classes (nếu cần)
5. ⏭️ Chạy ứng dụng Java

---

## 🆘 **XỬ LÝ LỖI**

### Lỗi "Table already exists":
```sql
DROP DATABASE QLChiTieu;
-- Sau đó import lại setup.sql
```

### Lỗi charset:
```sql
ALTER DATABASE QLChiTieu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Reset dữ liệu:
```sql
USE QLChiTieu;
DELETE FROM giao_dich;
DELETE FROM ngan_sach;
DELETE FROM nguoi_dung WHERE ma_nguoi_dung > 0;
-- Import lại setup.sql
```

---

**🎉 Chúc bạn thành công!**
