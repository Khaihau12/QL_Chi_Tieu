# HƯỚNG DẪN CÁC TÍNH NĂNG MỚI

## 🎯 Tổng Quan

Đã thêm 4 tính năng mới vào ứng dụng Quản Lý Chi Tiêu:

1. **Phân loại giao dịch** - Chọn danh mục khi chuyển tiền
2. **Quản lý ngân sách** - Thiết lập giới hạn chi tiêu theo danh mục
3. **Cảnh báo chi tiêu** - Tự động kiểm tra vượt ngân sách (tích hợp vào quản lý ngân sách)
4. **Thống kê chi tiêu** - Xem báo cáo chi tiêu theo danh mục

---

## 📋 BƯỚC 1: Chạy SQL Script

**QUAN TRỌNG**: Trước khi sử dụng, bạn phải chạy file SQL để tạo database:

```sql
-- Mở MySQL Workbench hoặc command line
mysql -u root -p < setup.sql
```

Hoặc copy nội dung file `setup.sql` vào MySQL Workbench và Execute.

File này sẽ:
- Tạo database `QLChiTieu`
- Tạo bảng `nguoi_dung` với 3 tài khoản mẫu
- Tạo bảng `danh_muc` với 8 danh mục mặc định (Ăn uống, Di chuyển, Mua sắm, Giải trí, Học tập, Sức khỏe, Hóa đơn, Khác)
- Tạo bảng `giao_dich` với cột `danh_muc_id`
- Tạo bảng `ngan_sach` để lưu trữ giới hạn chi tiêu
- Thêm dữ liệu mẫu (người dùng, danh mục, giao dịch)

---

## 💸 TÍNH NĂNG 1: Phân Loại Giao Dịch

### Cách sử dụng:
1. Vào màn hình **Chuyển Tiền** (nút "💸 Chuyển tiền")
2. Điền thông tin như bình thường:
   - Số tài khoản người nhận
   - Số tiền
3. **MỚI**: Chọn danh mục từ dropdown "Danh mục"
   - Mặc định: "Khác"
   - Có thể chọn: Ăn uống, Di chuyển, Mua sắm, Giải trí, Học tập, Sức khỏe, Hóa đơn, Khác
4. Nhập nội dung (tùy chọn)
5. Nhấn "Chuyển tiền"

### Lợi ích:
- Theo dõi được chi tiêu vào mục nào
- Chuẩn bị cho tính năng thống kê và ngân sách

---

## 📊 TÍNH NĂNG 2: Quản Lý Ngân Sách

### Cách sử dụng:

#### A. Vào màn hình Quản Lý Ngân Sách:
- Từ Dashboard, nhấn nút **"📋 Ngân sách"** (màu cam)

#### B. Thêm ngân sách mới:
1. Chọn tháng/năm muốn quản lý
2. Nhấn nút **"Thêm Ngân Sách"**
3. Chọn danh mục (VD: Ăn uống)
4. Nhập giới hạn (VD: 5000000 = 5 triệu)
5. Nhấn "Thêm"

#### C. Xem chi tiết:
Bảng hiển thị:
- **Danh Mục**: Loại chi tiêu
- **Giới Hạn**: Số tiền tối đa cho phép
- **Đã Chi**: Số tiền đã chi trong tháng (màu đỏ nếu vượt, xanh nếu OK)
- **Còn Lại**: Số tiền còn lại (màu đỏ nếu âm, xanh nếu dương)
- **Tháng/Năm**: Thời gian áp dụng

#### D. Sửa ngân sách:
1. Click chọn 1 dòng trong bảng
2. Nhấn "Sửa"
3. Nhập giới hạn mới
4. Nhấn OK

#### E. Xóa ngân sách:
1. Click chọn 1 dòng
2. Nhấn "Xóa"
3. Xác nhận

### Lợi ích:
- Kiểm soát chi tiêu theo từng danh mục
- Cảnh báo trực quan khi vượt ngân sách (chữ đỏ)
- Quản lý theo từng tháng

---

## 📈 TÍNH NĂNG 3: Thống Kê Chi Tiêu

### Cách sử dụng:

#### A. Vào màn hình Thống Kê:
- Từ Dashboard, nhấn nút **"📊 Thống kê"** (màu tím)

#### B. Xem báo cáo:
1. Chọn tháng/năm muốn xem
2. Nhấn "Xem Thống Kê"

#### C. Thông tin hiển thị:
**Tổng quan:**
- **Tổng Chi**: Tổng số tiền đã gửi trong tháng (màu đỏ)
- **Tổng Thu**: Tổng số tiền đã nhận trong tháng (màu xanh)

**Biểu đồ chi tiêu theo danh mục:**
- Mỗi danh mục hiển thị:
  - Tên danh mục
  - Thanh progress bar (tương đối)
  - Số tiền chi (VNĐ)
  - Phần trăm so với tổng chi (%)
  
**Ví dụ:**
```
Ăn uống        [███████████████░░░]  2,500,000 VNĐ (50.0%)
Di chuyển      [███████░░░░░░░░░░░]  1,000,000 VNĐ (20.0%)
Giải trí       [█████░░░░░░░░░░░░░]    800,000 VNĐ (16.0%)
```

### Lợi ích:
- Biết được chi tiêu nhiều nhất vào mục nào
- So sánh giữa các danh mục
- Theo dõi xu hướng chi tiêu theo tháng

---

## 🚀 QUY TRÌNH SỬ DỤNG HOÀN CHỈNH

### Ví dụ thực tế:

**Tháng 2/2026 - Quản lý chi tiêu:**

1. **Thiết lập ngân sách đầu tháng:**
   - Vào "Ngân sách"
   - Chọn Tháng 2, Năm 2026
   - Thêm:
     - Ăn uống: 3,000,000 đ
     - Di chuyển: 1,000,000 đ
     - Giải trí: 500,000 đ
     - Mua sắm: 2,000,000 đ

2. **Trong tháng - Khi chi tiêu:**
   - Vào "Chuyển tiền"
   - Chuyển tiền và **chọn đúng danh mục**
   - VD: Mua đồ ăn → Chọn "Ăn uống"

3. **Giữa tháng - Kiểm tra:**
   - Vào "Ngân sách"
   - Xem cột "Đã Chi" và "Còn Lại"
   - Nếu thấy chữ đỏ → Đã vượt ngân sách!

4. **Cuối tháng - Xem báo cáo:**
   - Vào "Thống kê"
   - Xem chi tiết chi tiêu theo từng danh mục
   - So sánh với ngân sách đã đặt

---

## 🎨 CÁC NÚT VÀ BIỂU TƯỢNG

### Từ Dashboard:
- **💸 Chuyển tiền** (màu xanh dương) - Chuyển tiền với danh mục
- **🔄 Làm mới** (màu xám) - Cập nhật dữ liệu
- **📋 Ngân sách** (màu cam) - Quản lý ngân sách
- **📊 Thống kê** (màu tím) - Xem báo cáo
- **Đăng xuất** (màu đỏ) - Thoát tài khoản

### Trong màn hình Ngân sách:
- **Thêm Ngân Sách** - Tạo giới hạn mới
- **Sửa** - Thay đổi giới hạn
- **Xóa** - Xóa ngân sách
- **Quay Lại** - Về Dashboard

### Trong màn hình Thống kê:
- **Xem Thống Kê** - Tải dữ liệu
- **Quay Lại** - Về Dashboard

---

## 📝 LƯU Ý

1. **Phải chạy file setup.sql** để tạo database hoàn chỉnh với tất cả tính năng
2. **Chọn đúng danh mục** khi chuyển tiền để thống kê chính xác
3. **Thiết lập ngân sách đầu tháng** để kiểm soát chi tiêu tốt hơn
4. **Kiểm tra thường xuyên** màn hình Ngân sách để tránh chi quá
5. **Số liệu thống kê** chỉ chính xác khi đã chọn danh mục cho giao dịch

---

## 🐛 XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi: Không thấy ComboBox "Danh mục"
- ✅ Kiểm tra đã chạy file `setup.sql` chưa
- ✅ Reload VS Code hoặc Maven reimport

### Lỗi: Bảng thống kê trống
- ✅ Đảm bảo đã có giao dịch với danh mục trong tháng đó
- ✅ Kiểm tra chọn đúng tháng/năm

### Lỗi: Không thêm được ngân sách
- ✅ Kiểm tra bảng `danh_muc` có dữ liệu không (phải có 8 danh mục)
- ✅ Kiểm tra connection database

---

## 📚 CẤU TRÚC CODE MỚI

### Models (model/):
- `DanhMuc.java` - Danh mục chi tiêu
- `NganSach.java` - Ngân sách
- `GiaoDich.java` - Đã thêm field `danhMucId`

### DAO (dao/):
- `DanhMucDAO.java` - CRUD danh mục
- `NganSachDAO.java` - CRUD ngân sách + kiểm tra vượt ngân sách
- `GiaoDichDAO.java` - Đã cập nhật method `chuyenTien()`

### Controllers (controller/):
- `BudgetController.java` - UI quản lý ngân sách
- `StatisticsController.java` - UI thống kê
- `TransactionController.java` - Đã thêm ComboBox danh mục
- `DashboardController.java` - Đã thêm 2 nút mới

### Database:
- `setup.sql` - Script tạo database hoàn chỉnh (đã bao gồm tất cả tính năng mới)

---

## ✅ CHECKLIST

- [ ] Đã chạy file `setup.sql`
- [ ] Kiểm tra bảng `danh_muc` có 8 dòng dữ liệu
- [ ] Vào Dashboard thấy 2 nút mới: "Ngân sách" và "Thống kê"
- [ ] Vào Chuyển tiền thấy ComboBox "Danh mục"
- [ ] Thử thêm/sửa/xóa ngân sách
- [ ] Thử xem thống kê theo tháng

---

**Chúc bạn sử dụng vui vẻ! 🎉**
