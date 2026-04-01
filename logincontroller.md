Hướng Dẫn Tìm Hiểu LoginController
File: src/main/java/com/example/controller/LoginController.java

1. LoginController là gì?
LoginController là class chịu trách nhiệm toàn bộ màn hình đăng nhập của ứng dụng. Nó làm 3 việc chính:

Tạo giao diện (UI) màn hình đăng nhập
Xử lý logic khi người dùng bấm "Đăng nhập"
Điều hướng sang màn hình phù hợp sau khi đăng nhập thành công
2. Các Thành Phần Trong Class


LoginController
├── stage          → Cửa sổ ứng dụng (JavaFX Stage)
├── scene          → Nội dung hiển thị trong cửa sổ
├── txtTenDangNhap → Ô nhập tên đăng nhập
├── txtMatKhau     → Ô nhập mật khẩu (ẩn ký tự)
├── lblThongBao    → Label hiển thị thông báo lỗi / thành công
├── btnDangNhap    → Nút "Đăng nhập"
├── linkDangKy     → Link chuyển sang màn hình đăng ký
├── nguoiDungDAO   → Kết nối đến database để kiểm tra tài khoản
└── currentUser    → [static] Lưu thông tin người dùng đang đăng nhập
Lưu ý currentUser là static: Nghĩa là biến này dùng chung cho toàn bộ ứng dụng. Sau khi đăng nhập xong, các màn hình khác (Dashboard, Transaction...) đọc LoginController.currentUser để biết ai đang dùng.

3. Luồng Hoạt Động Tổng Quan


Khởi động app
      │
      ▼
LoginController(stage)
      │
      ├── createUI()        → Vẽ giao diện lên màn hình
      │
      └── [User bấm nút]
            │
            ▼
      handleDangNhap()      → Xử lý logic đăng nhập
            │
            ├── ✅ Thành công  → chuyenSangDashboard()
            ├── ❌ Sai MK      → Hiện thông báo lỗi
            └── 🔒 Bị khóa    → Hiện thông báo + thời gian mở khóa
4. Chi Tiết Từng Method
4.1 LoginController(Stage stage) — Constructor
java


public LoginController(Stage stage) {
    this.stage = stage;
    createUI();           // Gọi ngay để tạo giao diện
}
Nhận vào stage (cửa sổ ứng dụng)
Gọi createUI() để xây dựng giao diện ngay khi khởi tạo
4.2 createUI() — Tạo Giao Diện
Phương thức này xây dựng toàn bộ giao diện bằng JavaFX thuần (không dùng file FXML).



VBox (layout chính, căn giữa)
 ├── Label "ĐĂNG NHẬP"       (tiêu đề lớn)
 ├── Label "Quản Lý Chi Tiêu" (phụ đề)
 ├── Label ""                 (khoảng cách)
 ├── Label "Tên đăng nhập:"
 ├── TextField txtTenDangNhap
 ├── Label "Mật khẩu:"
 ├── PasswordField txtMatKhau
 ├── Button btnDangNhap       → gọi handleDangNhap()
 ├── Label lblThongBao        (thông báo lỗi / thành công)
 └── Hyperlink linkDangKy     → gọi handleDangKy()
Kích thước cửa sổ: 500 × 550 px

4.3 handleDangNhap() — Xử Lý Logic Đăng Nhập ⭐
Đây là phương thức quan trọng nhất. Sơ đồ chi tiết:



handleDangNhap()
      │
      ▼
[Bước 1] Đọc input
      tenDangNhap = txtTenDangNhap.getText().trim()
      matKhau     = txtMatKhau.getText()
      │
      ▼
[Bước 2] Kiểm tra rỗng
      Nếu bỏ trống → hiện "Vui lòng nhập đầy đủ!" → dừng
      │
      ▼
[Bước 3] Gọi database
      nguoiDungDAO.dangNhap(tenDangNhap, matKhau)
      │
      ├──► Trả về NguoiDung  → [Bước 4A] Đăng nhập thành công
      ├──► Trả về null        → [Bước 4B] Sai tên/mật khẩu
      └──► Ném Exception      → [Bước 4C] Tài khoản bị khóa
Bước 4A — Đăng nhập thành công ✅
java


currentUser = nguoiDung;              // Lưu user vào biến static
lblThongBao.setText("Thành công!");
// Chờ 0.8 giây rồi chuyển màn hình (dùng PauseTransition — không block UI)
PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
pause.setOnFinished(event -> chuyenSangDashboard());
pause.play();
Bước 4B — Sai tên đăng nhập hoặc mật khẩu ❌
java


// nguoiDungDAO.dangNhap() trả về null
lblThongBao.setText("Tên đăng nhập hoặc mật khẩu không đúng!");
// Hiện màu đỏ, không làm gì thêm
Bước 4C — Tài khoản bị khóa 🔒
java


// Bắt TaiKhoanBiKhoaException
// Exception này mang theo: lyDoKhoa + thoiGianMoKhoa
// TH1: Khóa CÓ thời hạn (thoiGianMoKhoa != null)
//   → Tính thời gian còn lại bằng Duration API
//   → Hiện: "Tự mở khóa sau: X ngày Y giờ Z phút"
// TH2: Khóa VĨNH VIỄN (thoiGianMoKhoa == null)
//   → Hiện: "Vui lòng liên hệ quản trị viên"
Cách tính thời gian còn lại:

java


long conLaiMs = thoiGianMoKhoa.getTime() - System.currentTimeMillis(); // ms còn lại
java.time.Duration d = java.time.Duration.ofMillis(conLaiMs);
long ngay = d.toDays();        // số ngày
long gio  = d.toHoursPart();   // giờ lẻ (không tính ngày)
long phut = d.toMinutesPart(); // phút lẻ
long giay = d.toSecondsPart(); // giây lẻ
4.4 chuyenSangDashboard() — Điều Hướng Sau Đăng Nhập
Kiểm tra vai trò của người dùng rồi chuyển màn hình tương ứng:



chuyenSangDashboard()
        │
        ├── vaiTro == "quan_ly"  → AdminDashboardController  (màn hình Admin)
        └── vaiTro khác          → DashboardController        (màn hình User thường)
Sau đó: đổi title, resize 1200×830, căn giữa màn hình
4.5 handleDangKy() — Chuyển Sang Đăng Ký
java


// Tạo controller mới và đổi scene của stage
RegisterController registerController = new RegisterController(stage);
stage.setScene(registerController.getScene());
stage.setTitle("Đăng ký");
// Resize 520×690
5. Liên Kết Với Các Class Khác


LoginController
      │
      │  gọi dangNhap()
      ├──────────────────► NguoiDungDAO
      │                         │
      │                         │  query SQL: ten_dang_nhap + MD5(mat_khau)
      │                         ▼
      │                      Database (bảng nguoi_dung)
      │                         │
      │                         │  tài khoản bị khóa?
      │                         ▼
      │                  TaiKhoanBiKhoaException
      │                  (mang lyDoKhoa + thoiGianMoKhoa)
      │
      │  đăng nhập thành công
      ├──────────────────► currentUser = NguoiDung (static)
      │
      │  vaiTro = "quan_ly"
      ├──────────────────► AdminDashboardController
      │
      │  vaiTro = "nguoi_dung"
      └──────────────────► DashboardController
6. Cách Database Kiểm Tra Đăng Nhập
Trong NguoiDungDAO.dangNhap():

sql


SELECT * FROM nguoi_dung
WHERE ten_dang_nhap = ?
  AND mat_khau = ?           -- mat_khau đã được hash MD5 trước khi so sánh
Quy trình xử lý kết quả:



Query trả về dữ liệu?
      │
      ├── KHÔNG → return null  (sai tên đăng nhập hoặc mật khẩu)
      │
      └── CÓ → kiểm tra trang_thai
                    │
                    ├── "hoat_dong" → cập nhật lần đăng nhập cuối → return NguoiDung ✅
                    │
                    └── "bi_khoa"  → hết hạn khóa chưa?
                                         │
                                         ├── HẾT HẠN → tự mở khóa → return NguoiDung ✅
                                         └── CÒN HẠN → throw TaiKhoanBiKhoaException 🔒
7. Tóm Tắt Nhanh


Method	Làm gì
LoginController(stage)	Khởi tạo, tạo UI
createUI()	Vẽ giao diện (VBox, TextField, Button...)
handleDangNhap()	Đọc input → gọi DAO → xử lý kết quả
chuyenSangDashboard()	Chuyển màn hình theo vai trò (admin/user)
handleDangKy()	Chuyển sang màn hình đăng ký
getScene()	Trả về scene để App.java hoặc controller khác dùng


Biến quan trọng	Ý nghĩa
currentUser	User đang đăng nhập (static, dùng toàn app)
nguoiDungDAO	Cầu nối đến database
stage	Cửa sổ ứng dụng (dùng chung 1 cửa sổ, chỉ đổi scene)