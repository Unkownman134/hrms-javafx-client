package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Department;
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
import java.util.List;

/**
 * 部门管理子视图控制器 (加载并显示部门列表)
 */
public class DepartmentController implements HRSubController {

    @FXML private TableView<Department> departmentTable;
    // 🌟 Table Columns (fx:id 必须匹配 FXML)
    @FXML private TableColumn<Department, Integer> deptIdCol;
    @FXML private TableColumn<Department, String> deptNameCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    /**
     * 配置 TableView 的列与 Department Model 的属性绑定。
     */
    private void setupTableColumns() {
        // 绑定 Department.java 中的属性名
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        deptNameCol.setCellValueFactory(new PropertyValueFactory<>("deptName"));

        departmentTable.setPlaceholder(new Label("正在加载部门数据..."));
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadDepartmentData();
    }

    /**
     * 在后台线程中调用 API 获取部门数据。
     */
    private void loadDepartmentData() {
        departmentTable.getItems().clear();
        departmentTable.setPlaceholder(new Label("正在从 API 加载数据，请稍候..."));

        Task<List<Department>> loadTask = new Task<>() {
            @Override
            protected List<Department> call() throws Exception {
                // 🌟 调用 Service 获取真实数据
                return hrDataService.getAllDepartments(authToken);
            }

            @Override
            protected void succeeded() {
                List<Department> result = getValue();
                Platform.runLater(() -> {
                    if (result.isEmpty()) {
                        departmentTable.setPlaceholder(new Label("暂无部门数据 🙅‍♀️ 或 API 调用失败。"));
                    } else {
                        departmentTable.setItems(FXCollections.observableArrayList(result));
                        departmentTable.setPlaceholder(new Label(""));
                    }
                    System.out.println("部门列表加载成功，共 " + result.size() + " 条记录。");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("加载失败 ❌", "无法加载部门数据，请检查网络或后端服务。");
                    departmentTable.setPlaceholder(new Label("数据加载失败。"));
                });
                getException().printStackTrace();
            }
        };
        new Thread(loadTask).start();
    }

    @FXML
    private void handleAddDepartment() {
        showAlert("提示 💡", "新增部门功能待实现。");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}