package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.service.admin.UserAdminService;
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

public class UserManagementController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, Integer> roleIdCol;
    @FXML private TableColumn<User, Integer> empIdCol;
    @FXML private TableColumn<User, Void> actionCol;

    private final UserAdminService userService = new UserAdminService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML public void initialize() {
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleIdCol.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        setupActionColumn();
        userTable.setItems(userList);
        loadUserData();
    }

    public void loadUserData() {
        try { userList.setAll(userService.getAllUsers()); } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(p -> new TableCell<>() {
            private final Button edit = new Button("编辑");
            private final Button del = new Button("删除");
            private final HBox box = new HBox(8, edit, del);
            { edit.getStyleClass().add("action-button-edit"); del.getStyleClass().add("action-button-delete"); }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    setGraphic(box);
                    User u = getTableView().getItems().get(getIndex());
                    edit.setOnAction(e -> showEditDialog(u));
                    del.setOnAction(e -> handleDelete(u));
                }
            }
        });
    }

    private void showEditDialog(User user) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(user.getUserId() == null ? "新增用户" : "编辑用户");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setStyle("-fx-padding: 20;");

        TextField nameInput = new TextField(user.getUsername());
        PasswordField passInput = new PasswordField();
        TextField roleInput = new TextField(String.valueOf(user.getRoleId() == null ? "" : user.getRoleId()));
        TextField empInput = new TextField(String.valueOf(user.getEmpId() == null ? "" : user.getEmpId()));

        grid.add(new Label("用户名:"), 0, 0); grid.add(nameInput, 1, 0);
        grid.add(new Label("密码:"), 0, 1); grid.add(passInput, 1, 1);
        grid.add(new Label("角色ID:"), 0, 2); grid.add(roleInput, 1, 2);
        grid.add(new Label("员工ID:"), 0, 3); grid.add(empInput, 1, 3);

        Button save = new Button("保存提交");
        save.setOnAction(e -> {
            user.setUsername(nameInput.getText());
            user.setRawPassword(passInput.getText());
            user.setRoleId(Integer.parseInt(roleInput.getText()));
            user.setEmpId(Integer.parseInt(empInput.getText()));
            try {
                if (user.getUserId() == null) userService.createUser(user);
                else userService.updateUser(user.getUserId(), user);
                stage.close(); loadUserData();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        VBox root = new VBox(15, grid, save);
        root.setStyle("-fx-alignment: center; -fx-padding: 20;");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML private void handleNewUser() { showEditDialog(new User()); }

    private void handleDelete(User u) {
        new Alert(Alert.AlertType.CONFIRMATION, "确定删除 " + u.getUsername() + " ?").showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try { userService.deleteUser(u.getUserId()); loadUserData(); } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }
}