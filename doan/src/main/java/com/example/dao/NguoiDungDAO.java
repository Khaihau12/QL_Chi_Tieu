package com.example.dao;

import com.example.exception.TaiKhoanBiKhoaException;
import com.example.model.NguoiDung;
import com.example.util.DatabaseConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * DAO để thao tác với bảng nguoi_dung
 */
public class NguoiDungDAO {

    // Đăng nhập
    public NguoiDung dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        // Kiểm tra credentials (không lọc trang_thai để phân biệt sai mk vs bị khóa)
        String sql = "SELECT * FROM nguoi_dung WHERE ten_dang_nhap = ? AND mat_khau = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tenDangNhap);
            stmt.setString(2, hashMD5(matKhau));
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                NguoiDung nd = mapResultSetToNguoiDung(rs);
                // Kiểm tra tài khoản bị khóa
                if ("bi_khoa".equals(nd.getTrangThai())) {
                    // Kiểm tra nếu hết thời gian khóa thì tự mở khóa
                    if (nd.getThoiGianMoKhoa() != null &&
                            nd.getThoiGianMoKhoa().toLocalDateTime().isBefore(LocalDateTime.now())) {
                        // Hết hạn khóa → tự mở khóa
                        capNhatTrangThai(nd.getMaNguoiDung(), "hoat_dong", null);
                        nd.setTrangThai("hoat_dong");
                        nd.setLyDoKhoa(null);
                        nd.setThoiGianMoKhoa(null);
                    } else {
                        throw new TaiKhoanBiKhoaException(nd.getLyDoKhoa(), nd.getThoiGianMoKhoa());
                    }
                }
                // Cập nhật lần đăng nhập cuối
                capNhatLanDangNhapCuoi(nd.getMaNguoiDung());
                return nd;
            }
        }
        return null; // sai tên đăng nhập hoặc mật khẩu
    }

    // Đăng ký (Tự động tạo số tài khoản)
    public boolean dangKy(NguoiDung nguoiDung) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Tạo số tài khoản mới (10 chữ số bắt đầu từ 1000000001)
            String soTaiKhoan = taoSoTaiKhoanMoi(conn);
            
            String sql = "INSERT INTO nguoi_dung (so_tai_khoan, ten_dang_nhap, email, mat_khau, ho_ten, so_du, so_du_tien_mat, vai_tro) " +
                         "VALUES (?, ?, ?, ?, ?, 0, 0, 'nguoi_dung')";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, soTaiKhoan);
            stmt.setString(2, nguoiDung.getTenDangNhap());
            stmt.setString(3, nguoiDung.getEmail());
            stmt.setString(4, hashMD5(nguoiDung.getMatKhau())); // Mã hóa MD5
            stmt.setString(5, nguoiDung.getHoTen());
            
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

    // Lấy user thường theo số tài khoản (dùng cho Admin xác nhận trước khi nạp tiền)
    public NguoiDung layNguoiDungThuongTheoSoTaiKhoan(String soTaiKhoan) throws SQLException {
        String sql = "SELECT * FROM nguoi_dung WHERE so_tai_khoan = ? AND vai_tro = 'nguoi_dung'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, soTaiKhoan);
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

    // Đổi mật khẩu (kiểm tra mật khẩu cũ trước)
    public boolean doiMatKhau(int maNguoiDung, String matKhauCu, String matKhauMoi) throws SQLException {
        // Kiểm tra mật khẩu cũ
        String sqlCheck = "SELECT ma_nguoi_dung FROM nguoi_dung WHERE ma_nguoi_dung = ? AND mat_khau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
            stmt.setInt(1, maNguoiDung);
            stmt.setString(2, hashMD5(matKhauCu));
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return false; // mật khẩu cũ sai
        }
        // Cập nhật mật khẩu mới
        String sqlUpdate = "UPDATE nguoi_dung SET mat_khau = ? WHERE ma_nguoi_dung = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
            stmt.setString(1, hashMD5(matKhauMoi));
            stmt.setInt(2, maNguoiDung);
            return stmt.executeUpdate() > 0;
        }
    }

    // Admin đặt lại mật khẩu cho user (không cần mật khẩu cũ)
    public boolean datLaiMatKhauChoUser(int maNguoiDung, String matKhauMoi) throws SQLException {
        if (matKhauMoi == null || matKhauMoi.length() < 6) return false;

        String sql = "UPDATE nguoi_dung SET mat_khau = ? " +
                     "WHERE ma_nguoi_dung = ? AND vai_tro = 'nguoi_dung'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashMD5(matKhauMoi));
            stmt.setInt(2, maNguoiDung);
            return stmt.executeUpdate() > 0;
        }
    }

    // Khóa tài khoản có lý do và thời gian (admin)
    public boolean capNhatTrangThai(int maNguoiDung, String trangThai, String lyDoKhoa, Timestamp thoiGianMoKhoa) throws SQLException {
        String sql = "UPDATE nguoi_dung SET trang_thai = ?, ly_do_khoa = ?, thoi_gian_mo_khoa = ? WHERE ma_nguoi_dung = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trangThai);
            if (lyDoKhoa != null) stmt.setString(2, lyDoKhoa);
            else stmt.setNull(2, Types.VARCHAR);
            if (thoiGianMoKhoa != null) stmt.setTimestamp(3, thoiGianMoKhoa);
            else stmt.setNull(3, Types.TIMESTAMP);
            stmt.setInt(4, maNguoiDung);
            return stmt.executeUpdate() > 0;
        }
    }

    // Khóa tài khoản có lý do (admin) - không có thời gian
    public boolean capNhatTrangThai(int maNguoiDung, String trangThai, String lyDoKhoa) throws SQLException {
        return capNhatTrangThai(maNguoiDung, trangThai, lyDoKhoa, null);
    }

    // Mở khóa tài khoản (xóa lý do khóa)
    public boolean capNhatTrangThai(int maNguoiDung, String trangThai) throws SQLException {
        return capNhatTrangThai(maNguoiDung, trangThai, null, null);
    }

    // Admin nạp tiền trực tiếp vào tài khoản người dùng
    public boolean napTienVaoTaiKhoan(int maNguoiDung, java.math.BigDecimal soTien) throws SQLException {
        if (soTien == null || soTien.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return false;
        }

        String sql = "UPDATE nguoi_dung " +
                     "SET so_du = so_du + ? " +
                     "WHERE ma_nguoi_dung = ? AND vai_tro = 'nguoi_dung'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, soTien);
            stmt.setInt(2, maNguoiDung);
            return stmt.executeUpdate() > 0;
        }
    }

    // Admin nạp tiền + ghi lịch sử giao dịch nhận tiền cho user (atomic transaction)
    public boolean napTienVaoTaiKhoanVaTaoGiaoDich(String soTaiKhoanGui, int maNguoiDungNhan,
                                                   java.math.BigDecimal soTien, String noiDung) throws SQLException {
        if (soTaiKhoanGui == null || soTaiKhoanGui.isBlank()) return false;
        if (soTien == null || soTien.compareTo(java.math.BigDecimal.ZERO) <= 0) return false;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1) Lock user nhận để đảm bảo nhất quán
            String sqlLayNguoiNhan = "SELECT so_tai_khoan, vai_tro FROM nguoi_dung WHERE ma_nguoi_dung = ? FOR UPDATE";
            String soTaiKhoanNhan;
            try (PreparedStatement stmt = conn.prepareStatement(sqlLayNguoiNhan)) {
                stmt.setInt(1, maNguoiDungNhan);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }
                if (!"nguoi_dung".equals(rs.getString("vai_tro"))) {
                    conn.rollback();
                    return false;
                }
                soTaiKhoanNhan = rs.getString("so_tai_khoan");
            }

            // 2) Cộng số dư cho user nhận
            String sqlCongTien = "UPDATE nguoi_dung SET so_du = so_du + ? WHERE ma_nguoi_dung = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCongTien)) {
                stmt.setBigDecimal(1, soTien);
                stmt.setInt(2, maNguoiDungNhan);
                if (stmt.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 3) Lấy danh mục thu mặc định 'Thu khác' (nếu có)
            Integer thuKhacId = null;
            String sqlThuKhac = "SELECT id FROM danh_muc WHERE ten_danh_muc = 'Thu khác' AND loai = 'thu' AND so_tai_khoan IS NULL LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sqlThuKhac);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) thuKhacId = rs.getInt("id");
            }

            // 4) Ghi lịch sử giao dịch: user nhận sẽ thấy dạng 'Nhận'
            String sqlInsertGD = "INSERT INTO giao_dich " +
                                 "(so_tai_khoan_gui, so_tai_khoan_nhan, so_tien, noi_dung, danh_muc_id, danh_muc_thu_id, trang_thai) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, 'thanh_cong')";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsertGD)) {
                stmt.setString(1, soTaiKhoanGui);
                stmt.setString(2, soTaiKhoanNhan);
                stmt.setBigDecimal(3, soTien);
                stmt.setString(4, (noiDung == null || noiDung.isBlank()) ? "Ngân hàng chuyển tiền" : noiDung.trim());
                stmt.setNull(5, Types.INTEGER); // không dùng danh mục chi cho luồng nạp tiền
                if (thuKhacId != null) stmt.setInt(6, thuKhacId);
                else stmt.setNull(6, Types.INTEGER);
                if (stmt.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) conn.rollback();
            throw ex;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
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
        nd.setSoDuTienMat(getBigDecimalIfColumnExists(rs, "so_du_tien_mat"));
        nd.setVaiTro(rs.getString("vai_tro"));
        nd.setTrangThai(rs.getString("trang_thai"));
        nd.setLyDoKhoa(rs.getString("ly_do_khoa"));
        nd.setThoiGianMoKhoa(rs.getTimestamp("thoi_gian_mo_khoa"));
        nd.setLanDangNhapCuoi(rs.getTimestamp("lan_dang_nhap_cuoi"));
        nd.setNgayTao(rs.getTimestamp("ngay_tao"));
        return nd;
    }

    private java.math.BigDecimal getBigDecimalIfColumnExists(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(meta.getColumnLabel(i)) || columnName.equalsIgnoreCase(meta.getColumnName(i))) {
                java.math.BigDecimal value = rs.getBigDecimal(columnName);
                return value != null ? value : java.math.BigDecimal.ZERO;
            }
        }
        return java.math.BigDecimal.ZERO;
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
