package com.example.dao;

import com.example.model.DanhMuc;
import com.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanhMucDAO {

    /**
     * Kiểm tra tên danh mục đã tồn tại cho loại + phạm vi hay chưa.
     *
     * scopeUserSoTaiKhoan:
     * - null: chỉ kiểm tra danh mục mặc định (so_tai_khoan IS NULL)
     * - có giá trị: kiểm tra cả mặc định + danh mục riêng của user đó
     */
    public boolean tonTaiTenDanhMuc(String tenDanhMuc, String loai, String scopeUserSoTaiKhoan) {
        String ten = tenDanhMuc != null ? tenDanhMuc.trim() : "";
        String loaiCheck = (loai == null || loai.isBlank()) ? "chi" : loai.trim().toLowerCase();

        if (ten.isEmpty()) return false;

        String sql;
        if (scopeUserSoTaiKhoan == null) {
            sql = "SELECT 1 FROM danh_muc " +
                  "WHERE loai = ? AND so_tai_khoan IS NULL " +
                  "AND LOWER(TRIM(ten_danh_muc)) = LOWER(TRIM(?)) LIMIT 1";
        } else {
            sql = "SELECT 1 FROM danh_muc " +
                  "WHERE loai = ? AND (so_tai_khoan IS NULL OR so_tai_khoan = ?) " +
                  "AND LOWER(TRIM(ten_danh_muc)) = LOWER(TRIM(?)) LIMIT 1";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loaiCheck);
            if (scopeUserSoTaiKhoan == null) {
                pstmt.setString(2, ten);
            } else {
                pstmt.setString(2, scopeUserSoTaiKhoan);
                pstmt.setString(3, ten);
            }
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Lấy tất cả danh mục (mặc định + riêng của user)
    public List<DanhMuc> layTatCaDanhMuc(String soTaiKhoan) {
        List<DanhMuc> danhSachDanhMuc = new ArrayList<>();
        String sql = "SELECT * FROM danh_muc " +
                    "WHERE so_tai_khoan IS NULL OR so_tai_khoan = ? " +
                    "ORDER BY so_tai_khoan IS NULL DESC, ten_danh_muc";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DanhMuc dm = new DanhMuc(
                    rs.getInt("id"),
                    rs.getString("ten_danh_muc"),
                    rs.getString("mo_ta"),
                    rs.getString("loai") != null ? rs.getString("loai") : "chi",
                    rs.getString("so_tai_khoan")
                );
                danhSachDanhMuc.add(dm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return danhSachDanhMuc;
    }

    // Lấy danh mục theo loại (chi/thu) cho một user (mặc định + riêng)
    public List<DanhMuc> layDanhMucTheoLoai(String soTaiKhoan, String loai) {
        List<DanhMuc> result = new ArrayList<>();
        String sql = "SELECT * FROM danh_muc " +
                    "WHERE loai = ? AND (so_tai_khoan IS NULL OR so_tai_khoan = ?) " +
                    "ORDER BY so_tai_khoan IS NULL DESC, ten_danh_muc";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, loai);
            pstmt.setString(2, soTaiKhoan);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(new DanhMuc(
                    rs.getInt("id"),
                    rs.getString("ten_danh_muc"),
                    rs.getString("mo_ta"),
                    loai,
                    rs.getString("so_tai_khoan")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Lấy ID của danh mục "Thu khác" mặc định (dùng khi nhận tiền auto)
    public Integer layIdThuKhacMacDinh() {
        String sql = "SELECT id FROM danh_muc WHERE ten_danh_muc = 'Thu khác' AND loai = 'thu' AND so_tai_khoan IS NULL LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    
    // Lấy tất cả danh mục (chỉ mặc định - cho backward compatible)
    public List<DanhMuc> layTatCaDanhMuc() {
        List<DanhMuc> danhSachDanhMuc = new ArrayList<>();
        String sql = "SELECT * FROM danh_muc WHERE so_tai_khoan IS NULL ORDER BY ten_danh_muc";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                DanhMuc dm = new DanhMuc(
                    rs.getInt("id"),
                    rs.getString("ten_danh_muc"),
                    rs.getString("mo_ta"),
                    rs.getString("so_tai_khoan")
                );
                danhSachDanhMuc.add(dm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return danhSachDanhMuc;
    }
    
    // Lấy danh mục theo ID
    public DanhMuc layDanhMucTheoId(int id) {
        String sql = "SELECT * FROM danh_muc WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new DanhMuc(
                    rs.getInt("id"),
                    rs.getString("ten_danh_muc"),
                    rs.getString("mo_ta"),
                    rs.getString("loai") != null ? rs.getString("loai") : "chi",
                    rs.getString("so_tai_khoan")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Thêm danh mục mới (hỗ trợ danh mục riêng và loại chi/thu)
    public boolean themDanhMuc(DanhMuc danhMuc) {
        String sql = "INSERT INTO danh_muc (ten_danh_muc, mo_ta, loai, so_tai_khoan) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, danhMuc.getTenDanhMuc());
            pstmt.setString(2, danhMuc.getMoTa());
            pstmt.setString(3, danhMuc.getLoai() != null ? danhMuc.getLoai() : "chi");
            pstmt.setString(4, danhMuc.getSoTaiKhoan());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Sửa danh mục
    public boolean suaDanhMuc(DanhMuc danhMuc) {
        String sql = "UPDATE danh_muc SET ten_danh_muc = ?, mo_ta = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, danhMuc.getTenDanhMuc());
            pstmt.setString(2, danhMuc.getMoTa());
            pstmt.setInt(3, danhMuc.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Xóa danh mục
    public boolean xoaDanhMuc(int id) {
        String sql = "DELETE FROM danh_muc WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Method test để kiểm tra kết nối
    public static void main(String[] args) {
        System.out.println("Testing DanhMucDAO...");
        DanhMucDAO dao = new DanhMucDAO();
        try {
            List<DanhMuc> list = dao.layTatCaDanhMuc();
            System.out.println("Số danh mục: " + list.size());
            for (DanhMuc dm : list) {
                System.out.println("- " + dm.getTenDanhMuc());
            }
        } catch (Exception e) {
            System.err.println("LỖI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
