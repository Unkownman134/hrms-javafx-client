package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.admin.UserAdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class UserManagementController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, Integer> roleIdCol;
    @FXML private TableColumn<User, Integer> empIdCol;
    @FXML private TableColumn<User, Void> actionCol;

    private final UserAdminService userService = new UserAdminService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleIdCol.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));

        setupActionColumn();
        loadUserData();
    }

    @FXML
    private void loadUserData() {
        new Thread(() -> {
            try {
                var users = userService.getAllUsers();
                Platform.runLater(() -> {
                    userList.setAll(users);
                    userTable.setItems(userList);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "加载数据失败: " + e.getMessage());
                    alert.show();
                });
            }
        }).start();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("修改");
            private final Button delBtn = new Button("删除");
            private final HBox box = new HBox(10, editBtn, delBtn);
            {
                editBtn.getStyleClass().add("action-button-white");
                delBtn.getStyleClass().add("action-button-delete");
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void handleNewUser() {
        showEditDialog(new User());
    }

    private void showEditDialog(User user) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(user.getUserId() == null ? "新增系统用户" : "修改用户信息");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setStyle("-fx-padding: 25;");

        TextField nameInput = new TextField(user.getUsername());
        PasswordField passInput = new PasswordField();
        passInput.setPromptText(user.getUserId() == null ? "请输入密码" : "留空则不修改");

        ComboBox<Integer> roleCombo = new ComboBox<>(FXCollections.observableArrayList(2, 3, 4, 5));
        roleCombo.setValue(user.getRoleId() == null ? 5 : user.getRoleId());

        ComboBox<Employee> empCombo = new ComboBox<>();
        empCombo.setPromptText("选择关联员工");
        empCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Employee e) { return e == null ? "未关联" : e.getEmpName() + " (ID:" + e.getEmpId() + ")"; }
            @Override public Employee fromString(String s) { return null; }
        });

        new Thread(() -> {
            try {
                var employees = userService.getAllEmployees();
                Platform.runLater(() -> {
                    empCombo.setItems(FXCollections.observableArrayList(employees));
                    if (user.getEmpId() != null) {
                        employees.stream()
                                .filter(e -> e.getEmpId().equals(user.getEmpId()))
                                .findFirst()
                                .ifPresent(empCombo::setValue);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        grid.add(new Label("用户名:"), 0, 0);  grid.add(nameInput, 1, 0);
        grid.add(new Label("密码:"), 0, 1); grid.add(passInput, 1, 1);
        grid.add(new Label("角色权限:"), 0, 2); grid.add(roleCombo, 1, 2);
        grid.add(new Label("关联员工:"), 0, 3); grid.add(empCombo, 1, 3);

        Button save = new Button("保存提交");
        save.getStyleClass().add("action-button");
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            try {
                user.setUsername(nameInput.getText());
                if (!passInput.getText().isEmpty()) {
                    user.setRawPassword(passInput.getText());
                }
                user.setRoleId(roleCombo.getValue());
                if (empCombo.getValue() != null) {
                    user.setEmpId(empCombo.getValue().getEmpId());
                }

                new Thread(() -> {
                    try {
                        if (user.getUserId() == null) userService.createUser(user);
                        else userService.updateUser(user.getUserId(), user);
                        Platform.runLater(() -> {
                            stage.close();
                            loadUserData();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            new Alert(Alert.AlertType.ERROR, "保存失败: " + ex.getMessage()).show();
                        });
                    }
                }).start();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "输入格式错误！").show();
            }
        });

        VBox root = new VBox(20, grid, save);
        root.setStyle("-fx-alignment: center; -fx-padding: 10;");
        stage.setScene(new Scene(root, 400, 500));
        stage.show();
    }

    private void handleDelete(User u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除用户 [" + u.getUsername() + "] 吗？");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        userService.deleteUser(u.getUserId());
                        Platform.runLater(this::loadUserData);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}