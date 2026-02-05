package com.example.model;

import java.math.BigDecimal;

/**
 * Model Ngân sách (Budget) - Giới hạn chi tiêu cho từng danh mục
 */
public class NganSach {
    private int id;
    private int danhMucId;        // ID danh mục
    private String soTaiKhoan;    // Số tài khoản người dùng
    private BigDecimal gioiHan;   // Giới hạn chi tiêu
    private int thang;            // Tháng áp dụng (1-12)
    private int nam;              // Năm áp dụng
    private String tenDanhMuc;    // Tên danh mục (để hiển thị, không lưu DB)

    // Constructor mặc định (no-arg constructor)
    public NganSach() {
    }

    // Constructor đầy đủ
    public NganSach(int id, int danhMucId, String soTaiKhoan, BigDecimal gioiHan, int thang, int nam) {
        this.id = id;
        this.danhMucId = danhMucId;
        this.soTaiKhoan = soTaiKhoan;
        this.gioiHan = gioiHan;
        this.thang = thang;
        this.nam = nam;
    }

    // Constructor không có ID
    public NganSach(int danhMucId, String soTaiKhoan, BigDecimal gioiHan, int thang, int nam) {
        this.danhMucId = danhMucId;
        this.soTaiKhoan = soTaiKhoan;
        this.gioiHan = gioiHan;
        this.thang = thang;
        this.nam = nam;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDanhMucId() {
        return danhMucId;
    }

    public void setDanhMucId(int danhMucId) {
        this.danhMucId = danhMucId;
    }

    public String getSoTaiKhoan() {
        return soTaiKhoan;
    }

    public void setSoTaiKhoan(String soTaiKhoan) {
        this.soTaiKhoan = soTaiKhoan;
    }

    public BigDecimal getGioiHan() {
        return gioiHan;
    }

    public void setGioiHan(BigDecimal gioiHan) {
        this.gioiHan = gioiHan;
    }

    public int getThang() {
        return thang;
    }

    public void setThang(int thang) {
        this.thang = thang;
    }

    public int getNam() {
        return nam;
    }

    public void setNam(int nam) {
        this.nam = nam;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }
}
