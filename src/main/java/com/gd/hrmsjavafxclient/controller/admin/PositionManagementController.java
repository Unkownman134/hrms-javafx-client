package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.model.SalaryStandard;
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
import javafx.util.StringConverter;

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

        setupActionColumn();
        loadPositionData();
    }

    @FXML
    private void loadPositionData() {
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
    private void handleNewPosition() {
        showEditDialog(new Position());
    }

    private void showEditDialog(Position p) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(p.getPosId() == null ? "新增职位" : "修改职位");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setStyle("-fx-padding: 25;");

        TextField nameField = new TextField(p.getPosName());
        TextField levelField = new TextField(p.getPosLevel());

        ComboBox<SalaryStandard> salaryCombo = new ComboBox<>();
        salaryCombo.setPromptText("请选择薪资标准");
        salaryCombo.setMaxWidth(Double.MAX_VALUE);

        salaryCombo.setConverter(new StringConverter<>() {
            @Override public String toString(SalaryStandard s) {
                return s == null ? "" : s.getStandardName() + " (￥" + s.getTotalAmount() + ")";
            }
            @Override public SalaryStandard fromString(String string) { return null; }
        });

        new Thread(() -> {
            try {
                var standards = service.getAllSalaryStandards();
                Platform.runLater(() -> {
                    salaryCombo.setItems(FXCollections.observableArrayList(standards));
                    if (p.getBaseSalaryLevel() != null) {
                        standards.stream()
                                .filter(s -> s.getStdId().equals(p.getBaseSalaryLevel()))
                                .findFirst()
                                .ifPresent(salaryCombo::setValue);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        grid.add(new Label("职位名称:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("职位级别:"), 0, 1); grid.add(levelField, 1, 1);
        grid.add(new Label("薪资标准:"), 0, 2); grid.add(salaryCombo, 1, 2);

        Button saveBtn = new Button("保存提交");
        saveBtn.getStyleClass().add("action-button");
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        saveBtn.setOnAction(e -> {
            try {
                p.setPosName(nameField.getText());
                p.setPosLevel(levelField.getText());

                if (salaryCombo.getValue() != null) {
                    p.setBaseSalaryLevel(salaryCombo.getValue().getStdId());
                } else {
                    throw new RuntimeException("请选择薪资标准！");
                }

                new Thread(() -> {
                    try {
                        if (p.getPosId() == null) service.createPosition(p);
                        else service.updatePosition(p.getPosId(), p);
                        Platform.runLater(() -> {
                            stage.close();
                            loadPositionData();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "保存失败: " + ex.getMessage()).show());
                    }
                }).start();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
            }
        });

        VBox root = new VBox(20, grid, saveBtn);
        root.setStyle("-fx-alignment: center; -fx-padding: 10;");
        stage.setScene(new Scene(root, 400, 350));
        stage.show();
    }

    private void handleDelete(Position p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除岗位 [" + p.getPosName() + "] 吗？");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        service.deletePosition(p.getPosId());
                        Platform.runLater(this::loadPositionData);
                    } catch (Exception ex) {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "删除失败: " + ex.getMessage()).show());
                    }
                }).start();
            }
        });
    }
}