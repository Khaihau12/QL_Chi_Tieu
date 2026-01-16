package com.example.controller;

import com.example.dao.GiaoDichDAO;
import com.example.model.GiaoDich;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller cho màn hình Dashboard - Ngân hàng
 */
public class DashboardController {

    @FXML private Label lblXinChao;
    @FXML private Label lblSoTaiKhoan;
    @FXML private Label lblSoDu;
    
    @FXML private TableView<GiaoDichInfo> tableGiaoDich;
    @FXML private TableColumn<GiaoDichInfo, String> colNgay;
    @FXML private TableColumn<GiaoDichInfo, String> colLoai;
    @FXML private TableColumn<GiaoDichInfo, String> colTaiKhoan;
    @FXML private TableColumn<GiaoDichInfo, String> colSoTien;
    @FXML private TableColumn<GiaoDichInfo, String> colNoiDung;

    @FXML private Button btnGiaoDich;
    @FXML private Button btnDangXuat;

    private GiaoDichDAO giaoDichDAO = new GiaoDichDAO();
    private DecimalFormat df = new DecimalFormat("#,###");

    // Inner class để hiển thị giao dịch
    public static class GiaoDichInfo {
        private String ngay;
        private String loai;
        private String taiKhoan;
        private String soTien;
        private String noiDung;

        public GiaoDichInfo(String ngay, String loai, String taiKhoan, String soTien, String noiDung) {
            this.ngay = ngay;
            this.loai = loai;
            this.taiKhoan = taiKhoan;
            this.soTien = soTien;
            this.noiDung = noiDung;
        }

        public String getNgay() { return ngay; }
        public String getLoai() { return loai; }
        public String getTaiKhoan() { return taiKhoan; }
        public String getSoTien() { return soTien; }
        public String getNoiDung() { return noiDung; }
    }

    @FXML
    private void initialize() {
        // Hiển thị thông tin người dùng
        lblXinChao.setText("Xin chào, " + LoginController.currentUser.getHoTen());
        lblSoTaiKhoan.setText("STK: " + LoginController.currentUser.getSoTaiKhoan());
        
        // Setup table
        setupTable();
        
        // Load dữ liệu
        loadDashboardData();
    }

    private void setupTable() {
        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngay"));
        colLoai.setCellValueFactory(new PropertyValueFactory<>("loai"));
        colTaiKhoan.setCellValueFactory(new PropertyValueFactory<>("taiKhoan"));
        colSoTien.setCellValueFactory(new PropertyValueFactory<>("soTien"));
        colNoiDung.setCellValueFactory(new PropertyValueFactory<>("noiDung"));
        
        // Format loại giao dịch (Gửi/Nhận)
        colLoai.setCellFactory(column -> new TableCell<GiaoDichInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("Gửi") ? 
                        "-fx-text-fill: red; -fx-font-weight: bold;" : 
                        "-fx-text-fill: green; -fx-font-weight: bold;");
                }
            }
        });
        
        // Format số tiền
        colSoTien.setCellFactory(column -> new TableCell<GiaoDichInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });
    }

    private void loadDashboardData() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            
            // Lấy số dư hiện tại
            BigDecimal soDu = giaoDichDAO.laySoDu(soTaiKhoan);
            lblSoDu.setText(df.format(soDu) + " đ");
            lblSoDu.setStyle(soDu.compareTo(BigDecimal.ZERO) >= 0 ? 
                "-fx-text-fill: green; -fx-font-weight: bold;" : 
                "-fx-text-fill: red; -fx-font-weight: bold;");
            
            // Load 15 giao dịch gần nhất
            List<GiaoDich> danhSach = giaoDichDAO.layLichSuGiaoDich(soTaiKhoan);
            List<GiaoDichInfo> displayList = new ArrayList<>();
            
            for (GiaoDich gd : danhSach.stream().limit(15).collect(Collectors.toList())) {
                String ngay = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(gd.getNgayGiaoDich());
                String loai;
                String taiKhoan;
                
                if (gd.getSoTaiKhoanGui().equals(soTaiKhoan)) {
                    // Giao dịch gửi tiền
                    loai = "Gửi";
                    taiKhoan = gd.getSoTaiKhoanNhan();
                    String tenNguoiNhan = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanNhan());
                    if (tenNguoiNhan != null) {
                        taiKhoan += " (" + tenNguoiNhan + ")";
                    }
                } else {
                    // Giao dịch nhận tiền
                    loai = "Nhận";
                    taiKhoan = gd.getSoTaiKhoanGui();
                    String tenNguoiGui = giaoDichDAO.layTenNguoiDung(gd.getSoTaiKhoanGui());
                    if (tenNguoiGui != null) {
                        taiKhoan += " (" + tenNguoiGui + ")";
                    }
                }
                
                String soTien = df.format(gd.getSoTien()) + " đ";
                String noiDung = gd.getNoiDung() != null ? gd.getNoiDung() : "";
                
                displayList.add(new GiaoDichInfo(ngay, loai, taiKhoan, soTien, noiDung));
            }
            
            ObservableList<GiaoDichInfo> data = FXCollections.observableArrayList(displayList);
            tableGiaoDich.setItems(data);
            
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGiaoDich() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/transaction.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnGiaoDich.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Chuyển Tiền");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDangXuat() {
        try {
            LoginController.currentUser = null;
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnDangXuat.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 550));
            stage.setTitle("Đăng nhập");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
