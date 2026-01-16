package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Utility Class - Đơn giản cho đồ án
 * Kết nối MySQL bằng JDBC
 */
public class DatabaseConnection {
    
    // Thông tin kết nối
    private static final String URL = "jdbc:mysql://localhost:3306/QLChiTieu?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // XAMPP mặc định không có mật khẩu
    
    /**
     * Lấy connection đến database
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Tạo và trả về connection
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver không tìm thấy!");
            e.printStackTrace();
            throw new SQLException("Driver không tìm thấy", e);
        }
    }
    
    /**
     * Test kết nối database
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Kết nối MySQL thành công!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối database!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Đóng connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
