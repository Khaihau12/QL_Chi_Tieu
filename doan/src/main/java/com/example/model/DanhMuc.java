package com.example.model;

/**
 * Model Danh mục (Category) - Phân loại giao dịch
 */
public class DanhMuc {
    private int id;
    private String tenDanhMuc;
    private String moTa;
    private String loai;         // 'chi' = chi tiêu, 'thu' = thu nhập
    private String soTaiKhoan;   // NULL = mặc định, có giá trị = riêng tư

    // Constructor đầy đủ (với loai)
    public DanhMuc(int id, String tenDanhMuc, String moTa, String loai, String soTaiKhoan) {
        this.id = id;
        this.tenDanhMuc = tenDanhMuc;
        this.moTa = moTa;
        this.loai = loai;
        this.soTaiKhoan = soTaiKhoan;
    }

    // Constructor đầy đủ (backward-compat, mặc định loai='chi')
    public DanhMuc(int id, String tenDanhMuc, String moTa, String soTaiKhoan) {
        this(id, tenDanhMuc, moTa, "chi", soTaiKhoan);
    }

    // Constructor không có ID (cho insert mới)
    public DanhMuc(String tenDanhMuc, String moTa, String loai, String soTaiKhoan) {
        this.tenDanhMuc = tenDanhMuc;
        this.moTa = moTa;
        this.loai = loai;
        this.soTaiKhoan = soTaiKhoan;
    }

    // Constructor không có ID (backward-compat, mặc định loai='chi')
    public DanhMuc(String tenDanhMuc, String moTa, String soTaiKhoan) {
        this(tenDanhMuc, moTa, "chi", soTaiKhoan);
    }

    // Constructor đầy đủ không có soTaiKhoan
    public DanhMuc(int id, String tenDanhMuc, String moTa) {
        this(id, tenDanhMuc, moTa, "chi", null);
    }

    // Constructor không ID, không soTaiKhoan
    public DanhMuc(String tenDanhMuc, String moTa) {
        this(tenDanhMuc, moTa, "chi", null);
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

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public boolean isLoaiChi() {
        return "chi".equals(loai);
    }

    public boolean isLoaiThu() {
        return "thu".equals(loai);
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
        return tenDanhMuc;
    }
}
