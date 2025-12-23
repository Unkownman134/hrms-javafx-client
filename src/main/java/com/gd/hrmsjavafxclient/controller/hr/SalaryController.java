package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.SalaryStandard;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;

public class SalaryController implements HRSubController {

    @FXML private TableView<SalaryStandard> salaryTable;
    @FXML private TableColumn<SalaryStandard, Integer> stdIdCol;
    @FXML private TableColumn<SalaryStandard, String> nameCol;
    @FXML private TableColumn<SalaryStandard, Double> basicCol;
    @FXML private TableColumn<SalaryStandard, Double> mealCol;
    @FXML private TableColumn<SalaryStandard, Double> allowanceCol;
    @FXML private TableColumn<SalaryStandard, Double> totalCol;
    @FXML private TableColumn<SalaryStandard, Void> actionCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        stdIdCol.setCellValueFactory(new PropertyValueFactory<>("stdId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("standardName"));
        basicCol.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));
        mealCol.setCellValueFactory(new PropertyValueFactory<>("mealAllowance"));
        allowanceCol.setCellValueFactory(new PropertyValueFactory<>("allowances"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        setupActionColumn();
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadData();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final HBox container = new HBox(10, editBtn, deleteBtn);
            {
                container.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML private void handleRefresh(ActionEvent e) { loadData(); }
    @FXML private void handleNewSalary(ActionEvent e) { showEditDialog(new SalaryStandard()); }

    private void loadData() {
        if (authToken == null) return;
        Task<List<SalaryStandard>> task = new Task<>() {
            @Override protected List<SalaryStandard> call() { return hrDataService.getAllSalaryStandards(authToken); }
            @Override protected void succeeded() { salaryTable.setItems(FXCollections.observableArrayList(getValue())); }
        };
        new Thread(task).start();
    }

    private void showEditDialog(SalaryStandard std) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(std.getStdId() == null ? "新增薪资标准" : "编辑薪资标准");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField(std.getStandardName());
        Spinner<Double> basicSpin = new Spinner<>(0.0, 100000.0, std.getBasicSalary() == null ? 0.0 : std.getBasicSalary(), 100.0);
        basicSpin.setEditable(true);
        Spinner<Double> mealSpin = new Spinner<>(0.0, 5000.0, std.getMealAllowance() == null ? 0.0 : std.getMealAllowance(), 10.0);
        mealSpin.setEditable(true);
        Spinner<Double> allowSpin = new Spinner<>(0.0, 20000.0, std.getAllowances() == null ? 0.0 : std.getAllowances(), 50.0);
        allowSpin.setEditable(true);

        grid.add(new Label("标准名称:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("基本工资:"), 0, 1); grid.add(basicSpin, 1, 1);
        grid.add(new Label("餐补:"), 0, 2); grid.add(mealSpin, 1, 2);
        grid.add(new Label("其他补贴:"), 0, 3); grid.add(allowSpin, 1, 3);

        Button saveBtn = new Button("保存");
        saveBtn.setOnAction(e -> {
            std.setStandardName(nameField.getText());
            std.setBasicSalary(basicSpin.getValue());
            std.setMealAllowance(mealSpin.getValue());
            std.setAllowances(allowSpin.getValue());

            new Thread(() -> {
                boolean ok = (std.getStdId() == null) ? hrDataService.addSalaryStandard(std, authToken) : hrDataService.updateSalaryStandard(std, authToken);
                Platform.runLater(() -> { if(ok) { stage.close(); loadData(); } });
            }).start();
        });

        VBox layout = new VBox(20, grid, saveBtn);
        layout.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(layout, 350, 400));
        stage.show();
    }

    private void handleDelete(SalaryStandard std) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "确定删除薪资标准 [" + std.getStandardName() + "] 吗？");
        a.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = hrDataService.deleteSalaryStandard(std.getStdId(), authToken);
                    Platform.runLater(() -> { if(ok) loadData(); });
                }).start();
            }
        });
    }
}