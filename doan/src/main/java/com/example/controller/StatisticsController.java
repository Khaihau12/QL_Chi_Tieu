package com.example.controller;

import com.example.dao.GiaoDichDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Map;

public class StatisticsController {
    private Stage stage;
    private Scene scene;
    private String soTaiKhoan;
    
    private ComboBox<Integer> cbThang;
    private ComboBox<Integer> cbNam;
    private VBox chartBox;
    private VBox chartThuBox;
    private Label lblTongChi;
    private Label lblTongThu;
    private Label lblThuTienMat;
    private Label lblThuChuyenKhoan;
    private GiaoDichDAO giaoDichDAO;
    
    public StatisticsController(Stage stage, String soTaiKhoan) {
        this.stage = stage;
        this.soTaiKhoan = soTaiKhoan;
        this.giaoDichDAO = new GiaoDichDAO();
        
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
        
        // Biểu đồ chi theo danh mục
        Label lblChart = new Label("Chi tiêu theo danh mục:");
        lblChart.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        chartBox = new VBox(8);
        chartBox.setPadding(new Insets(10));
        chartBox.setStyle("-fx-border-color: #e67e22; -fx-border-width: 1; -fx-background-color: #fffbf5;");

        // Biểu đồ thu theo danh mục
        Label lblThuChart = new Label("Thu nhập theo danh mục:");
        lblThuChart.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        chartThuBox = new VBox(8);
        chartThuBox.setPadding(new Insets(10));
        chartThuBox.setStyle("-fx-border-color: #27ae60; -fx-border-width: 1; -fx-background-color: #f5fffb;");

        // Nút quay lại
        Button btnQuayLai = new Button("Quay Lại");
        btnQuayLai.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnQuayLai.setOnAction(e -> {
            DashboardController dashboard = new DashboardController(stage);
            stage.setScene(dashboard.getScene());
            stage.setResizable(false);
            stage.setWidth(1200);
            stage.setHeight(830);
            stage.centerOnScreen();
        });

        ScrollPane scroll = new ScrollPane();
        VBox scrollContent = new VBox(15,
                title, filterBox, summaryBox,
                lblChart, chartBox,
                lblThuChart, chartThuBox,
                btnQuayLai);
        scrollContent.setPadding(new Insets(20));
        scroll.setContent(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        scene = new Scene(root, 1200, 800);
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

        lblThuTienMat = new Label("Thu tiền mặt: 0 VNĐ");
        lblThuTienMat.setStyle("-fx-font-size: 14px; -fx-text-fill: #16a085; -fx-font-weight: bold;");

        lblThuChuyenKhoan = new Label("Thu chuyển khoản: 0 VNĐ");
        lblThuChuyenKhoan.setStyle("-fx-font-size: 14px; -fx-text-fill: #2980b9; -fx-font-weight: bold;");
        
        box.getChildren().addAll(lblTongChi, lblTongThu, lblThuTienMat, lblThuChuyenKhoan);
        return box;
    }
    
    private void loadData() {
        int thang = cbThang.getValue();
        int nam = cbNam.getValue();
        
        // Tính tổng chi và tổng thu
        Map<String, Double> summary = giaoDichDAO.layTongQuanTheoThang(soTaiKhoan, thang, nam);
        double tongChi = summary.getOrDefault("chi", 0.0);
        double tongThu = summary.getOrDefault("thu", 0.0);

        Map<String, Double> thuTheoNguon = giaoDichDAO.layTongThuTheoNguon(soTaiKhoan, thang, nam);
        double thuTienMat = thuTheoNguon.getOrDefault("thu_tien_mat", 0.0);
        double thuChuyenKhoan = thuTheoNguon.getOrDefault("thu_chuyen_khoan", 0.0);
        
        lblTongChi.setText(String.format("Tổng Chi: %,.0f VNĐ", tongChi));
        lblTongThu.setText(String.format("Tổng Thu: %,.0f VNĐ", tongThu));
        lblThuTienMat.setText(String.format("Thu tiền mặt: %,.0f VNĐ", thuTienMat));
        lblThuChuyenKhoan.setText(String.format("Thu chuyển khoản: %,.0f VNĐ", thuChuyenKhoan));
        
        // Lấy chi tiêu theo danh mục
        Map<String, Double> chiTheoDanhMuc = giaoDichDAO.layChiTheoDanhMuc(soTaiKhoan, thang, nam);
        
        // Vẽ biểu đồ chi
        chartBox.getChildren().clear();
        if (chiTheoDanhMuc.isEmpty()) {
            Label lblEmpty = new Label("Không có dữ liệu chi tiêu trong tháng này");
            lblEmpty.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
            chartBox.getChildren().add(lblEmpty);
        } else {
            double maxChi = chiTheoDanhMuc.values().stream().max(Double::compare).orElse(1.0);
            for (Map.Entry<String, Double> entry : chiTheoDanhMuc.entrySet()) {
                chartBox.getChildren().add(buildBarRow(
                    entry.getKey(), entry.getValue(), maxChi, tongChi, "#e67e22"));
            }
        }

        // Vẽ biểu đồ thu
        Map<String, Double> thuTheoDanhMuc = giaoDichDAO.layThuTheoDanhMuc(soTaiKhoan, thang, nam);
        chartThuBox.getChildren().clear();
        if (thuTheoDanhMuc.isEmpty()) {
            Label lblEmpty2 = new Label("Không có dữ liệu thu nhập trong tháng này");
            lblEmpty2.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
            chartThuBox.getChildren().add(lblEmpty2);
        } else {
            double maxThu = thuTheoDanhMuc.values().stream().max(Double::compare).orElse(1.0);
            for (Map.Entry<String, Double> entry : thuTheoDanhMuc.entrySet()) {
                chartThuBox.getChildren().add(buildBarRow(
                    entry.getKey(), entry.getValue(), maxThu, tongThu, "#27ae60"));
            }
        }
    }

    private HBox buildBarRow(String label, double value, double max, double total, String color) {
        HBox barBox = new HBox(10);
        barBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblDM = new Label(label);
        lblDM.setPrefWidth(130);
        lblDM.setStyle("-fx-font-weight: bold;");

        ProgressBar bar = new ProgressBar(max > 0 ? value / max : 0);
        bar.setPrefWidth(300);
        bar.setStyle("-fx-accent: " + color + ";");

        Label lblVal = new Label(String.format("%,.0f VNĐ", value));
        lblVal.setStyle("-fx-text-fill: #2c3e50;");

        String pct = total > 0 ? String.format("(%.1f%%)", (value / total) * 100) : "";
        Label lblPct = new Label(pct);
        lblPct.setStyle("-fx-text-fill: #7f8c8d;");

        barBox.getChildren().addAll(lblDM, bar, lblVal, lblPct);
        return barBox;
    }
    
    public Scene getScene() {
        return scene;
    }
}
