package com.example.dao;

import com.example.model.NguoiDung;
import com.example.util.DatabaseConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO để thao tác với bảng nguoi_dung
 */
public class NguoiDungDAO {

    // Đăng nhập
    public NguoiDung dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT * FROM nguoi_dung WHERE ten_dang_nhap = ? AND mat_khau = ? AND trang_thai = 'hoat_dong'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tenDangNhap);
            stmt.setString(2, hashMD5(matKhau)); // Mã hóa MD5
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Cập nhật lần đăng nhập cuối
                capNhatLanDangNhapCuoi(rs.getInt("ma_nguoi_dung"));
                
                return mapResultSetToNguoiDung(rs);
            }
        }
        return null;
    }

    // Đăng ký (Tự động tạo số tài khoản)
    public boolean dangKy(NguoiDung nguoiDung) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Tạo số tài khoản mới (10 chữ số bắt đầu từ 1000000001)
            String soTaiKhoan = taoSoTaiKhoanMoi(conn);
            
            String sql = "INSERT INTO nguoi_dung (so_tai_khoan, ten_dang_nhap, email, mat_khau, ho_ten, so_dien_thoai, so_du, vai_tro) " +
                         "VALUES (?, ?, ?, ?, ?, ?, 0, 'nguoi_dung')";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, soTaiKhoan);
            stmt.setString(2, nguoiDung.getTenDangNhap());
            stmt.setString(3, nguoiDung.getEmail());
            stmt.setString(4, hashMD5(nguoiDung.getMatKhau())); // Mã hóa MD5
            stmt.setString(5, nguoiDung.getHoTen());
            stmt.setString(6, nguoiDung.getEmail()); // so_dien_thoai tạm để email
            
            int result = stmt.executeUpdate();
            conn.commit();
            return result > 0;
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    
    /**
     * Tạo số tài khoản mới tự động (10 chữ số, bắt đầu từ 1000000001)
     */
    private String taoSoTaiKhoanMoi(Connection conn) throws SQLException {
        String sql = "SELECT MAX(CAST(so_tai_khoan AS UNSIGNED)) AS max_stk FROM nguoi_dung";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        long maxSoTaiKhoan = 1000000000L; // Số tài khoản bắt đầu
        if (rs.next() && rs.getLong("max_stk") > 0) {
            maxSoTaiKhoan = rs.getLong("max_stk");
        }
        
        return String.valueOf(maxSoTaiKhoan + 1);
    }

    // Cập nhật lần đăng nhập cuối
    private void capNhatLanDangNhapCuoi(int maNguoiDung) throws SQLException {
        String sql = "UPDATE nguoi_dung SET lan_dang_nhap_cuoi = NOW() WHERE ma_nguoi_dung = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maNguoiDung);
            stmt.executeUpdate();
        }
    }

    // Lấy thông tin người dùng theo ID
    public NguoiDung layTheoId(int maNguoiDung) throws SQLException {
        String sql = "SELECT * FROM nguoi_dung WHERE ma_nguoi_dung = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maNguoiDung);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToNguoiDung(rs);
            }
        }
        return null;
    }

    // Lấy tất cả người dùng (cho admin)
    public List<NguoiDung> layTatCa() throws SQLException {
        String sql = "SELECT * FROM nguoi_dung ORDER BY ngay_tao DESC";
        List<NguoiDung> danhSach = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                danhSach.add(mapResultSetToNguoiDung(rs));
            }
        }
        return danhSach;
    }

    // Khóa/Mở khóa tài khoản (admin)
    public boolean capNhatTrangThai(int maNguoiDung, String trangThai) throws SQLException {
        String sql = "UPDATE nguoi_dung SET trang_thai = ? WHERE ma_nguoi_dung = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, trangThai);
            stmt.setInt(2, maNguoiDung);
            
            return stmt.executeUpdate() > 0;
        }
    }

    // Map ResultSet sang đối tượng NguoiDung
    private NguoiDung mapResultSetToNguoiDung(ResultSet rs) throws SQLException {
        NguoiDung nd = new NguoiDung();
        nd.setMaNguoiDung(rs.getInt("ma_nguoi_dung"));
        nd.setSoTaiKhoan(rs.getString("so_tai_khoan"));
        nd.setTenDangNhap(rs.getString("ten_dang_nhap"));
        nd.setMatKhau(rs.getString("mat_khau"));
        nd.setHoTen(rs.getString("ho_ten"));
        nd.setEmail(rs.getString("email"));
        nd.setSoDu(rs.getBigDecimal("so_du"));
        nd.setVaiTro(rs.getString("vai_tro"));
        nd.setTrangThai(rs.getString("trang_thai"));
        nd.setLanDangNhapCuoi(rs.getTimestamp("lan_dang_nhap_cuoi"));
        nd.setNgayTao(rs.getTimestamp("ngay_tao"));
        return nd;
    }
    
    // Mã hóa MD5
    private String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
