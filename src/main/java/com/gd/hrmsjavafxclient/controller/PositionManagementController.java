package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.service.PositionAdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.util.Optional;
import java.util.List;

/**
 * R9: 职位信息管理控制器 (超级管理员/人事管理员子视图)
 */
public class PositionManagementController {

    // --- TableView 控件 ---
    @FXML private TableView<Position> positionTable;
    @FXML private TableColumn<Position, Integer> posIdCol;
    @FXML private TableColumn<Position, String> posNameCol;
    @FXML private TableColumn<Position, String> posLevelCol;
    @FXML private TableColumn<Position, Integer> baseSalaryLevelCol;
    @FXML private TableColumn<Position, Void> actionCol;

    // --- Form 控件 ---
    @FXML private Label formTitle;
    @FXML private TextField posIdField;
    @FXML private TextField posNameField;
    @FXML private TextField posLevelField;
    @FXML private TextField baseSalaryLevelField;
    @FXML private Button saveButton;

    // --- 数据和 Service ---
    private final PositionAdminService positionService = new PositionAdminService();
    private final ObservableList<Position> positionList = FXCollections.observableArrayList();
    private Position selectedPosition = null; // 用于跟踪当前编辑/新增的职位

    @FXML
    public void initialize() {
        // 1. 初始化表格列和数据绑定
        posIdCol.setCellValueFactory(new PropertyValueFactory<>("posId"));
        posNameCol.setCellValueFactory(new PropertyValueFactory<>("posName"));
        posLevelCol.setCellValueFactory(new PropertyValueFactory<>("posLevel"));
        baseSalaryLevelCol.setCellValueFactory(new PropertyValueFactory<>("baseSalaryLevel"));
        positionTable.setItems(positionList);

        // 2. 监听表格选择事件，加载详情
        positionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPositionDetails(newValue));

        // 3. 设置操作列 (Edit/Delete Button)
        setupActionColumn();

        // 4. 默认加载数据
        loadPositionData();
    }

    // --- 数据加载 (R) ---

    private void loadPositionData() {
        Task<List<Position>> loadTask = new Task<>() {
            @Override
            protected List<Position> call() throws Exception {
                return positionService.getAllPositions();
            }

            @Override
            protected void succeeded() {
                positionList.clear();
                positionList.addAll(getValue());
                // 成功加载，但一般不弹窗，避免打扰用户
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                showAlert(Alert.AlertType.ERROR, "加载失败 🚨", "无法从服务器获取职位数据：" + getException().getMessage());
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
                    Position position = getTableView().getItems().get(getIndex());

                    editButton.setOnAction(event -> {
                        showPositionDetails(position); // 选中并填充表单
                        formTitle.setText("编辑职位 ID: " + position.getPosId());
                        selectedPosition = position; // 标记为编辑状态
                    });

                    deleteButton.setOnAction(event -> handleDelete(position));
                }
            }
        });
    }

    // --- 详情显示与编辑 (R/U Form) ---

    private void showPositionDetails(Position position) {
        if (position == null) {
            handleCancel();
            return;
        }

        // 填充表单字段
        posIdField.setText(position.getPosId() != null ? String.valueOf(position.getPosId()) : "");
        posNameField.setText(position.getPosName());
        posLevelField.setText(position.getPosLevel());
        baseSalaryLevelField.setText(position.getBaseSalaryLevel() != null ? String.valueOf(position.getBaseSalaryLevel()) : "");

        // 更新表单标题和状态
        formTitle.setText("职位信息详情/编辑 ID: " + position.getPosId());
        selectedPosition = position;
    }

    // --- 按钮事件处理 (C/U/D) ---

    @FXML
    private void handleRefresh() {
        loadPositionData();
    }

    @FXML
    private void handleNewPosition() {
        clearForm();
        formTitle.setText("新增职位信息");
        selectedPosition = new Position(); // 标记为新增状态
    }

    @FXML
    private void handleCancel() {
        clearForm();
        formTitle.setText("职位信息详情");
        selectedPosition = null;
        positionTable.getSelectionModel().clearSelection(); // 清除表格选中
    }

    // 创建/保存 (C/U)
    @FXML
    private void handleSave() {
        if (selectedPosition == null) {
            showAlert(Alert.AlertType.WARNING, "操作警告", "请先选择要编辑的职位或点击 '新增职位' 按钮。");
            return;
        }

        // 1. 校验和构建数据对象
        Position dataToSend = new Position();
        boolean isNew = selectedPosition.getPosId() == null;

        try {
            String name = posNameField.getText().trim();
            String level = posLevelField.getText().trim();
            String baseSalaryLevelText = baseSalaryLevelField.getText().trim();

            if (name.isEmpty() || level.isEmpty() || baseSalaryLevelText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "输入错误", "职位名称、职位等级和薪酬标准ID都是必填项！");
                return;
            }

            // --- 赋值 ---
            dataToSend.setPosName(name);
            dataToSend.setPosLevel(level);

            // 外键赋值 (必须是数字)
            dataToSend.setBaseSalaryLevel(Integer.parseInt(baseSalaryLevelText));

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "薪酬标准ID必须是有效的数字。");
            return;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "请检查所有输入字段是否正确填写。");
            return;
        }

        // 2. 执行网络操作
        Task<Position> saveTask = new Task<>() {
            @Override
            protected Position call() throws Exception {
                if (isNew) {
                    // C: Create
                    return positionService.createPosition(dataToSend);
                } else {
                    // U: Update (更新操作需要 ID)
                    return positionService.updatePosition(selectedPosition.getPosId(), dataToSend);
                }
            }

            @Override
            protected void succeeded() {
                Position result = getValue();
                showAlert(Alert.AlertType.INFORMATION, "成功 ✅", (isNew ? "新增" : "更新") + "职位信息成功！ID: " + result.getPosId());
                clearForm();
                loadPositionData(); // 刷新数据
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
    private void handleDelete(Position position) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("确认删除职位: " + position.getPosName() + " (ID: " + position.getPosId() + ") 吗？");
        confirmAlert.setContentText("注意：如果该职位下有员工，后端通常会阻止删除！");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    positionService.deletePosition(position.getPosId());
                    return null;
                }

                @Override
                protected void succeeded() {
                    showAlert(Alert.AlertType.INFORMATION, "删除成功 ✅", "职位信息 " + position.getPosName() + " 已被删除。");
                    loadPositionData();
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
        posIdField.setText("");
        posNameField.setText("");
        posLevelField.setText("");
        baseSalaryLevelField.setText("");
        selectedPosition = null;
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