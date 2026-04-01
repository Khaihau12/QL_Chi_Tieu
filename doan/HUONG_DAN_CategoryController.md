# 📘 Hướng Dẫn Tìm Hiểu `CategoryController.java`

> **Dự án:** QL_Chi_Tieu — Quản lý chi tiêu cá nhân
> **File:** `src/main/java/com/example/controller/CategoryController.java`
> **Vai trò:** Quản lý toàn bộ màn hình **Quản Lý Danh Mục** (Chi + Thu)

---

## 1. Tổng Quan

`CategoryController` là một lớp JavaFX controller chịu trách nhiệm xây dựng và điều khiển màn hình **Quản lý Danh Mục**. Màn hình này cho phép người dùng xem, thêm, sửa, xóa các danh mục chi tiêu và thu nhập của mình.

```
CategoryController
│
├── Tab "Danh mục Chi"   ──► TableView (hiển thị danh mục chi)
│                              └── Nút: Thêm | Sửa | Xóa
│
└── Tab "Danh mục Thu"   ──► TableView (hiển thị danh mục thu)
                               └── Nút: Thêm | Sửa | Xóa
```

---

## 2. Các Thuộc Tính (Fields)

```java
private Stage stage;               // Cửa sổ JavaFX chính
private Scene scene;               // Màn hình hiển thị
private String soTaiKhoan;         // Mã tài khoản người dùng đang đăng nhập

private TableView<DanhMuc> tablechi;   // Bảng danh mục Chi
private TableView<DanhMuc> tableThu;   // Bảng danh mục Thu
private DanhMucDAO danhMucDAO;         // Lớp truy xuất dữ liệu (DAO)
```

| Thuộc tính | Kiểu | Mục đích |
|---|---|---|
| `stage` | `Stage` | Giữ tham chiếu cửa sổ để điều hướng màn hình |
| `soTaiKhoan` | `String` | Lọc danh mục theo từng người dùng |
| `tablechi` | `TableView` | Hiển thị danh mục Chi |
| `tableThu` | `TableView` | Hiển thị danh mục Thu |
| `danhMucDAO` | `DanhMucDAO` | Cầu nối với cơ sở dữ liệu |

---

## 3. Constructor

```java
public CategoryController(Stage stage, String soTaiKhoan) {
    this.stage = stage;
    this.soTaiKhoan = soTaiKhoan;
    this.danhMucDAO = new DanhMucDAO();
    createUI();   // ← Gọi ngay để vẽ giao diện
}
```

> **Lưu ý:** Ngay khi khởi tạo, `createUI()` được gọi để xây dựng toàn bộ giao diện. Controller này **tự xây UI** thay vì dùng file FXML.

---

## 4. Phương Thức `createUI()` — Xây Dựng Giao Diện Chính

```
createUI()
│
├── VBox root (layout chính, padding 20px, khoảng cách 15px)
│   ├── Label "QUẢN LÝ DANH MỤC"    ← Tiêu đề
│   ├── Label hướng dẫn (italic)    ← Ghi chú mặc định vs riêng tư
│   ├── TabPane
│   │   ├── Tab "Danh mục Chi"  → buildTabContent("chi")
│   │   └── Tab "Danh mục Thu"  → buildTabContent("thu")
│   └── Button "Quay Lại"           ← Điều hướng về Dashboard
│
└── Scene (1200 x 800)
```

### Nút "Quay Lại"
```java
btnQuayLai.setOnAction(e -> {
    DashboardController dashboard = new DashboardController(stage);
    stage.setScene(dashboard.getScene());
    stage.setWidth(1200);
    stage.setHeight(830);
    stage.centerOnScreen();
});
```
> Khi nhấn, tạo một `DashboardController` mới và chuyển `Scene` về trang chủ Dashboard.

---

## 5. Phương Thức `buildTabContent(String loai)` — Tạo Nội Dung Từng Tab

Được gọi 2 lần: một lần với `"chi"`, một lần với `"thu"`.

```
buildTabContent("chi")
│
├── TableView ← buildTable()          // Tạo bảng 4 cột
├── Gán tablechi hoặc tableThu
├── HBox chứa 3 nút:
│   ├── "Thêm danh mục Chi"  → handleThem(tbl, loai, loaiLabel)
│   ├── "Sửa"                → handleSua(tbl)
│   └── "Xóa"                → handleXoa(tbl)
└── Load dữ liệu:
    tbl.getItems().setAll(danhMucDAO.layDanhMucTheoLoai(soTaiKhoan, loai))
```

> Dữ liệu được tải từ database **ngay khi tab được tạo**, thông qua `layDanhMucTheoLoai()`.

---

## 6. Phương Thức `buildTable()` — Xây Dựng Bảng Dữ Liệu

Tạo `TableView<DanhMuc>` với **4 cột**:

### Cột 1: Tên Danh Mục (`colTen`)
```
Danh mục CHA  →  "▣ Ăn uống"        (chữ in đậm, màu xanh đậm)
Danh mục CON  →  "   ↳ Cà phê"      (thụt vào, màu tối)
```
- Dùng `isDanhMucCon()` để phân biệt cha/con và hiển thị ký hiệu tương ứng.

### Cột 2: Mô Tả (`colMoTa`)
- Hiển thị nội dung mô tả của danh mục.
- Màu văn bản: `#2c3e50` (xanh đen).

### Cột 3: Nhóm (`colCha`)
```
Danh mục CHA  →  "Danh mục cha"      (màu xanh, in đậm)
Danh mục CON  →  "Tên danh mục cha"  (hiển thị tên nhóm thuộc về)
```

### Cột 4: Phân Loại (`colPhanLoai`)
```
isDanhMucMacDinh() == true   →  "Mặc định"  (màu xanh dương)
isDanhMucMacDinh() == false  →  "Riêng tư"  (màu cam)
```

---

## 7. Phương Thức `handleThem()` — Thêm Danh Mục Mới

```
Người dùng nhấn "Thêm"
│
├── Mở Dialog với form:
│   ├── TextField: Tên danh mục  (bắt buộc)
│   ├── TextArea:  Mô tả         (tùy chọn)
│   └── ComboBox:  Chọn danh mục cha (tùy chọn — nếu bỏ trống = danh mục gốc)
│
├── Nhấn "Thêm" → Validate:
│   ├── Tên rỗng?          → Báo lỗi, dừng
│   ├── Là danh mục con?   → Kiểm tra trùng tên CON (tonTaiTenDanhMucCon)
│   └── Là danh mục gốc?   → Kiểm tra trùng tên (tonTaiTenDanhMuc)
│
└── Nếu hợp lệ → danhMucDAO.themDanhMuc(dm) → Reload bảng
```

> **Chú ý quan trọng:** Danh mục chỉ có **1 cấp** (cha → con). Không hỗ trợ đa cấp.

---

## 8. Phương Thức `handleSua()` — Sửa Danh Mục

```
Người dùng chọn 1 dòng → nhấn "Sửa"
│
├── Không chọn?          → Báo lỗi "Vui lòng chọn danh mục!"
├── Danh mục mặc định?   → Báo lỗi "Không thể sửa danh mục mặc định!"
│
├── Mở Dialog với form điền sẵn thông tin cũ:
│   ├── TextField: Tên (đã điền sẵn)
│   ├── TextArea:  Mô tả (đã điền sẵn)
│   └── ComboBox:  Danh mục cha (đã chọn sẵn nếu là danh mục con)
│
├── Nhấn "Lưu" → Validate:
│   ├── Kiểm tra xem tên / cha có thay đổi không (daDoiTen, daDoiCha)
│   ├── Là danh mục con?  → Kiểm tra trùng tên với exclude ID hiện tại
│   └── Là danh mục gốc? → Kiểm tra trùng tên danh mục gốc
│
└── Nếu hợp lệ → danhMucDAO.suaDanhMuc(dm) → Reload bảng
```

---

## 9. Phương Thức `handleXoa()` — Xóa Danh Mục

```
Người dùng chọn 1 dòng → nhấn "Xóa"
│
├── Không chọn?          → Báo lỗi
├── Danh mục mặc định?   → Báo lỗi "Không thể xóa danh mục mặc định!"
│
├── Hiển thị hộp thoại xác nhận:
│   "Xóa danh mục: [Tên]
│    Các giao dịch dùng danh mục này sẽ mất liên kết!"
│
└── Nếu xác nhận OK → danhMucDAO.xoaDanhMuc(id) → Reload bảng
```

> ⚠️ **Cảnh báo:** Khi xóa, các giao dịch liên kết với danh mục đó sẽ mất liên kết nhưng **không bị xóa**.

---

## 10. Phương Thức `layDanhMucChaUngVien()` — Lấy Danh Sách Danh Mục Cha

```java
private List<DanhMuc> layDanhMucChaUngVien(String loai, Integer excludeId) {
    // Lấy tất cả danh mục theo loại
    // Lọc: chỉ giữ danh mục GỐC (parentId == null)
    // Loại bỏ chính danh mục đang sửa (tránh chọn chính mình làm cha)
}
```

> **Mục đích:** Khi tạo/sửa danh mục con, chỉ cho phép chọn danh mục **gốc** làm cha, đảm bảo cây chỉ có **1 cấp**.

---

## 11. Luồng Dữ Liệu Tổng Quát

```
[Database]
    │
    ▼ layDanhMucTheoLoai(soTaiKhoan, loai)
[DanhMucDAO]
    │
    ▼ List<DanhMuc>
[TableView]  ──► Hiển thị ra màn hình
    │
    ▼ Người dùng tương tác (thêm/sửa/xóa)
[handleThem / handleSua / handleXoa]
    │
    ▼ themDanhMuc / suaDanhMuc / xoaDanhMuc
[DanhMucDAO]
    │
    ▼ Commit xuống Database
    ▼ Reload lại TableView
```

---

## 12. Các Lớp Liên Quan

| Lớp | Vai trò |
|---|---|
| `DanhMuc` | Model — đối tượng đại diện cho 1 danh mục |
| `DanhMucDAO` | Data Access Object — thực hiện các câu SQL |
| `DashboardController` | Màn hình chính — điều hướng quay lại |
| `Stage` / `Scene` | JavaFX — quản lý cửa sổ và màn hình |

---

## 13. Các Quy Tắc Nghiệp Vụ Quan Trọng

| # | Quy tắc |
|---|---|
| 1 | **Danh mục mặc định** không được sửa hoặc xóa |
| 2 | **Danh mục riêng tư** mỗi người dùng tự tạo, có thể sửa/xóa |
| 3 | Cây danh mục chỉ có **1 cấp** (cha → con, không có cháu) |
| 4 | **Không được trùng tên** trong cùng loại (chi/thu) và nhóm |
| 5 | Xóa danh mục sẽ **mất liên kết giao dịch**, không xóa giao dịch |
| 6 | Danh mục **thuộc về từng tài khoản** (`soTaiKhoan`), không chia sẻ giữa người dùng |

---

## 14. Sơ Đồ Phương Thức (Method Map)

```
CategoryController
│
├── CategoryController(stage, soTaiKhoan)   ← Constructor
│
├── createUI()                               ← Vẽ toàn bộ giao diện
│   └── buildTabContent(loai)               ← Tạo nội dung mỗi tab
│       └── buildTable()                    ← Tạo TableView 4 cột
│
├── handleThem(tbl, loai, loaiLabel)         ← Xử lý thêm mới
├── handleSua(tbl)                           ← Xử lý sửa
├── handleXoa(tbl)                           ← Xử lý xóa
│
├── layDanhMucChaUngVien(loai, excludeId)    ← Lấy danh sách cha hợp lệ
├── showAlert(title, content)                ← Hiển thị hộp thoại thông báo
│
└── getScene()                               ← Trả về Scene để gán vào Stage
```

---

*Tài liệu được tạo tự động dựa trên phân tích mã nguồn `CategoryController.java`*
