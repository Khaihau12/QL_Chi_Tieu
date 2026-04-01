# 📘 Hướng Dẫn Tìm Hiểu `CategoryController.java`

> **Dự án:** QL_Chi_Tieu — Quản lý chi tiêu cá nhân
> **File:** `src/main/java/com/example/controller/CategoryController.java`
> **Vai trò:** Màn hình Quản lý Danh mục — phân loại giao dịch Chi và Thu

---

## 1. CategoryController là gì?

`CategoryController` là màn hình **Quản lý Danh mục** — nơi người dùng có thể xem, thêm, sửa, xóa các danh mục phân loại giao dịch của mình.

Nó làm những việc:

- Hiển thị danh sách danh mục **Chi** và **Thu** trong 2 tab riêng
- Cho phép **thêm** danh mục riêng tư của từng user
- Cho phép **sửa / xóa** danh mục riêng tư (không được đụng vào danh mục mặc định)
- Hỗ trợ cấu trúc **cha → con** (2 cấp)

---

## 2. Khái Niệm Quan Trọng Cần Hiểu Trước

### 2.1 Danh mục Mặc định vs Riêng tư

| Loại | `soTaiKhoan` trong DB | Ai thấy | Có sửa/xóa không |
|---|---|---|---|
| Mặc định | `NULL` | Tất cả user | ❌ Không |
| Riêng tư | STK của user | Chỉ user đó | ✅ Có |

```java
// Trong model DanhMuc.java
public boolean isDanhMucMacDinh() {
    return soTaiKhoan == null;  // NULL = mặc định
}
```

### 2.2 Danh mục Cha vs Con

Hệ thống chỉ hỗ trợ **2 cấp** (cha → con), không có cấp 3:

```
▣ Ăn uống              ← Danh mục cha (parentId = NULL)
   ↳ Ăn sáng           ← Danh mục con (parentId = ID của "Ăn uống")
   ↳ Ăn trưa           ← Danh mục con
▣ Di chuyển            ← Danh mục cha
   ↳ Xăng xe           ← Danh mục con
```

```java
// Trong model DanhMuc.java
public boolean isDanhMucCon() {
    return parentId != null;  // Có cha = danh mục con
}
```

### 2.3 Loại Chi vs Thu

```
loai = "chi"  →  Tab "Danh mục Chi"   (phân loại tiền ra)
loai = "thu"  →  Tab "Danh mục Thu"   (phân loại tiền vào)
```

---

## 3. Các Thành Phần Trong Class

```
CategoryController
 ├── stage         → Cửa sổ ứng dụng
 ├── scene         → Nội dung màn hình
 ├── soTaiKhoan    → STK của user đang đăng nhập (để phân biệt danh mục riêng)
 ├── tableChi      → TableView hiển thị danh mục Chi
 ├── tableThu      → TableView hiển thị danh mục Thu
 └── danhMucDAO    → Kết nối database để CRUD danh mục
```

| Thuộc tính | Kiểu | Mục đích |
|---|---|---|
| `stage` | `Stage` | Giữ cửa sổ để điều hướng về Dashboard |
| `soTaiKhoan` | `String` | Lọc danh mục theo từng người dùng |
| `tableChi` / `tableThu` | `TableView` | Hiển thị dữ liệu 2 tab |
| `danhMucDAO` | `DanhMucDAO` | Cầu nối thực hiện các câu SQL |

---

## 4. Luồng Hoạt Động Tổng Quan

```
DashboardController → handleDanhMuc()
        │
        ▼
CategoryController(stage, soTaiKhoan)
        │
        ├── createUI()
        │       │
        │       ├── buildTabContent("chi")  → Tab "Danh mục Chi"
        │       └── buildTabContent("thu")  → Tab "Danh mục Thu"
        │
        └── [User tương tác]
                │
                ├── Bấm "Thêm"      → handleThem()
                ├── Bấm "Sửa"       → handleSua()
                ├── Bấm "Xóa"       → handleXoa()
                └── Bấm "Quay lại"  → về DashboardController
```

---

## 5. Giao Diện (Layout)

```
VBox (root, padding 20px, khoảng cách 15px)
 │
 ├── Label "QUẢN LÝ DANH MỤC"       (22px, đậm)
 ├── Label hướng dẫn                 (italic, xám — ghi chú mặc định/riêng tư)
 ├── TabPane
 │    ├── Tab "Danh mục Chi"
 │    │    ├── TableView (danh sách danh mục chi)
 │    │    └── HBox: [Thêm Chi] [Sửa] [Xóa]
 │    │
 │    └── Tab "Danh mục Thu"
 │         ├── TableView (danh sách danh mục thu)
 │         └── HBox: [Thêm Thu] [Sửa] [Xóa]
 │
 └── Button "Quay Lại"               (xám, điều hướng về Dashboard)
```

**Kích thước cửa sổ:** `1200 × 800 px`

---

## 6. Bảng TableView — 4 Cột

| Cột | Hiển thị | Định dạng đặc biệt |
|---|---|---|
| Tên Danh Mục | Tên danh mục | Cha: `▣ Ăn uống` (xanh đậm, bold) · Con: `   ↳ Ăn sáng` (thụt vào) |
| Mô Tả | Mô tả ngắn | — |
| Nhóm | Tên danh mục cha | Cha hiển thị: `Danh mục cha` · Con hiển thị: tên cha |
| Phân loại | Mặc định / Riêng tư | 🔵 Xanh = Mặc định · 🟠 Cam = Riêng tư |

---

## 7. Chi Tiết Các Method

### 7.1 `buildTabContent(loai)` — Xây Nội Dung Tab

Được gọi **2 lần**: 1 cho `"chi"`, 1 cho `"thu"`.

```
buildTabContent("chi")
      │
      ├── buildTable()           → tạo TableView với 4 cột
      ├── Tạo các nút: [Thêm] [Sửa] [Xóa]
      └── Load dữ liệu ngay:
            danhMucDAO.layDanhMucTheoLoai(soTaiKhoan, "chi")
```

> Mỗi tab **load dữ liệu ngay khi tạo**, không cần đợi user click.

### 7.2 `handleThem()` — Thêm Danh Mục Mới ⭐

```
handleThem()
      │
      ├── Mở Dialog gồm 3 trường:
      │     [Tên danh mục]  ← bắt buộc
      │     [Mô tả]         ← tùy chọn
      │     [Danh mục cha]  ← ComboBox chọn cha (để trống = danh mục gốc)
      │
      ├── User bấm "Thêm"
      │     │
      │     ├── Tên rỗng? → báo lỗi, hủy
      │     │
      │     ├── Kiểm tra trùng tên:
      │     │     Có chọn cha (danh mục con)?
      │     │       → tonTaiTenDanhMucCon()  (tên con không được trùng trong toàn bộ loại)
      │     │     Không chọn cha (danh mục gốc)?
      │     │       → tonTaiTenDanhMuc()     (tên gốc không được trùng)
      │     │
      │     ├── Trùng tên? → báo lỗi, hủy
      │     │
      │     └── OK → danhMucDAO.themDanhMuc() → reload bảng
```

> **Lưu ý:** Danh mục mới luôn là **riêng tư** (`soTaiKhoan` = STK của user). User không thể tạo danh mục mặc định.

### 7.3 `handleSua()` — Sửa Danh Mục ⭐

```
handleSua()
      │
      ├── Không chọn dòng nào?         → báo lỗi
      ├── Chọn danh mục MẶC ĐỊNH?      → báo lỗi "Không thể sửa danh mục mặc định!"
      │
      ├── Mở Dialog (điền sẵn dữ liệu hiện tại):
      │     [Tên danh mục]  ← có sẵn tên cũ
      │     [Mô tả]         ← có sẵn mô tả cũ
      │     [Danh mục cha]  ← chọn sẵn cha cũ (nếu có)
      │
      ├── User bấm "Lưu"
      │     │
      │     ├── Kiểm tra có thay đổi tên hoặc cha không?
      │     │     daDoiTen = tenMoi != tenCu
      │     │     daDoiCha = cha mới != cha cũ
      │     │
      │     ├── Nếu có thay đổi → kiểm tra trùng tên (giống handleThem)
      │     │
      │     └── OK → danhMucDAO.suaDanhMuc() → reload bảng
```

> **Lưu ý khi chọn danh mục cha:** ComboBox cha chỉ hiện các danh mục **gốc** (không có cha), và **loại trừ chính danh mục đang sửa** để tránh tự làm cha của mình.

### 7.4 `handleXoa()` — Xóa Danh Mục

```
handleXoa()
      │
      ├── Không chọn dòng nào?         → báo lỗi
      ├── Chọn danh mục MẶC ĐỊNH?      → báo lỗi "Không thể xóa danh mục mặc định!"
      │
      ├── Hiện Dialog xác nhận:
      │     "Xóa danh mục: [tên]"
      │     "Các giao dịch dùng danh mục này sẽ mất liên kết!"
      │
      └── User bấm OK → danhMucDAO.xoaDanhMuc() → reload bảng
```

> ⚠️ **Cảnh báo quan trọng:** Xóa danh mục **không xóa** các giao dịch liên quan, nhưng các giao dịch đó sẽ mất liên kết danh mục (hiển thị `— Chưa phân loại`).

### 7.5 `layDanhMucChaUngVien()` — Lấy Danh Sách Cha Có Thể Chọn

```java
private List<DanhMuc> layDanhMucChaUngVien(String loai, Integer excludeId)
```

Dùng trong Dialog Thêm/Sửa để hiện ComboBox chọn cha:

```
Lấy tất cả danh mục theo loại
      │
      └── Lọc chỉ giữ lại danh mục GỐC (parentId == null)
            + Loại trừ danh mục đang sửa (tránh tự làm cha mình)
```

> **Tại sao chỉ lấy danh mục gốc?**
> Vì hệ thống chỉ hỗ trợ 2 cấp. Danh mục con **không được** làm cha của danh mục khác.

---

## 8. Quy Tắc Kiểm Tra Trùng Tên

| Trường hợp | Hàm kiểm tra | Quy tắc |
|---|---|---|
| Thêm/Sửa danh mục gốc | `tonTaiTenDanhMuc()` | Không trùng tên trong cùng loại (chi/thu) |
| Thêm/Sửa danh mục con | `tonTaiTenDanhMucCon()` | Không trùng tên với **BẤT KỲ** danh mục con nào trong cùng loại, dù khác cha |

**Ví dụ:**

```
▣ Ăn uống
   ↳ Ăn sáng   ← đã có
▣ Nhà cửa
   ↳ Ăn sáng   ← ❌ KHÔNG cho phép! (tên con trùng dù khác cha)
```

---

## 9. Model `DanhMuc` — Các Field Quan Trọng

```
DanhMuc
 ├── id             → Khóa chính trong DB
 ├── tenDanhMuc     → Tên hiển thị
 ├── moTa           → Mô tả ngắn
 ├── loai           → "chi" hoặc "thu"
 ├── soTaiKhoan     → NULL = mặc định | STK = riêng tư
 ├── parentId       → NULL = danh mục gốc | ID = danh mục con
 └── tenDanhMucCha  → Tên cha (JOIN từ DB, chỉ để hiển thị)
```

Các method helper trong `DanhMuc`:

```java
isDanhMucMacDinh()  → soTaiKhoan == null
isDanhMucCon()      → parentId != null
isLoaiChi()         → loai.equals("chi")
isLoaiThu()         → loai.equals("thu")
toString()          → trả về tenDanhMuc   // để ComboBox hiển thị đúng
```

---

## 10. Liên Kết Với Các Class Khác

```
CategoryController
      │
      │  truy vấn DB
      └──────────────────► DanhMucDAO
                                │
                                ├── layDanhMucTheoLoai()     → lấy danh sách hiển thị
                                ├── tonTaiTenDanhMuc()       → kiểm tra trùng tên gốc
                                ├── tonTaiTenDanhMucCon()    → kiểm tra trùng tên con
                                ├── themDanhMuc()            → INSERT vào DB
                                ├── suaDanhMuc()             → UPDATE trong DB
                                └── xoaDanhMuc()             → DELETE trong DB
      │  điều hướng
      └──────────────────► DashboardController  (khi bấm "Quay lại")
```

---

## 11. Tóm Tắt Nhanh

| Method | Làm gì |
|---|---|
| `CategoryController(stage, stk)` | Khởi tạo, tạo UI |
| `createUI()` | Dựng layout VBox + TabPane |
| `buildTabContent(loai)` | Tạo nội dung 1 tab (chi hoặc thu) |
| `buildTable()` | Tạo TableView với 4 cột |
| `handleThem(tbl, loai, label)` | Dialog thêm danh mục mới |
| `handleSua(tbl)` | Dialog sửa danh mục đã chọn |
| `handleXoa(tbl)` | Xác nhận rồi xóa danh mục |
| `layDanhMucChaUngVien(loai, excludeId)` | Lấy danh sách cha để chọn trong ComboBox |
| `showAlert(title, content)` | Hiện hộp thoại thông báo |
| `getScene()` | Trả về scene để điều hướng |

| Quy tắc | Mô tả |
|---|---|
| Danh mục mặc định | Chỉ đọc, không sửa/xóa được |
| Danh mục riêng tư | User tự tạo, tự sửa/xóa |
| Tối đa 2 cấp | Chỉ có cha và con, không có cháu |
| Tên con không trùng | Trong cùng loại, dù khác cha |

---

*Tài liệu được tạo dựa trên phân tích mã nguồn `CategoryController.java`*
