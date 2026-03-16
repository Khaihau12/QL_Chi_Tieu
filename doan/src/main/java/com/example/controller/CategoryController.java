package com.example.controller;

import com.example.dao.DanhMucDAO;
import com.example.model.DanhMuc;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller cho màn hình Quản lý Danh mục (Chi + Thu) với TabPane
 */
public class CategoryController {
    private Stage stage;
    private Scene scene;
    private String soTaiKhoan;

    private TableView<DanhMuc> tablechi;
    private TableView<DanhMuc> tableThu;
    private DanhMucDAO danhMucDAO;

    public CategoryController(Stage stage, String soTaiKhoan) {
        this.stage = stage;
        this.soTaiKhoan = soTaiKhoan;
        this.danhMucDAO = new DanhMucDAO();
        createUI();
    }

    private void createUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("QUẢN LÝ DANH MỤC");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label guide = new Label("Danh mục mặc định: chỉ đọc  |  Danh mục riêng: có thể sửa/xóa");
        guide.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        // ===== Tab pane =====
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabChi = new Tab("Danh mục Chi");
        tabChi.setContent(buildTabContent("chi"));

        Tab tabThu = new Tab("Danh mục Thu");
        tabThu.setContent(buildTabContent("thu"));

        tabPane.getTabs().addAll(tabChi, tabThu);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

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

        root.getChildren().addAll(title, guide, tabPane, btnQuayLai);
        scene = new Scene(root, 1200, 800);
    }

    /** Tạo nội dung một tab (chi hoặc thu) */
    private VBox buildTabContent(String loai) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        TableView<DanhMuc> tbl = buildTable();
        if ("chi".equals(loai)) tablechi = tbl;
        else tableThu = tbl;
        VBox.setVgrow(tbl, Priority.ALWAYS);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        String loaiLabel = "chi".equals(loai) ? "Chi" : "Thu";

        Button btnThem = new Button("Thêm danh mục " + loaiLabel);
        btnThem.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnThem.setOnAction(e -> handleThem(tbl, loai, loaiLabel));

        Button btnSua = new Button("Sửa");
        btnSua.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSua.setOnAction(e -> handleSua(tbl));

        Button btnXoa = new Button("Xóa");
        btnXoa.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnXoa.setOnAction(e -> handleXoa(tbl));

        btnBox.getChildren().addAll(btnThem, btnSua, btnXoa);
        content.getChildren().addAll(tbl, btnBox);

        // Load data
        tbl.getItems().setAll(danhMucDAO.layDanhMucTheoLoai(soTaiKhoan, loai));
        return content;
    }

    private TableView<DanhMuc> buildTable() {
        TableView<DanhMuc> tbl = new TableView<>();

        TableColumn<DanhMuc, String> colTen = new TableColumn<>("Tên Danh Mục");
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenDanhMuc"));
        colTen.setPrefWidth(250);

        TableColumn<DanhMuc, String> colMoTa = new TableColumn<>("Mô Tả");
        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));
        colMoTa.setPrefWidth(420);

        TableColumn<DanhMuc, String> colPhanLoai = new TableColumn<>("Phân loại");
        colPhanLoai.setPrefWidth(130);
        colPhanLoai.setCellFactory(col -> new TableCell<DanhMuc, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                DanhMuc dm = getTableView().getItems().get(getIndex());
                if (dm.isDanhMucMacDinh()) {
                    setText("Mặc định");
                    setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                } else {
                    setText("Riêng tư");
                    setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                }
            }
        });

        tbl.getColumns().addAll(colTen, colMoTa, colPhanLoai);
        return tbl;
    }

    private void handleThem(TableView<DanhMuc> tbl, String loai, String loaiLabel) {
        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Thêm Danh Mục " + loaiLabel + " Riêng");
        dialog.setHeaderText("Tạo danh mục " + loaiLabel.toLowerCase() + " cho riêng bạn");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtTen = new TextField();
        txtTen.setPromptText("Tên danh mục...");
        txtTen.setPrefWidth(300);
        TextArea txtMoTa = new TextArea();
        txtMoTa.setPromptText("Mô tả chi tiết...");
        txtMoTa.setPrefRowCount(3); txtMoTa.setPrefWidth(300);

        grid.add(new Label("Tên danh mục:"), 0, 0); grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1); grid.add(txtMoTa, 1, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType btnOK = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                if (ten.isEmpty()) { showAlert("Lỗi", "Vui lòng nhập tên danh mục!"); return null; }
                return new DanhMuc(ten, txtMoTa.getText().trim(), loai, soTaiKhoan);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dm -> {
            if (danhMucDAO.tonTaiTenDanhMuc(dm.getTenDanhMuc(), loai, soTaiKhoan)) {
                showAlert("Lỗi", "Tên danh mục đã tồn tại trong nhóm " + loaiLabel + "!\nVui lòng nhập tên khác.");
                return;
            }

            if (danhMucDAO.themDanhMuc(dm)) {
                showAlert("Thành công", "Đã thêm: " + dm.getTenDanhMuc());
                tbl.getItems().setAll(danhMucDAO.layDanhMucTheoLoai(soTaiKhoan, loai));
            } else {
                showAlert("Lỗi", "Thêm thất bại!");
            }
        });
    }

    private void handleSua(TableView<DanhMuc> tbl) {
        DanhMuc selected = tbl.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Lỗi", "Vui lòng chọn danh mục!"); return; }
        if (selected.isDanhMucMacDinh()) { showAlert("Lỗi", "Không thể sửa danh mục mặc định!"); return; }

        Dialog<DanhMuc> dialog = new Dialog<>();
        dialog.setTitle("Sửa Danh Mục");
        dialog.setHeaderText("Chỉnh sửa: " + selected.getTenDanhMuc());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        TextField txtTen = new TextField(selected.getTenDanhMuc());
        txtTen.setPrefWidth(300);
        TextArea txtMoTa = new TextArea(selected.getMoTa() != null ? selected.getMoTa() : "");
        txtMoTa.setPrefRowCount(3); txtMoTa.setPrefWidth(300);

        grid.add(new Label("Tên danh mục:"), 0, 0); grid.add(txtTen, 1, 0);
        grid.add(new Label("Mô tả:"), 0, 1); grid.add(txtMoTa, 1, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType btnOK = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == btnOK) {
                String ten = txtTen.getText().trim();
                if (ten.isEmpty()) { showAlert("Lỗi", "Vui lòng nhập tên!"); return null; }
                return new DanhMuc(
                    selected.getId(),
                    ten,
                    txtMoTa.getText().trim(),
                    selected.getLoai(),
                    selected.getSoTaiKhoan()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dm -> {
            if (tonTaiTenDanhMucBatKyChoUser(dm.getTenDanhMuc(), dm.getId())) {
                showAlert("Lỗi", "Tên danh mục đã tồn tại (bao gồm cả Thu/Chi, mặc định hoặc riêng)!\nVui lòng nhập tên khác.");
                return;
            }

            if (danhMucDAO.suaDanhMuc(dm)) {
                showAlert("Thành công", "Đã cập nhật danh mục!");
                tbl.getItems().setAll(danhMucDAO.layDanhMucTheoLoai(soTaiKhoan, dm.getLoai()));
            } else {
                showAlert("Lỗi", "Cập nhật thất bại!");
            }
        });
    }

    private void handleXoa(TableView<DanhMuc> tbl) {
        DanhMuc selected = tbl.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Lỗi", "Vui lòng chọn danh mục!"); return; }
        if (selected.isDanhMucMacDinh()) { showAlert("Lỗi", "Không thể xóa danh mục mặc định!"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa danh mục: " + selected.getTenDanhMuc() +
                "\n\nCác giao dịch dùng danh mục này sẽ mất liên kết!",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Xác nhận xóa"); confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (danhMucDAO.xoaDanhMuc(selected.getId())) {
                    showAlert("Thành công", "Đã xóa!");
                    tbl.getItems().setAll(danhMucDAO.layDanhMucTheoLoai(soTaiKhoan, selected.getLoai()));
                } else {
                    showAlert("Lỗi", "Xóa thất bại!");
                }
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(null); a.showAndWait();
    }

    // Kiểm tra trùng tên trên toàn bộ danh mục user có thể nhìn thấy (Thu + Chi, mặc định + riêng)
    private boolean tonTaiTenDanhMucBatKyChoUser(String tenDanhMuc, Integer excludeId) {
        String ten = tenDanhMuc != null ? tenDanhMuc.trim() : "";
        if (ten.isEmpty()) return false;

        List<DanhMuc> all = danhMucDAO.layTatCaDanhMuc(soTaiKhoan);
        for (DanhMuc dm : all) {
            if (excludeId != null && dm.getId() == excludeId) continue;
            String tenHienCo = dm.getTenDanhMuc() != null ? dm.getTenDanhMuc().trim() : "";
            if (ten.equalsIgnoreCase(tenHienCo)) return true;
        }
        return false;
    }

    public Scene getScene() { return scene; }
}
