# Hướng Dẫn Cài Đặt và Sử Dụng Database

## 1. YÊU CẦU HỆ THỐNG

- MySQL Server 5.7 trở lên (hoặc MariaDB 10.2+)
- Java 11 trở lên
- Maven 3.6+

## 2. CÀI ĐẶT MYSQL

### Windows:
1. Tải MySQL Community Server từ: https://dev.mysql.com/downloads/mysql/
2. Cài đặt và thiết lập mật khẩu root
3. Mở MySQL Workbench hoặc dòng lệnh MySQL

### Linux:
```bash
sudo apt-get update
sudo apt-get install mysql-server
sudo mysql_secure_installation
```

### macOS:
```bash
brew install mysql
brew services start mysql
```

## 3. TẠO DATABASE

### Bước 1: Kết nối MySQL
```bash
mysql -u root -p
```

### Bước 2: Chạy script tạo database
```bash
mysql -u root -p < database/schema.sql
```

Hoặc trong MySQL Workbench:
1. Mở file `database/schema.sql`
2. Nhấn Execute (Ctrl+Shift+Enter)

## 4. CẤU HÌNH KẾT NỐI

Chỉnh sửa file `src/main/resources/database.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/expense_management_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=YOUR_PASSWORD_HERE
```

**Lưu ý:** Thay `YOUR_PASSWORD_HERE` bằng mật khẩu MySQL của bạn.

## 5. CẤU TRÚC DATABASE

### Các Bảng Chính:

#### 1. `users` - Bảng người dùng
- `user_id`: ID người dùng (Primary Key)
- `username`: Tên đăng nhập (Unique)
- `email`: Email (Unique)
- `password_hash`: Mật khẩu đã mã hóa (BCrypt)
- `full_name`: Họ tên
- `phone_number`: Số điện thoại
- `is_active`: Trạng thái hoạt động

#### 2. `managers` - Bảng quản lý
- `manager_id`: ID quản lý (Primary Key)
- `username`: Tên đăng nhập
- `email`: Email
- Tương tự như users

#### 3. `categories` - Phân loại giao dịch
- `category_id`: ID phân loại (Primary Key)
- `category_name`: Tên phân loại
- `category_type`: Loại (INCOME/EXPENSE)
- `is_default`: Phân loại mặc định
- `user_id`: ID người dùng (NULL nếu là default)

#### 4. `transactions` - Giao dịch
- `transaction_id`: ID giao dịch (Primary Key)
- `user_id`: ID người dùng
- `category_id`: ID phân loại
- `transaction_type`: Loại (INCOME/EXPENSE)
- `amount`: Số tiền
- `description`: Mô tả
- `transaction_date`: Ngày giao dịch

#### 5. `budgets` - Ngân sách
- `budget_id`: ID ngân sách (Primary Key)
- `user_id`: ID người dùng
- `category_id`: ID phân loại (NULL = tổng ngân sách)
- `budget_amount`: Số tiền ngân sách
- `start_date`: Ngày bắt đầu
- `end_date`: Ngày kết thúc
- `budget_type`: Loại (MONTHLY/QUARTERLY/YEARLY/CUSTOM)
- `is_active`: Trạng thái

#### 6. `alerts` - Cảnh báo
- `alert_id`: ID cảnh báo (Primary Key)
- `user_id`: ID người dùng
- `alert_type`: Loại cảnh báo
- `message`: Nội dung cảnh báo
- `is_read`: Đã đọc chưa

#### 7. `account_balances` - Số dư tài khoản
- `balance_id`: ID số dư (Primary Key)
- `user_id`: ID người dùng
- `current_balance`: Số dư hiện tại

#### 8. `system_reports` - Báo cáo hệ thống
- `report_id`: ID báo cáo (Primary Key)
- `manager_id`: ID quản lý
- `report_type`: Loại báo cáo
- `report_data`: Dữ liệu báo cáo (JSON)

#### 9. `user_access_logs` - Lịch sử truy cập
- `log_id`: ID log (Primary Key)
- `user_id`: ID người dùng
- `login_time`: Thời gian đăng nhập
- `logout_time`: Thời gian đăng xuất

## 6. TRIGGERS TỰ ĐỘNG

### Trigger 1: `after_transaction_insert`
- Tự động cập nhật số dư khi thêm giao dịch mới
- Tạo cảnh báo khi số dư âm
- Kiểm tra ngân sách và tạo cảnh báo

### Trigger 2: `after_transaction_update`
- Cập nhật số dư khi sửa giao dịch

### Trigger 3: `after_transaction_delete`
- Hoàn trả số dư khi xóa giao dịch

## 7. STORED PROCEDURES

### `check_budget_alerts(user_id, category_id, amount)`
- Kiểm tra và tạo cảnh báo ngân sách
- Cảnh báo khi vượt 80% ngân sách
- Cảnh báo khi vượt ngân sách

## 8. SỬ DỤNG TRONG CODE

### Ví dụ 1: Đăng ký người dùng
```java
UserDAO userDAO = new UserDAO();
User user = new User("john_doe", "john@email.com", null);
user.setFullName("John Doe");
user.setPhoneNumber("0123456789");

if (userDAO.register(user, "password123")) {
    System.out.println("Đăng ký thành công!");
}
```

### Ví dụ 2: Đăng nhập
```java
UserDAO userDAO = new UserDAO();
User user = userDAO.login("john_doe", "password123");
if (user != null) {
    System.out.println("Đăng nhập thành công! Xin chào " + user.getFullName());
}
```

### Ví dụ 3: Thêm giao dịch
```java
TransactionDAO transactionDAO = new TransactionDAO();
Transaction transaction = new Transaction(
    userId,
    categoryId,
    Transaction.TransactionType.EXPENSE,
    new BigDecimal("50000"),
    "Mua cafe",
    new Date(System.currentTimeMillis())
);

if (transactionDAO.addTransaction(transaction)) {
    System.out.println("Thêm giao dịch thành công!");
}
```

### Ví dụ 4: Thiết lập ngân sách
```java
BudgetDAO budgetDAO = new BudgetDAO();
Budget budget = new Budget(
    userId,
    categoryId,
    new BigDecimal("2000000"),
    Date.valueOf("2025-01-01"),
    Date.valueOf("2025-01-31"),
    Budget.BudgetType.MONTHLY
);

if (budgetDAO.addBudget(budget)) {
    System.out.println("Thiết lập ngân sách thành công!");
}
```

### Ví dụ 5: Lấy danh sách giao dịch
```java
TransactionDAO transactionDAO = new TransactionDAO();
List<Transaction> transactions = transactionDAO.getTransactionsByUser(userId);
for (Transaction t : transactions) {
    System.out.println(t.getDescription() + ": " + t.getAmount() + " VND");
}
```

## 9. KIỂM TRA KẾT NỐI

```java
DatabaseConnection dbConn = DatabaseConnection.getInstance();
if (dbConn.testConnection()) {
    System.out.println("Kết nối database thành công!");
} else {
    System.out.println("Không thể kết nối database!");
}
```

## 10. TÀI KHOẢN MẶC ĐỊNH

### Quản lý:
- Username: `admin`
- Password: `admin123`
- Email: `admin@expense.com`

### Phân loại mặc định:
**Thu nhập:**
- Lương
- Thưởng
- Đầu tư
- Khác

**Chi tiêu:**
- Ăn uống
- Đi lại
- Giải trí
- Mua sắm
- Y tế
- Giáo dục
- Nhà cửa
- Khác

## 11. LƯU Ý BẢO MẬT

1. **Mật khẩu:** Được mã hóa bằng BCrypt với salt factor 10
2. **SQL Injection:** Tất cả queries sử dụng PreparedStatement
3. **Connection:** Sử dụng singleton pattern và connection pooling
4. **Không lưu plain text password trong database**

## 12. BUILD VÀ CHẠY

```bash
# Build project
mvn clean install

# Chạy application
mvn javafx:run
```

## 13. XỬ LÝ LỖI THƯỜNG GẶP

### Lỗi: "Access denied for user"
- Kiểm tra username/password trong database.properties
- Đảm bảo MySQL đang chạy

### Lỗi: "Unknown database"
- Chạy lại script schema.sql để tạo database

### Lỗi: "Communications link failure"
- Kiểm tra MySQL service có đang chạy không
- Kiểm tra port 3306 có bị chặn không

### Lỗi: "Table doesn't exist"
- Import lại database schema

## 14. BACKUP VÀ RESTORE

### Backup:
```bash
mysqldump -u root -p expense_management_db > backup.sql
```

### Restore:
```bash
mysql -u root -p expense_management_db < backup.sql
```

## 15. HỖ TRỢ

Nếu gặp vấn đề, kiểm tra:
1. MySQL service đang chạy
2. Thông tin kết nối trong database.properties đúng
3. Database đã được tạo
4. Dependencies đã được download (mvn install)
