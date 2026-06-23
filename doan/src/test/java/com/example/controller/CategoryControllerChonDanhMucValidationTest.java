package com.example.controller;

import com.example.model.DanhMuc;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CategoryControllerChonDanhMucValidationTest {

    private DanhMuc taoDanhMucMacDinh() {
        return new DanhMuc(1, "An uong", "Danh muc mac dinh", "chi", null);
    }

    private DanhMuc taoDanhMucThuong() {
        return new DanhMuc(2, "An uong", "Danh muc cua user", "chi", "123");
    }

    @Test
    public void TC08_validateInputChonDanhMuc_chuaChonDanhMuc() {
        String actual = CategoryController.validateInputChonDanhMuc(null, "sua");

        assertEquals("Vui lòng chọn danh mục!", actual);
    }

    @Test
    public void TC09_validateInputChonDanhMuc_danhMucMacDinhVaXoa() {
        String actual = CategoryController.validateInputChonDanhMuc(taoDanhMucMacDinh(), "xoa");

        assertEquals("Không thể xóa danh mục mặc định!", actual);
    }

    @Test
    public void TC10_validateInputChonDanhMuc_danhMucMacDinhVaSua() {
        String actual = CategoryController.validateInputChonDanhMuc(taoDanhMucMacDinh(), "sua");

        assertEquals("Không thể sửa danh mục mặc định!", actual);
    }

    @Test
    public void TC11_validateInputChonDanhMuc_danhMucThuongVaSua() {
        String actual = CategoryController.validateInputChonDanhMuc(taoDanhMucThuong(), "sua");

        assertNull(actual);
    }
}
