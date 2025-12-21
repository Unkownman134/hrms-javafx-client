package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.service.admin.PositionAdminService;
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

public class PositionManagementController {
    @FXML private TableView<Position> positionTable;
    @FXML private TableColumn<Position, Integer> posIdCol;
    @FXML private TableColumn<Position, String> posNameCol;
    @FXML private TableColumn<Position, String> posLevelCol;
    @FXML private TableColumn<Position, Integer> baseSalaryLevelCol;
    @FXML private TableColumn<Position, Void> actionCol;

    private final PositionAdminService service = new PositionAdminService();
    private final ObservableList<Position> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        posIdCol.setCellValueFactory(new PropertyValueFactory<>("posId"));
        posNameCol.setCellValueFactory(new PropertyValueFactory<>("posName"));
        posLevelCol.setCellValueFactory(new PropertyValueFactory<>("posLevel"));
        baseSalaryLevelCol.setCellValueFactory(new PropertyValueFactory<>("baseSalaryLevel"));

        initActionButtons();
        loadPositionData();
    }

    public void loadPositionData() {
        new Thread(() -> {
            try {
                var data = service.getAllPositions();
                Platform.runLater(() -> {
                    list.setAll(data);
                    positionTable.setItems(list);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initActionButtons() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("修改");
            private final Button delBtn = new Button("删除");
            private final HBox box = new HBox(10, editBtn, delBtn);

            {
                editBtn.getStyleClass().add("action-button-small");
                delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
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

    @FXML private void handleNewPosition() { showEditDialog(new Position()); }

    private void showEditDialog(Position p) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(p.getPosId() == null ? "新增岗位" : "修改岗位");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField nameField = new TextField(p.getPosName());
        TextField levelField = new TextField(p.getPosLevel());
        TextField salIdField = new TextField(p.getBaseSalaryLevel() == null ? "" : String.valueOf(p.getBaseSalaryLevel()));

        grid.add(new Label("岗位名称:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("职级等级:"), 0, 1); grid.add(levelField, 1, 1);
        grid.add(new Label("薪标ID:"), 0, 2); grid.add(salIdField, 1, 2);

        Button saveBtn = new Button("保存提交");
        saveBtn.getStyleClass().add("action-button");
        saveBtn.setOnAction(e -> {
            try {
                p.setPosName(nameField.getText());
                p.setPosLevel(levelField.getText());
                p.setBaseSalaryLevel(Integer.parseInt(salIdField.getText()));

                if (p.getPosId() == null) service.createPosition(p);
                else service.updatePosition(p.getPosId(), p);

                stage.close();
                loadPositionData();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "保存失败: " + ex.getMessage()).show();
            }
        });

        VBox root = new VBox(20, grid, saveBtn);
        root.setStyle("-fx-alignment: center; -fx-padding: 10;");
        stage.setScene(new Scene(root, 350, 300));
        stage.show();
    }

    private void handleDelete(Position p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除岗位 [" + p.getPosName() + "] 吗？");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    service.deletePosition(p.getPosId());
                    loadPositionData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}