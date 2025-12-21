package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.Department;
import com.gd.hrmsjavafxclient.service.admin.DepartmentAdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DepartmentManagementController {
    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, Integer> deptIdCol;
    @FXML private TableColumn<Department, String> deptNameCol;
    @FXML private TableColumn<Department, Void> actionCol;

    private final DepartmentAdminService service = new DepartmentAdminService();
    private final ObservableList<Department> dataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        deptNameCol.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        addActionButtons();
        loadDepartmentData();
    }

    @FXML
    public void loadDepartmentData() {
        new Thread(() -> {
            try {
                var list = service.getAllDepartments();
                Platform.runLater(() -> {
                    dataList.setAll(list);
                    departmentTable.setItems(dataList);
                });
            } catch (Exception e) {
                showError("加载失败", e.getMessage());
            }
        }).start();
    }

    private void addActionButtons() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button delBtn = new Button("删除");
            private final HBox container = new HBox(10, editBtn, delBtn);
            {
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> {
                    Department d = getTableView().getItems().get(getIndex());
                    try {
                        service.deleteDepartment(d.getDeptId());
                        loadDepartmentData();
                    } catch (Exception ex) { showError("删除失败", ex.getMessage()); }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML private void handleNewDepartment() { showEditDialog(new Department()); }

    private void showEditDialog(Department d) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        TextField nameIn = new TextField(d.getDeptName());
        Button save = new Button("保存");
        save.setOnAction(e -> {
            try {
                d.setDeptName(nameIn.getText());
                if (d.getDeptId() == null) service.createDepartment(d);
                else service.updateDepartment(d.getDeptId(), d);
                stage.close(); loadDepartmentData();
            } catch (Exception ex) { showError("保存失败", ex.getMessage()); }
        });
        VBox root = new VBox(15, new Label("部门名称:"), nameIn, save);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        stage.setScene(new Scene(root, 300, 200));
        stage.show();
    }

    private void showError(String t, String c) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(t); a.setContentText(c); a.showAndWait();
        });
    }
}