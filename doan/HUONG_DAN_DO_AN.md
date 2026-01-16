# HƯỚNG DẪN DATABASE CHO ĐỒ ÁN

## 📌 **TỔNG QUAN**
Database đơn giản cho đồ án đại học - Quản lý chi tiêu cá nhân

**Đặc điểm:**
- ✅ 5 bảng cơ bản
- ✅ Không có trigger phức tạp
- ✅ Tính toán trong code Java
- ✅ Dữ liệu mẫu sẵn có

---

## 🗂️ **CẤU TRÚC DATABASE**

### 1. **nguoi_dung** - Người dùng
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| ma_nguoi_dung | INT | ID (Primary Key) |
| ten_dang_nhap | VARCHAR(50) | Username |
| email | VARCHAR(100) | Email |
| mat_khau | VARCHAR(255) | Password (mã hóa BCrypt) |
| ho_ten | VARCHAR(100) | Họ tên |
| so_dien_thoai | VARCHAR(20) | SĐT |
| ngay_tao | DATETIME | Ngày tạo |

### 2. **quan_ly** - Quản lý hệ thống
Giống bảng nguoi_dung (cho admin)

### 3. **phan_loai** - Phân loại thu/chi
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| ma_phan_loai | INT | ID |
| ten_phan_loai | VARCHAR(100) | Tên: Ăn uống, Lương... |
| loai_phan_loai | VARCHAR(20) | THU_NHAP hoặc CHI_TIEU |
| mo_ta | TEXT | Mô tả |
| la_mac_dinh | TINYINT(1) | 1=mặc định, 0=user tạo |
| ma_nguoi_dung | INT | ID người dùng (NULL=default) |

### 4. **giao_dich** - Giao dịch
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| ma_giao_dich | INT | ID |
| ma_nguoi_dung | INT | ID người dùng |
| ma_phan_loai | INT | ID phân loại |
| loai_giao_dich | VARCHAR(20) | THU_NHAP/CHI_TIEU |
| so_tien | DECIMAL(15,2) | Số tiền |
| mo_ta | TEXT | Mô tả |
| ngay_giao_dich | DATE | Ngày giao dịch |
| ngay_tao | DATETIME | Ngày tạo |

### 5. **ngan_sach** - Ngân sách
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| ma_ngan_sach | INT | ID |
| ma_nguoi_dung | INT | ID người dùng |
| ma_phan_loai | INT | ID phân loại (NULL=tổng) |
| so_tien_ngan_sach | DECIMAL(15,2) | Số tiền ngân sách |
| thang | INT | Tháng (1-12) |
| nam | INT | Năm |
| ngay_tao | DATETIME | Ngày tạo |

---

## 🚀 **CÁCH SỬ DỤNG**

### Bước 1: Khởi động XAMPP
- Start **Apache**
- Start **MySQL**

### Bước 2: Mở phpMyAdmin
- Truy cập: http://localhost/phpmyadmin

### Bước 3: Import Database
1. Click tab **SQL**
2. Copy toàn bộ nội dung file `setup.sql`
3. Paste vào
4. Click **Go** (Thực hiện)

### Bước 4: Kiểm tra
- Refresh → Thấy database **QLChiTieu** bên trái
- Click vào xem 5 bảng

---

## 👤 **TÀI KHOẢN MẪU**

### Admin:
```
Username: admin
Password: admin123
```

### User 1:
```
Username: nguyenvana
Password: 123456
```

### User 2:
```
Username: tranthib
Password: 123456
```

---

## 📊 **DỮ LIỆU CÓ SẴN**

- ✅ 10 phân loại (3 thu + 7 chi)
- ✅ 2 người dùng mẫu
- ✅ 9 giao dịch mẫu
- ✅ 6 ngân sách mẫu

---

## 💻 **XỬ LÝ TRONG CODE JAVA**

### Tính số dư:
```java
// Lấy tổng thu
SELECT SUM(so_tien) FROM giao_dich 
WHERE ma_nguoi_dung = ? AND loai_giao_dich = 'THU_NHAP';

// Lấy tổng chi
SELECT SUM(so_tien) FROM giao_dich 
WHERE ma_nguoi_dung = ? AND loai_giao_dich = 'CHI_TIEU';

// Số dư = Tổng thu - Tổng chi
BigDecimal soDu = tongThu.subtract(tongChi);
```

### Kiểm tra ngân sách:
```java
// Lấy ngân sách tháng hiện tại
SELECT * FROM ngan_sach 
WHERE ma_nguoi_dung = ? AND thang = ? AND nam = ?;

// Tính tổng chi trong tháng
SELECT SUM(so_tien) FROM giao_dich 
WHERE ma_nguoi_dung = ? AND loai_giao_dich = 'CHI_TIEU'
AND MONTH(ngay_giao_dich) = ? AND YEAR(ngay_giao_dich) = ?;

// So sánh
if (tongChi > nganSach) {
    System.out.println("Vượt ngân sách!");
}
```

### Báo cáo chi tiêu theo tháng:
```java
SELECT pl.ten_phan_loai, SUM(gd.so_tien) as tong
FROM giao_dich gd
JOIN phan_loai pl ON gd.ma_phan_loai = pl.ma_phan_loai
WHERE gd.ma_nguoi_dung = ? 
AND MONTH(gd.ngay_giao_dich) = ?
AND YEAR(gd.ngay_giao_dich) = ?
GROUP BY pl.ten_phan_loai;
```

---

## 📝 **MẸO CHO ĐỒ ÁN**

### Các chức năng nên làm:
1. ✅ Đăng ký / Đăng nhập
2. ✅ Thêm / Sửa / Xóa giao dịch
3. ✅ Xem danh sách giao dịch
4. ✅ Tính số dư hiện tại
5. ✅ Thiết lập ngân sách
6. ✅ Xem báo cáo thu chi
7. ✅ So sánh với ngân sách

### Không cần làm (phức tạp):
- ❌ Trigger tự động
- ❌ Stored procedures
- ❌ Cảnh báo realtime
- ❌ Export PDF

---

## 🔧 **CẤU HÌNH CODE**

File `database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/QLChiTieu?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
db.username=root
db.password=
```

---

## 🧪 **TEST QUERIES**

```sql
-- Xem tất cả giao dịch của user 1
SELECT * FROM giao_dich WHERE ma_nguoi_dung = 1;

-- Tính tổng thu của user 1
SELECT SUM(so_tien) FROM giao_dich 
WHERE ma_nguoi_dung = 1 AND loai_giao_dich = 'THU_NHAP';

-- Tính tổng chi của user 1
SELECT SUM(so_tien) FROM giao_dich 
WHERE ma_nguoi_dung = 1 AND loai_giao_dich = 'CHI_TIEU';

-- Xem ngân sách tháng 1/2026
SELECT * FROM ngan_sach 
WHERE ma_nguoi_dung = 1 AND thang = 1 AND nam = 2026;

-- Thống kê chi tiêu theo loại
SELECT pl.ten_phan_loai, COUNT(*) as so_luong, SUM(gd.so_tien) as tong_tien
FROM giao_dich gd
JOIN phan_loai pl ON gd.ma_phan_loai = pl.ma_phan_loai
WHERE gd.ma_nguoi_dung = 1 AND gd.loai_giao_dich = 'CHI_TIEU'
GROUP BY pl.ten_phan_loai;
```

---

## ❓ **XỬ LÝ LỖI**

### Lỗi import:
```sql
DROP DATABASE QLChiTieu;
-- Import lại
```

### Reset dữ liệu:
```sql
USE QLChiTieu;
DELETE FROM giao_dich;
DELETE FROM ngan_sach;
DELETE FROM nguoi_dung WHERE ma_nguoi_dung > 0;
-- Import lại
```

---

## 📚 **TÀI LIỆU THAM KHẢO**

- JDBC Tutorial: https://docs.oracle.com/javase/tutorial/jdbc/
- JavaFX: https://openjfx.io/
- BCrypt: https://github.com/jeremyh/jBCrypt
- MySQL: https://dev.mysql.com/doc/

---

**Chúc bạn làm đồ án tốt! 🎓**
