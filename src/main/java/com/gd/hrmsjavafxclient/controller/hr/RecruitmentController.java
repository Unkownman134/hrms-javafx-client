package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.Candidate;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 招聘管理控制器 - 用于展示候选人列表，通过 API 获取数据。
 * 修复了表格职位名显示和新增候选人 400 错误。
 */
public class RecruitmentController implements HRSubController {

    @FXML private Label titleLabel;
    @FXML private TableView<Candidate> candidateTable;
    // 绑定到 Candidate 模型的属性
    @FXML private TableColumn<Candidate, Integer> candIDColumn;
    @FXML private TableColumn<Candidate, String> nameColumn;
    @FXML private TableColumn<Candidate, String> phoneColumn;
    @FXML private TableColumn<Candidate, String> emailColumn;
    @FXML private TableColumn<Candidate, String> positionColumn; // 绑定到 applyPositionName
    @FXML private TableColumn<Candidate, LocalDate> interviewDateColumn;
    @FXML private TableColumn<Candidate, String> resultColumn;

    private final HRDataService dataService = new HRDataService();
    private ObservableList<Candidate> candidateList = FXCollections.observableArrayList();
    private String authToken;

    // 用于存储和映射职位数据
    private List<Position> allPositions;
    private Map<Integer, String> positionIdToNameMap;

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        // 先加载职位数据，再加载候选人数据，以确保能正确映射职位名称
        loadPositionData();
    }

    @FXML
    public void initialize() {
        // 确保 ID 和 申请职位 的列绑定正确
        candIDColumn.setCellValueFactory(new PropertyValueFactory<>("candID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // 绑定到辅助属性 applyPositionName，这是显示职位名称的关键
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("applyPositionName"));

        interviewDateColumn.setCellValueFactory(new PropertyValueFactory<>("interviewDate"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));

        candidateTable.setItems(candidateList);
    }

    // 关键方法 1: 加载所有职位数据 (异步)
    private void loadPositionData() {
        Task<List<Position>> loadTask = new Task<>() {
            @Override
            protected List<Position> call() throws Exception {
                // 调用 HRDataService 获取所有职位
                return dataService.getAllPositions(authToken);
            }

            @Override
            protected void succeeded() {
                allPositions = getValue();
                if (allPositions != null && !allPositions.isEmpty()) {
                    // 构建 ID -> Name 的映射表，用于后续转换
                    positionIdToNameMap = allPositions.stream()
                            .collect(Collectors.toMap(Position::getPosId, Position::getPosName));
                } else {
                    positionIdToNameMap = Collections.emptyMap();
                    showAlert("提示 💡", "未能加载职位数据，请检查后端服务是否启动。");
                }
                loadCandidateData(); // 职位加载成功后，再加载候选人数据
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("错误 ❌", "加载职位数据失败: " + getException().getMessage()));
                // 即使失败，也要尝试加载候选人数据，只是职位名会显示为 ID
                loadCandidateData();
            }
        };
        new Thread(loadTask).start();
    }


    // 关键方法 2: 加载候选人数据 (异步并映射职位名称)
    private void loadCandidateData() {
        // 确保 positionIdToNameMap 被初始化，即使是空的
        if (positionIdToNameMap == null) {
            positionIdToNameMap = Collections.emptyMap();
        }

        Task<List<Candidate>> loadTask = new Task<>() {
            @Override
            protected List<Candidate> call() throws Exception {
                List<Candidate> candidates = dataService.getAllCandidates(authToken);

                // 关键：将 applyPositionId 映射为 applyPositionName
                for (Candidate candidate : candidates) {
                    // 使用 applyPositionId 查找对应的名称
                    String posName = positionIdToNameMap.get(candidate.getApplyPositionId());
                    if (posName != null) {
                        candidate.setApplyPositionName(posName);
                    } else {
                        // 如果找不到 ID 对应的名称，显示未知或 ID
                        candidate.setApplyPositionName("未知职位 (ID: " + candidate.getApplyPositionId() + ")");
                    }
                }

                return candidates;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    candidateList.clear();
                    candidateList.addAll(getValue());
                    showAlert("成功 🎉", "候选人列表加载完成。");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> showAlert("错误 ❌", "加载候选人列表失败: " + getException().getMessage()));
            }
        };
        new Thread(loadTask).start();
    }

    // 关键方法 3: 处理新增候选人按钮事件 (解决 400 错误)
    @FXML
    private void handleAddCandidate() {
        if (allPositions == null || allPositions.isEmpty()) {
            showAlert("提示 💡", "职位数据尚未加载或为空，无法新增候选人。请检查 API 或稍候重试！");
            return;
        }

        // 尝试打开新增候选人对话框
        Optional<Candidate> result = showNewCandidateDialog(allPositions);

        result.ifPresent(newCandidate -> {
            // 此时 newCandidate 应该已经包含了有效的 applyPositionId
            Task<Boolean> addTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    // 调用 HRDataService 的新增方法
                    // HRDataService 修正后，如果失败会抛出 RuntimeException
                    return dataService.addCandidate(newCandidate, authToken);
                }

                @Override
                protected void succeeded() {
                    if (getValue()) {
                        Platform.runLater(() -> {
                            showAlert("成功 🎉", "新候选人 " + newCandidate.getName() + " 已成功添加！");
                            loadCandidateData(); // 刷新列表，保证职位名称能正确显示
                        });
                    }
                }

                @Override
                protected void failed() {
                    // 关键：捕获 RuntimeException，获取详细的 400 错误信息！
                    Throwable e = getException();
                    // 尝试获取根源异常的 message，通常是 ServiceUtil 抛出的详细 API 错误信息
                    String errorMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                    Platform.runLater(() -> showAlert("新增失败 😭", "新增候选人请求发生错误: \n" + errorMessage));
                }
            };
            new Thread(addTask).start();
        });
    }

    // 辅助方法 4: 弹出新增候选人对话框（包含职位选择 ComboBox）
    private Optional<Candidate> showNewCandidateDialog(List<Position> positions) {
        Dialog<Candidate> dialog = new Dialog<>();
        dialog.setTitle("新增候选人");
        dialog.setHeaderText("请输入候选人详细信息并选择申请职位 (职位 ID 必须有效哦！)");

        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        DatePicker interviewDateField = new DatePicker(LocalDate.now());

        // 关键：使用 ComboBox 选择 Position 对象
        ComboBox<Position> positionCombo = new ComboBox<>(FXCollections.observableArrayList(positions));
        positionCombo.setPromptText("请选择职位");

        // 设置 ComboBox 如何显示 Position 名称
        positionCombo.setConverter(new StringConverter<Position>() {
            @Override
            public String toString(Position position) {
                // 显示职位名称
                return position != null ? position.getPosName() : "";
            }
            @Override
            public Position fromString(String string) {
                // 仅用于输入，这里简化处理，通常不需要从字符串转换回来
                return positions.stream().filter(p -> p.getPosName().equals(string)).findFirst().orElse(null);
            }
        });


        grid.add(new Label("姓名:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("电话:"), 0, 1); grid.add(phoneField, 1, 1);
        grid.add(new Label("邮箱:"), 0, 2); grid.add(emailField, 1, 2);
        grid.add(new Label("申请职位:"), 0, 3); grid.add(positionCombo, 1, 3); // 职位选择
        grid.add(new Label("面试日期:"), 0, 4); grid.add(interviewDateField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // 转换结果
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                // 验证输入
                if (positionCombo.getValue() == null) {
                    showAlert("输入错误 ❌", "请选择申请职位！");
                    return null; // 返回 null，表示输入不完整
                }
                if (nameField.getText().isEmpty() || phoneField.getText().isEmpty() || emailField.getText().isEmpty()) {
                    showAlert("输入错误 ❌", "姓名、电话和邮箱不能为空！");
                    return null; // 返回 null
                }

                Candidate newCandidate = new Candidate();
                newCandidate.setName(nameField.getText());
                // 暂时性别写死，实际应从输入获取
                newCandidate.setGender("男");
                newCandidate.setPhone(phoneField.getText());
                newCandidate.setEmail(emailField.getText());
                newCandidate.setInterviewDate(interviewDateField.getValue());
                newCandidate.setResult("待定"); // 默认结果

                // 关键：从选中的 Position 对象中获取正确的 PosID
                newCandidate.setApplyPositionId(positionCombo.getValue().getPosId());

                return newCandidate;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // 处理结果更新按钮的点击事件
    @FXML
    private void handleUpdateResult() {
        Candidate selectedCandidate = candidateTable.getSelectionModel().getSelectedItem();
        if (selectedCandidate == null) {
            showAlert("提示 💡", "请先选择一个候选人！");
            return;
        }

        List<String> choices = List.of("录用", "淘汰", "待定");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selectedCandidate.getResult(), choices);
        dialog.setTitle("更新候选人结果");
        dialog.setHeaderText("请为候选人 " + selectedCandidate.getName() + " 选择处理结果");
        dialog.setContentText("结果:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newResult -> {
            Task<Boolean> updateTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    // 调用 HRDataService 的更新方法
                    return dataService.updateCandidateResult(selectedCandidate.getCandID(), newResult, authToken);
                }

                @Override
                protected void succeeded() {
                    if (getValue()) {
                        Platform.runLater(() -> {
                            showAlert("成功 🎉", "候选人 " + selectedCandidate.getName() + " 的结果已更新为: " + newResult);
                            loadCandidateData(); // 刷新列表
                        });
                    } else {
                        Platform.runLater(() -> showAlert("失败 😢", "更新结果失败，请检查网络。"));
                    }
                }

                @Override
                protected void failed() {
                    Platform.runLater(() -> showAlert("错误 ❌", "更新结果请求发生错误: " + getException().getMessage()));
                }
            };
            new Thread(updateTask).start();
        });
    }


    // 处理刷新列表按钮的点击事件
    @FXML
    private void handleRefresh() {
        // 刷新会重新加载候选人列表，并重新映射职位名称
        loadCandidateData();
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