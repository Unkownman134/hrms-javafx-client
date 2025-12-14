package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.service.EmployeeManagerService;
import com.gd.hrmsjavafxclient.service.PositionManagerService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门员工管理视图控制器
 * 🌟 修正：通过 posId 映射 positionName，并增强了对 position API 访问失败（如 403）的容错处理。
 */
public class DeptEmployeeController implements ManagerSubController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> empIdCol;
    @FXML private TableColumn<Employee, String> nameCol;
    @FXML private TableColumn<Employee, String> positionCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, String> emailCol;
    @FXML private Label deptNameLabel;

    // --- 数据和状态 ---
    private final ObservableList<Employee> data = FXCollections.observableArrayList();
    private final EmployeeManagerService employeeManagerService = new EmployeeManagerService();
    // 假设 PositionManagerService 存在，如果它在你那边没有，请用我上次给的代码创建哦！
    private final PositionManagerService positionManagerService = new PositionManagerService();

    // 🌟 缓存职位映射表: PosId -> PositionName
    private Map<Integer, String> positionMap;

    // 🌟 增加上下文缓存
    private CurrentUserInfo currentUserInfo;
    private String authToken;

    @FXML
    public void initialize() {
        // 初始化 TableView 列
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        // 修正：使用 Employee Model 中实际的属性名 "empName"
        nameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));

        // 关键修正：使用 Cell Value Factory 来自定义显示，通过 posId 查找 positionMap
        positionCol.setCellValueFactory(cellData -> {
            Integer posId = cellData.getValue().getPosId();
            String posName = positionMap != null && posId != null
                    // 如果 positionMap 是空的（因为 API 失败），会触发默认值
                    ? positionMap.getOrDefault(posId, "未知 ID: " + posId + " (权限不足)")
                    : "待加载/N/A";
            return new SimpleStringProperty(posName);
        });

        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        employeeTable.setItems(data);
    }

    /**
     * 🌟 实现统一接口：接收并设置上下文
     */
    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUserInfo = userInfo;
        this.authToken = authToken;

        // 接收到上下文后，启动数据加载 (先加载职位，再加载员工)
        loadData();
    }

    /**
     * 异步加载职位映射和部门员工数据
     */
    private void loadData() {
        if (currentUserInfo == null || authToken == null) {
            showAlert("错误 ❌", "用户或认证信息丢失，无法加载数据。", Alert.AlertType.ERROR);
            return;
        }

        Integer deptId = currentUserInfo.getDeptId();
        deptNameLabel.setText(currentUserInfo.getDepartmentName() + " 部门员工列表");
        employeeTable.setPlaceholder(new Label("正在加载职位和员工列表... 🏃‍♀️"));

        Task<List<Employee>> loadTask = new Task<>() {
            @Override
            protected List<Employee> call() throws Exception {
                // 1. 加载所有职位并创建映射表
                try {
                    List<Position> allPositions = positionManagerService.getAllPositions(authToken);
                    positionMap = allPositions.stream()
                            .collect(Collectors.toMap(Position::getPosId, Position::getPosName));
                    System.out.println("✅ 职位信息加载成功。");
                } catch (RuntimeException | IOException | InterruptedException e) {
                    // 🚨 关键：捕获 API 失败（如 403 权限不足），使用空映射表并警告，**不抛出异常**
                    System.err.println("❌ 警告：加载职位信息失败，可能是权限不足（403）。职位将无法正确显示。");
                    System.err.println("错误信息：" + e.getMessage());

                    // 初始化为空映射，让 positionCol 能够使用其默认的 '未知 ID: X' 逻辑
                    positionMap = Collections.emptyMap();
                }

                // 2. 加载所有员工数据 (如果前面职位 API 失败了，这里仍可继续)
                List<Employee> allEmployees;
                try {
                    allEmployees = employeeManagerService.getAllEmployees(authToken);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("员工 API 请求被中断。", e);
                }

                // 3. 在 Controller (客户端) 层面过滤本部门员工
                return allEmployees.stream()
                        .filter(e -> deptId.equals(e.getDeptId()))
                        .collect(Collectors.toList());
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    if (data.isEmpty()) {
                        employeeTable.setPlaceholder(new Label("本部门暂时没有其他员工数据呢。"));
                    }
                    // 刷新表格确保 Position 列能正确显示 (无论是职位名还是占位符)
                    employeeTable.refresh();
                });
            }

            @Override
            protected void failed() {
                // 这个 failed 块只有在加载员工数据失败（而不是职位数据失败）时才会执行
                Platform.runLater(() -> {
                    employeeTable.setPlaceholder(new Label("加载员工信息失败 ❌: " + getException().getMessage()));
                    showAlert("错误 ❌", "加载部门员工信息失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    getException().printStackTrace();
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}