package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Department;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DepartmentController implements HRSubController {

    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, Integer> deptIdCol;
    @FXML private TableColumn<Department, String> deptNameCol;
    @FXML private TableColumn<Department, String> deptDescCol;
    @FXML private TableColumn<Department, Void> actionCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        deptNameCol.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        deptDescCol.setCellValueFactory(new PropertyValueFactory<>("deptDesc"));
        setupActionColumn();
    }

    @Override
    public void setHRContext(CurrentUserInfo u, String t) {
        this.authToken = t;
        loadData();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final HBox container = new HBox(10, editBtn, deleteBtn);
            {
                container.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML private void handleRefresh(ActionEvent e) { loadData(); }
    @FXML private void handleNewDepartment(ActionEvent e) { showEditDialog(new Department()); }

    private void loadData() {
        if (authToken == null) return;
        Task<List<Department>> task = new Task<>() {
            @Override protected List<Department> call() throws IOException, InterruptedException { return hrDataService.getAllDepartments(authToken); }
            @Override protected void succeeded() { departmentTable.setItems(FXCollections.observableArrayList(getValue())); }
        };
        new Thread(task).start();
    }

    private void showEditDialog(Department d) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        TextField nameIn = new TextField(d.getDeptName());
        TextArea descIn = new TextArea(d.getDeptDesc());
        Button save = new Button("保存");
        save.setOnAction(e -> {
            d.setDeptName(nameIn.getText()); d.setDeptDesc(descIn.getText());
            new Thread(() -> {
                boolean ok = (d.getDeptId() == null) ? hrDataService.addDepartment(d, authToken) : hrDataService.updateDepartment(d, authToken);
                Platform.runLater(() -> { if(ok) { stage.close(); loadData(); } });
            }).start();
        });
        VBox root = new VBox(10, new Label("名称:"), nameIn, new Label("描述:"), descIn, save);
        root.setPadding(new javafx.geometry.Insets(20));
        stage.setScene(new Scene(root, 300, 350));
        stage.show();
    }

    private void handleDelete(Department d) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "确认删除部门 " + d.getDeptName() + " 吗？");
        a.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = hrDataService.deleteDepartment(d.getDeptId(), authToken);
                    Platform.runLater(() -> { if(ok) loadData(); });
                }).start();
            }
        });
    }
}