package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class EmployeeController implements HRSubController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private Label titleLabel;
    @FXML private TableColumn<Employee, Integer> empIdCol;
    @FXML private TableColumn<Employee, String> empNameCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, String> emailCol;
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, java.time.LocalDate> joinDateCol; // 新增列映射

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        // 这里的字符串必须和 Employee 类中的属性名完全一致
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        empNameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        joinDateCol.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadEmployeeData();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadEmployeeData();
    }

    private void loadEmployeeData() {
        if (authToken == null) return;
        Task<List<Employee>> task = new Task<>() {
            @Override protected List<Employee> call() throws Exception {
                return hrDataService.getAllEmployees(authToken);
            }
            @Override protected void succeeded() {
                employeeTable.setItems(FXCollections.observableArrayList(getValue()));
            }
        };
        new Thread(task).start();
    }

    @FXML private void handleAddEmployee(ActionEvent event) { showAlert("提示", "功能开发中..."); }
    @FXML private void handleDeleteEmployee(ActionEvent event) { showAlert("提示", "功能开发中..."); }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}