package com.example.controller;

import com.example.dao.GiaoDichDAO;
import com.example.dao.DanhMucDAO;
import com.example.model.DanhMuc;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.util.DatabaseConnection;

public class StatisticsController {
    private Stage stage;
    private Scene scene;
    private String soTaiKhoan;
    
    private ComboBox<Integer> cbThang;
    private ComboBox<Integer> cbNam;
    private VBox chartBox;
    private Label lblTongChi;
    private Label lblTongThu;
    private GiaoDichDAO giaoDichDAO;
    private DanhMucDAO danhMucDAO;
    
    public StatisticsController(Stage stage, String soTaiKhoan) {
        this.stage = stage;
        this.soTaiKhoan = soTaiKhoan;
        this.giaoDichDAO = new GiaoDichDAO();
        this.danhMucDAO = new DanhMucDAO();
        
        createUI();
    }
    
    private void createUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Label title = new Label("THỐNG KÊ CHI TIÊU");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Bộ lọc tháng/năm
        HBox filterBox = createFilterBox();
        
        // Tổng quan
        HBox summaryBox = createSummaryBox();
        
        // Biểu đồ (đơn giản bằng thanh ngang)
        Label lblChart = new Label("Chi tiêu theo danh mục:");
        lblChart.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        chartBox = new VBox(10);
        chartBox.setPadding(new Insets(10));
        chartBox.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-background-color: white;");
        VBox.setVgrow(chartBox, Priority.ALWAYS);
        
        // Nút quay lại
        Button btnQuayLai = new Button("Quay Lại");
        btnQuayLai.setOnAction(e -> {
            DashboardController dashboard = new DashboardController(stage);
            stage.setScene(dashboard.getScene());
        });
        
        root.getChildren().addAll(title, filterBox, summaryBox, lblChart, chartBox, btnQuayLai);
        
        scene = new Scene(root, 800, 600);
        loadData();
    }
    
    private HBox createFilterBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        
        Label lblThang = new Label("Tháng:");
        cbThang = new ComboBox<>();
        for (int i = 1; i <= 12; i++) {
            cbThang.getItems().add(i);
        }
        cbThang.setValue(LocalDate.now().getMonthValue());
        
        Label lblNam = new Label("Năm:");
        cbNam = new ComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear + 2; i++) {
            cbNam.getItems().add(i);
        }
        cbNam.setValue(currentYear);
        
        // Tự động load khi thay đổi tháng/năm
        cbThang.setOnAction(e -> loadData());
        cbNam.setOnAction(e -> loadData());
        
        box.getChildren().addAll(lblThang, cbThang, lblNam, cbNam);
        return box;
    }
    
    private HBox createSummaryBox() {
        HBox box = new HBox(40);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5;");
        
        lblTongChi = new Label("Tổng Chi: 0 VNĐ");
        lblTongChi.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-font-weight: bold;");
        
        lblTongThu = new Label("Tổng Thu: 0 VNĐ");
        lblTongThu.setStyle("-fx-font-size: 16px; -fx-text-fill: green; -fx-font-weight: bold;");
        
        box.getChildren().addAll(lblTongChi, lblTongThu);
        return box;
    }
    
    private void loadData() {
        int thang = cbThang.getValue();
        int nam = cbNam.getValue();
        
        // Tính tổng chi và tổng thu
        Map<String, Double> summary = layTongQuanTheoThang(thang, nam);
        double tongChi = summary.getOrDefault("chi", 0.0);
        double tongThu = summary.getOrDefault("thu", 0.0);
        
        lblTongChi.setText(String.format("Tổng Chi: %,.0f VNĐ", tongChi));
        lblTongThu.setText(String.format("Tổng Thu: %,.0f VNĐ", tongThu));
        
        // Lấy chi tiêu theo danh mục
        Map<String, Double> chiTheoDanhMuc = layChiTheoDanhMuc(thang, nam);
        
        // Vẽ biểu đồ đơn giản
        chartBox.getChildren().clear();
        
        if (chiTheoDanhMuc.isEmpty()) {
            Label lblEmpty = new Label("Không có dữ liệu trong tháng này");
            lblEmpty.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
            chartBox.getChildren().add(lblEmpty);
            return;
        }
        
        // Tìm giá trị lớn nhất để scale
        double max = chiTheoDanhMuc.values().stream().max(Double::compare).orElse(1.0);
        
        for (Map.Entry<String, Double> entry : chiTheoDanhMuc.entrySet()) {
            String danhMuc = entry.getKey();
            double soTien = entry.getValue();
            
            HBox barBox = new HBox(10);
            barBox.setAlignment(Pos.CENTER_LEFT);
            
            Label lblDanhMuc = new Label(danhMuc);
            lblDanhMuc.setPrefWidth(120);
            lblDanhMuc.setStyle("-fx-font-weight: bold;");
            
            // Thanh ngang (progress bar đơn giản)
            ProgressBar bar = new ProgressBar(soTien / max);
            bar.setPrefWidth(300);
            bar.setStyle("-fx-accent: #3498db;");
            
            Label lblSoTien = new Label(String.format("%,.0f VNĐ", soTien));
            lblSoTien.setStyle("-fx-text-fill: #2c3e50;");
            
            Label lblPercent = new Label(String.format("(%.1f%%)", (soTien / tongChi) * 100));
            lblPercent.setStyle("-fx-text-fill: #7f8c8d;");
            
            barBox.getChildren().addAll(lblDanhMuc, bar, lblSoTien, lblPercent);
            chartBox.getChildren().add(barBox);
        }
    }
    
    private Map<String, Double> layTongQuanTheoThang(int thang, int nam) {
        Map<String, Double> result = new HashMap<>();
        String sql = "SELECT " +
                    "SUM(CASE WHEN so_tai_khoan_gui = ? THEN so_tien ELSE 0 END) as tong_chi, " +
                    "SUM(CASE WHEN so_tai_khoan_nhan = ? THEN so_tien ELSE 0 END) as tong_thu " +
                    "FROM giao_dich " +
                    "WHERE (so_tai_khoan_gui = ? OR so_tai_khoan_nhan = ?) " +
                    "AND MONTH(ngay_giao_dich) = ? AND YEAR(ngay_giao_dich) = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            pstmt.setString(2, soTaiKhoan);
            pstmt.setString(3, soTaiKhoan);
            pstmt.setString(4, soTaiKhoan);
            pstmt.setInt(5, thang);
            pstmt.setInt(6, nam);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result.put("chi", rs.getDouble("tong_chi"));
                result.put("thu", rs.getDouble("tong_thu"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private Map<String, Double> layChiTheoDanhMuc(int thang, int nam) {
        Map<String, Double> result = new HashMap<>();
        String sql = "SELECT dm.ten_danh_muc, SUM(gd.so_tien) as tong_chi " +
                    "FROM giao_dich gd " +
                    "JOIN danh_muc dm ON gd.danh_muc_id = dm.id " +
                    "WHERE gd.so_tai_khoan_gui = ? " +
                    "AND MONTH(gd.ngay_giao_dich) = ? AND YEAR(gd.ngay_giao_dich) = ? " +
                    "GROUP BY dm.id, dm.ten_danh_muc " +
                    "ORDER BY tong_chi DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, soTaiKhoan);
            pstmt.setInt(2, thang);
            pstmt.setInt(3, nam);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("ten_danh_muc"), rs.getDouble("tong_chi"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    public Scene getScene() {
        return scene;
    }
}
