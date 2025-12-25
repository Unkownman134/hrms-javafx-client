package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.*;
import com.gd.hrmsjavafxclient.service.hr.RecruitmentService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 招聘管理子视图控制器 - 终极修复版 (๑•̀ㅂ•́)و✧
 */
public class RecruitmentController implements HRSubController {

    @FXML private TableView<Candidate> candidateTable;
    @FXML private TableColumn<Candidate, Integer> candIDColumn;
    @FXML private TableColumn<Candidate, String> nameColumn;
    @FXML private TableColumn<Candidate, String> genderColumn;
    @FXML private TableColumn<Candidate, String> phoneColumn;
    @FXML private TableColumn<Candidate, String> emailColumn;
    @FXML private TableColumn<Candidate, String> positionColumn;
    @FXML private TableColumn<Candidate, String> resultColumn;
    @FXML private TableColumn<Candidate, LocalDate> interviewDateColumn;

    private final RecruitmentService recruitmentService = new RecruitmentService();
    private ObservableList<Candidate> candidateList = FXCollections.observableArrayList();
    private String authToken;

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

        // 绑定模型中的 applyPositionName 属性
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("applyPositionName"));

        interviewDateColumn.setCellValueFactory(new PropertyValueFactory<>("interviewDate"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));

        candidateTable.setItems(candidateList);
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

    /**
     * 加载数据并关联职位名称
     */
    private void loadCandidateData() {
        if (authToken == null) return;

        Task<List<Candidate>> task = new Task<>() {
            @Override
            protected List<Candidate> call() throws Exception {
                // 1. 获取所有职位并转为 Map<ID, Name>
                List<Position> positions = recruitmentService.getAllPositions(authToken);
                Map<Integer, String> posMap = positions.stream()
                        .collect(Collectors.toMap(Position::getPosId, Position::getPosName, (oldVal, newVal) -> oldVal));

                // 2. 获取候选人
                List<Candidate> candidates = recruitmentService.getAllCandidates(authToken);

                // 3. 匹配名称
                for (Candidate c : candidates) {
                    String name = posMap.get(c.getApplyPositionId());
                    c.setApplyPositionName(name != null ? name : "职位ID: " + c.getApplyPositionId());
                }
                return candidates;
            }

            @Override
            protected void succeeded() {
                candidateList.setAll(getValue());
            }

            @Override
            protected void failed() {
                showAlert("错误", "获取招聘数据失败，请检查后端服务！");
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleAddCandidate(ActionEvent event) {
        // 异步获取职位列表后再弹窗
        new Thread(() -> {
            List<Position> positions = recruitmentService.getAllPositions(authToken);
            Platform.runLater(() -> showAddDialog(positions));
        }).start();
    }

    private void showAddDialog(List<Position> positions) {
        Dialog<Candidate> dialog = new Dialog<>();
        dialog.setTitle("新增候选人");
        dialog.setHeaderText("录入面试者信息");

        ButtonType saveButton = new ButtonType("提交", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("男", "女"));
        genderBox.getSelectionModel().selectFirst();

        // 职位下拉框
        ComboBox<Position> posBox = new ComboBox<>(FXCollections.observableArrayList(positions));
        posBox.setConverter(new StringConverter<Position>() {
            @Override public String toString(Position p) { return p == null ? "" : p.getPosName(); }
            @Override public Position fromString(String s) { return null; }
        });

        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());

        grid.add(new Label("姓名:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("性别:"), 0, 1); grid.add(genderBox, 1, 1);
        grid.add(new Label("职位:"), 0, 2); grid.add(posBox, 1, 2);
        grid.add(new Label("手机:"), 0, 3); grid.add(phoneField, 1, 3);
        grid.add(new Label("邮箱:"), 0, 4); grid.add(emailField, 1, 4);
        grid.add(new Label("日期:"), 0, 5); grid.add(datePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveButton) {
                Candidate c = new Candidate();
                c.setName(nameField.getText());
                c.setGender(genderBox.getValue());
                c.setPhone(phoneField.getText());
                c.setEmail(emailField.getText());
                c.setInterviewDate(datePicker.getValue());
                c.setResult("面试中");
                if (posBox.getValue() != null) {
                    c.setApplyPositionId(posBox.getValue().getPosId());
                }
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            new Thread(() -> {
                if (recruitmentService.createCandidate(c, authToken)) {
                    Platform.runLater(this::loadCandidateData);
                }
            }).start();
        });
    }

    @FXML
    private void handleUpdateResult(ActionEvent event) {
        Candidate selected = candidateTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选中一行数据！");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getResult(), "面试中", "拟录用", "淘汰");
        dialog.setTitle("录入结果");
        dialog.setHeaderText("更新候选人状态: " + selected.getName());

        dialog.showAndWait().ifPresent(res -> {
            new Thread(() -> {
                if (recruitmentService.updateCandidateResult(selected.getCandID(), res, authToken)) {
                    Platform.runLater(this::loadCandidateData);
                }
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