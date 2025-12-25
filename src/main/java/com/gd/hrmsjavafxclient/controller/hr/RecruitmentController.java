package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.*;
import com.gd.hrmsjavafxclient.service.hr.RecruitmentService;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * 招聘管理子视图控制器 - 全字段入职手续 & ServiceUtil 兼容版 ✨
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

    private RecruitmentService recruitmentService = new RecruitmentService();
    private ObservableList<Candidate> candidateData = FXCollections.observableArrayList();

    // 缓存基础数据用于下拉框
    private List<Position> allPositions = new ArrayList<>();
    private List<Department> allDepts = new ArrayList<>();
    private List<Employee> allManagers = new ArrayList<>();

    private String authToken;
    private CurrentUserInfo currentUser;

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
        loadInitialData();
    }

    @FXML
    public void initialize() {
        candIDColumn.setCellValueFactory(new PropertyValueFactory<>("candID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        interviewDateColumn.setCellValueFactory(new PropertyValueFactory<>("interviewDate"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("applyPositionName"));

        candidateTable.setItems(candidateData);
    }

    /**
     * 加载初始数据，兼容 ServiceUtil 的异常处理
     */
    private void loadInitialData() {
        new Thread(() -> {
            try {
                // 1. 加载职位
                allPositions = recruitmentService.getAllPositions(authToken);

                // 2. 加载部门 (对应 /api/departments)
                allDepts = ServiceUtil.sendGet("/departments", authToken, new TypeReference<List<Department>>() {})
                        .orElse(new ArrayList<>());

                // 3. 加载员工列表作为上级经理 (对应 /api/employees)
                allManagers = ServiceUtil.sendGet("/employees", authToken, new TypeReference<List<Employee>>() {})
                        .orElse(new ArrayList<>());

                // 4. 加载候选人
                loadCandidateData();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                showAlert("错误 ❌", "基础数据加载失败，请检查网络连接。");
            }
        }).start();
    }

    private void loadCandidateData() {
        new Thread(() -> {
            List<Candidate> list = recruitmentService.getAllCandidates(authToken);
            for (Candidate c : list) {
                if ((c.getApplyPositionName() == null || c.getApplyPositionName().isEmpty()) && allPositions != null) {
                    allPositions.stream()
                            .filter(p -> p.getPosId().equals(c.getApplyPositionId()))
                            .findFirst()
                            .ifPresent(p -> c.setApplyPositionName(p.getPosName()));
                }
            }
            Platform.runLater(() -> {
                candidateData.clear();
                candidateData.addAll(list);
            });
        }).start();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadInitialData();
    }

    @FXML
    private void handleUpdateResult(ActionEvent event) {
        Candidate selected = candidateTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示 ⚠️", "请先在列表中选中一名候选人哦！");
            return;
        }

        List<String> choices = Arrays.asList("待定", "录用", "淘汰");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getResult(), choices);
        dialog.setTitle("结果录入");
        dialog.setHeaderText("更新候选人: " + selected.getName() + " 的面试结果");
        dialog.setContentText("请选择最终状态:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(res -> {
            if ("录用".equals(res)) {
                showHireForm(selected);
            } else {
                new Thread(() -> {
                    boolean success = recruitmentService.updateCandidateResult(selected.getCandID(), res, authToken);
                    Platform.runLater(() -> {
                        if (success) loadInitialData();
                        else showAlert("失败 ❌", "更新失败，请检查后端服务。");
                    });
                }).start();
            }
        });
    }

    /**
     * 🌸 录用入职大表单
     */
    private void showHireForm(Candidate candidate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🎉 录用入职确认");
        dialog.setHeaderText("正在为 " + candidate.getName() + " 办理入职手续");

        ButtonType hireButtonType = new ButtonType("确认入职并创建账号", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(hireButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // --- 员工字段 ---
        TextField nameField = new TextField(candidate.getName());
        TextField phoneField = new TextField(candidate.getPhone());
        TextField emailField = new TextField(candidate.getEmail());
        DatePicker joinDatePicker = new DatePicker(LocalDate.now());

        ComboBox<Department> deptBox = new ComboBox<>(FXCollections.observableArrayList(allDepts));
        deptBox.setConverter(new StringConverter<>() {
            public String toString(Department d) { return d == null ? "" : d.getDeptName(); }
            public Department fromString(String s) { return null; }
        });

        ComboBox<Position> posBox = new ComboBox<>(FXCollections.observableArrayList(allPositions));
        posBox.setConverter(new StringConverter<>() {
            public String toString(Position p) { return p == null ? "" : p.getPosName(); }
            public Position fromString(String s) { return null; }
        });
        // 预设选中当初投递的职位
        allPositions.stream().filter(p -> p.getPosId().equals(candidate.getApplyPositionId())).findFirst().ifPresent(posBox::setValue);

        ComboBox<Employee> managerBox = new ComboBox<>(FXCollections.observableArrayList(allManagers));
        managerBox.setConverter(new StringConverter<>() {
            public String toString(Employee e) { return e == null ? "" : e.getEmpName(); }
            public Employee fromString(String s) { return null; }
        });

        // --- 账号字段 ---
        TextField usernameField = new TextField();
        usernameField.setPromptText("建议使用手机号或拼音");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123456"); // 默认初始密码
        ComboBox<Integer> roleBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3));
        roleBox.setValue(3); // 默认设为 3 (普通员工)

        // 界面布局
        grid.add(new Label("姓名:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("电话:"), 0, 1); grid.add(phoneField, 1, 1);
        grid.add(new Label("邮箱:"), 0, 2); grid.add(emailField, 1, 2);
        grid.add(new Label("入职日期:"), 0, 3); grid.add(joinDatePicker, 1, 3);
        grid.add(new Label("分配部门:"), 0, 4); grid.add(deptBox, 1, 4);
        grid.add(new Label("分配职位:"), 0, 5); grid.add(posBox, 1, 5);
        grid.add(new Label("汇报经理:"), 0, 6); grid.add(managerBox, 1, 6);

        grid.add(new Separator(), 0, 7, 2, 1);

        grid.add(new Label("系统用户名:"), 0, 8); grid.add(usernameField, 1, 8);
        grid.add(new Label("登录密码:"), 0, 9); grid.add(passwordField, 1, 9);
        grid.add(new Label("角色权限:"), 0, 10); grid.add(roleBox, 1, 10);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == hireButtonType) {
            // 组装符合 /api/employees 要求的对象
            Employee emp = new Employee();
            emp.setEmpName(nameField.getText());
            emp.setGender(candidate.getGender());
            emp.setPhone(phoneField.getText());
            emp.setEmail(emailField.getText());
            emp.setJoinDate(joinDatePicker.getValue());
            emp.setStatus("在职");
            if (deptBox.getValue() != null) emp.setDeptId(deptBox.getValue().getDeptId());
            if (posBox.getValue() != null) emp.setPosId(posBox.getValue().getPosId());
            if (managerBox.getValue() != null) emp.setManagerId(managerBox.getValue().getEmpId());

            String uname = usernameField.getText();
            String pwd = passwordField.getText();
            // 注意：如果需要传角色ID，可能需要修改 RecruitmentService.hireCandidate 的参数

            new Thread(() -> {
                boolean success = recruitmentService.hireCandidate(
                        candidate.getCandID(), emp, uname, pwd, authToken);

                Platform.runLater(() -> {
                    if (success) {
                        showAlert("成功 🎊", candidate.getName() + " 的入职手续已办结！");
                        loadInitialData();
                    } else {
                        showAlert("失败 ❌", "办理入职时发生错误，请查看控制台日志。");
                    }
                });
            }).start();
        }
    }

    @FXML
    private void handleAddCandidate(ActionEvent event) {
        if (allPositions.isEmpty()) {
            new Thread(() -> {
                allPositions = recruitmentService.getAllPositions(authToken);
                Platform.runLater(() -> showAddCandidateDialog(allPositions));
            }).start();
        } else {
            showAddCandidateDialog(allPositions);
        }
    }

    private void showAddCandidateDialog(List<Position> positions) {
        Dialog<Candidate> dialog = new Dialog<>();
        dialog.setTitle("➕ 新增候选人");
        dialog.setHeaderText("录入候选人基础面试信息");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10));

        TextField nameField = new TextField();
        ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("男", "女"));
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        ComboBox<Position> posBox = new ComboBox<>(FXCollections.observableArrayList(positions));

        posBox.setConverter(new StringConverter<>() {
            public String toString(Position p) { return p == null ? "" : p.getPosName(); }
            public Position fromString(String s) { return null; }
        });

        grid.add(new Label("姓名:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("性别:"), 0, 1); grid.add(genderBox, 1, 1);
        grid.add(new Label("电话:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("邮箱:"), 0, 3); grid.add(emailField, 1, 3);
        grid.add(new Label("申请职位:"), 0, 4); grid.add(posBox, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Candidate c = new Candidate();
                c.setName(nameField.getText());
                c.setGender(genderBox.getValue());
                c.setPhone(phoneField.getText());
                c.setEmail(emailField.getText());
                if (posBox.getValue() != null) {
                    c.setApplyPositionId(posBox.getValue().getPosId());
                    c.setApplyPositionName(posBox.getValue().getPosName());
                }
                c.setResult("待定");
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            new Thread(() -> {
                if (recruitmentService.createCandidate(c, authToken)) {
                    Platform.runLater(this::loadInitialData);
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