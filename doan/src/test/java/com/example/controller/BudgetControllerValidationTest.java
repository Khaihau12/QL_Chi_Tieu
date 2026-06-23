package com.example.controller;

import com.example.model.DanhMuc;
import com.example.model.NganSach;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BudgetControllerValidationTest {

    private DanhMuc taoDanhMucHopLe() {
        return new DanhMuc(1, "Ăn uống", "Danh mục ăn uống", "chi", "123");
    }

    private NganSach taoNganSachHopLe() {
        return new NganSach(1, 1, "123", new BigDecimal("500000"), 6, 2026);
    }

    @Test
    public void TC01_validateInputSuaNganSach_gioiHanRong() {
        String actual = BudgetController.validateInputSuaNganSach("");

        assertEquals("Vui lòng nhập giới hạn ngân sách!", actual);
    }

    @Test
    public void TC02_validateInputSuaNganSach_toanDauCham() {
        String actual = BudgetController.validateInputSuaNganSach("...");

        assertEquals("Số tiền không hợp lệ! Vui lòng nhập số.", actual);
    }

    @Test
    public void TC03_validateInputSuaNganSach_gioiHanBangKhong() {
        String actual = BudgetController.validateInputSuaNganSach("0");

        assertEquals("Giới hạn phải lớn hơn 0!", actual);
    }

    @Test
    public void TC04_validateInputSuaNganSach_gioiHanQuaLon() {
        String actual = BudgetController.validateInputSuaNganSach("10000000000");

        assertEquals("Giới hạn quá lớn! Vui lòng nhập số tiền nhỏ hơn 10 tỷ.", actual);
    }

    @Test
    public void TC05_validateInputSuaNganSach_hopLe() {
        String actual = BudgetController.validateInputSuaNganSach("500000");

        assertNull(actual);
    }

    @Test
    public void TC06_validateInputThemNganSach_thieuDanhMuc() {
        String actual = BudgetController.validateInputThemNganSach(null, "500000", 6, 2026, false);

        assertEquals("Vui lòng điền đầy đủ thông tin!", actual);
    }

    @Test
    public void TC07_validateInputThemNganSach_thangKhongHopLe() {
        String actual = BudgetController.validateInputThemNganSach(taoDanhMucHopLe(), "500000", 13, 2026, false);

        assertEquals("Tháng không hợp lệ!", actual);
    }

    @Test
    public void TC08_validateInputThemNganSach_gioiHanBangKhong() {
        String actual = BudgetController.validateInputThemNganSach(taoDanhMucHopLe(), "0", 6, 2026, false);

        assertEquals("Giới hạn phải lớn hơn 0!", actual);
    }

    @Test
    public void TC09_validateInputThemNganSach_nganSachDaTonTai() {
        String actual = BudgetController.validateInputThemNganSach(taoDanhMucHopLe(), "500000", 6, 2026, true);

        assertEquals("Ngân sách cho danh mục \"Ăn uống\" tháng 6/2026 đã tồn tại!\n"
                + "Vui lòng chọn danh mục hoặc tháng khác.", actual);
    }

    @Test
    public void TC10_validateInputThemNganSach_hopLe() {
        String actual = BudgetController.validateInputThemNganSach(taoDanhMucHopLe(), "500000", 6, 2026, false);

        assertNull(actual);
    }

    @Test
    public void TC11_validateInputChonNganSach_chuaChonVaXoa() {
        String actual = BudgetController.validateInputChonNganSach(null, "xoa");

        assertEquals("Vui lòng chọn ngân sách cần xóa!", actual);
    }

    @Test
    public void TC12_validateInputChonNganSach_chuaChonVaSua() {
        String actual = BudgetController.validateInputChonNganSach(null, "sua");

        assertEquals("Vui lòng chọn ngân sách cần sửa!", actual);
    }

    @Test
    public void TC13_validateInputChonNganSach_hopLe() {
        String actual = BudgetController.validateInputChonNganSach(taoNganSachHopLe(), "sua");

        assertNull(actual);
    }
}
