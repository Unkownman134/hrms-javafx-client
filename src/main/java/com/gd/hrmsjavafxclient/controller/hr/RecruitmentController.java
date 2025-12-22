package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.*;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 招聘管理子视图控制器
 * 处理候选人展示及录用流程 (๑•̀ㅂ•́)و✧
 */
public class RecruitmentController implements HRSubController {

    @FXML private TableView<Candidate> candidateTable;
    @FXML private TableColumn<Candidate, Integer> candIDColumn;
    @FXML private TableColumn<Candidate, String> nameColumn;
    @FXML private TableColumn<Candidate, String> phoneColumn;
    @FXML private TableColumn<Candidate, String> emailColumn;
    @FXML private TableColumn<Candidate, String> positionColumn;
    @FXML private TableColumn<Candidate, String> resultColumn;
    @FXML private TableColumn<Candidate, LocalDate> interviewDateColumn;
    @FXML private TableColumn<Candidate, String> genderColumn;

    private final HRDataService dataService = new HRDataService();
    private ObservableList<Candidate> candidateList = FXCollections.observableArrayList();
    private String authToken;
    private List<Position> allPositions;

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    private void setupTableColumns() {
        candIDColumn.setCellValueFactory(new PropertyValueFactory<>("candID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("applyPosition"));
        interviewDateColumn.setCellValueFactory(new PropertyValueFactory<>("interviewDate"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadCandidateData();
    }

    @FXML
    private void handleRefresh() {
        loadCandidateData();
    }

    private void loadCandidateData() {
        if (authToken == null) return;

        Task<List<Candidate>> task = new Task<>() {
            @Override
            protected List<Candidate> call() throws Exception {
                return dataService.getAllCandidates(authToken);
            }
            @Override
            protected void succeeded() {
                candidateList.setAll(getValue());
                candidateTable.setItems(candidateList);
            }
            @Override
            protected void failed() {
                showAlert("加载失败 ❌", "获取招聘列表数据失败！");
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleAddCandidate(ActionEvent event) {
        showAlert("提示 💡", "新增候选人登记表单正在制作中，先喝杯茶吧~");
    }

    @FXML
    private void handleUpdateResult(ActionEvent event) {
        Candidate selected = candidateTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("警告 ⚠️", "请先在列表中选择一名候选人哦！");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("面试中", "拟录用", "淘汰");
        dialog.setTitle("处理面试结果");
        dialog.setHeaderText("更改候选人 [" + selected.getName() + "] 的状态");
        dialog.setContentText("请选择最终结果：");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(res -> {
            new Thread(() -> {
                boolean success = dataService.updateCandidateResult(selected.getCandID(), res, authToken);
                Platform.runLater(() -> {
                    if (success) {
                        loadCandidateData();
                    } else {
                        showAlert("失败 ❌", "更新结果失败，请重试。");
                    }
                });
            }).start();
        });
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}