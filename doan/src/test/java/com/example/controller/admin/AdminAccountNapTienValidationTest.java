package com.example.controller.admin;

import com.example.model.NguoiDung;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AdminAccountNapTienValidationTest {

    private NguoiDung taoTaiKhoanHopLe() {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setSoTaiKhoan("123456");
        nguoiDung.setVaiTro("nguoi_dung");
        nguoiDung.setTrangThai("hoat_dong");
        return nguoiDung;
    }

    @Test
    public void TC01_validateInputNapTien_chuaChonTaiKhoan() {
        String actual = AdminAccountController.validateInputNapTien(null, "50000", "admin");

        assertEquals("Vui lòng nhập STK hợp lệ trước khi nạp tiền!", actual);
    }

    @Test
    public void TC02_validateInputNapTien_soTienRong() {
        String actual = AdminAccountController.validateInputNapTien(taoTaiKhoanHopLe(), " ", "admin");

        assertEquals("Vui lòng nhập số tiền cần nạp!", actual);
    }

    @Test
    public void TC03_validateInputNapTien_saiDinhDang() {
        String actual = AdminAccountController.validateInputNapTien(taoTaiKhoanHopLe(), "...", "admin");

        assertEquals("Số tiền không hợp lệ!", actual);
    }

    @Test
    public void TC04_validateInputNapTien_soTienBangKhong() {
        String actual = AdminAccountController.validateInputNapTien(taoTaiKhoanHopLe(), "0", "admin");

        assertEquals("Số tiền nạp phải lớn hơn 0!", actual);
    }

    @Test
    public void TC05_validateInputNapTien_soTienQuaLon() {
        String actual = AdminAccountController.validateInputNapTien(taoTaiKhoanHopLe(), "1000000000000", "admin");

        assertEquals("Số tiền nạp quá lớn!", actual);
    }

    @Test
    public void TC06_validateInputNapTien_chuaCoTaiKhoanAdmin() {
        String actual = AdminAccountController.validateInputNapTien(taoTaiKhoanHopLe(), "50000", null);

        assertEquals("Không xác định được tài khoản Admin để ghi lịch sử giao dịch!", actual);
    }

    @Test
    public void TC07_validateInputNapTien_hopLe() {
        String actual = AdminAccountController.validateInputNapTien(taoTaiKhoanHopLe(), "50000", "admin");

        assertNull(actual);
    }
}
