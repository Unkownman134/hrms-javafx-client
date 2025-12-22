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
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class PositionController implements HRSubController {

    @FXML private TableView<Position> positionTable;
    @FXML private TableColumn<Position, Integer> posIdCol;
    @FXML private TableColumn<Position, String> posNameCol;
    @FXML private TableColumn<Position, String> levelCol;
    @FXML private TableColumn<Position, Integer> salaryLevelCol; // 对应 baseSalaryLevel

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        // ✨ 修复：对应 Model 属性名
        posIdCol.setCellValueFactory(new PropertyValueFactory<>("posId"));
        posNameCol.setCellValueFactory(new PropertyValueFactory<>("posName"));
        levelCol.setCellValueFactory(new PropertyValueFactory<>("posLevel"));
        salaryLevelCol.setCellValueFactory(new PropertyValueFactory<>("baseSalaryLevel"));
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadPositionData();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPositionData();
    }

    private void loadPositionData() {
        if (authToken == null) return;
        Task<List<Position>> loadTask = new Task<>() {
            @Override protected List<Position> call() throws Exception {
                return hrDataService.getAllPositions(authToken);
            }
            @Override protected void succeeded() {
                List<Position> result = getValue();
                Platform.runLater(() -> {
                    positionTable.setItems(FXCollections.observableArrayList(result));
                });
            }
        };
        new Thread(loadTask).start();
    }

    @FXML private void handleAddPosition(ActionEvent event) {
        showAlert("提示 💡", "功能开发中...");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}