# 📘 Hướng Dẫn Tìm Hiểu `LoginController.java`

> **Dự án:** QL_Chi_Tieu — Quản lý chi tiêu cá nhân
> **File:** `src/main/java/com/example/controller/LoginController.java`
> **Vai trò:** Màn hình đầu tiên của ứng dụng — xử lý toàn bộ luồng đăng nhập

---

## 1. LoginController là gì?

`LoginController` là class chịu trách nhiệm toàn bộ màn hình đăng nhập. Nó làm 3 việc chính:

- Tạo **giao diện (UI)** màn hình đăng nhập bằng JavaFX thuần (không dùng FXML)
- Xử lý **logic** khi người dùng bấm "Đăng nhập"
- **Điều hướng** sang màn hình phù hợp sau khi đăng nhập thành công

---

## 2. Các Thuộc Tính (Fields)

```java
private Stage stage;               // Cửa sổ ứng dụng (JavaFX)
private Scene scene;               // Màn hình hiển thị trong cửa sổ

private TextField txtTenDangNhap;  // Ô nhập tên đăng nhập
private PasswordField txtMatKhau;  // Ô nhập mật khẩu (ẩn ký tự)
private Label lblThongBao;         // Dòng thông báo lỗi / thành công
private Button btnDangNhap;        // Nút "Đăng nhập"
private Hyperlink linkDangKy;      // Link chuyển sang màn hình đăng ký

private NguoiDungDAO nguoiDungDAO; // Kết nối database kiểm tra tài khoản

public static NguoiDung currentUser; // [static] Lưu user đang đăng nhập
```

| Thuộc tính | Kiểu | Mục đích |
|---|---|---|
| `stage` | `Stage` | Giữ cửa sổ để đổi màn hình sau đăng nhập |
| `txtMatKhau` | `PasswordField` | Tự động ẩn ký tự khi gõ |
| `lblThongBao` | `Label` | Hiện thông báo lỗi (đỏ) hoặc thành công (xanh) |
| `nguoiDungDAO` | `NguoiDungDAO` | Cầu nối đến database |
| `currentUser` | `NguoiDung` (**static**) | Dùng chung toàn app — các màn hình khác đọc biến này |

> **Vì sao `currentUser` là `static`?**
> Vì đây là thông tin dùng chung toàn ứng dụng. Sau khi đăng nhập, các màn hình khác như `DashboardController`, `TransactionController`... đều đọc `LoginController.currentUser` để biết ai đang đăng nhập.

---

## 3. Luồng Hoạt Động Tổng Quan

```
Khởi động ứng dụng (App.java)
        │
        ▼
LoginController(stage)
        │
        ├── createUI()           → Vẽ toàn bộ giao diện
        │
        └── [Người dùng tương tác]
                │
                ├── Bấm "Đăng nhập"         → handleDangNhap()
                │       │
                │       ├── ✅ Thành công   → chuyenSangDashboard()
                │       ├── ❌ Sai MK       → hiện thông báo lỗi
                │       └── 🔒 Bị khóa     → hiện lý do + thời gian mở khóa
                │
                └── Bấm link "Đăng ký"      → handleDangKy()
                        │
                        └── → RegisterController
```

---

## 4. Phương Thức `createUI()` — Xây Dựng Giao Diện

Toàn bộ giao diện được dựng bằng JavaFX thuần (không file FXML):

```
VBox root (căn giữa, padding 30px, nền #f0f0f0)
 │
 ├── Label "ĐĂNG NHẬP"            (font 24px, đậm, màu #2c3e50)
 ├── Label "Quản Lý Chi Tiêu"     (font 16px, màu xám)
 ├── Label ""                      (khoảng cách)
 ├── Label "Tên đăng nhập:"
 ├── TextField txtTenDangNhap      (rộng 350px)
 ├── Label "Mật khẩu:"
 ├── PasswordField txtMatKhau      (rộng 350px, ẩn ký tự)
 ├── Button "Đăng nhập"            (xanh dương, cao 40px, rộng 350px)
 ├── Label lblThongBao             (thông báo lỗi / thành công)
 └── Hyperlink "Chưa có tài khoản? Đăng ký ngay"
```

**Kích thước cửa sổ:** `500 × 550 px`

---

## 5. Phương Thức `handleDangNhap()` — Xử Lý Logic Đăng Nhập ⭐

Đây là phương thức quan trọng nhất của class. Toàn bộ luồng xử lý:

```
handleDangNhap()
      │
      ▼
[Bước 1] Đọc input từ form
      tenDangNhap = txtTenDangNhap.getText().trim()
      matKhau     = txtMatKhau.getText()
      │
      ▼
[Bước 2] Kiểm tra rỗng
      Nếu bỏ trống bất kỳ trường nào
        → lblThongBao = "Vui lòng nhập đầy đủ thông tin!" (đỏ)
        → return (dừng lại)
      │
      ▼
[Bước 3] Gọi database
      nguoiDungDAO.dangNhap(tenDangNhap, matKhau)
      │
      ├── Trả về NguoiDung   → [4A] Đăng nhập thành công ✅
      ├── Trả về null         → [4B] Sai tên/mật khẩu ❌
      └── Ném Exception       → [4C] Tài khoản bị khóa 🔒
```

### Bước 4A — Đăng nhập thành công ✅

```java
currentUser = nguoiDung;                      // Lưu user vào biến static toàn app
lblThongBao.setText("Đăng nhập thành công!"); // Hiện màu xanh

// Chờ 0.8 giây rồi mới chuyển màn hình
// Dùng PauseTransition để KHÔNG block luồng UI
PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
pause.setOnFinished(event -> chuyenSangDashboard());
pause.play();
```

> **Tại sao dùng `PauseTransition` thay vì `Thread.sleep()`?**
> `Thread.sleep()` sẽ đóng băng toàn bộ giao diện. `PauseTransition` chạy bất đồng bộ, người dùng vẫn thấy màn hình bình thường trong 0.8 giây trước khi chuyển.

### Bước 4B — Sai tên đăng nhập hoặc mật khẩu ❌

```java
// nguoiDungDAO.dangNhap() trả về null
lblThongBao.setText("Tên đăng nhập hoặc mật khẩu không đúng!");
// Hiện màu đỏ, không làm gì thêm
```

### Bước 4C — Tài khoản bị khóa 🔒

Khi DB phát hiện tài khoản bị khóa, nó ném ra `TaiKhoanBiKhoaException`. Exception này mang theo 2 thông tin: `lyDoKhoa` và `thoiGianMoKhoa`.

```
TaiKhoanBiKhoaException được bắt
      │
      ├── Lấy lyDoKhoa → hiện lý do bị khóa
      │
      └── thoiGianMoKhoa != null?
            │
            ├── YES (khóa có thời hạn)
            │     → Tính thời gian còn lại bằng Duration API
            │     → Hiện: "⏳ Tự mở khóa sau: X ngày Y giờ Z phút"
            │
            └── NO (khóa vĩnh viễn)
                  → Hiện: "Vui lòng liên hệ quản trị viên để mở khóa."
```

**Cách tính thời gian còn lại:**

```java
long conLaiMs = thoiGianMoKhoa.getTime() - System.currentTimeMillis();
java.time.Duration d = java.time.Duration.ofMillis(conLaiMs);

long ngay = d.toDays();         // Số ngày
long gio  = d.toHoursPart();    // Giờ lẻ (không bao gồm phần ngày)
long phut = d.toMinutesPart();  // Phút lẻ
long giay = d.toSecondsPart();  // Giây lẻ (chỉ hiện khi < 1 giờ)
```

---

## 6. Phương Thức `chuyenSangDashboard()` — Điều Hướng Sau Đăng Nhập

Sau khi đăng nhập thành công, kiểm tra vai trò để chuyển đúng màn hình:

```
chuyenSangDashboard()
        │
        ├── vaiTro == "quan_ly"   → AdminDashboardController
        │                              Title: "Quản Lý Chi Tiêu - Quản Trị Admin"
        │
        └── vaiTro khác (user)    → DashboardController
                                       Title: "Quản Lý Chi Tiêu - Trang Chủ"

[Cả hai trường hợp đều:]
  stage.setResizable(false)
  stage.setWidth(1200)
  stage.setHeight(830)
  stage.centerOnScreen()
```

---

## 7. Phương Thức `handleDangKy()` — Chuyển Sang Đăng Ký

```java
RegisterController registerController = new RegisterController(stage);
stage.setScene(registerController.getScene());
stage.setTitle("Đăng ký");
stage.setWidth(520);
stage.setHeight(690);
stage.centerOnScreen();
```

> Chú ý: toàn ứng dụng chỉ dùng **1 cửa sổ duy nhất** (`stage`). Chuyển màn hình = đổi `Scene` của stage, không mở cửa sổ mới.

---

## 8. Cơ Chế Kiểm Tra Đăng Nhập Trong Database

Bên trong `NguoiDungDAO.dangNhap()` thực hiện câu SQL:

```sql
SELECT * FROM nguoi_dung
WHERE ten_dang_nhap = ?
  AND mat_khau = MD5(?)    -- Mật khẩu được hash MD5 trước khi so sánh
```

Sau đó xử lý kết quả:

```
Query có trả về kết quả không?
      │
      ├── KHÔNG → return null   (sai tên hoặc mật khẩu)
      │
      └── CÓ → kiểm tra trang_thai
                    │
                    ├── "hoat_dong"
                    │     → Cập nhật lần đăng nhập cuối
                    │     → return NguoiDung ✅
                    │
                    └── "bi_khoa" → Hết hạn khóa chưa?
                                         │
                                         ├── HẾT HẠN → tự mở khóa → return NguoiDung ✅
                                         └── CÒN HẠN → throw TaiKhoanBiKhoaException 🔒
```

---

## 9. Liên Kết Với Các Class Khác

```
LoginController
      │
      │  gọi dangNhap(ten, matKhau)
      ├──────────────────────────► NguoiDungDAO
      │                                 │
      │                                 │  SQL query + MD5 hash
      │                                 ▼
      │                            Database (bảng nguoi_dung)
      │                                 │
      │                                 ├── OK → return NguoiDung
      │                                 └── Bị khóa → TaiKhoanBiKhoaException
      │
      │  đăng nhập thành công
      ├──────────────────────────► currentUser = NguoiDung (static, toàn app dùng)
      │
      │  vaiTro = "quan_ly"
      ├──────────────────────────► AdminDashboardController
      │
      │  vaiTro = "nguoi_dung"
      ├──────────────────────────► DashboardController
      │
      │  bấm link đăng ký
      └──────────────────────────► RegisterController
```

---

## 10. Tóm Tắt Nhanh

| Method | Làm gì |
|---|---|
| `LoginController(stage)` | Khởi tạo, gọi `createUI()` ngay |
| `createUI()` | Dựng layout VBox, TextField, Button... |
| `handleDangNhap()` | Đọc input → gọi DAO → xử lý 3 kết quả |
| `chuyenSangDashboard()` | Phân vai trò → Admin hoặc User dashboard |
| `handleDangKy()` | Đổi scene sang `RegisterController` |
| `getScene()` | Trả về scene để `App.java` hoặc controller khác dùng |

| Biến quan trọng | Ý nghĩa |
|---|---|
| `currentUser` | User đang đăng nhập — **static**, dùng chung toàn app |
| `nguoiDungDAO` | Cầu nối truy vấn database |
| `stage` | Cửa sổ duy nhất của app — chỉ đổi scene, không tạo mới |

---

*Tài liệu được tạo dựa trên phân tích mã nguồn `LoginController.java`*
