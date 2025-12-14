package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.service.admin.UserAdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

/**
 * R11: 用户账号信息管理控制器 (超级管理员子视图)
 */
public class UserManagementController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, Integer> roleIdCol;
    @FXML private TableColumn<User, Integer> empIdCol;
    @FXML private TableColumn<User, Void> actionCol;

    @FXML private Label formTitle;
    @FXML private TextField userIdField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField roleIdField;
    @FXML private TextField empIdField;
    @FXML private Button saveButton;

    private final UserAdminService userService = new UserAdminService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private User selectedUser = null; // 用于跟踪当前编辑的用户

    @FXML
    public void initialize() {
        // 1. 初始化表格列和数据绑定
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleIdCol.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));

        userTable.setItems(userList);

        // 2. 监听表格选择事件，加载详情
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showUserDetails(newValue));

        // 3. 设置操作列 (Edit/Delete Button)
        setupActionColumn();

        // 4. 默认加载数据
        loadUserData();
    }

    // --- 数据加载 (R) ---

    private void loadUserData() {
        Task<List<User>> loadTask = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                // 调用服务层接口获取所有用户
                return userService.getAllUsers();
            }

            @Override
            protected void succeeded() {
                // UI 线程更新表格数据
                userList.clear();
                userList.addAll(getValue());
//                showAlert(Alert.AlertType.INFORMATION, "数据刷新成功", "已加载 " + userList.size() + " 条用户记录。");
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                showAlert(Alert.AlertType.ERROR, "加载失败 🚨", "无法从服务器获取数据：" + getException().getMessage());
            }
        };

        new Thread(loadTask).start();
    }

    // --- 表格操作列 (Edit/Delete) ---
    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {

            final Button editButton = new Button("编辑");
            final Button deleteButton = new Button("删除");
            final HBox pane = new HBox(5, editButton, deleteButton);

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                    User user = getTableView().getItems().get(getIndex());

                    editButton.setOnAction(event -> {
                        showUserDetails(user); // 选中并填充表单
                        formTitle.setText("编辑用户 ID: " + user.getUserId());
                        selectedUser = user; // 标记为编辑状态
                    });

                    deleteButton.setOnAction(event -> handleDelete(user));
                }
            }
        });
    }

    // --- 详情显示与编辑 (R/U Form) ---

    private void showUserDetails(User user) {
        if (user == null) {
            handleCancel();
            return;
        }

        // 填充表单字段
        userIdField.setText(user.getUserId() != null ? String.valueOf(user.getUserId()) : "");
        usernameField.setText(user.getUsername());
        roleIdField.setText(user.getRoleId() != null ? String.valueOf(user.getRoleId()) : "");
        empIdField.setText(user.getEmpId() != null ? String.valueOf(user.getEmpId()) : "");

        // 密码字段留空，编辑时不显示旧密码
        passwordField.setText("");

        // 更新表单标题和状态
        formTitle.setText("用户详情/编辑 ID: " + user.getUserId());
        selectedUser = user;
    }

    // --- 按钮事件处理 (C/U/D) ---

    @FXML
    private void handleRefresh() {
        loadUserData();
    }

    @FXML
    private void handleNewUser() {
        clearForm();
        formTitle.setText("新增用户账号");
        selectedUser = new User(); // 标记为新增状态
    }

    @FXML
    private void handleCancel() {
        clearForm();
        formTitle.setText("用户详情");
        selectedUser = null;
        userTable.getSelectionModel().clearSelection(); // 清除表格选中
    }

    // 创建/保存 (C/U)
    @FXML
    private void handleSave() {
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "操作警告", "请先选择要编辑的用户或点击 '新增用户' 按钮。");
            return;
        }

        // 1. 校验和构建数据对象
        User dataToSend = new User();

        // C: 新增时，用户ID为空。 U: 更新时，用户ID非空。
        boolean isNew = selectedUser.getUserId() == null;

        try {
            dataToSend.setUsername(usernameField.getText().trim());
            dataToSend.setRoleId(Integer.parseInt(roleIdField.getText().trim()));
            // EmpID 允许为空，但如果是数字则解析
            String empIdText = empIdField.getText().trim();
            dataToSend.setEmpId(empIdText.isEmpty() ? null : Integer.parseInt(empIdText));

            // 密码处理
            String rawPwd = passwordField.getText();
            if (isNew && rawPwd.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "输入错误", "新增用户时，密码不能为空！");
                return;
            }
            // 只有当密码字段不为空时，才发送给后端
            if (!rawPwd.isEmpty()) {
                dataToSend.setRawPassword(rawPwd);
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "角色ID和员工ID必须是有效的数字。");
            return;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "请检查所有输入字段是否正确填写。");
            return;
        }

        // 2. 执行网络操作
        Task<User> saveTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                if (isNew) {
                    // C: Create
                    return userService.createUser(dataToSend);
                } else {
                    // U: Update
                    return userService.updateUser(selectedUser.getUserId(), dataToSend);
                }
            }

            @Override
            protected void succeeded() {
                showAlert(Alert.AlertType.INFORMATION, "成功 ✅", (isNew ? "新增" : "更新") + "用户成功！");
                clearForm();
                loadUserData(); // 刷新数据
            }

            @Override
            protected void failed() {
                showAlert(Alert.AlertType.ERROR, "操作失败 ❌", "执行操作时出错：" + getException().getMessage());
                getException().printStackTrace();
            }
        };
        new Thread(saveTask).start();
    }

    // 删除 (D)
    private void handleDelete(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("确认删除用户: " + user.getUsername() + " (ID: " + user.getUserId() + ") 吗？");
        confirmAlert.setContentText("此操作不可逆！");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.deleteUser(user.getUserId());
                    return null;
                }

                @Override
                protected void succeeded() {
                    showAlert(Alert.AlertType.INFORMATION, "删除成功 ✅", "用户 " + user.getUsername() + " 已被删除。");
                    loadUserData();
                    handleCancel();
                }

                @Override
                protected void failed() {
                    showAlert(Alert.AlertType.ERROR, "删除失败 ❌", "删除操作失败：" + getException().getMessage());
                    getException().printStackTrace();
                }
            };
            new Thread(deleteTask).start();
        }
    }

    // --- 辅助方法 ---
    private void clearForm() {
        userIdField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        roleIdField.setText("");
        empIdField.setText("");
        selectedUser = null;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}