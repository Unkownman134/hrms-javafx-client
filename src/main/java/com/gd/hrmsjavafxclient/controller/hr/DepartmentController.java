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
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class DepartmentController implements HRSubController {

    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, Integer> deptIdCol;
    @FXML private TableColumn<Department, String> deptNameCol;
    @FXML private TableColumn<Department, String> deptDescCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        // 映射 Model 中的属性名
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        deptNameCol.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        deptDescCol.setCellValueFactory(new PropertyValueFactory<>("deptDesc"));
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadDepartmentData();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadDepartmentData();
    }

    private void loadDepartmentData() {
        if (authToken == null) return;
        Task<List<Department>> task = new Task<>() {
            @Override protected List<Department> call() throws Exception {
                return hrDataService.getAllDepartments(authToken);
            }
            @Override protected void succeeded() {
                departmentTable.setItems(FXCollections.observableArrayList(getValue()));
            }
            @Override protected void failed() {
                System.err.println("加载部门数据失败: " + getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    @FXML private void handleAddDepartment(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setContentText("新增部门功能待实现。");
        alert.showAndWait();
    }
}