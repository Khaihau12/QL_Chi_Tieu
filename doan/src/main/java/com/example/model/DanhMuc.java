package com.example.model;

/**
 * Model Danh mục (Category) - Phân loại giao dịch
 */
public class DanhMuc {
    private int id;
    private String tenDanhMuc;   // Tên danh mục (Ăn uống, Di chuyển, Mua sắm...)
    private String moTa;          // Mô tả
    private String mauSac;        // Màu sắc hiển thị (hex color)
    private String soTaiKhoan;    // NULL = danh mục mặc định, khác NULL = danh mục riêng

    // Constructor đầy đủ
    public DanhMuc(int id, String tenDanhMuc, String moTa, String mauSac, String soTaiKhoan) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
        this.moTa = moTa;
        this.mauSac = mauSac;
        this.soTaiKhoan = soTaiKhoan;
    }
    
    // Constructor không có ID (cho insert mới)
    public DanhMuc(String tenDanhMuc, String moTa, String mauSac, String soTaiKhoan) {
        this.tenDanhMuc = tenDanhMuc;
        this.moTa = moTa;
        this.mauSac = mauSac;
        this.soTaiKhoan = soTaiKhoan;
    }

    // Constructor đầy đủ (backward compatible)
    public DanhMuc(int id, String tenDanhMuc, String moTa, String mauSac) {
        this(id, tenDanhMuc, moTa, mauSac, null);
    }

    // Constructor không có ID (backward compatible)
    public DanhMuc(String tenDanhMuc, String moTa, String mauSac) {
        this(tenDanhMuc, moTa, mauSac, null);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getMauSac() {
        return mauSac;
    }

    public void setMauSac(String mauSac) {
        this.mauSac = mauSac;
    }
    
    public String getSoTaiKhoan() {
        return soTaiKhoan;
    }
    
    public void setSoTaiKhoan(String soTaiKhoan) {
        this.soTaiKhoan = soTaiKhoan;
    }
    
    // Kiểm tra có phải danh mục mặc định không
    public boolean isDanhMucMacDinh() {
        return soTaiKhoan == null;
    }

    @Override
    public String toString() {
        return tenDanhMuc + (isDanhMucMacDinh() ? "" : " ⭐"); // ⭐ = danh mục riêng
    }
}
