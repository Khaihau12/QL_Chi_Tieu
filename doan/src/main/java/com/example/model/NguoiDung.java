package com.example.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Entity class đại diện cho bảng nguoi_dung (Tài khoản ngân hàng)
 */
public class NguoiDung {
    private Integer maNguoiDung;
    private String soTaiKhoan; // Số tài khoản ngân hàng
    private String tenDangNhap;
    private String matKhau;
    private String hoTen;
    private String email;
    private BigDecimal soDu; // Số dư tài khoản
    private String vaiTro; // 'quan_ly' hoặc 'nguoi_dung'
    private String trangThai; // 'hoat_dong' hoặc 'bi_khoa'
    private String lyDoKhoa; // Lý do khóa (null nếu đang hoạt động)
    private Timestamp thoiGianMoKhoa; // Thời điểm tự mở khóa (null = vĩnh viễn)
    private Timestamp lanDangNhapCuoi;
    private Timestamp ngayTao;

    // Constructor mặc định
    public NguoiDung() {
    }

    // Constructor không có ID (dùng khi đăng ký)
    public NguoiDung(String tenDangNhap, String matKhau, String hoTen, String email) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.email = email;
        this.soDu = BigDecimal.ZERO;
    }

    // Getters và Setters
    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }
    
    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getSoTaiKhoan() {
        return soTaiKhoan;
    }

    public void setSoTaiKhoan(String soTaiKhoan) {
        this.soTaiKhoan = soTaiKhoan;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getSoDu() {
        return soDu;
    }

    public void setSoDu(BigDecimal soDu) {
        this.soDu = soDu;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getLyDoKhoa() {
        return lyDoKhoa;
    }

    public void setLyDoKhoa(String lyDoKhoa) {
        this.lyDoKhoa = lyDoKhoa;
    }

    public Timestamp getThoiGianMoKhoa() {
        return thoiGianMoKhoa;
    }

    public void setThoiGianMoKhoa(Timestamp thoiGianMoKhoa) {
        this.thoiGianMoKhoa = thoiGianMoKhoa;
    }

    public Timestamp getLanDangNhapCuoi() {
        return lanDangNhapCuoi;
    }

    public void setLanDangNhapCuoi(Timestamp lanDangNhapCuoi) {
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp ngayTao) {
        this.ngayTao = ngayTao;
    }

    @Override
    public String toString() {
        return "NguoiDung{" +
                "soTaiKhoan='" + soTaiKhoan + '\'' +
                ", tenDangNhap='" + tenDangNhap + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", soDu=" + soDu +
                '}';
    }
}
