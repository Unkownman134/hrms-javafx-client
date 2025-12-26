package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.ShiftRule;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * 班次规则管理控制器
 * 负责配置上下班时间逻辑 (oﾟvﾟ)ノ
 */
public class ShiftController implements HRSubController {

    @FXML private TableView<ShiftRule> shiftTable;
    @FXML private TableColumn<ShiftRule, String> nameCol;
    @FXML private TableColumn<ShiftRule, String> startCol;
    @FXML private TableColumn<ShiftRule, String> endCol;
    @FXML private TableColumn<ShiftRule, Integer> lateCol;
    @FXML private TableColumn<ShiftRule, Void> actionCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("ruleName"));
        startCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("workStartTime"));
        endCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("workEndTime"));
        lateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("lateToleranceMin"));

        setupActionColumn();
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadData();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadData();
    }

    @FXML
    private void handleNewRule(ActionEvent event) {
        ShiftRule newRule = new ShiftRule();
        showEditDialog(newRule);
    }

    private void loadData() {
        Task<List<ShiftRule>> task = new Task<>() {
            @Override
            protected List<ShiftRule> call() throws Exception {
                return hrDataService.getAllShiftRules(authToken);
            }
        };
        task.setOnSucceeded(e -> shiftTable.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("修改");
            private final Button delBtn = new Button("删除");
            private final HBox box = new HBox(10, editBtn, delBtn);

            {
                editBtn.getStyleClass().add("action-button-secondary");
                delBtn.getStyleClass().add("action-button-danger");
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

    private void showEditDialog(ShiftRule rule) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        boolean isAdding = (rule.getRuleId() == null || rule.getRuleId() == 0);
        stage.setTitle(isAdding ? "新增班次规则" : "修改班次规则");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField(rule.getRuleName());
        TextField startField = new TextField(rule.getWorkStartTime());
        TextField endField = new TextField(rule.getWorkEndTime());
        Spinner<Integer> lateSpinner = new Spinner<>(0, 120, rule.getLateToleranceMin() != null ? rule.getLateToleranceMin() : 0);

        grid.add(new Label("规则名称:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("上班时间(HH:mm:ss):"), 0, 1); grid.add(startField, 1, 1);
        grid.add(new Label("下班时间(HH:mm:ss):"), 0, 2); grid.add(endField, 1, 2);
        grid.add(new Label("允许迟到(分钟):"), 0, 3); grid.add(lateSpinner, 1, 3);

        Button saveBtn = new Button("确认保存");
        saveBtn.getStyleClass().add("action-button-primary");
        saveBtn.setOnAction(e -> {
            rule.setRuleName(nameField.getText());
            rule.setWorkStartTime(startField.getText());
            rule.setWorkEndTime(endField.getText());
            rule.setLateToleranceMin(lateSpinner.getValue());

            new Thread(() -> {
                boolean ok = isAdding ?
                        hrDataService.addShiftRule(rule, authToken) :
                        hrDataService.updateShiftRule(rule, authToken);

                Platform.runLater(() -> {
                    if(ok) {
                        stage.close();
                        loadData();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "保存失败！请确认时间格式为 HH:mm:ss。");
                        alert.show();
                    }
                });
            }).start();
        });

        VBox layout = new VBox(20, grid, saveBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new javafx.geometry.Insets(0, 0, 20, 0));
        stage.setScene(new Scene(layout, 400, 450));
        stage.show();
    }

    private void handleDelete(ShiftRule rule) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除规则 [" + rule.getRuleName() + "] 吗？");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = hrDataService.deleteShiftRule(rule.getRuleId(), authToken);
                    Platform.runLater(() -> {
                        if (ok) loadData();
                        else new Alert(Alert.AlertType.ERROR, "删除失败").show();
                    });
                }).start();
            }
        });
    }
}