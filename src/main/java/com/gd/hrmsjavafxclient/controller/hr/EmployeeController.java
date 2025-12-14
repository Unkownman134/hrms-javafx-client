package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

/**
 * 员工管理子视图控制器 (加载并显示员工列表)
 */
public class EmployeeController implements HRSubController {

    // --- FXML 控件 ---
    @FXML private TableView<Employee> employeeTable;
    @FXML private Label titleLabel; // 标题，可以用来做动态提示

    // 🌟 Table Columns (fx:id 必须匹配 EmployeeView.fxml 中的定义)
    @FXML private TableColumn<Employee, Integer> empIdCol;
    @FXML private TableColumn<Employee, String> empNameCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, String> emailCol;
    @FXML private TableColumn<Employee, LocalDate> joinDateCol; // 使用 LocalDate
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, Integer> deptIdCol; // 部门 ID
    @FXML private TableColumn<Employee, Integer> posIdCol;  // 职位 ID
    @FXML private TableColumn<Employee, Integer> managerIdCol; // 经理 ID

    // --- 服务与状态 ---
    private final HRDataService hrDataService = new HRDataService();
    private CurrentUserInfo userInfo;
    private String authToken;

    // --- 初始化和数据设置 ---

    @FXML
    public void initialize() {
        // 绑定 TableView 列与 Employee 对象的属性
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        empNameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        joinDateCol.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        posIdCol.setCellValueFactory(new PropertyValueFactory<>("posId"));
        managerIdCol.setCellValueFactory(new PropertyValueFactory<>("managerId"));

        // 设置占位文本，在数据加载前显示
        employeeTable.setPlaceholder(new Label("正在从服务器加载员工数据... ⏳"));
    }

    /**
     * 实现 HRSubController 接口，接收 HRMainController 传递的上下文。
     * @param userInfo 当前用户信息
     * @param authToken 认证 Token
     */
    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.userInfo = userInfo;
        this.authToken = authToken;
        // 接收到上下文后，立即开始加载数据
        loadEmployeeData();
    }

    /**
     * 异步加载员工数据。
     */
    private void loadEmployeeData() {
        // 使用 Task 在后台线程中进行 API 调用，避免阻塞 JavaFX UI 线程
        Task<List<Employee>> loadTask = new Task<>() {
            @Override
            protected List<Employee> call() throws Exception {
                // 🌟 使用缓存的 authToken 调用 HRDataService
                return hrDataService.getAllEmployees(authToken);
            }

            @Override
            protected void succeeded() {
                List<Employee> result = getValue();
                Platform.runLater(() -> {
                    if (result.isEmpty()) {
                        employeeTable.setPlaceholder(new Label("暂无员工数据 🙅‍♀️ 或 API 调用失败，请检查后端服务。"));
                    } else {
                        employeeTable.setItems(FXCollections.observableArrayList(result));
                        employeeTable.setPlaceholder(new Label("")); // 清除占位文本
                    }
                    System.out.println("员工列表加载成功，共 " + result.size() + " 条记录。");
                    // 动态更新标题，增加用户体验
                    titleLabel.setText(String.format("员工档案管理 (共 %d 人)", result.size()));
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("加载失败 ❌", "无法加载员工数据，请检查网络或后端服务。\\n错误: " + getException().getMessage());
                    employeeTable.setPlaceholder(new Label("数据加载失败。"));
                });
                getException().printStackTrace();
            }
        };

        // 启动后台线程
        new Thread(loadTask).start();
    }

    // --- 业务操作 ---

    @FXML
    private void handleAddEmployee() {
        showAlert("提示 💡", "新增员工功能待实现。");
    }

    @FXML
    private void handleDeleteEmployee() {
        showAlert("提示 💡", "删除员工功能待实现。");
    }

    private void showAlert(String title, String content) {
        // 确保在 JavaFX 线程中显示 Alert
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}