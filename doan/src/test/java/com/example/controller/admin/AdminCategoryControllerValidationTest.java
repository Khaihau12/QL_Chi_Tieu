package com.example.controller.admin;

import com.example.model.DanhMuc;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AdminCategoryControllerValidationTest {

    private DanhMuc taoDanhMucThuHopLe() {
        return new DanhMuc(1, "Lương", "Danh mục thu nhập", "thu", null);
    }

    private DanhMuc taoDanhMucChiHopLe() {
        return new DanhMuc(2, "Ăn uống", "Danh mục chi tiêu", "chi", null);
    }

    @Test
    public void TC01_validateInputDanhMuc_tenRong() {
        assertEquals("Vui lòng nhập tên danh mục!",
                AdminIncomeCategoryController.validateInputDanhMuc("", false));
        assertEquals("Vui lòng nhập tên danh mục!",
                AdminExpenseCategoryController.validateInputDanhMuc("", false));
    }

    @Test
    public void TC02_validateInputDanhMuc_tenBiTrung() {
        assertEquals("Tên danh mục đã tồn tại! Vui lòng nhập tên khác.",
                AdminIncomeCategoryController.validateInputDanhMuc("Lương", true));
        assertEquals("Tên danh mục đã tồn tại! Vui lòng nhập tên khác.",
                AdminExpenseCategoryController.validateInputDanhMuc("Lương", true));
    }

    @Test
    public void TC03_validateInputDanhMuc_hopLe() {
        assertNull(AdminIncomeCategoryController.validateInputDanhMuc("Lương", false));
        assertNull(AdminExpenseCategoryController.validateInputDanhMuc("Lương", false));
    }

    @Test
    public void TC04_validateInputChonDanhMuc_chuaChonVaXoa() {
        assertEquals("Vui lòng chọn danh mục cần xóa!",
                AdminIncomeCategoryController.validateInputChonDanhMuc(null, "xoa"));
        assertEquals("Vui lòng chọn danh mục cần xóa!",
                AdminExpenseCategoryController.validateInputChonDanhMuc(null, "xoa"));
    }

    @Test
    public void TC05_validateInputChonDanhMuc_chuaChonVaSua() {
        assertEquals("Vui lòng chọn danh mục cần sửa!",
                AdminIncomeCategoryController.validateInputChonDanhMuc(null, "sua"));
        assertEquals("Vui lòng chọn danh mục cần sửa!",
                AdminExpenseCategoryController.validateInputChonDanhMuc(null, "sua"));
    }

    @Test
    public void TC06_validateInputChonDanhMuc_hopLe() {
        assertNull(AdminIncomeCategoryController.validateInputChonDanhMuc(taoDanhMucThuHopLe(), "sua"));
        assertNull(AdminExpenseCategoryController.validateInputChonDanhMuc(taoDanhMucChiHopLe(), "sua"));
    }
}
