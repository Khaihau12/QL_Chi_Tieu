package com.example.dao;

import com.example.model.GiaoDich;
import com.example.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO để thao tác với bảng giao_dich (Chuyển tiền)
 */
public class GiaoDichDAO {

    /**
     * Chuyển tiền giữa 2 tài khoản (Transaction có rollback)
     */
    public boolean chuyenTien(String soTaiKhoanGui, String soTaiKhoanNhan, BigDecimal soTien, String noiDung) throws SQLException {
        return chuyenTien(soTaiKhoanGui, soTaiKhoanNhan, soTien, noiDung, null);
    }
    
    /**
     * Chuyển tiền giữa 2 tài khoản với danh mục (Transaction có rollback)
     * Tự động gán danh mục thu "Thu khác" cho người nhận
     */
    public boolean chuyenTien(String soTaiKhoanGui, String soTaiKhoanNhan, BigDecimal soTien, String noiDung, Integer danhMucId) throws SQLException {
        // Lấy ID danh mục "Thu khác" mặc định
        Integer thuKhacId = null;
        try {
            String sqlThuKhac = "SELECT id FROM danh_muc WHERE ten_danh_muc = 'Thu khác' AND loai = 'thu' AND so_tai_khoan IS NULL LIMIT 1";
            try (Connection c = DatabaseConnection.getConnection();
                 Statement st = c.createStatement();
                 ResultSet r = st.executeQuery(sqlThuKhac)) {
                if (r.next()) thuKhacId = r.getInt("id");
            }
        } catch (SQLException ignored) {}

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
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
            
            // 2. Kiểm tra tài khoản nhận hợp lệ (chỉ cho phép tài khoản người dùng)
            String sqlKiemTraNhan = "SELECT so_tai_khoan, vai_tro FROM nguoi_dung WHERE so_tai_khoan = ?";
            PreparedStatement stmtKiemTraNhan = conn.prepareStatement(sqlKiemTraNhan);
            stmtKiemTraNhan.setString(1, soTaiKhoanNhan);
            ResultSet rsNhan = stmtKiemTraNhan.executeQuery();
            
            if (!rsNhan.next()) {
                conn.rollback();
                throw new SQLException("Tài khoản nhận không tồn tại!");
            }
            if (!"nguoi_dung".equals(rsNhan.getString("vai_tro"))) {
                conn.rollback();
                throw new SQLException("Không thể chuyển tiền đến tài khoản quản trị viên!");
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
            
            // 5. Lưu giao dịch với cả danh mục chi (người gửi) và danh mục thu (người nhận)
            String sqlGiaoDich = "INSERT INTO giao_dich (so_tai_khoan_gui, so_tai_khoan_nhan, so_tien, noi_dung, danh_muc_id, danh_muc_thu_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtGiaoDich = conn.prepareStatement(sqlGiaoDich);
            stmtGiaoDich.setString(1, soTaiKhoanGui);
            stmtGiaoDich.setString(2, soTaiKhoanNhan);
            stmtGiaoDich.setBigDecimal(3, soTien);
            stmtGiaoDich.setString(4, noiDung);
            if (danhMucId != null) stmtGiaoDich.setInt(5, danhMucId);
            else stmtGiaoDich.setNull(5, Types.INTEGER);
            if (thuKhacId != null) stmtGiaoDich.setInt(6, thuKhacId);
            else stmtGiaoDich.setNull(6, Types.INTEGER);
            stmtGiaoDich.executeUpdate();
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Lấy lịch sử giao dịch của 1 tài khoản (cả gửi và nhận), JOIN danh mục chi + thu
     */
    public List<GiaoDich> layLichSuGiaoDich(String soTaiKhoan) throws SQLException {
        String sql = "SELECT gd.*, " +
                     "dc.ten_danh_muc AS ten_dm_chi, " +
                     "dt.ten_danh_muc AS ten_dm_thu " +
                     "FROM giao_dich gd " +
                     "LEFT JOIN danh_muc dc ON gd.danh_muc_id    = dc.id " +
                     "LEFT JOIN danh_muc dt ON gd.danh_muc_thu_id = dt.id " +
                     "WHERE gd.so_tai_khoan_gui = ? OR gd.so_tai_khoan_nhan = ? " +
                     "ORDER BY gd.ngay_giao_dich DESC LIMIT 100";
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
                int dmId = rs.getInt("danh_muc_id");
                if (!rs.wasNull()) gd.setDanhMucId(dmId);
                int dmThuId = rs.getInt("danh_muc_thu_id");
                if (!rs.wasNull()) gd.setDanhMucThuId(dmThuId);
                gd.setTenDanhMucChi(rs.getString("ten_dm_chi"));
                gd.setTenDanhMucThu(rs.getString("ten_dm_thu"));
                danhSach.add(gd);
            }
        }
        return danhSach;
    }

    /** Cập nhật danh mục chi của giao dịch (người gửi) */
    public boolean capNhatDanhMucChi(int maGiaoDich, Integer danhMucId) throws SQLException {
        String sql = "UPDATE giao_dich SET danh_muc_id = ? WHERE ma_giao_dich = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (danhMucId != null) stmt.setInt(1, danhMucId);
            else stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, maGiaoDich);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Cập nhật danh mục thu của giao dịch (người nhận) */
    public boolean capNhatDanhMucThu(int maGiaoDich, Integer danhMucThuId) throws SQLException {
        String sql = "UPDATE giao_dich SET danh_muc_thu_id = ? WHERE ma_giao_dich = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (danhMucThuId != null) stmt.setInt(1, danhMucThuId);
            else stmt.setNull(1, Types.INTEGER);
            stmt.setInt(2, maGiaoDich);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Thống kê thu nhập theo danh mục thu trong tháng */
    public Map<String, Double> layThuTheoDanhMuc(String soTaiKhoan, int thang, int nam) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT dm.ten_danh_muc, SUM(gd.so_tien) AS tong_thu " +
                     "FROM giao_dich gd " +
                     "JOIN danh_muc dm ON gd.danh_muc_thu_id = dm.id " +
                     "WHERE gd.so_tai_khoan_nhan = ? " +
                     "AND MONTH(gd.ngay_giao_dich) = ? AND YEAR(gd.ngay_giao_dich) = ? " +
                     "GROUP BY dm.id, dm.ten_danh_muc " +
                     "ORDER BY tong_thu DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soTaiKhoan);
            stmt.setInt(2, thang);
            stmt.setInt(3, nam);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("ten_danh_muc"), rs.getDouble("tong_thu"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
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
     * Đếm tổng số giao dịch trong hệ thống (dùng cho báo cáo admin)
     */
    public long demTatCaGiaoDich() throws SQLException {
        String sql = "SELECT COUNT(*) FROM giao_dich";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    /**
     * Tính tổng số tiền đã lưu chuyển trong hệ thống (dùng cho báo cáo admin)
     */
    public BigDecimal tongSoTienGiaoDich() throws SQLException {
        String sql = "SELECT COALESCE(SUM(so_tien), 0) FROM giao_dich";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Đếm số giao dịch đã gửi của một tài khoản
     */
    public long demGDGui(String soTaiKhoan) throws SQLException {
        String sql = "SELECT COUNT(*) FROM giao_dich WHERE so_tai_khoan_gui = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soTaiKhoan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    /**
     * Đếm số giao dịch đã nhận của một tài khoản
     */
    public long demGDNhan(String soTaiKhoan) throws SQLException {
        String sql = "SELECT COUNT(*) FROM giao_dich WHERE so_tai_khoan_nhan = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soTaiKhoan);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
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

    /**
     * Tìm tên người dùng thường theo số tài khoản (không bao gồm admin)
     */
    public String layTenNguoiDungThuong(String soTaiKhoan) throws SQLException {
        String sql = "SELECT ho_ten FROM nguoi_dung WHERE so_tai_khoan = ? AND vai_tro = 'nguoi_dung'";

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
