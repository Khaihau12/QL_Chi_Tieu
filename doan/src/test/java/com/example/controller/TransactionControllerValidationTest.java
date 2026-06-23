package com.example.controller;

import com.example.model.DanhMuc;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TransactionControllerValidationTest {

    private DanhMuc taoDanhMucHopLe() {
        return new DanhMuc(1, "An uong", "Chi tieu an uong", "chi", "123", 10, "Sinh hoat");
    }

    @Test
    public void TC01_validateInputChuyenTien_soTaiKhoanNhanRong() {
        String actual = TransactionController.validateInputChuyenTien("", "50000", "123", "Nam");

        assertEquals("Vui lòng nhập số tài khoản người nhận!", actual);
    }

    @Test
    public void TC02_validateInputChuyenTien_soTaiKhoanNhanKhongHopLe() {
        String actual = TransactionController.validateInputChuyenTien("12a", "50000", "123", "Nam");

        assertEquals("Số tài khoản người nhận không hợp lệ!", actual);
    }

    @Test
    public void TC03_validateInputChuyenTien_soTienRong() {
        String actual = TransactionController.validateInputChuyenTien("456", "", "123", "Nam");

        assertEquals("Vui lòng nhập số tiền!", actual);
    }

    @Test
    public void TC04_validateInputChuyenTien_soTienCoChu() {
        String actual = TransactionController.validateInputChuyenTien("456", "50a00", "123", "Nam");

        assertEquals("Số tiền không hợp lệ!", actual);
    }

    @Test
    public void TC05_validateInputChuyenTien_soTienToanDauCham() {
        String actual = TransactionController.validateInputChuyenTien("456", "...", "123", "Nam");

        assertEquals("Số tiền không hợp lệ!", actual);
    }

    @Test
    public void TC06_validateInputChuyenTien_soTienBangKhong() {
        String actual = TransactionController.validateInputChuyenTien("456", "0", "123", "Nam");

        assertEquals("Số tiền phải lớn hơn 0!", actual);
    }

    @Test
    public void TC07_validateInputChuyenTien_soTaiKhoanGuiRong() {
        String actual = TransactionController.validateInputChuyenTien("456", "50000", "", "Nam");

        assertEquals("Không xác định được tài khoản người gửi!", actual);
    }

    @Test
    public void TC08_validateInputChuyenTien_chuyenChoChinhMinh() {
        String actual = TransactionController.validateInputChuyenTien("123", "50000", "123", "Nam");

        assertEquals("Không thể chuyển tiền cho chính mình!", actual);
    }

    @Test
    public void TC09_validateInputChuyenTien_nguoiNhanKhongTonTai() {
        String actual = TransactionController.validateInputChuyenTien("456", "50000", "123", null);

        assertEquals("Số tài khoản người nhận không hợp lệ hoặc không tồn tại!", actual);
    }

    @Test
    public void TC10_validateInputChuyenTien_hopLe() {
        String actual = TransactionController.validateInputChuyenTien("456", "50000", "123", "Nam");

        assertNull(actual);
    }

    @Test
    public void TC11_validateInputTienMat_loaiKhongHopLe() {
        String actual = TransactionController.validateInputTienMat("abc", "50000", taoDanhMucHopLe());

        assertEquals("Loại giao dịch không hợp lệ!", actual);
    }

    @Test
    public void TC12_validateInputTienMat_soTienRong() {
        String actual = TransactionController.validateInputTienMat("chi", "", taoDanhMucHopLe());

        assertEquals("Vui lòng nhập số tiền!", actual);
    }

    @Test
    public void TC13_validateInputTienMat_soTienToanDauCham() {
        String actual = TransactionController.validateInputTienMat("chi", "...", taoDanhMucHopLe());

        assertEquals("Số tiền không hợp lệ!", actual);
    }

    @Test
    public void TC14_validateInputTienMat_soTienBangKhong() {
        String actual = TransactionController.validateInputTienMat("chi", "0", taoDanhMucHopLe());

        assertEquals("Số tiền không hợp lệ!", actual);
    }

    @Test
    public void TC15_validateInputTienMat_chuaChonDanhMuc() {
        String actual = TransactionController.validateInputTienMat("chi", "50000", null);

        assertEquals("Vui lòng chọn danh mục!", actual);
    }

    @Test
    public void TC16_validateInputTienMat_hopLe() {
        String actual = TransactionController.validateInputTienMat("chi", "50000", taoDanhMucHopLe());

        assertNull(actual);
    }
}
