package com.example.dao;

import com.example.model.GiaoDich;
import com.example.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO để thao tác với bảng giao_dich (Chuyển tiền)
 */
public class GiaoDichDAO {

    public static final String TIEN_MAT_CHI_PREFIX = "[TIEN_MAT_CHI]";
    public static final String TIEN_MAT_THU_PREFIX = "[TIEN_MAT_THU]";

    public static boolean isGiaoDichTienMatChi(String noiDung) {
        return noiDung != null && noiDung.startsWith(TIEN_MAT_CHI_PREFIX);
    }

    public static boolean isGiaoDichTienMatThu(String noiDung) {
        return noiDung != null && noiDung.startsWith(TIEN_MAT_THU_PREFIX);
    }

    public static String boPrefixTienMat(String noiDung) {
        if (noiDung == null) return "";
        if (isGiaoDichTienMatChi(noiDung)) {
            return noiDung.substring(TIEN_MAT_CHI_PREFIX.length()).trim();
        }
        if (isGiaoDichTienMatThu(noiDung)) {
            return noiDung.substring(TIEN_MAT_THU_PREFIX.length()).trim();
        }
        return noiDung;
    }

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
            String sqlGiaoDich = "INSERT INTO giao_dich (so_tai_khoan_gui, so_tai_khoan_nhan, so_tien, noi_dung, danh_muc_chi_id, danh_muc_thu_id) VALUES (?, ?, ?, ?, ?, ?)";
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
        return layLichSuGiaoDich(soTaiKhoan, null, null, null);
    }

    /**
     * Lấy lịch sử giao dịch theo tháng/năm (mới nhất trước)
     */
    public List<GiaoDich> layLichSuGiaoDichTheoThang(String soTaiKhoan, int thang, int nam) throws SQLException {
        return layLichSuGiaoDich(soTaiKhoan, thang, nam, null);
    }

    /**
     * Lấy lịch sử giao dịch theo ngày cụ thể (mới nhất trước)
     */
    public List<GiaoDich> layLichSuGiaoDichTheoNgay(String soTaiKhoan, LocalDate ngay) throws SQLException {
        return layLichSuGiaoDich(soTaiKhoan, null, null, ngay);
    }

    private List<GiaoDich> layLichSuGiaoDich(String soTaiKhoan, Integer thang, Integer nam, LocalDate ngay) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT gd.*, " +
                        "dc.ten_danh_muc AS ten_dm_chi, " +
                        "dt.ten_danh_muc AS ten_dm_thu " +
                        "FROM giao_dich gd " +
                        "LEFT JOIN danh_muc dc ON gd.danh_muc_chi_id = dc.id " +
                        "LEFT JOIN danh_muc dt ON gd.danh_muc_thu_id = dt.id " +
                        "WHERE (gd.so_tai_khoan_gui = ? OR gd.so_tai_khoan_nhan = ?) ");

        if (ngay != null) {
            sql.append("AND DATE(gd.ngay_giao_dich) = ? ");
        } else if (thang != null && nam != null) {
            sql.append("AND MONTH(gd.ngay_giao_dich) = ? AND YEAR(gd.ngay_giao_dich) = ? ");
        }
        sql.append("ORDER BY gd.ngay_giao_dich DESC");

        List<GiaoDich> danhSach = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            stmt.setString(idx++, soTaiKhoan);
            stmt.setString(idx++, soTaiKhoan);

            if (ngay != null) {
                stmt.setDate(idx, Date.valueOf(ngay));
            } else if (thang != null && nam != null) {
                stmt.setInt(idx++, thang);
                stmt.setInt(idx, nam);
            }
            
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
                int dmId = rs.getInt("danh_muc_chi_id");
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
        String sql = "UPDATE giao_dich SET danh_muc_chi_id = ? WHERE ma_giao_dich = ?";
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

    /** Tổng quan chi/thu theo tháng của 1 tài khoản */
    public Map<String, Double> layTongQuanTheoThang(String soTaiKhoan, int thang, int nam) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT " +
                "SUM(CASE WHEN so_tai_khoan_gui = ? THEN so_tien ELSE 0 END) AS tong_chi, " +
                "SUM(CASE WHEN so_tai_khoan_nhan = ? THEN so_tien ELSE 0 END) AS tong_thu " +
                "FROM giao_dich " +
                "WHERE (so_tai_khoan_gui = ? OR so_tai_khoan_nhan = ?) " +
                "AND MONTH(ngay_giao_dich) = ? AND YEAR(ngay_giao_dich) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soTaiKhoan);
            stmt.setString(2, soTaiKhoan);
            stmt.setString(3, soTaiKhoan);
            stmt.setString(4, soTaiKhoan);
            stmt.setInt(5, thang);
            stmt.setInt(6, nam);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result.put("chi", rs.getDouble("tong_chi"));
                result.put("thu", rs.getDouble("tong_thu"));
            } else {
                result.put("chi", 0.0);
                result.put("thu", 0.0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("chi", 0.0);
            result.put("thu", 0.0);
        }

        return result;
    }

    /** Thống kê chi tiêu theo danh mục chi trong tháng */
    public Map<String, Double> layChiTheoDanhMuc(String soTaiKhoan, int thang, int nam) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT dm.ten_danh_muc, SUM(gd.so_tien) AS tong_chi " +
                "FROM giao_dich gd " +
                "JOIN danh_muc dm ON gd.danh_muc_chi_id = dm.id " +
                "WHERE gd.so_tai_khoan_gui = ? " +
                "AND MONTH(gd.ngay_giao_dich) = ? AND YEAR(gd.ngay_giao_dich) = ? " +
                "GROUP BY dm.id, dm.ten_danh_muc " +
                "ORDER BY tong_chi DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soTaiKhoan);
            stmt.setInt(2, thang);
            stmt.setInt(3, nam);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("ten_danh_muc"), rs.getDouble("tong_chi"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
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
     * Tách nguồn thu theo tháng:
     * - thu_tien_mat: thu do người dùng tự ghi nhận tiền mặt
     * - thu_chuyen_khoan: thu do người khác chuyển khoản
     */
    public Map<String, Double> layTongThuTheoNguon(String soTaiKhoan, int thang, int nam) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT " +
                "COALESCE(SUM(CASE WHEN so_tai_khoan_nhan = ? AND noi_dung LIKE ? THEN so_tien ELSE 0 END), 0) AS thu_tien_mat, " +
                "COALESCE(SUM(CASE WHEN so_tai_khoan_nhan = ? AND (noi_dung IS NULL OR noi_dung NOT LIKE ?) THEN so_tien ELSE 0 END), 0) AS thu_chuyen_khoan " +
                "FROM giao_dich " +
                "WHERE so_tai_khoan_nhan = ? AND MONTH(ngay_giao_dich) = ? AND YEAR(ngay_giao_dich) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String marker = TIEN_MAT_THU_PREFIX + "%";
            stmt.setString(1, soTaiKhoan);
            stmt.setString(2, marker);
            stmt.setString(3, soTaiKhoan);
            stmt.setString(4, marker);
            stmt.setString(5, soTaiKhoan);
            stmt.setInt(6, thang);
            stmt.setInt(7, nam);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result.put("thu_tien_mat", rs.getDouble("thu_tien_mat"));
                result.put("thu_chuyen_khoan", rs.getDouble("thu_chuyen_khoan"));
            } else {
                result.put("thu_tien_mat", 0.0);
                result.put("thu_chuyen_khoan", 0.0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("thu_tien_mat", 0.0);
            result.put("thu_chuyen_khoan", 0.0);
        }
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
     * Lấy số dư ví tiền mặt hiện tại của tài khoản
     */
    public BigDecimal laySoDuTienMat(String soTaiKhoan) throws SQLException {
        String sql = "SELECT so_du_tien_mat FROM nguoi_dung WHERE so_tai_khoan = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, soTaiKhoan);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                BigDecimal soDuTienMat = rs.getBigDecimal("so_du_tien_mat");
                return soDuTienMat != null ? soDuTienMat : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Đếm tổng số giao dịch CHUYỂN KHOẢN trong hệ thống (dùng cho báo cáo admin)
     * Loại trừ các giao dịch tiền mặt nội bộ có marker [TIEN_MAT_*].
     */
    public long demTatCaGiaoDich() throws SQLException {
        String sql = "SELECT COUNT(*) FROM giao_dich " +
                "WHERE noi_dung IS NULL OR (noi_dung NOT LIKE ? AND noi_dung NOT LIKE ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, TIEN_MAT_CHI_PREFIX + "%");
            stmt.setString(2, TIEN_MAT_THU_PREFIX + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    /**
     * Tính tổng số tiền chuyển khoản đã lưu chuyển trong hệ thống (dùng cho báo cáo admin)
     * Loại trừ các giao dịch tiền mặt nội bộ có marker [TIEN_MAT_*].
     */
    public BigDecimal tongSoTienGiaoDich() throws SQLException {
        String sql = "SELECT COALESCE(SUM(so_tien), 0) FROM giao_dich " +
                "WHERE noi_dung IS NULL OR (noi_dung NOT LIKE ? AND noi_dung NOT LIKE ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, TIEN_MAT_CHI_PREFIX + "%");
            stmt.setString(2, TIEN_MAT_THU_PREFIX + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getBigDecimal(1);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Đếm số giao dịch CHUYỂN KHOẢN đã gửi của một tài khoản
     */
    public long demGDGui(String soTaiKhoan) throws SQLException {
        String sql = "SELECT COUNT(*) FROM giao_dich " +
                "WHERE so_tai_khoan_gui = ? " +
                "AND (noi_dung IS NULL OR (noi_dung NOT LIKE ? AND noi_dung NOT LIKE ?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soTaiKhoan);
            stmt.setString(2, TIEN_MAT_CHI_PREFIX + "%");
            stmt.setString(3, TIEN_MAT_THU_PREFIX + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong(1);
        }
        return 0;
    }

    /**
     * Đếm số giao dịch CHUYỂN KHOẢN đã nhận của một tài khoản
     */
    public long demGDNhan(String soTaiKhoan) throws SQLException {
        String sql = "SELECT COUNT(*) FROM giao_dich " +
                "WHERE so_tai_khoan_nhan = ? " +
                "AND (noi_dung IS NULL OR (noi_dung NOT LIKE ? AND noi_dung NOT LIKE ?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, soTaiKhoan);
            stmt.setString(2, TIEN_MAT_CHI_PREFIX + "%");
            stmt.setString(3, TIEN_MAT_THU_PREFIX + "%");
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

    /**
     * Ghi nhận giao dịch tiền mặt (không chuyển khoản cho người khác) để thống kê thu/chi.
     * loai = "chi" hoặc "thu"
     */
    public boolean ghiNhanTienMat(String soTaiKhoanUser, BigDecimal soTien, String noiDung,
                                  String loai, Integer danhMucId) throws SQLException {
        if (soTaiKhoanUser == null || soTaiKhoanUser.isBlank()) {
            throw new SQLException("Không xác định được tài khoản người dùng!");
        }
        if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
            throw new SQLException("Số tiền phải lớn hơn 0!");
        }
        if (!("chi".equals(loai) || "thu".equals(loai))) {
            throw new SQLException("Loại giao dịch không hợp lệ!");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) Khóa tài khoản user để cập nhật số dư ví tiền mặt an toàn
            BigDecimal soDuHienTai;
            String sqlLockUser = "SELECT so_du_tien_mat FROM nguoi_dung WHERE so_tai_khoan = ? FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLockUser)) {
                stmt.setString(1, soTaiKhoanUser);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    throw new SQLException("Tài khoản người dùng không tồn tại!");
                }
                soDuHienTai = rs.getBigDecimal("so_du_tien_mat");
                if (soDuHienTai == null) {
                    soDuHienTai = BigDecimal.ZERO;
                }
            }

            if ("chi".equals(loai) && soDuHienTai.compareTo(soTien) < 0) {
                conn.rollback();
                throw new SQLException("Số dư không đủ để ghi nhận khoản chi!");
            }

                // 2) Cập nhật ví tiền mặt user
                String sqlCapNhatSoDu = "UPDATE nguoi_dung SET so_du_tien_mat = so_du_tien_mat " +
                    ("chi".equals(loai) ? "-" : "+") + " ? WHERE so_tai_khoan = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCapNhatSoDu)) {
                stmt.setBigDecimal(1, soTien);
                stmt.setString(2, soTaiKhoanUser);
                stmt.executeUpdate();
            }

            // 3) Lấy STK admin làm tài khoản đối ứng kỹ thuật để tránh đếm thu/chi bị nhân đôi
            String soTaiKhoanAdmin = null;
            String sqlAdmin = "SELECT so_tai_khoan FROM nguoi_dung WHERE vai_tro = 'quan_ly' ORDER BY ma_nguoi_dung LIMIT 1";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlAdmin)) {
                if (rs.next()) {
                    soTaiKhoanAdmin = rs.getString("so_tai_khoan");
                }
            }
            if (soTaiKhoanAdmin == null || soTaiKhoanAdmin.isBlank()) {
                conn.rollback();
                throw new SQLException("Không tìm thấy tài khoản Admin để ghi nhận giao dịch!");
            }

            // 4) Lưu giao dịch tiền mặt
            String soTaiKhoanGui = "chi".equals(loai) ? soTaiKhoanUser : soTaiKhoanAdmin;
            String soTaiKhoanNhan = "chi".equals(loai) ? soTaiKhoanAdmin : soTaiKhoanUser;
            String noiDungPrefix = "chi".equals(loai) ? TIEN_MAT_CHI_PREFIX : TIEN_MAT_THU_PREFIX;
            String noiDungDayDu = noiDungPrefix + (noiDung != null && !noiDung.isBlank() ? (" " + noiDung.trim()) : "");

            String sqlInsert = "INSERT INTO giao_dich " +
                    "(so_tai_khoan_gui, so_tai_khoan_nhan, so_tien, noi_dung, danh_muc_chi_id, danh_muc_thu_id, trang_thai) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'thanh_cong')";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                stmt.setString(1, soTaiKhoanGui);
                stmt.setString(2, soTaiKhoanNhan);
                stmt.setBigDecimal(3, soTien);
                stmt.setString(4, noiDungDayDu);

                if ("chi".equals(loai)) {
                    if (danhMucId != null) stmt.setInt(5, danhMucId);
                    else stmt.setNull(5, Types.INTEGER);
                    stmt.setNull(6, Types.INTEGER);
                } else {
                    stmt.setNull(5, Types.INTEGER);
                    if (danhMucId != null) stmt.setInt(6, danhMucId);
                    else stmt.setNull(6, Types.INTEGER);
                }
                stmt.executeUpdate();
            }

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
}
