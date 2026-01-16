package com.example.dao;

import com.example.model.GiaoDich;
import com.example.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO để thao tác với bảng giao_dich (Chuyển tiền)
 */
public class GiaoDichDAO {

    /**
     * Chuyển tiền giữa 2 tài khoản (Transaction có rollback)
     */
    public boolean chuyenTien(String soTaiKhoanGui, String soTaiKhoanNhan, BigDecimal soTien, String noiDung) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction
            
            // 1. Kiểm tra số dư tài khoản gửi
            String sqlKiemTra = "SELECT so_du FROM nguoi_dung WHERE so_tai_khoan = ? FOR UPDATE";
            PreparedStatement stmtKiemTra = conn.prepareStatement(sqlKiemTra);
            stmtKiemTra.setString(1, soTaiKhoanGui);
            ResultSet rs = stmtKiemTra.executeQuery();
            
            if (!rs.next()) {
                conn.rollback();
                throw new SQLException("Tài khoản gửi không tồn tại!");
            }
            
            BigDecimal soDuHienTai = rs.getBigDecimal("so_du");
            if (soDuHienTai.compareTo(soTien) < 0) {
                conn.rollback();
                throw new SQLException("Số dư không đủ! Hiện có: " + soDuHienTai + " đ");
            }
            
            // 2. Kiểm tra tài khoản nhận có tồn tại
            String sqlKiemTraNhan = "SELECT so_tai_khoan FROM nguoi_dung WHERE so_tai_khoan = ?";
            PreparedStatement stmtKiemTraNhan = conn.prepareStatement(sqlKiemTraNhan);
            stmtKiemTraNhan.setString(1, soTaiKhoanNhan);
            ResultSet rsNhan = stmtKiemTraNhan.executeQuery();
            
            if (!rsNhan.next()) {
                conn.rollback();
                throw new SQLException("Tài khoản nhận không tồn tại!");
            }
            
            // 3. Trừ tiền tài khoản gửi
            String sqlTru = "UPDATE nguoi_dung SET so_du = so_du - ? WHERE so_tai_khoan = ?";
            PreparedStatement stmtTru = conn.prepareStatement(sqlTru);
            stmtTru.setBigDecimal(1, soTien);
            stmtTru.setString(2, soTaiKhoanGui);
            stmtTru.executeUpdate();
            
            // 4. Cộng tiền tài khoản nhận
            String sqlCong = "UPDATE nguoi_dung SET so_du = so_du + ? WHERE so_tai_khoan = ?";
            PreparedStatement stmtCong = conn.prepareStatement(sqlCong);
            stmtCong.setBigDecimal(1, soTien);
            stmtCong.setString(2, soTaiKhoanNhan);
            stmtCong.executeUpdate();
            
            // 5. Lưu giao dịch
            String sqlGiaoDich = "INSERT INTO giao_dich (so_tai_khoan_gui, so_tai_khoan_nhan, so_tien, noi_dung) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtGiaoDich = conn.prepareStatement(sqlGiaoDich);
            stmtGiaoDich.setString(1, soTaiKhoanGui);
            stmtGiaoDich.setString(2, soTaiKhoanNhan);
            stmtGiaoDich.setBigDecimal(3, soTien);
            stmtGiaoDich.setString(4, noiDung);
            stmtGiaoDich.executeUpdate();
            
            conn.commit(); // Commit transaction
            return true;
            
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
     * Lấy lịch sử giao dịch của 1 tài khoản (cả gửi và nhận)
     */
    public List<GiaoDich> layLichSuGiaoDich(String soTaiKhoan) throws SQLException {
        String sql = "SELECT * FROM giao_dich WHERE so_tai_khoan_gui = ? OR so_tai_khoan_nhan = ? ORDER BY ngay_giao_dich DESC LIMIT 100";
        List<GiaoDich> danhSach = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, soTaiKhoan);
            stmt.setString(2, soTaiKhoan);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GiaoDich gd = new GiaoDich();
                gd.setMaGiaoDich(rs.getInt("ma_giao_dich"));
                gd.setSoTaiKhoanGui(rs.getString("so_tai_khoan_gui"));
                gd.setSoTaiKhoanNhan(rs.getString("so_tai_khoan_nhan"));
                gd.setSoTien(rs.getBigDecimal("so_tien"));
                gd.setNoiDung(rs.getString("noi_dung"));
                gd.setNgayGiaoDich(rs.getTimestamp("ngay_giao_dich"));
                gd.setTrangThai(rs.getString("trang_thai"));
                danhSach.add(gd);
            }
        }
        return danhSach;
    }

    /**
     * Lấy số dư hiện tại của tài khoản
     */
    public BigDecimal laySoDu(String soTaiKhoan) throws SQLException {
        String sql = "SELECT so_du FROM nguoi_dung WHERE so_tai_khoan = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, soTaiKhoan);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBigDecimal("so_du");
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Tìm tên người dùng theo số tài khoản
     */
    public String layTenNguoiDung(String soTaiKhoan) throws SQLException {
        String sql = "SELECT ho_ten FROM nguoi_dung WHERE so_tai_khoan = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, soTaiKhoan);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("ho_ten");
            }
        }
        return null;
    }
}
