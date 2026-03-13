package com.example.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Entity class đại diện cho bảng giao_dich (Chuyển tiền giữa tài khoản)
 */
public class GiaoDich {
    private Integer maGiaoDich;
    private String soTaiKhoanGui; // Số tài khoản gửi tiền
    private String soTaiKhoanNhan; // Số tài khoản nhận tiền
    private BigDecimal soTien;
    private String noiDung; // Nội dung chuyển tiền
    private Timestamp ngayGiaoDich;
    private String trangThai; // 'thanh_cong', 'that_bai'
    private Integer danhMucId;     // ID danh mục chi (người gửi)
    private Integer danhMucThuId;  // ID danh mục thu (người nhận)
    // Tên danh mục (populated by DAO JOIN - không lưu DB)
    private String tenDanhMucChi;
    private String tenDanhMucThu;

    // Constructor mặc định
    public GiaoDich() {
    }

    // Constructor đầy đủ
    public GiaoDich(Integer maGiaoDich, String soTaiKhoanGui, String soTaiKhoanNhan,
                    BigDecimal soTien, String noiDung, Timestamp ngayGiaoDich, String trangThai) {
        this.maGiaoDich = maGiaoDich;
        this.soTaiKhoanGui = soTaiKhoanGui;
        this.soTaiKhoanNhan = soTaiKhoanNhan;
        this.soTien = soTien;
        this.noiDung = noiDung;
        this.ngayGiaoDich = ngayGiaoDich;
        this.trangThai = trangThai;
    }

    // Constructor không có ID (dùng khi thêm mới)
    public GiaoDich(String soTaiKhoanGui, String soTaiKhoanNhan, BigDecimal soTien, String noiDung) {
        this.soTaiKhoanGui = soTaiKhoanGui;
        this.soTaiKhoanNhan = soTaiKhoanNhan;
        this.soTien = soTien;
        this.noiDung = noiDung;
        this.trangThai = "thanh_cong";
    }

    // Getters và Setters
    public Integer getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(Integer maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public String getSoTaiKhoanGui() {
        return soTaiKhoanGui;
    }

    public void setSoTaiKhoanGui(String soTaiKhoanGui) {
        this.soTaiKhoanGui = soTaiKhoanGui;
    }

    public String getSoTaiKhoanNhan() {
        return soTaiKhoanNhan;
    }

    public void setSoTaiKhoanNhan(String soTaiKhoanNhan) {
        this.soTaiKhoanNhan = soTaiKhoanNhan;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public Timestamp getNgayGiaoDich() {
        return ngayGiaoDich;
    }

    public void setNgayGiaoDich(Timestamp ngayGiaoDich) {
        this.ngayGiaoDich = ngayGiaoDich;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getDanhMucId() {
        return danhMucId;
    }

    public void setDanhMucId(Integer danhMucId) {
        this.danhMucId = danhMucId;
    }

    public Integer getDanhMucThuId() {
        return danhMucThuId;
    }

    public void setDanhMucThuId(Integer danhMucThuId) {
        this.danhMucThuId = danhMucThuId;
    }

    public String getTenDanhMucChi() {
        return tenDanhMucChi;
    }

    public void setTenDanhMucChi(String tenDanhMucChi) {
        this.tenDanhMucChi = tenDanhMucChi;
    }

    public String getTenDanhMucThu() {
        return tenDanhMucThu;
    }

    public void setTenDanhMucThu(String tenDanhMucThu) {
        this.tenDanhMucThu = tenDanhMucThu;
    }

    @Override
    public String toString() {
        return "GiaoDich{" +
                "maGiaoDich=" + maGiaoDich +
                ", soTaiKhoanGui='" + soTaiKhoanGui + '\'' +
                ", soTaiKhoanNhan='" + soTaiKhoanNhan + '\'' +
                ", soTien=" + soTien +
                ", noiDung='" + noiDung + '\'' +
                ", ngayGiaoDich=" + ngayGiaoDich +
                ", trangThai='" + trangThai + '\'' +
                ", danhMucId=" + danhMucId +
                '}';
    }
}
