package com.example.controller.admin;

import com.example.dao.GiaoDichDAO;
import com.example.dao.NguoiDungDAO;
import com.example.model.NguoiDung;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Controller panel Báo cáo hệ thống cho Admin.
 */
public class AdminReportController {

    public static class UserReportRow {
        private String tenDangNhap;
        private String hoTen;
        private String soTaiKhoan;
        private String soDu;
        private String soGDDaGui;
        private String soGDDaNhan;
        private String trangThai;

        public UserReportRow(String tenDangNhap, String hoTen, String soTaiKhoan,
                             String soDu, String soGDDaGui, String soGDDaNhan, String trangThai) {
            this.tenDangNhap = tenDangNhap;
            this.hoTen = hoTen;
            this.soTaiKhoan = soTaiKhoan;
            this.soDu = soDu;
            this.soGDDaGui = soGDDaGui;
            this.soGDDaNhan = soGDDaNhan;
            this.trangThai = trangThai;
        }

        public String getTenDangNhap() { return tenDangNhap; }

        public String getHoTen() { return hoTen; }

        public String getSoTaiKhoan() { return soTaiKhoan; }

        public String getSoDu() { return soDu; }

        public String getSoGDDaGui() { return soGDDaGui; }

        public String getSoGDDaNhan() { return soGDDaNhan; }

        public String getTrangThai() { return trangThai; }
    }

    private final NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();
    private final GiaoDichDAO giaoDichDAO = new GiaoDichDAO();
    private final DecimalFormat df = new DecimalFormat("#,###");

    private VBox panel;

    private Label lblTongNguoiDung;
    private Label lblTongGiaoDich;
    private Label lblTongTienLuuChuyen;
    private Label lblBiKhoa;

    private TableView<UserReportRow> tableUserReport;

    public AdminReportController() {
        buildPanel();
    }

    public VBox getPanel() {
        return panel;
    }

    public void refresh() {
        try {
            List<NguoiDung> all = nguoiDungDAO.layTatCa();
            long tongUser = all.stream().filter(nd -> "nguoi_dung".equals(nd.getVaiTro())).count();
            long biKhoa = all.stream().filter(nd -> "bi_khoa".equals(nd.getTrangThai())).count();

            long tongGD = giaoDichDAO.demTatCaGiaoDich();
            BigDecimal tongTien = giaoDichDAO.tongSoTienGiaoDich();

            lblTongNguoiDung.setText(String.valueOf(tongUser));
            lblTongGiaoDich.setText(String.valueOf(tongGD));
            lblTongTienLuuChuyen.setText(df.format(tongTien) + " đ");
            lblBiKhoa.setText(String.valueOf(biKhoa));

            tableUserReport.getItems().clear();
            for (NguoiDung nd : all) {
                if ("quan_ly".equals(nd.getVaiTro())) continue;
                long gui = giaoDichDAO.demGDGui(nd.getSoTaiKhoan());
                long nhan = giaoDichDAO.demGDNhan(nd.getSoTaiKhoan());
                String soDuFmt = nd.getSoDu() != null ? df.format(nd.getSoDu()) + " đ" : "0 đ";
                String tt = "hoat_dong".equals(nd.getTrangThai()) ? " Hoạt động" : " Bị khóa";
                tableUserReport.getItems().add(new UserReportRow(
                        nd.getTenDangNhap(), nd.getHoTen(), nd.getSoTaiKhoan(),
                        soDuFmt, String.valueOf(gui), String.valueOf(nhan), tt));
            }
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải báo cáo: " + e.getMessage());
        }
    }

    private void buildPanel() {
        panel = new VBox(20);
        panel.setPadding(new Insets(5));

        Label title = new Label("Báo cáo hệ thống");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        lblTongNguoiDung = new Label("...");
        lblTongGiaoDich = new Label("...");
        lblTongTienLuuChuyen = new Label("...");
        lblBiKhoa = new Label("...");

        statsRow.getChildren().addAll(
                createStatCard("Tổng người dùng", lblTongNguoiDung, "#3498db"),
                createStatCard("Tổng GD chuyển khoản", lblTongGiaoDich, "#27ae60"),
                createStatCard("Tổng tiền chuyển khoản", lblTongTienLuuChuyen, "#9b59b6"),
                createStatCard("Tài khoản bị khóa", lblBiKhoa, "#e74c3c")
        );

        Label lblUserTitle = new Label("Chi tiết theo từng người dùng");
        lblUserTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        tableUserReport = new TableView<>();
        VBox.setVgrow(tableUserReport, Priority.ALWAYS);

        TableColumn<UserReportRow, String> c1 = new TableColumn<>("Tên đăng nhập");
        c1.setCellValueFactory(new PropertyValueFactory<>("tenDangNhap"));
        c1.setPrefWidth(130);

        TableColumn<UserReportRow, String> c2 = new TableColumn<>("Họ tên");
        c2.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        c2.setPrefWidth(160);

        TableColumn<UserReportRow, String> c3 = new TableColumn<>("Số TK");
        c3.setCellValueFactory(new PropertyValueFactory<>("soTaiKhoan"));
        c3.setPrefWidth(110);

        TableColumn<UserReportRow, String> c4 = new TableColumn<>("Số dư hiện tại");
        c4.setCellValueFactory(new PropertyValueFactory<>("soDu"));
        c4.setPrefWidth(140);

        TableColumn<UserReportRow, String> c5 = new TableColumn<>("GD CK đã gửi");
        c5.setCellValueFactory(new PropertyValueFactory<>("soGDDaGui"));
        c5.setPrefWidth(100);

        TableColumn<UserReportRow, String> c6 = new TableColumn<>("GD CK đã nhận");
        c6.setCellValueFactory(new PropertyValueFactory<>("soGDDaNhan"));
        c6.setPrefWidth(100);

        TableColumn<UserReportRow, String> c7 = new TableColumn<>("Trạng thái");
        c7.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        c7.setPrefWidth(120);

        tableUserReport.getColumns().addAll(c1, c2, c3, c4, c5, c6, c7);

        Button btnRefresh = new Button("Làm mới báo cáo");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnRefresh.setOnAction(e -> refresh());

        panel.getChildren().addAll(title, statsRow, lblUserTitle, tableUserReport, btnRefresh);
        refresh();
    }

    private VBox createStatCard(String label, Label valueLabel, String color) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(15));
        card.setPrefWidth(215);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: " + color + "; -fx-border-width: 0 0 4 0;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        card.getChildren().addAll(lbl, valueLabel);
        return card;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
