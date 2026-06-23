package com.example.controller.admin;

import com.example.model.NguoiDung;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AdminAccountControllerValidationTest {

    private NguoiDung taoTaiKhoan(String vaiTro, String trangThai) {
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setVaiTro(vaiTro);
        nguoiDung.setTrangThai(trangThai);
        return nguoiDung;
    }

    @Test
    public void TC01_validateInputKhoaTaiKhoan_chuaChonTaiKhoan() {
        String actual = AdminAccountController.validateInputKhoaTaiKhoan(null);

        assertEquals("Vui lòng chọn tài khoản cần khóa!", actual);
    }

    @Test
    public void TC02_validateInputKhoaTaiKhoan_laAdmin() {
        String actual = AdminAccountController.validateInputKhoaTaiKhoan(taoTaiKhoan("quan_ly", "hoat_dong"));

        assertEquals("Không thể khóa tài khoản Admin!", actual);
    }

    @Test
    public void TC03_validateInputKhoaTaiKhoan_daBiKhoa() {
        String actual = AdminAccountController.validateInputKhoaTaiKhoan(taoTaiKhoan("nguoi_dung", "bi_khoa"));

        assertEquals("Tài khoản này đã bị khóa!", actual);
    }

    @Test
    public void TC04_validateInputKhoaTaiKhoan_hopLe() {
        String actual = AdminAccountController.validateInputKhoaTaiKhoan(taoTaiKhoan("nguoi_dung", "hoat_dong"));

        assertNull(actual);
    }

    @Test
    public void TC05_validateInputMoKhoaTaiKhoan_chuaChonTaiKhoan() {
        String actual = AdminAccountController.validateInputMoKhoaTaiKhoan(null);

        assertEquals("Vui lòng chọn tài khoản cần mở khóa!", actual);
    }

    @Test
    public void TC06_validateInputMoKhoaTaiKhoan_dangHoatDong() {
        String actual = AdminAccountController.validateInputMoKhoaTaiKhoan(taoTaiKhoan("nguoi_dung", "hoat_dong"));

        assertEquals("Tài khoản này đang hoạt động bình thường!", actual);
    }

    @Test
    public void TC07_validateInputMoKhoaTaiKhoan_hopLe() {
        String actual = AdminAccountController.validateInputMoKhoaTaiKhoan(taoTaiKhoan("nguoi_dung", "bi_khoa"));

        assertNull(actual);
    }

    @Test
    public void TC08_validateInputXemThongTinTaiKhoan_chuaChonUser() {
        String actual = AdminAccountController.validateInputXemThongTinTaiKhoan(null, null);

        assertEquals("Vui lòng chọn user cần xem thông tin!", actual);
    }

    @Test
    public void TC09_validateInputXemThongTinTaiKhoan_khongTimThayThongTin() {
        String actual = AdminAccountController.validateInputXemThongTinTaiKhoan(
                taoTaiKhoan("nguoi_dung", "hoat_dong"), null);

        assertEquals("Không tìm thấy thông tin người dùng trong cơ sở dữ liệu!", actual);
    }

    @Test
    public void TC10_validateInputXemThongTinTaiKhoan_hopLe() {
        String actual = AdminAccountController.validateInputXemThongTinTaiKhoan(
                taoTaiKhoan("nguoi_dung", "hoat_dong"),
                taoTaiKhoan("nguoi_dung", "hoat_dong"));

        assertNull(actual);
    }
}
