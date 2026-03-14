# Đặc tả chi tiết Use Case — Style 2 (Có sử dụng luồng phụ)

## Use Case: Quản lý danh mục mặc định (Admin)

### Brief Description

Use case này mô tả cách Admin quản lý (xem, thêm, sửa, xóa) các danh mục mặc định của hệ thống — gồm **danh mục Chi** (dùng khi người dùng gửi tiền) và **danh mục Thu** (dùng khi người dùng nhận tiền). Danh mục mặc định hiển thị cho tất cả người dùng; chỉ Admin mới có quyền thay đổi.

---

### Basic Flow (Luồng cơ bản)

1. Use case bắt đầu khi Admin đã đăng nhập và chọn chức năng **quản lý danh mục mặc định** từ Dashboard (menu bên trái).
2. Hệ thống hiển thị menu chức năng với hai lựa chọn: **Danh mục Chi mặc định** và **Danh mục Thu mặc định**.
   **{chọn loại danh mục}**
3. Admin chọn một trong hai: **Danh mục Chi mặc định** hoặc **Danh mục Thu mặc định**.
4. Hệ thống truy xuất và hiển thị bảng danh sách danh mục mặc định tương ứng (cột: ID, Tên danh mục, Mô tả), kèm các nút **Thêm**, **Sửa**, **Xóa**.
   **{chọn hoạt động}**
5. Trong khi Admin còn thao tác cần thực hiện:
   - **5.1.** Nếu chọn hoạt động **Thêm**  
     - 5.1.1. Thực hiện subflow **S1: Thêm danh mục mặc định**.
   - **5.2.** Nếu chọn hoạt động **Sửa**  
     - 5.2.1. Thực hiện subflow **S2: Sửa danh mục mặc định**.
   - **5.3.** Nếu chọn hoạt động **Xóa**  
     - 5.3.1. Thực hiện subflow **S3: Xóa danh mục mặc định**.
6. Use case kết thúc (Admin có thể chuyển sang panel khác hoặc đăng xuất).

---

### Subflows (Luồng phụ)

#### S1: Thêm danh mục mặc định

**{bắt đầu thêm danh mục}**

1. Hệ thống hiển thị hộp thoại **Thêm danh mục mặc định** với các trường: **Tên danh mục**, **Mô tả** (tùy chọn).
2. Admin nhập tên danh mục và (tùy ý) mô tả, rồi bấm **Thêm**.
   **{xác nhận thêm}**
3. Hệ thống kiểm tra tên danh mục không rỗng và không trùng với danh mục mặc định cùng loại (Chi hoặc Thu) đã có trong cơ sở dữ liệu.
4. Hệ thống lưu danh mục mới vào cơ sở dữ liệu (loại và phạm vi mặc định được xác định theo panel đang mở: Chi hoặc Thu, `so_tai_khoan = NULL`).
5. Hệ thống thông báo **"Đã thêm danh mục: &lt;tên&gt;"** (hoặc **"Đã thêm: &lt;tên&gt;"** cho danh mục Thu) và cập nhật lại bảng danh sách.

---

#### S2: Sửa danh mục mặc định

**{bắt đầu sửa danh mục}**

1. Hệ thống kiểm tra Admin đã chọn một dòng trong bảng danh mục.
2. Hệ thống hiển thị hộp thoại **Sửa danh mục** (hoặc **Sửa danh mục Thu**) với các trường **Tên danh mục**, **Mô tả** đã được điền sẵn theo danh mục đang chọn.
3. Admin chỉnh sửa tên và/hoặc mô tả, rồi bấm **Lưu**.
   **{xác nhận sửa}**
4. Hệ thống kiểm tra tên danh mục không rỗng.
5. Hệ thống cập nhật danh mục trong cơ sở dữ liệu.
6. Hệ thống thông báo **"Đã cập nhật danh mục!"** (hoặc **"Đã cập nhật!"**) và cập nhật lại bảng danh sách.

---

#### S3: Xóa danh mục mặc định

**{bắt đầu xóa danh mục}**

1. Hệ thống kiểm tra Admin đã chọn một dòng trong bảng danh mục.
2. Hệ thống hiển thị hộp thoại xác nhận: **"Xóa danh mục: &lt;tên&gt;"** với nội dung cảnh báo *"Các giao dịch đã dùng danh mục này sẽ mất liên kết! Bạn có chắc chắn muốn xóa?"*.
   **{xác nhận xóa}**
3. Admin xác nhận **OK**.
4. Hệ thống xóa danh mục khỏi cơ sở dữ liệu.
5. Hệ thống thông báo **"Đã xóa danh mục!"** (hoặc **"Đã xóa!"**) và cập nhật lại bảng danh sách.

---

### Alternative Flows (Luồng thay thế)

#### A1: Tên danh mục rỗng

**Tại** **{bắt đầu thêm danh mục}** (trong S1) hoặc **{xác nhận thêm}**, **hoặc** tại **{bắt đầu sửa danh mục}** (trong S2) hoặc **{xác nhận sửa}**, nếu tên danh mục sau khi bỏ khoảng trắng đầu/cuối là rỗng:

1. Hệ thống thông báo **"Vui lòng nhập tên danh mục!"** (hoặc **"Vui lòng nhập tên!"** trong màn hình Thu).
2. Luồng xử lý quay lại bước nhập trong cùng hộp thoại (không đóng dialog).

---

#### A2: Tên danh mục mặc định đã tồn tại

**Tại** **{xác nhận thêm}** (trong S1), nếu tên danh mục đã tồn tại trong danh mục mặc định cùng loại (Chi hoặc Thu):

1. Hệ thống thông báo **"Tên danh mục Chi mặc định đã tồn tại! Vui lòng nhập tên khác."** (hoặc tương ứng **"Tên danh mục Thu mặc định đã tồn tại! Vui lòng nhập tên khác."**).
2. Luồng xử lý quay lại bước **{bắt đầu thêm danh mục}** (hộp thoại vẫn mở, Admin có thể sửa tên).

---

#### A3: Chưa chọn danh mục khi Sửa hoặc Xóa

**Tại** **{chọn hoạt động}**, nếu Admin bấm **Sửa** hoặc **Xóa** mà chưa chọn bất kỳ dòng nào trong bảng:

1. Hệ thống thông báo **"Vui lòng chọn danh mục cần sửa!"** hoặc **"Vui lòng chọn danh mục cần xóa!"** tương ứng.
2. Luồng xử lý quay lại bước **{chọn hoạt động}** (vẫn ở cùng panel, Admin chọn dòng rồi thao tác lại).

---

#### A4: Thao tác cơ sở dữ liệu thất bại

**Tại** **{xác nhận thêm}** (S1), **{xác nhận sửa}** (S2), hoặc **{xác nhận xóa}** (S3), nếu thực hiện thêm/sửa/xóa trong cơ sở dữ liệu thất bại (ví dụ lỗi kết nối hoặc ràng buộc):

1. Hệ thống thông báo **"Thêm danh mục thất bại!"**, **"Cập nhật thất bại!"**, hoặc **"Xóa thất bại!"** tương ứng.
2. Luồng xử lý quay lại bước **{chọn hoạt động}** (hộp thoại đóng, bảng danh sách không thay đổi).

---

#### A5: Hủy thao tác

**Tại** bất kỳ thời điểm nào trong khoảng từ **{bắt đầu thêm danh mục}** đến **{xác nhận thêm}**, từ **{bắt đầu sửa danh mục}** đến **{xác nhận sửa}**, hoặc từ **{bắt đầu xóa danh mục}** đến **{xác nhận xóa}**:

1. Admin bấm **Cancel** (hoặc đóng hộp thoại) để hủy thao tác đang thực hiện.
2. Hệ thống đóng hộp thoại, không ghi thay đổi.
3. Luồng xử lý quay lại bước **{chọn hoạt động}**.

---

#### A6: Không xác nhận xóa

**Tại** **{xác nhận xóa}** (trong S3), nếu Admin chọn **Cancel** (không xác nhận OK):

1. Hệ thống đóng hộp thoại xác nhận.
2. Luồng xử lý quay lại bước **{chọn hoạt động}**.

---

### Tham chiếu code (để kiểm chứng)

| Mô tả trong đặc tả | Vị trí trong code |
|--------------------|-------------------|
| Menu "Danh mục Chi mặc định" / "Danh mục Thu mặc định" | `AdminDashboardController`: `btnNavDanhMuc`, `btnNavDanhMucThu` |
| Hiển thị bảng danh mục (ID, Tên, Mô tả) | `buildPanelDanhMuc()`, `buildPanelDanhMucThu()` — `tableDanhMuc`, `tableDanhMucThu` |
| Truy xuất danh sách mặc định theo loại | `DanhMucDAO.layDanhMucTheoLoai(null, "chi")` / `"thu"` |
| Thêm: kiểm tra tên rỗng, trùng tên | `handleThemDanhMuc()` / `handleThemDanhMucThu()` — `tonTaiTenDanhMuc(..., null)` |
| Thêm: lưu DB | `DanhMucDAO.themDanhMuc(dm)` với `so_tai_khoan = null` |
| Sửa: chọn dòng, kiểm tra tên, cập nhật | `handleSuaDanhMuc()` / `handleSuaDanhMucThu()` — `suaDanhMuc(dm)` |
| Xóa: xác nhận, cảnh báo mất liên kết | `handleXoaDanhMuc()` / `handleXoaDanhMucThu()` — `xoaDanhMuc(id)` |
