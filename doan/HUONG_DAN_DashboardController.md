# 📘 Hướng Dẫn Tìm Hiểu `DashboardController.java`

> **Dự án:** QL_Chi_Tieu — Quản lý chi tiêu cá nhân
> **File:** `src/main/java/com/example/controller/DashboardController.java`
> **Vai trò:** Màn hình trang chủ chính — trung tâm điều phối toàn bộ ứng dụng

---

## 1. DashboardController là gì?

`DashboardController` là màn hình **trang chủ** hiển thị ngay sau khi đăng nhập thành công. Đây là trung tâm của ứng dụng với các chức năng:

- Hiển thị **số dư tài khoản** và **ví tiền mặt** theo thời gian thực
- Xem **lịch sử giao dịch** với bộ lọc theo ngày / tháng
- **Điều hướng** đến tất cả chức năng: Chuyển tiền, Ngân sách, Thống kê, Danh mục
- **Đổi danh mục** cho giao dịch ngay trên trang chủ
- **Đổi mật khẩu** và **đăng xuất**

---

## 2. Các Thuộc Tính (Fields)

```java
// Giao diện
private Stage stage;
private Scene scene;
private Label lblXinChao;        // "Xin chào, [Tên người dùng]"
private Label lblSoTaiKhoan;     // Hiển thị số tài khoản
private Label lblSoDu;           // Số dư tài khoản (xanh lá / đỏ)
private Label lblSoDuTienMat;    // Số dư ví tiền mặt (vàng / đỏ)
private TableView<GiaoDichInfo> tableGiaoDich;  // Bảng lịch sử giao dịch

// Bộ lọc
private ComboBox<String>  cbLoaiLoc;       // "Tất cả" / "Chọn tháng" / "Chọn ngày"
private ComboBox<Integer> cbThangLoc;      // Tháng (lọc theo tháng)
private ComboBox<Integer> cbNamLoc;        // Năm (lọc theo tháng)
private ComboBox<Integer> cbNgayLoc;       // Ngày (lọc theo ngày)
private ComboBox<Integer> cbThangNgayLoc;  // Tháng (lọc theo ngày)
private ComboBox<Integer> cbNamNgayLoc;    // Năm (lọc theo ngày)
private boolean dangCapNhatBoLoc;          // Cờ chặn sự kiện lặp vô hạn

// DAO (truy cập database)
private GiaoDichDAO giaoDichDAO;
private NguoiDungDAO nguoiDungDAO;
private DanhMucDAO danhMucDAO;
private NganSachDAO nganSachDAO;

// Dữ liệu
private List<GiaoDich> rawGiaoDichList;    // Danh sách gốc từ DB
private DecimalFormat df;                  // Format số tiền "#,###"
```

---

## 3. Inner Class `GiaoDichInfo` — Dữ Liệu Cho Bảng

`GiaoDichInfo` là lớp nội bộ (inner class) dùng để **chuyển đổi dữ liệu** từ model `GiaoDich` sang dạng phù hợp để hiển thị trên `TableView`.

```
GiaoDich (model DB)            GiaoDichInfo (hiển thị)
─────────────────────          ──────────────────────────────
maGiaoDich          ────────►  maGiaoDich
ngayGiaoDich        ────────►  ngay (định dạng "dd/MM/yyyy HH:mm")
soTaiKhoanGui/Nhan  ────────►  loai ("Gửi" / "Nhận" / "Chi TM" / "Thu TM")
soTaiKhoanGui/Nhan  ────────►  taiKhoan (kèm tên người dùng nếu có)
soTien (BigDecimal) ────────►  soTien (định dạng "#,### đ")
noiDung             ────────►  noiDung (đã bỏ prefix tiền mặt)
tenDanhMucChi/Thu   ────────►  danhMuc (tên hiển thị)
```

**4 loại giao dịch được phân biệt:**

| `loai` | Ý nghĩa | Màu sắc |
|---|---|---|
| `Gửi` | Chuyển tiền đi (chi qua tài khoản) | Đỏ |
| `Nhận` | Nhận tiền (thu qua tài khoản) | Xanh lá |
| `Chi TM` | Chi tiêu tiền mặt | Đỏ |
| `Thu TM` | Thu tiền mặt | Xanh lá |

---

## 4. Luồng Hoạt Động Tổng Quan

```
LoginController → chuyenSangDashboard()
        │
        ▼
DashboardController(stage)
        │
        ├── createUI()            → Dựng toàn bộ giao diện
        │     ├── createHeader()  → Phần trên (tên, số dư, nút chức năng)
        │     └── createCenter()  → Phần giữa (bảng giao dịch + bộ lọc)
        │
        └── loadDashboardData()   → Load dữ liệu từ DB
                │
                ├── Cập nhật lblSoDu, lblSoDuTienMat
                └── Cập nhật tableGiaoDich

[Người dùng tương tác:]
        ├── Đổi bộ lọc    → loadDashboardData() (tự động)
        ├── "Chuyển tiền" → handleGiaoDich()
        ├── "Ngân sách"   → handleNganSach()
        ├── "Thống kê"    → handleThongKe()
        ├── "Danh mục"    → handleDanhMuc()
        ├── "Đổi danh mục"→ handleDoiDanhMuc()
        ├── "Đổi mật khẩu"→ handleDoiMatKhau()
        ├── "Làm mới"     → handleRefresh()
        └── "Đăng xuất"   → handleDangXuat()
```

---

## 5. Giao Diện (Layout)

Layout chính dùng `BorderPane` chia 2 vùng:

```
BorderPane (root, nền #f5f5f5)
 │
 ├── TOP: VBox header (nền #2c3e50 - xanh đen)
 │    ├── Hàng 1 (HBox):
 │    │    ├── Label "Xin chào, [Tên]"      (trắng, 20px, đậm)
 │    │    └── [Đổi mật khẩu] [Đăng xuất]  (căn phải)
 │    │
 │    ├── Hàng 2 (HBox):
 │    │    ├── "STK: [số tài khoản]"
 │    │    ├── "Số dư TK: [số tiền] đ"      (xanh lá nếu >= 0, đỏ nếu < 0)
 │    │    └── "Ví tiền mặt: [số tiền] đ"   (vàng nếu >= 0, đỏ nếu < 0)
 │    │
 │    └── Hàng 3 (HBox):
 │         ├── [Chuyển tiền]   (xanh dương)
 │         ├── [Ngân sách]     (cam)
 │         ├── [Thống kê]      (tím)
 │         └── [Danh mục]      (xanh lá)
 │
 └── CENTER: VBox center
      ├── Hàng tiêu đề: "Lịch sử giao dịch" + [Làm mới] + [Đổi danh mục]
      ├── Hàng bộ lọc:  [Loại lọc ▼] [Tháng ▼] [Năm ▼]  (ẩn/hiện theo loại)
      └── TableView (6 cột lịch sử giao dịch)
```

---

## 6. Bảng TableView — 6 Cột

| Cột | Dữ liệu | Định dạng đặc biệt |
|---|---|---|
| Ngày giờ | `dd/MM/yyyy HH:mm` | — |
| Loại | Gửi / Nhận / Chi TM / Thu TM | 🔴 Đỏ = Gửi/Chi TM · 🟢 Xanh = Nhận/Thu TM |
| Tài khoản | STK + (Tên người nhận/gửi) | — |
| Số tiền | `#,### đ` | Căn phải, in đậm |
| Nội dung | Ghi chú giao dịch | — |
| Danh mục | Tên danh mục | 🟢 Xanh = Thu · 🟠 Cam = Chi · *Chưa phân loại* (xám) |

---

## 7. Bộ Lọc Lịch Sử Giao Dịch

Có 3 chế độ lọc, hiển thị bằng `ComboBox`:

```
cbLoaiLoc = "Tất cả các ngày"
      → Ẩn hết bộ lọc phụ
      → Gọi layLichSuGiaoDich(soTaiKhoan) — lấy tất cả

cbLoaiLoc = "Chọn tháng"
      → Hiện: [Tháng ▼] [Năm ▼]
      → Gọi layLichSuGiaoDichTheoThang(soTaiKhoan, thang, nam)

cbLoaiLoc = "Chọn ngày"
      → Hiện: [Ngày ▼] [Tháng ▼] [Năm ▼]
      → Gọi layLichSuGiaoDichTheoNgay(soTaiKhoan, ngayLoc)
```

**Cơ chế `dangCapNhatBoLoc`:**
Khi thay đổi một ComboBox (ví dụ Năm), code sẽ tự cập nhật ComboBox khác (Tháng, Ngày). Nếu không có cờ này, việc cập nhật sẽ kích hoạt event → gọi lại loadData → cập nhật lại → event lại kích hoạt... vòng lặp vô hạn.

```java
dangCapNhatBoLoc = true;   // Bật cờ: đang cập nhật nội bộ
// ... cập nhật ComboBox ...
dangCapNhatBoLoc = false;  // Tắt cờ: cho phép event bình thường
```

---

## 8. Phương Thức `loadDashboardData()` — Tải Dữ Liệu ⭐

Đây là phương thức quan trọng nhất, được gọi khi khởi tạo và mỗi khi bộ lọc thay đổi:

```
loadDashboardData()
      │
      ├── [1] Lấy số dư
      │     giaoDichDAO.laySoDu(soTaiKhoan)         → cập nhật lblSoDu
      │     giaoDichDAO.laySoDuTienMat(soTaiKhoan)  → cập nhật lblSoDuTienMat
      │     (số dư âm → hiện màu đỏ, dương → màu bình thường)
      │
      ├── [2] Lấy lịch sử giao dịch theo bộ lọc
      │     "Tất cả"    → layLichSuGiaoDich()
      │     "Chọn tháng"→ layLichSuGiaoDichTheoThang(thang, nam)
      │     "Chọn ngày" → layLichSuGiaoDichTheoNgay(ngayLoc)
      │
      └── [3] Chuyển đổi List<GiaoDich> → List<GiaoDichInfo>
            Với mỗi GiaoDich:
              ├── Phân loại: Gửi / Nhận / Chi TM / Thu TM
              ├── Xác định tài khoản: STK + (tên người dùng)
              ├── Format ngày: "dd/MM/yyyy HH:mm"
              ├── Format tiền: "#,### đ"
              └── Lấy tên danh mục để hiển thị
            → Đổ vào tableGiaoDich.setItems(...)
```

---

## 9. Phương Thức `handleDoiDanhMuc()` — Đổi Danh Mục Giao Dịch ⭐

Cho phép đổi danh mục của một giao dịch đã có, ngay trên trang chủ:

```
handleDoiDanhMuc()
      │
      ├── Không chọn giao dịch nào? → Cảnh báo, dừng
      │
      ├── Xác định loại giao dịch (isChi hay không)
      │     "Gửi" / "Chi TM" → isChi = true  → lấy danh mục CHI
      │     "Nhận" / "Thu TM"→ isChi = false → lấy danh mục THU
      │
      ├── Chưa có danh mục con nào? → Cảnh báo yêu cầu tạo trước, dừng
      │
      ├── Mở Dialog chọn danh mục mới (ComboBox)
      │
      └── Người dùng xác nhận
            │
            ├── Nếu là giao dịch CHI:
            │     → xacNhanNeuVuotNganSach()  (kiểm tra hạn mức ngân sách)
            │     → Nếu vượt: hỏi xác nhận tiếp tục
            │     → giaoDichDAO.capNhatDanhMucChi(maGD, newDM.getId())
            │
            ├── Nếu là giao dịch THU:
            │     → giaoDichDAO.capNhatDanhMucThu(maGD, newDM.getId())
            │
            └── Thông báo thành công → loadDashboardData() (reload bảng)
```

---

## 10. Phương Thức `xacNhanNeuVuotNganSach()` — Kiểm Tra Ngân Sách

Được gọi trước khi đổi danh mục Chi, để cảnh báo nếu vượt hạn mức:

```
xacNhanNeuVuotNganSach(soTaiKhoan, danhMuc, soTien, thang, nam)
      │
      ├── Lấy giới hạn ngân sách của danh mục đó trong tháng/năm
      │
      ├── Không có ngân sách → return true (cho phép tiếp tục)
      │
      ├── Tính tổng đã chi + giao dịch này
      │
      ├── Không vượt hạn mức → return true
      │
      └── Vượt hạn mức → Hiện Alert cảnh báo với thông tin chi tiết:
            "Hạn mức: X đ | Đã chi: Y đ | Giao dịch này: +Z đ → Tổng: W đ"
            [Vẫn tiếp tục?]  YES → return true  |  NO → return false
```

> **Chú ý:** Dùng `thang/nam` từ **ngày của giao dịch**, không phải tháng hiện tại. Điều này đảm bảo kiểm tra đúng ngân sách của tháng giao dịch đó.

---

## 11. Phương Thức `handleDoiMatKhau()` — Đổi Mật Khẩu

```
handleDoiMatKhau()
      │
      ├── Mở Dialog với 3 trường:
      │     [Mật khẩu hiện tại]
      │     [Mật khẩu mới]        (tối thiểu 6 ký tự)
      │     [Xác nhận mật khẩu]
      │
      ├── Validate TRƯỚC KHI đóng Dialog (dùng event filter):
      │     ├── Có trường rỗng?          → Lỗi, chặn đóng Dialog
      │     ├── Mật khẩu mới < 6 ký tự? → Lỗi, chặn đóng Dialog
      │     ├── Xác nhận không khớp?     → Lỗi, chặn đóng Dialog
      │     └── Mật khẩu cũ sai?         → Lỗi, chặn đóng Dialog
      │
      └── Hợp lệ → nguoiDungDAO.doiMatKhau() → Thông báo thành công
```

> **Kỹ thuật `event filter`:** Khác với `setOnAction` thông thường, `addEventFilter` cho phép **chặn** (`consume()`) sự kiện trước khi Dialog đóng. Điều này giúp giữ Dialog mở khi có lỗi validate, thay vì đóng rồi mở lại.

---

## 12. Các Xử Lý Điều Hướng

| Method | Chuyển đến | Kích thước |
|---|---|---|
| `handleGiaoDich()` | `TransactionController` | 1200 × 830 |
| `handleNganSach()` | `BudgetController` | 1200 × 830 |
| `handleThongKe()` | `StatisticsController` | 1200 × 830 |
| `handleDanhMuc()` | `CategoryController` | 1200 × 830 |
| `handleDangXuat()` | `LoginController` | 520 × 590 |

**Khi đăng xuất:**
```java
LoginController.currentUser = null;  // Xóa thông tin user khỏi bộ nhớ
// Tạo LoginController mới và đổi scene về màn hình đăng nhập
```

---

## 13. Liên Kết Với Các Class Khác

```
DashboardController
      │
      │  đọc thông tin user
      ├──────────────────► LoginController.currentUser (static)
      │
      │  truy vấn số dư + lịch sử
      ├──────────────────► GiaoDichDAO
      │                        ├── laySoDu()
      │                        ├── laySoDuTienMat()
      │                        ├── layLichSuGiaoDich()
      │                        ├── layLichSuGiaoDichTheoThang()
      │                        ├── layLichSuGiaoDichTheoNgay()
      │                        ├── capNhatDanhMucChi()
      │                        └── capNhatDanhMucThu()
      │
      │  lấy danh mục để đổi
      ├──────────────────► DanhMucDAO
      │                        └── layDanhMucConTheoLoai()
      │
      │  kiểm tra ngân sách
      ├──────────────────► NganSachDAO
      │                        ├── layGioiHanNganSach()
      │                        └── layTongChiTheoDanhMuc()
      │
      │  đổi mật khẩu
      └──────────────────► NguoiDungDAO
                               └── doiMatKhau()
```

---

## 14. Tóm Tắt Nhanh

| Method | Làm gì |
|---|---|
| `DashboardController(stage)` | Khởi tạo, dựng UI, load dữ liệu |
| `createUI()` | Dựng `BorderPane` với header + center |
| `createHeader()` | Vùng trên: tên user, số dư, nút chức năng |
| `createCenter()` | Vùng giữa: bộ lọc + bảng giao dịch |
| `createFilterRow()` | Tạo bộ lọc theo tháng / ngày |
| `loadDashboardData()` | Tải số dư + lịch sử từ DB, đổ vào bảng |
| `handleDoiDanhMuc()` | Đổi danh mục cho giao dịch đã chọn |
| `handleDoiMatKhau()` | Dialog 3 bước đổi mật khẩu |
| `handleDangXuat()` | Xóa `currentUser`, về màn hình Login |
| `handleRefresh()` | Gọi lại `loadDashboardData()` |
| `xacNhanNeuVuotNganSach()` | Cảnh báo khi giao dịch vượt hạn mức |
| `getScene()` | Trả về scene để điều hướng |

| Hằng số | Giá trị | Ý nghĩa |
|---|---|---|
| `LOC_TAT_CA` | `"Tất cả các ngày"` | Không lọc, lấy toàn bộ |
| `LOC_THANG` | `"Chọn tháng"` | Lọc theo tháng + năm |
| `LOC_NGAY` | `"Chọn ngày"` | Lọc theo ngày + tháng + năm |

---

*Tài liệu được tạo dựa trên phân tích mã nguồn `DashboardController.java`*
