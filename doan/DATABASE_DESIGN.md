# PHÁC THẢO THIẾT KẾ DATABASE - HỆ THỐNG QUẢN LÝ CHI TIÊU

## 📋 TỔNG QUAN

**Tên Database:** `expense_management_db`  
**Công cụ:** XAMPP - MySQL (phpMyAdmin)  
**Charset:** utf8mb4_unicode_ci

---

## 🗂️ DANH SÁCH CÁC BẢNG

### 1. **BẢNG USERS** (Người dùng)
Lưu thông tin tài khoản người dùng

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| user_id | INT | ID người dùng | Primary Key, Auto Increment |
| username | VARCHAR(50) | Tên đăng nhập | Unique, Not Null |
| email | VARCHAR(100) | Email | Unique, Not Null |
| password_hash | VARCHAR(255) | Mật khẩu đã mã hóa | Not Null |
| full_name | VARCHAR(100) | Họ tên đầy đủ | |
| phone_number | VARCHAR(20) | Số điện thoại | |
| created_at | TIMESTAMP | Thời gian tạo | Default CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Thời gian cập nhật | Auto update |
| last_login | TIMESTAMP | Lần đăng nhập cuối | NULL |
| is_active | BOOLEAN | Trạng thái hoạt động | Default TRUE |

**Index:** username, email

---

### 2. **BẢNG MANAGERS** (Quản lý hệ thống)
Lưu thông tin tài khoản quản lý

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| manager_id | INT | ID quản lý | Primary Key, Auto Increment |
| username | VARCHAR(50) | Tên đăng nhập | Unique, Not Null |
| email | VARCHAR(100) | Email | Unique, Not Null |
| password_hash | VARCHAR(255) | Mật khẩu đã mã hóa | Not Null |
| full_name | VARCHAR(100) | Họ tên đầy đủ | |
| created_at | TIMESTAMP | Thời gian tạo | Default CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Thời gian cập nhật | Auto update |
| last_login | TIMESTAMP | Lần đăng nhập cuối | NULL |
| is_active | BOOLEAN | Trạng thái hoạt động | Default TRUE |

---

### 3. **BẢNG CATEGORIES** (Phân loại giao dịch)
Lưu các loại thu/chi

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| category_id | INT | ID phân loại | Primary Key, Auto Increment |
| category_name | VARCHAR(100) | Tên phân loại | Not Null |
| category_type | ENUM | Loại | 'INCOME' hoặc 'EXPENSE' |
| description | TEXT | Mô tả | |
| icon | VARCHAR(50) | Tên icon | |
| color | VARCHAR(20) | Màu sắc | Mã màu hex |
| is_default | BOOLEAN | Phân loại mặc định | Default FALSE |
| created_by_manager | INT | ID quản lý tạo | Foreign Key -> managers |
| user_id | INT | ID người dùng | Foreign Key -> users |
| created_at | TIMESTAMP | Thời gian tạo | Default CURRENT_TIMESTAMP |

**Dữ liệu mẫu:**
- Thu nhập: Lương, Thưởng, Đầu tư, Khác
- Chi tiêu: Ăn uống, Đi lại, Giải trí, Mua sắm, Y tế, Giáo dục, Nhà cửa, Khác

---

### 4. **BẢNG TRANSACTIONS** (Giao dịch thu/chi)
Lưu lịch sử các giao dịch

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| transaction_id | INT | ID giao dịch | Primary Key, Auto Increment |
| user_id | INT | ID người dùng | Foreign Key -> users, Not Null |
| category_id | INT | ID phân loại | Foreign Key -> categories, Not Null |
| transaction_type | ENUM | Loại giao dịch | 'INCOME' hoặc 'EXPENSE' |
| amount | DECIMAL(15,2) | Số tiền | Not Null |
| description | TEXT | Mô tả giao dịch | |
| transaction_date | DATE | Ngày giao dịch | Not Null |
| created_at | TIMESTAMP | Thời gian tạo | Default CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Thời gian cập nhật | Auto update |

**Index:** user_id, category_id, transaction_date, transaction_type

**Ví dụ:**
- Thu: +5,000,000 VND - Lương tháng 1
- Chi: -50,000 VND - Mua cafe sáng

---

### 5. **BẢNG BUDGETS** (Ngân sách)
Lưu kế hoạch ngân sách của người dùng

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| budget_id | INT | ID ngân sách | Primary Key, Auto Increment |
| user_id | INT | ID người dùng | Foreign Key -> users, Not Null |
| category_id | INT | ID phân loại | Foreign Key -> categories, NULL = tổng ngân sách |
| budget_amount | DECIMAL(15,2) | Số tiền ngân sách | Not Null |
| start_date | DATE | Ngày bắt đầu | Not Null |
| end_date | DATE | Ngày kết thúc | Not Null |
| budget_type | ENUM | Loại ngân sách | 'MONTHLY', 'QUARTERLY', 'YEARLY', 'CUSTOM' |
| is_active | BOOLEAN | Đang hoạt động | Default TRUE |
| created_at | TIMESTAMP | Thời gian tạo | Default CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | Thời gian cập nhật | Auto update |

**Ví dụ:**
- Ngân sách ăn uống tháng 1: 2,000,000 VND
- Tổng ngân sách tháng: 10,000,000 VND

---

### 6. **BẢNG ALERTS** (Cảnh báo)
Lưu các thông báo cảnh báo cho người dùng

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| alert_id | INT | ID cảnh báo | Primary Key, Auto Increment |
| user_id | INT | ID người dùng | Foreign Key -> users, Not Null |
| alert_type | ENUM | Loại cảnh báo | 'BUDGET_EXCEEDED', 'BUDGET_WARNING', 'LOW_BALANCE', 'UNUSUAL_SPENDING' |
| message | TEXT | Nội dung cảnh báo | Not Null |
| related_budget_id | INT | ID ngân sách liên quan | Foreign Key -> budgets |
| related_transaction_id | INT | ID giao dịch liên quan | Foreign Key -> transactions |
| is_read | BOOLEAN | Đã đọc chưa | Default FALSE |
| created_at | TIMESTAMP | Thời gian tạo | Default CURRENT_TIMESTAMP |

**Ví dụ:**
- "Cảnh báo: Đã sử dụng 80% ngân sách ăn uống!"
- "Số dư của bạn đã âm: -500,000 VND"

---

### 7. **BẢNG ACCOUNT_BALANCES** (Số dư tài khoản)
Lưu số dư hiện tại của người dùng

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| balance_id | INT | ID số dư | Primary Key, Auto Increment |
| user_id | INT | ID người dùng | Foreign Key -> users, Unique |
| current_balance | DECIMAL(15,2) | Số dư hiện tại | Default 0.00 |
| last_updated | TIMESTAMP | Cập nhật lần cuối | Auto update |

**Tự động cập nhật:** Khi thêm/sửa/xóa giao dịch

---

### 8. **BẢNG SYSTEM_REPORTS** (Báo cáo hệ thống)
Lưu các báo cáo do quản lý tạo

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| report_id | INT | ID báo cáo | Primary Key, Auto Increment |
| manager_id | INT | ID quản lý | Foreign Key -> managers, Not Null |
| report_type | ENUM | Loại báo cáo | 'USER_STATISTICS', 'TRANSACTION_STATISTICS', 'SYSTEM_USAGE', 'ANONYMOUS_REPORT' |
| report_title | VARCHAR(200) | Tiêu đề báo cáo | Not Null |
| report_content | TEXT | Nội dung báo cáo | |
| report_data | JSON | Dữ liệu JSON | |
| start_date | DATE | Ngày bắt đầu | |
| end_date | DATE | Ngày kết thúc | |
| created_at | TIMESTAMP | Thời gian tạo | Default CURRENT_TIMESTAMP |

---

### 9. **BẢNG USER_ACCESS_LOGS** (Lịch sử truy cập)
Theo dõi người dùng đăng nhập

| Tên Cột | Kiểu Dữ Liệu | Mô Tả | Ghi Chú |
|----------|--------------|-------|---------|
| log_id | INT | ID log | Primary Key, Auto Increment |
| user_id | INT | ID người dùng | Foreign Key -> users, Not Null |
| login_time | TIMESTAMP | Thời gian đăng nhập | Default CURRENT_TIMESTAMP |
| logout_time | TIMESTAMP | Thời gian đăng xuất | NULL |
| ip_address | VARCHAR(45) | Địa chỉ IP | |
| session_duration | INT | Thời lượng phiên (giây) | |

---

## 🔗 QUAN HỆ GIỮA CÁC BẢNG

```
USERS (1) ----< (N) TRANSACTIONS
  |
  +----< (N) BUDGETS
  |
  +----< (N) ALERTS
  |
  +----(1) ACCOUNT_BALANCES
  |
  +----< (N) USER_ACCESS_LOGS
  |
  +----< (N) CATEGORIES (user's custom)

MANAGERS (1) ----< (N) CATEGORIES (default)
  |
  +----< (N) SYSTEM_REPORTS

CATEGORIES (1) ----< (N) TRANSACTIONS
  |
  +----< (N) BUDGETS

BUDGETS (1) ----< (N) ALERTS
TRANSACTIONS (1) ----< (N) ALERTS
```

---

## ⚙️ TÍNH NĂNG TỰ ĐỘNG

### 1. **Triggers (Tự động thực thi)**

#### Trigger 1: Cập nhật số dư khi THÊM giao dịch
- Thu nhập (+) → Tăng số dư
- Chi tiêu (-) → Giảm số dư
- Nếu số dư âm → Tạo cảnh báo

#### Trigger 2: Cập nhật số dư khi SỬA giao dịch
- Tính chênh lệch và điều chỉnh số dư

#### Trigger 3: Cập nhật số dư khi XÓA giao dịch
- Hoàn trả số tiền về số dư

### 2. **Stored Procedure**

#### Kiểm tra ngân sách (`check_budget_alerts`)
- Tự động gọi khi thêm giao dịch chi tiêu
- Nếu chi tiêu ≥ 80% ngân sách → Cảnh báo
- Nếu chi tiêu > 100% ngân sách → Cảnh báo vượt

---

## 📊 KỊCh BẢN SỬ DỤNG

### Kịch bản 1: Người dùng đăng ký
1. Insert vào bảng `users`
2. Tự động tạo bản ghi trong `account_balances` (số dư = 0)

### Kịch bản 2: Thêm giao dịch thu
1. Insert vào bảng `transactions` (type = INCOME)
2. **Trigger tự động:** Cộng tiền vào `account_balances`

### Kịch bản 3: Thêm giao dịch chi
1. Insert vào bảng `transactions` (type = EXPENSE)
2. **Trigger tự động:** 
   - Trừ tiền từ `account_balances`
   - Nếu số dư âm → Insert `alerts` (LOW_BALANCE)
   - Gọi `check_budget_alerts` → Nếu vượt ngân sách → Insert `alerts` (BUDGET_EXCEEDED)

### Kịch bản 4: Thiết lập ngân sách
1. Insert vào bảng `budgets`
2. Khi có giao dịch chi tiêu trong category này → Tự động kiểm tra ngân sách

### Kịch bản 5: Xem báo cáo
1. Query từ `transactions` theo `user_id` và `transaction_date`
2. Tính tổng thu/chi theo category
3. So sánh với `budgets`

---

## 💡 LƯU Ý QUAN TRỌNG

### Bảo mật
- ✅ Mật khẩu KHÔNG lưu dạng plain text
- ✅ Sử dụng BCrypt để mã hóa (đã có trong code)
- ✅ Mật khẩu mẫu: `admin123` → Hash BCrypt

### Hiệu suất
- ✅ Đánh index các cột thường query (user_id, transaction_date)
- ✅ Sử dụng Foreign Key để đảm bảo tính toàn vẹn
- ✅ Triggers chỉ thực thi khi cần thiết

### Tính toàn vẹn dữ liệu
- ✅ Foreign Keys với ON DELETE CASCADE/RESTRICT
- ✅ UNIQUE constraints cho username, email
- ✅ NOT NULL cho các trường bắt buộc
- ✅ ENUM để giới hạn giá trị hợp lệ

---

## 📈 THỐNG KÊ DỮ LIỆU MẪU

**Sau khi tạo database:**
- 1 tài khoản quản lý (admin)
- 12 phân loại mặc định (4 thu nhập + 8 chi tiêu)
- 0 user (người dùng tự đăng ký)
- 0 giao dịch (người dùng tự thêm)

---

## 🎯 BƯỚC TIẾP THEO

1. ✅ Xem phác thảo này (BẠN ĐANG Ở ĐÂY)
2. ⏭️ Mở XAMPP → Start Apache + MySQL
3. ⏭️ Mở phpMyAdmin (http://localhost/phpmyadmin)
4. ⏭️ Tạo database mới: `expense_management_db`
5. ⏭️ Tạo từng bảng theo hướng dẫn tiếp theo
6. ⏭️ Cấu hình kết nối trong code
7. ⏭️ Chạy ứng dụng

---

**Bạn có muốn tôi tạo hướng dẫn từng bước để tạo database trên phpMyAdmin không?**
