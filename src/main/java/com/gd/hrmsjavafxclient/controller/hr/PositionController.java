package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Position;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class PositionController implements HRSubController {

    @FXML private TableView<Position> positionTable;
    @FXML private TableColumn<Position, Integer> posIdCol;
    @FXML private TableColumn<Position, String> posNameCol;
    @FXML private TableColumn<Position, String> levelCol;
    @FXML private TableColumn<Position, Integer> salaryLevelCol;
    @FXML private TableColumn<Position, Void> actionCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        posIdCol.setCellValueFactory(new PropertyValueFactory<>("posId"));
        posNameCol.setCellValueFactory(new PropertyValueFactory<>("posName"));
        levelCol.setCellValueFactory(new PropertyValueFactory<>("posLevel"));
        salaryLevelCol.setCellValueFactory(new PropertyValueFactory<>("baseSalaryLevel"));
        setupActionColumn();
    }

    @Override
    public void setHRContext(CurrentUserInfo u, String t) {
        this.authToken = t;
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
    @FXML private void handleNewPosition(ActionEvent e) { showEditDialog(new Position()); }

    private void loadData() {
        if (authToken == null) return;
        Task<List<Position>> task = new Task<>() {
            @Override protected List<Position> call() throws IOException, InterruptedException { return hrDataService.getAllPositions(authToken); }
            @Override protected void succeeded() { positionTable.setItems(FXCollections.observableArrayList(getValue())); }
        };
        new Thread(task).start();
    }

    private void showEditDialog(Position p) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        TextField nameIn = new TextField(p.getPosName());
        TextField levelIn = new TextField(p.getPosLevel());
        Spinner<Integer> salarySpin = new Spinner<>(1, 10, p.getBaseSalaryLevel() == null ? 1 : p.getBaseSalaryLevel());
        Button save = new Button("保存");
        save.setOnAction(e -> {
            p.setPosName(nameIn.getText()); p.setPosLevel(levelIn.getText()); p.setBaseSalaryLevel(salarySpin.getValue());
            new Thread(() -> {
                boolean ok = (p.getPosId() == null) ? hrDataService.addPosition(p, authToken) : hrDataService.updatePosition(p, authToken);
                Platform.runLater(() -> { if(ok) { stage.close(); loadData(); } });
            }).start();
        });
        VBox root = new VBox(10, new Label("职位名:"), nameIn, new Label("职级:"), levelIn, new Label("薪资等级:"), salarySpin, save);
        root.setPadding(new javafx.geometry.Insets(20));
        stage.setScene(new Scene(root, 300, 350));
        stage.show();
    }

    private void handleDelete(Position p) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "确认删除岗位 " + p.getPosName() + " 吗？");
        a.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    boolean ok = hrDataService.deletePosition(p.getPosId(), authToken);
                    Platform.runLater(() -> { if(ok) loadData(); });
                }).start();
            }
        });
    }
}