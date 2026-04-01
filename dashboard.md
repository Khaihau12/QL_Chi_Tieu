Hướng Dẫn Tìm Hiểu DashboardController
File: src/main/java/com/example/controller/DashboardController.java

1. DashboardController là gì?
DashboardController là màn hình chính của người dùng thường sau khi đăng nhập thành công. Đây là trung tâm điều hướng — từ đây user có thể đi đến tất cả tính năng khác.

Nó làm những việc:

Hiển thị số dư tài khoản + ví tiền mặt
Hiển thị lịch sử giao dịch (có thể lọc theo ngày/tháng)
Điều hướng sang: Chuyển tiền, Ngân sách, Thống kê, Danh mục
Cho phép đổi danh mục của từng giao dịch
Đổi mật khẩu, Đăng xuất
2. Các Thành Phần Trong Class
2.1 Biến giao diện (UI)


lblXinChao        → Label "Xin chào, [Tên user]"
lblSoTaiKhoan     → Label hiển thị STK
lblSoDu           → Label số dư tài khoản ngân hàng (màu xanh)
lblSoDuTienMat    → Label số dư ví tiền mặt (màu vàng)
tableGiaoDich     → Bảng danh sách giao dịch
btnGiaoDich       → Nút "Chuyển tiền"
btnRefresh        → Nút "Làm mới"
btnDangXuat       → Nút "Đăng xuất"
cbLoaiLoc         → ComboBox chọn loại lọc (Tất cả / Tháng / Ngày)
cbThangLoc        → ComboBox chọn tháng (khi lọc theo tháng)
cbNamLoc          → ComboBox chọn năm (khi lọc theo tháng)
cbNgayLoc         → ComboBox chọn ngày (khi lọc theo ngày)
cbThangNgayLoc    → ComboBox chọn tháng (khi lọc theo ngày)
cbNamNgayLoc      → ComboBox chọn năm (khi lọc theo ngày)
2.2 Biến dữ liệu


giaoDichDAO       → Truy vấn giao dịch từ DB
nguoiDungDAO      → Truy vấn thông tin người dùng
danhMucDAO        → Truy vấn danh mục
nganSachDAO       → Truy vấn ngân sách
df                → DecimalFormat "#,###" để format tiền tệ
rawGiaoDichList   → Danh sách GiaoDich gốc từ DB (lưu để dùng khi đổi danh mục)
dangCapNhatBoLoc  → Cờ boolean ngăn sự kiện ComboBox kích hoạt lẫn nhau khi đang cập nhật
2.3 Hằng số lọc
java


LOC_TAT_CA = "Tất cả các ngày"   // Hiển thị tất cả giao dịch
LOC_THANG  = "Chọn tháng"        // Lọc theo tháng + năm
LOC_NGAY   = "Chọn ngày"         // Lọc theo ngày + tháng + năm
3. Inner Class GiaoDichInfo
GiaoDichInfo là class nội bộ dùng để hiển thị dữ liệu lên TableView.

Tại sao cần class này? TableView của JavaFX cần class có getter chuẩn để bind dữ liệu vào cột. GiaoDich (model từ DB) lưu dữ liệu thô, còn GiaoDichInfo lưu dữ liệu đã xử lý cho hiển thị (ví dụ: số tiền đã format thành "1,000,000 đ").



GiaoDichInfo
 ├── maGiaoDich      → ID giao dịch (để cập nhật DB sau)
 ├── ngay            → "dd/MM/yyyy HH:mm" (đã format)
 ├── loai            → "Gửi" / "Nhận" / "Chi TM" / "Thu TM"
 ├── taiKhoan        → STK + tên người gửi/nhận
 ├── soTien          → "1,000,000 đ" (đã format)
 ├── noiDung         → Nội dung giao dịch (đã bỏ prefix tiền mặt)
 ├── danhMuc         → Tên danh mục để hiển thị
 ├── danhMucId       → ID danh mục chi (dùng khi đổi danh mục)
 ├── danhMucThuId    → ID danh mục thu (dùng khi đổi danh mục)
 ├── soTienRaw       → BigDecimal số tiền gốc (dùng khi kiểm tra ngân sách)
 └── ngayGiaoDichRaw → Timestamp gốc (dùng khi kiểm tra ngân sách theo tháng)
4. Luồng Hoạt Động Tổng Quan


LoginController → chuyenSangDashboard()
      │
      ▼
DashboardController(stage)
      │
      ├── createUI()          → Vẽ giao diện
      └── loadDashboardData() → Tải dữ liệu từ DB lên màn hình
           [User tương tác]
                │
                ├── Đổi bộ lọc      → loadDashboardData() (tải lại)
                ├── Làm mới         → loadDashboardData() (tải lại)
                ├── Chuyển tiền     → handleGiaoDich()
                ├── Ngân sách       → handleNganSach()
                ├── Thống kê        → handleThongKe()
                ├── Danh mục        → handleDanhMuc()
                ├── Đổi danh mục    → handleDoiDanhMuc()
                ├── Đổi mật khẩu   → handleDoiMatKhau()
                └── Đăng xuất       → handleDangXuat()
5. Giao Diện (Layout)


BorderPane (root)
 ├── TOP: VBox Header (nền đen #2c3e50)
 │    ├── HBox Row1: "Xin chào [tên]"  ←→  [Đổi MK] [Đăng xuất]
 │    ├── HBox Row2: STK | Số dư TK | Ví tiền mặt
 │    └── HBox Row3: [Chuyển tiền] [Ngân sách] [Thống kê] [Danh mục]
 │
 └── CENTER: VBox
      ├── HBox titleRow: "Lịch sử giao dịch"  ←→  [Làm mới] [Đổi danh mục]
      ├── HBox filterRow: Bộ lọc (Tất cả / Tháng / Ngày)
      └── TableView (lịch sử giao dịch)
6. Chi Tiết Các Method Quan Trọng
6.1 createUI() — Xây Giao Diện
java


private void createUI() {
    BorderPane root = new BorderPane();
    root.setTop(createHeader());    // Phần header phía trên
    root.setCenter(createCenter()); // Phần bảng giao dịch ở giữa
    scene = new Scene(root, 1200, 800);
}
Tách thành 2 method phụ cho gọn:

createHeader() → vẽ phần trên (tên, số dư, các nút chức năng)
createCenter() → vẽ phần giữa (bộ lọc + bảng giao dịch)
6.2 loadDashboardData() — Tải Dữ Liệu ⭐
Đây là method quan trọng nhất, được gọi mỗi khi cần làm mới màn hình.



loadDashboardData()
      │
      ├── [Bước 1] Lấy số dư
      │     giaoDichDAO.laySoDu()        → cập nhật lblSoDu (xanh/đỏ)
      │     giaoDichDAO.laySoDuTienMat() → cập nhật lblSoDuTienMat (vàng/đỏ)
      │
      ├── [Bước 2] Lấy danh sách giao dịch theo bộ lọc
      │     LOC_TAT_CA → layLichSuGiaoDich()
      │     LOC_THANG  → layLichSuGiaoDichTheoThang(thang, nam)
      │     LOC_NGAY   → layLichSuGiaoDichTheoNgay(ngayLoc)
      │
      └── [Bước 3] Chuyển đổi GiaoDich → GiaoDichInfo rồi đưa vào TableView
Bước 3 — Phân loại từng giao dịch:



Với mỗi giao dịch trong danh sách:
      │
      ├── Nội dung có prefix "[TIEN_MAT_CHI]"?
      │       → loai = "Chi TM", taiKhoan = "Tiền mặt"
      │
      ├── Nội dung có prefix "[TIEN_MAT_THU]"?
      │       → loai = "Thu TM", taiKhoan = "Tiền mặt"
      │
      ├── STK gửi == STK của mình?
      │       → loai = "Gửi", taiKhoan = STK người nhận (+ tên nếu có)
      │
      └── Còn lại (mình là người nhận)
              → loai = "Nhận", taiKhoan = STK người gửi (+ tên nếu có)
6.3 Bộ Lọc Giao Dịch


cbLoaiLoc thay đổi
      │
      ├── "Tất cả các ngày" → ẩn tất cả dropdown ngày/tháng
      ├── "Chọn tháng"      → hiện [Tháng][Năm]
      └── "Chọn ngày"       → hiện [Ngày][Tháng][Năm]
Mỗi khi dropdown thay đổi → gọi loadDashboardData()
Cờ dangCapNhatBoLoc: Khi thay đổi năm, code tự cập nhật danh sách tháng hợp lệ, điều này kích hoạt sự kiện của ComboBox tháng. Cờ này dùng để chặn sự kiện domino — tránh loadDashboardData() bị gọi nhiều lần không cần thiết.

java


dangCapNhatBoLoc = true;   // Bắt đầu cập nhật
// ... thay đổi items của ComboBox ...
dangCapNhatBoLoc = false;  // Kết thúc — sự kiện sẽ hoạt động trở lại
6.4 handleDoiDanhMuc() — Đổi Danh Mục Giao Dịch
User chọn 1 giao dịch trong bảng → bấm "Đổi danh mục":



handleDoiDanhMuc()
      │
      ├── Không chọn dòng nào? → cảnh báo rồi dừng
      │
      ├── Xác định loại giao dịch (chi hay thu?)
      │     "Gửi" / "Chi TM" → loại = "chi"
      │     "Nhận" / "Thu TM" → loại = "thu"
      │
      ├── Mở Dialog chọn danh mục mới
      │
      └── Sau khi user xác nhận:
            ├── Nếu là giao dịch CHI:
            │     → Lấy tháng/năm của GIAO DỊCH (không phải tháng hiện tại!)
            │     → Kiểm tra vượt ngân sách tháng đó
            │     → Nếu OK: capNhatDanhMucChi()
            │
            └── Nếu là giao dịch THU:
                  → capNhatDanhMucThu() (không cần kiểm tra ngân sách)
⚠️ Lưu ý quan trọng: Khi đổi danh mục, hệ thống kiểm tra ngân sách theo tháng của giao dịch gốc, không phải tháng hiện tại. Ví dụ: Giao dịch tháng 3 → kiểm tra ngân sách tháng 3, dù bây giờ là tháng 4.

6.5 handleDoiMatKhau() — Đổi Mật Khẩu


Mở Dialog gồm 3 ô: [MK hiện tại] [MK mới] [Xác nhận MK mới]
      │
      User bấm "Lưu mật khẩu"
      │
      ├── Validation (chặn dialog đóng nếu lỗi):
      │     - Bỏ trống? → báo lỗi
      │     - MK mới < 6 ký tự? → báo lỗi
      │     - MK mới ≠ xác nhận? → báo lỗi
      │     - MK hiện tại sai? → báo lỗi
      │
      └── Tất cả OK → doiMatKhau() → thông báo thành công
Kỹ thuật dùng: btnLuuNode.addEventFilter(ActionEvent.ACTION, ev -> { ev.consume(); }) — chặn dialog tự đóng khi validate thất bại.

6.6 Các Method Điều Hướng


Method	Chuyển sang màn hình nào	Kích thước
handleGiaoDich()	TransactionController	1200×830
handleNganSach()	BudgetController	1200×830
handleThongKe()	StatisticsController	1200×830
handleDanhMuc()	CategoryController	1200×830
handleDangXuat()	LoginController	520×590
Lưu ý handleDangXuat(): Đặt LoginController.currentUser = null trước khi chuyển về màn hình đăng nhập — đảm bảo không còn thông tin user cũ trong bộ nhớ.

6.7 xacNhanNeuVuotNganSach() — Helper Kiểm Tra Ngân Sách
java


private boolean xacNhanNeuVuotNganSach(
        String soTaiKhoan, DanhMuc danhMuc, BigDecimal soTien, int thang, int nam)


Lấy giới hạn ngân sách của danh mục trong tháng/năm đó
      │
      ├── Không có giới hạn? → return true (cho phép tiếp tục)
      │
      ├── Tính tổng đã chi + số tiền giao dịch này
      │
      ├── Không vượt? → return true (cho phép tiếp tục)
      │
      └── Vượt → hiện Dialog cảnh báo với chi tiết:
                  [Giới hạn / Đã chi / Giao dịch này / Tổng / Vượt mức]
                  User chọn YES → return true
                  User chọn NO  → return false (hủy)
7. Bảng TableView và Màu Sắc


Cột	Dữ liệu	Định dạng đặc biệt
Ngày giờ	dd/MM/yyyy HH:mm	—
Loại	Gửi / Nhận / Chi TM / Thu TM	🔴 Đỏ nếu Gửi/Chi TM · 🟢 Xanh nếu Nhận/Thu TM
Tài khoản	STK (+ tên người dùng)	—
Số tiền	1,000,000 đ	Căn phải, in đậm
Nội dung	Nội dung giao dịch	—
Danh mục	Tên danh mục	🟠 Cam nếu Chi · 🟢 Xanh nếu Thu · Xám nghiêng nếu chưa phân loại
8. Liên Kết Với Các Class Khác


DashboardController
      │
      │  đọc currentUser
      ├──────────────────► LoginController.currentUser (static)
      │
      │  truy vấn DB
      ├──────────────────► GiaoDichDAO   (lịch sử GD, số dư, tên người dùng)
      ├──────────────────► NguoiDungDAO  (đổi mật khẩu)
      ├──────────────────► DanhMucDAO    (lấy danh sách danh mục khi đổi DM)
      └──────────────────► NganSachDAO   (kiểm tra vượt ngân sách)
      │
      │  điều hướng sang
      ├──────────────────► TransactionController  (chuyển tiền)
      ├──────────────────► BudgetController        (ngân sách)
      ├──────────────────► StatisticsController    (thống kê)
      ├──────────────────► CategoryController      (danh mục)
      └──────────────────► LoginController         (đăng xuất)
9. Tóm Tắt Nhanh


Method	Làm gì
DashboardController(stage)	Khởi tạo, vẽ UI, tải dữ liệu
createUI()	Dựng layout BorderPane (header + center)
createHeader()	Vẽ phần header: tên, số dư, các nút
createCenter()	Vẽ bảng giao dịch + bộ lọc
createFilterRow()	Tạo bộ lọc Tất cả / Tháng / Ngày
loadDashboardData()	⭐ Tải số dư + lịch sử GD từ DB lên bảng
handleGiaoDich()	Chuyển sang màn hình chuyển tiền
handleNganSach()	Chuyển sang màn hình ngân sách
handleThongKe()	Chuyển sang màn hình thống kê
handleDanhMuc()	Chuyển sang màn hình danh mục
handleDoiDanhMuc()	Đổi danh mục cho giao dịch đang chọn
handleDoiMatKhau()	Dialog đổi mật khẩu
handleDangXuat()	Xóa currentUser, về màn hình đăng nhập
handleRefresh()	Gọi lại loadDashboardData()
xacNhanNeuVuotNganSach()	Kiểm tra + cảnh báo vượt ngân sách