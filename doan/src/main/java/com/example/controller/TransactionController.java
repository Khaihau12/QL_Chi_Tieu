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
 * Controller cho màn hình chuyển tiền
 */
public class TransactionController {

    @FXML private TextField txtSoTaiKhoanNhan;
    @FXML private TextField txtSoTien;
    @FXML private TextArea txtNoiDung;
    @FXML private Button btnChuyenTien;
    @FXML private Button btnQuayLai;
    @FXML private Label lblSoTaiKhoan;
    @FXML private Label lblSoDu;

    @FXML private TableView<GiaoDichInfo> tableGiaoDich;
    @FXML private TableColumn<GiaoDichInfo, String> colNgay;
    @FXML private TableColumn<GiaoDichInfo, String> colLoai;
    @FXML private TableColumn<GiaoDichInfo, String> colTaiKhoan;
    @FXML private TableColumn<GiaoDichInfo, String> colSoTien;
    @FXML private TableColumn<GiaoDichInfo, String> colNoiDung;

    private GiaoDichDAO giaoDichDAO = new GiaoDichDAO();
    private DecimalFormat df = new DecimalFormat("#,###");

    // Inner class cho hiển thị giao dịch
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
        String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
        lblSoTaiKhoan.setText("STK: " + soTaiKhoan);
        
        setupTable();
        loadData();
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

    private void loadData() {
        try {
            String soTaiKhoan = LoginController.currentUser.getSoTaiKhoan();
            
            // Lấy số dư hiện tại
            BigDecimal soDu = giaoDichDAO.laySoDu(soTaiKhoan);
            lblSoDu.setText(df.format(soDu) + " đ");
            lblSoDu.setStyle(soDu.compareTo(BigDecimal.ZERO) >= 0 ? 
                "-fx-text-fill: green; -fx-font-weight: bold;" : 
                "-fx-text-fill: red; -fx-font-weight: bold;");
            
            // Load lịch sử giao dịch
            List<GiaoDich> danhSach = giaoDichDAO.layLichSuGiaoDich(soTaiKhoan);
            List<GiaoDichInfo> displayList = new ArrayList<>();
            
            for (GiaoDich gd : danhSach.stream().limit(20).collect(Collectors.toList())) {
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
    private void handleChuyenTien() {
        try {
            // Validate input
            String soTaiKhoanNhan = txtSoTaiKhoanNhan.getText().trim();
            String soTienStr = txtSoTien.getText().trim();
            String noiDung = txtNoiDung.getText().trim();
            
            if (soTaiKhoanNhan.isEmpty()) {
                showError("Vui lòng nhập số tài khoản người nhận!");
                return;
            }
            
            if (soTienStr.isEmpty()) {
                showError("Vui lòng nhập số tiền!");
                return;
            }
            
            BigDecimal soTien;
            try {
                soTien = new BigDecimal(soTienStr);
                if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Số tiền phải lớn hơn 0!");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Số tiền không hợp lệ!");
                return;
            }
            
            // Kiểm tra không tự chuyển cho chính mình
            String soTaiKhoanGui = LoginController.currentUser.getSoTaiKhoan();
            if (soTaiKhoanGui.equals(soTaiKhoanNhan)) {
                showError("Không thể chuyển tiền cho chính mình!");
                return;
            }
            
            // Kiểm tra tài khoản người nhận có tồn tại
            String tenNguoiNhan = giaoDichDAO.layTenNguoiDung(soTaiKhoanNhan);
            if (tenNguoiNhan == null) {
                showError("Số tài khoản người nhận không tồn tại!");
                return;
            }
            
            // Xác nhận chuyển tiền
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận chuyển tiền");
            confirmAlert.setHeaderText("Bạn có chắc chắn muốn chuyển tiền?");
            confirmAlert.setContentText(
                "Người nhận: " + tenNguoiNhan + " (" + soTaiKhoanNhan + ")\n" +
                "Số tiền: " + df.format(soTien) + " đ\n" +
                "Nội dung: " + (noiDung.isEmpty() ? "(Không có)" : noiDung)
            );
            
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                // Thực hiện chuyển tiền
                boolean success = giaoDichDAO.chuyenTien(soTaiKhoanGui, soTaiKhoanNhan, soTien, noiDung);
                
                if (success) {
                    showSuccess("Chuyển tiền thành công!");
                    clearForm();
                    loadData(); // Reload dữ liệu
                } else {
                    showError("Chuyển tiền thất bại! Vui lòng kiểm tra số dư.");
                }
            }
            
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuayLai() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnQuayLai.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Trang Chủ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        txtSoTaiKhoanNhan.clear();
        txtSoTien.clear();
        txtNoiDung.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
