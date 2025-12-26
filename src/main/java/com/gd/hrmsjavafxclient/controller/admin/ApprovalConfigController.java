package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.ApprovalConfig;
import com.gd.hrmsjavafxclient.model.Department;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.service.admin.ApprovalConfigService;
import com.gd.hrmsjavafxclient.service.admin.DepartmentAdminService;
import com.gd.hrmsjavafxclient.service.admin.PositionAdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.List;

public class ApprovalConfigController {
    @FXML private TableView<ApprovalConfig> configTable;
    @FXML private TableColumn<ApprovalConfig, Integer> idCol;
    @FXML private TableColumn<ApprovalConfig, String> typeCol;
    @FXML private TableColumn<ApprovalConfig, String> deptCol;
    @FXML private TableColumn<ApprovalConfig, String> posCol;
    @FXML private TableColumn<ApprovalConfig, Void> actionCol;

    private final ApprovalConfigService service = new ApprovalConfigService();
    private final DepartmentAdminService deptService = new DepartmentAdminService();
    private final PositionAdminService posService = new PositionAdminService();

    private final ObservableList<ApprovalConfig> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("configId"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("processType"));
        deptCol.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        posCol.setCellValueFactory(new PropertyValueFactory<>("positionName"));

        configTable.setItems(masterData);
        addActionButtons();
        loadConfigData();
    }

    @FXML
    public void loadConfigData() {
        new Thread(() -> {
            try {
                List<ApprovalConfig> list = service.getAllConfigs();
                List<Department> depts = deptService.getAllDepartments();
                List<Position> poss = posService.getAllPositions();

                for (ApprovalConfig config : list) {
                    depts.stream()
                            .filter(d -> d.getDeptId().equals(config.getDeptId()))
                            .findFirst()
                            .ifPresent(d -> config.setDeptName(d.getDeptName()));

                    poss.stream()
                            .filter(p -> p.getPosId().equals(config.getApproverPositionId()))
                            .findFirst()
                            .ifPresent(p -> config.setPositionName(p.getPosName()));
                }

                Platform.runLater(() -> masterData.setAll(list));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("加载失败", e.getMessage()));
            }
        }).start();
    }

    private void addActionButtons() {
        Callback<TableColumn<ApprovalConfig, Void>, TableCell<ApprovalConfig, Void>> cellFactory = param -> new TableCell<>() {
            private final Button editBtn = new Button("修改");
            private final Button delBtn = new Button("删除");
            private final HBox container = new HBox(10, editBtn, delBtn);

            {
                editBtn.getStyleClass().add("edit-button");
                delBtn.getStyleClass().add("delete-button");
                container.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> handleEditConfig(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        };
        actionCol.setCellFactory(cellFactory);
    }

    @FXML
    public void handleNewConfig() {
        showEditDialog(null);
    }

    private void handleEditConfig(ApprovalConfig config) {
        showEditDialog(config);
    }

    private void showEditDialog(ApprovalConfig existingConfig) {
        boolean isEdit = existingConfig != null;
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(isEdit ? "修改审批配置" : "新增审批配置");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(15);

        grid.add(new Label("流程类型:"), 0, 0);
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList("请假", "报销", "出差"));
        typeCombo.setPromptText("请选择类型");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        if (isEdit) typeCombo.setValue(existingConfig.getProcessType());
        grid.add(typeCombo, 1, 0);

        grid.add(new Label("对应部门:"), 0, 1);
        ComboBox<Department> deptCombo = new ComboBox<>();
        deptCombo.setMaxWidth(Double.MAX_VALUE);
        deptCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Department d) { return d == null ? "" : d.getDeptName(); }
            @Override public Department fromString(String s) { return null; }
        });
        grid.add(deptCombo, 1, 1);

        grid.add(new Label("审批职位:"), 0, 2);
        ComboBox<Position> posCombo = new ComboBox<>();
        posCombo.setMaxWidth(Double.MAX_VALUE);
        posCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Position p) { return p == null ? "" : p.getPosName(); }
            @Override public Position fromString(String s) { return null; }
        });
        grid.add(posCombo, 1, 2);

        new Thread(() -> {
            try {
                List<Department> depts = deptService.getAllDepartments();
                List<Position> poss = posService.getAllPositions();
                Platform.runLater(() -> {
                    deptCombo.setItems(FXCollections.observableArrayList(depts));
                    posCombo.setItems(FXCollections.observableArrayList(poss));

                    if (isEdit) {
                        depts.stream()
                                .filter(d -> d.getDeptId().equals(existingConfig.getDeptId()))
                                .findFirst().ifPresent(deptCombo::setValue);
                        poss.stream()
                                .filter(p -> p.getPosId().equals(existingConfig.getApproverPositionId()))
                                .findFirst().ifPresent(posCombo::setValue);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Button saveBtn = new Button(isEdit ? "更新" : "保存");
        saveBtn.getStyleClass().add("action-button");
        saveBtn.setOnAction(e -> {
            if (typeCombo.getValue() == null || deptCombo.getValue() == null || posCombo.getValue() == null) {
                showError("校验失败", "请确保所有项已选择！");
                return;
            }

            ApprovalConfig config = isEdit ? existingConfig : new ApprovalConfig();
            config.setProcessType(typeCombo.getValue());
            config.setDeptId(deptCombo.getValue().getDeptId());
            config.setApproverPositionId(posCombo.getValue().getPosId());

            new Thread(() -> {
                try {
                    if (isEdit) {
                        service.updateConfig(config.getConfigId(), config);
                    } else {
                        service.createConfig(config);
                    }
                    Platform.runLater(() -> {
                        stage.close();
                        loadConfigData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showError("保存失败", ex.getMessage()));
                }
            }).start();
        });

        grid.add(saveBtn, 1, 3);
        Scene scene = new Scene(grid, 400, 300);
        try {
            String css = getClass().getResource("/com/gd/hrmsjavafxclient/style/hrms-styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}
        stage.setScene(scene);
        stage.show();
    }

    private void handleDelete(ApprovalConfig config) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定要删除该配置吗？", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        service.deleteConfig(config.getConfigId());
                        Platform.runLater(this::loadConfigData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> showError("删除失败", e.getMessage()));
                    }
                }).start();
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}