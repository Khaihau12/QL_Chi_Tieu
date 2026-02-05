package com.example.dao;

import com.example.model.DanhMuc;
import com.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanhMucDAO {
    
    // Lấy tất cả danh mục (mặc định + riêng của user)
    // Thứ tự: Danh mục mặc định trước → Danh mục riêng sau
    public List<DanhMuc> layTatCaDanhMuc(String soTaiKhoan) {
        List<DanhMuc> danhSachDanhMuc = new ArrayList<>();
        String sql = "SELECT * FROM danh_muc " +
                    "WHERE so_tai_khoan IS NULL OR so_tai_khoan = ? " +
                    "ORDER BY so_tai_khoan IS NULL DESC, ten_danh_muc"; // NULL trước
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DanhMuc dm = new DanhMuc(
                    rs.getInt("id"),
                    rs.getString("ten_danh_muc"),
                    rs.getString("mo_ta"),
                    rs.getString("mau_sac"),
                    rs.getString("so_tai_khoan")
                );
                danhSachDanhMuc.add(dm);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return danhSachDanhMuc;
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
                    rs.getString("mau_sac"),
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
                    rs.getString("mau_sac"),
                    rs.getString("so_tai_khoan")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Thêm danh mục mới (hỗ trợ danh mục riêng)
    public boolean themDanhMuc(DanhMuc danhMuc) {
        String sql = "INSERT INTO danh_muc (ten_danh_muc, mo_ta, mau_sac, so_tai_khoan) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, danhMuc.getTenDanhMuc());
            pstmt.setString(2, danhMuc.getMoTa());
            pstmt.setString(3, danhMuc.getMauSac());
            pstmt.setString(4, danhMuc.getSoTaiKhoan());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Sửa danh mục
    public boolean suaDanhMuc(DanhMuc danhMuc) {
        String sql = "UPDATE danh_muc SET ten_danh_muc = ?, mo_ta = ?, mau_sac = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, danhMuc.getTenDanhMuc());
            pstmt.setString(2, danhMuc.getMoTa());
            pstmt.setString(3, danhMuc.getMauSac());
            pstmt.setInt(4, danhMuc.getId());
            
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
