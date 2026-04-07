package com.example.dao;

import com.example.model.NganSach;
import com.example.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NganSachDAO {
    
    // Lấy giới hạn ngân sách của một danh mục trong tháng
    public BigDecimal layGioiHanNganSach(String soTaiKhoan, int danhMucId, int thang, int nam) {
        String sql = "SELECT gioi_han FROM ngan_sach " +
                    "WHERE so_tai_khoan = ? AND danh_muc_id = ? AND thang = ? AND nam = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            pstmt.setInt(2, danhMucId);
            pstmt.setInt(3, thang);
            pstmt.setInt(4, nam);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("gioi_han");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null; // Không có giới hạn
    }
    
    // Kiểm tra ngân sách đã tồn tại chưa
    public boolean kiemTraTonTai(String soTaiKhoan, int danhMucId, int thang, int nam) {
        String sql = "SELECT COUNT(*) FROM ngan_sach " +
                    "WHERE so_tai_khoan = ? AND danh_muc_id = ? AND thang = ? AND nam = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            pstmt.setInt(2, danhMucId);
            pstmt.setInt(3, thang);
            pstmt.setInt(4, nam);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Lấy ngân sách theo người dùng (tháng hiện tại)
    public List<NganSach> layNganSachTheoUser(String soTaiKhoan, int thang, int nam) {
        List<NganSach> danhSachNganSach = new ArrayList<>();
        String sql = "SELECT ns.*, dm.ten_danh_muc " +
                    "FROM ngan_sach ns " +
                    "JOIN danh_muc dm ON ns.danh_muc_id = dm.id " +
                    "WHERE ns.so_tai_khoan = ? AND ns.thang = ? AND ns.nam = ? " +
                    "ORDER BY dm.ten_danh_muc";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            pstmt.setInt(2, thang);
            pstmt.setInt(3, nam);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                NganSach ns = new NganSach(
                    rs.getInt("id"),
                    rs.getInt("danh_muc_id"),
                    rs.getString("so_tai_khoan"),
                    rs.getBigDecimal("gioi_han"),
                    rs.getInt("thang"),
                    rs.getInt("nam")
                );
                ns.setTenDanhMuc(rs.getString("ten_danh_muc"));
                danhSachNganSach.add(ns);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return danhSachNganSach;
    }
    
    // Lấy tất cả ngân sách của user (tất cả tháng)
    public List<NganSach> layTatCaNganSach(String soTaiKhoan) {
        List<NganSach> danhSachNganSach = new ArrayList<>();
        String sql = "SELECT ns.*, dm.ten_danh_muc " +
                    "FROM ngan_sach ns " +
                    "JOIN danh_muc dm ON ns.danh_muc_id = dm.id " +
                    "WHERE ns.so_tai_khoan = ? " +
                    "ORDER BY ns.nam DESC, ns.thang DESC, dm.ten_danh_muc";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                NganSach ns = new NganSach(
                    rs.getInt("id"),
                    rs.getInt("danh_muc_id"),
                    rs.getString("so_tai_khoan"),
                    rs.getBigDecimal("gioi_han"),
                    rs.getInt("thang"),
                    rs.getInt("nam")
                );
                ns.setTenDanhMuc(rs.getString("ten_danh_muc"));
                danhSachNganSach.add(ns);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return danhSachNganSach;
    }
    
    // Thêm ngân sách mới
    public boolean themNganSach(NganSach nganSach) {
        String sql = "INSERT INTO ngan_sach (danh_muc_id, so_tai_khoan, gioi_han, thang, nam) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, nganSach.getDanhMucId());
            pstmt.setString(2, nganSach.getSoTaiKhoan());
            pstmt.setBigDecimal(3, nganSach.getGioiHan());
            pstmt.setInt(4, nganSach.getThang());
            pstmt.setInt(5, nganSach.getNam());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Sửa ngân sách
    public boolean suaNganSach(NganSach nganSach) {
        String sql = "UPDATE ngan_sach SET gioi_han = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBigDecimal(1, nganSach.getGioiHan());
            pstmt.setInt(2, nganSach.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Xóa ngân sách
    public boolean xoaNganSach(int id) {
        String sql = "DELETE FROM ngan_sach WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Lấy số tiền đã chi theo danh mục
    public double layTongChiTheoDanhMuc(String soTaiKhoan, int danhMucId, int thang, int nam) {
        String sql = "SELECT COALESCE(SUM(so_tien), 0) as tong_chi " +
                    "FROM giao_dich " +
                    "WHERE so_tai_khoan_gui = ? AND danh_muc_chi_id = ? " +
                    "AND MONTH(ngay_giao_dich) = ? AND YEAR(ngay_giao_dich) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            pstmt.setInt(2, danhMucId);
            pstmt.setInt(3, thang);
            pstmt.setInt(4, nam);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("tong_chi");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }
}
