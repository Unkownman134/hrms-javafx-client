package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.service.manager.EmployeeManagerService;
import com.gd.hrmsjavafxclient.service.manager.PositionManagerService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门员工管理视图控制器
 * 补全了所有缺失字段：性别、状态、入职日期。
 */
public class DeptEmployeeController implements ManagerSubController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> empIdCol;
    @FXML private TableColumn<Employee, String> nameCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, String> positionCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, String> emailCol;
    @FXML private TableColumn<Employee, LocalDate> joinDateCol;
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private Label deptNameLabel;
    @FXML private TextField searchField;

    private final ObservableList<Employee> data = FXCollections.observableArrayList();
    private final EmployeeManagerService employeeManagerService = new EmployeeManagerService();
    private final PositionManagerService positionManagerService = new PositionManagerService();

    private Map<Integer, String> positionMap;
    private CurrentUserInfo currentUserInfo;
    private String authToken;

    @FXML
    public void initialize() {
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        joinDateCol.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        positionCol.setCellValueFactory(cellData -> {
            Integer posId = cellData.getValue().getPosId();
            String posName = positionMap != null && posId != null
                    ? positionMap.getOrDefault(posId, "未知 ID: " + posId)
                    : "加载中...";
            return new SimpleStringProperty(posName);
        });

        employeeTable.setItems(data);
    }

    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUserInfo = userInfo;
        this.authToken = authToken;
        loadData();
    }

    /**
     * 搜索功能实现
     */
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText();
        if (searchText == null || searchText.isEmpty()) {
            employeeTable.setItems(data);
            return;
        }

        ObservableList<Employee> filteredData = data.filtered(emp ->
                emp.getEmpName().contains(searchText) ||
                        emp.getEmpId().toString().equals(searchText)
        );
        employeeTable.setItems(filteredData);
    }

    private void loadData() {
        if (currentUserInfo == null || authToken == null) return;

        Integer deptId = currentUserInfo.getDeptId();
        deptNameLabel.setText(currentUserInfo.getDepartmentName() + " 部门员工列表");
        employeeTable.setPlaceholder(new Label("正在同步部门数据..."));

        Task<List<Employee>> loadTask = new Task<>() {
            @Override
            protected List<Employee> call() throws Exception {
                try {
                    List<Position> allPositions = positionManagerService.getAllPositions(authToken);
                    positionMap = allPositions.stream()
                            .collect(Collectors.toMap(Position::getPosId, Position::getPosName));
                } catch (Exception e) {
                    positionMap = Collections.emptyMap();
                }

                List<Employee> allEmployees = employeeManagerService.getAllEmployees(authToken);
                return allEmployees.stream()
                        .filter(e -> deptId.equals(e.getDeptId()))
                        .collect(Collectors.toList());
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    if (data.isEmpty()) {
                        employeeTable.setPlaceholder(new Label("本部门暂时没有员工数据。"));
                    }
                    employeeTable.refresh();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("加载失败", "无法获取部门员工列表", Alert.AlertType.ERROR);
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}