package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.SalaryStandard;
import com.gd.hrmsjavafxclient.service.admin.SalaryStandardAdminService;
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

public class SalaryStandardManagementController {
    @FXML private TableView<SalaryStandard> standardTable;
    @FXML private TableColumn<SalaryStandard, Integer> standardIdCol;
    @FXML private TableColumn<SalaryStandard, String> standardNameCol;
    @FXML private TableColumn<SalaryStandard, Double> baseSalaryCol;
    @FXML private TableColumn<SalaryStandard, Double> allowanceCol;
    @FXML private TableColumn<SalaryStandard, Double> bonusCol;
    @FXML private TableColumn<SalaryStandard, Double> totalAmountCol;
    @FXML private TableColumn<SalaryStandard, Void> actionCol;

    private final SalaryStandardAdminService service = new SalaryStandardAdminService();
    private final ObservableList<SalaryStandard> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        standardIdCol.setCellValueFactory(new PropertyValueFactory<>("stdId"));
        standardNameCol.setCellValueFactory(new PropertyValueFactory<>("standardName"));
        baseSalaryCol.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));
        allowanceCol.setCellValueFactory(new PropertyValueFactory<>("mealAllowance"));
        bonusCol.setCellValueFactory(new PropertyValueFactory<>("allowances"));
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        setupActionColumn();
        loadStandardData();
    }

    @FXML
    private void loadStandardData() {
        new Thread(() -> {
            try {
                var data = service.getAllSalaryStandards();
                Platform.runLater(() -> {
                    list.setAll(data);
                    standardTable.setItems(list);
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
    private void handleNewStandard() {
        showEditDialog(new SalaryStandard());
    }

    private void showEditDialog(SalaryStandard s) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(s.getStdId() == null ? "制定新薪酬标准" : "修改薪酬标准");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setStyle("-fx-padding: 25;");

        TextField nameIn = new TextField(s.getStandardName());
        TextField baseIn = new TextField(String.valueOf(s.getBasicSalary() == null ? "" : s.getBasicSalary()));
        TextField mealIn = new TextField(String.valueOf(s.getMealAllowance() == null ? "" : s.getMealAllowance()));
        TextField otherIn = new TextField(String.valueOf(s.getAllowances() == null ? "" : s.getAllowances()));

        grid.add(new Label("标准名称:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("基本工资:"), 0, 1); grid.add(baseIn, 1, 1);
        grid.add(new Label("餐补津贴:"), 0, 2); grid.add(mealIn, 1, 2);
        grid.add(new Label("其他补贴:"), 0, 3); grid.add(otherIn, 1, 3);

        Button save = new Button("保存标准");
        save.getStyleClass().add("action-button");
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            try {
                s.setStandardName(nameIn.getText());
                s.setBasicSalary(Double.parseDouble(baseIn.getText()));
                s.setMealAllowance(Double.parseDouble(mealIn.getText()));
                s.setAllowances(Double.parseDouble(otherIn.getText()));

                if (s.getStdId() == null) {
                    service.createSalaryStandard(s);
                } else {
                    service.updateSalaryStandard(s.getStdId(), s);
                }

                stage.close();
                loadStandardData();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "保存失败：请输入正确的数字格式！").show();
            }
        });

        VBox root = new VBox(20, grid, save);
        root.setStyle("-fx-alignment: center; -fx-padding: 10;");
        stage.setScene(new Scene(root, 380, 450));
        stage.show();
    }

    private void handleDelete(SalaryStandard s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除标准 [" + s.getStandardName() + "] 吗？");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.deleteSalaryStandard(s.getStdId());
                    loadStandardData();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "删除失败: " + ex.getMessage()).show();
                }
            }
        });
    }
}