package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.*;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RecruitmentController implements HRSubController {

    @FXML private TableView<Candidate> candidateTable;
    @FXML private TableColumn<Candidate, Integer> candIDColumn;
    @FXML private TableColumn<Candidate, String> nameColumn, phoneColumn, emailColumn, positionColumn, resultColumn;
    @FXML private TableColumn<Candidate, LocalDate> interviewDateColumn;
    @FXML private TableColumn<Candidate, String> genderColumn;

    private final HRDataService dataService = new HRDataService();
    private ObservableList<Candidate> candidateList = FXCollections.observableArrayList();
    private String authToken;
    private List<Position> allPositions;
    private Map<Integer, String> positionIdToNameMap;

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
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
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("applyPositionName"));
        interviewDateColumn.setCellValueFactory(new PropertyValueFactory<>("interviewDate"));
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        candidateTable.setItems(candidateList);
    }

    private void loadInitialData() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                allPositions = dataService.getAllPositions(authToken);
                positionIdToNameMap = allPositions.stream().collect(Collectors.toMap(Position::getPosId, Position::getPosName));
                return null;
            }
            @Override protected void succeeded() { loadCandidateData(); }
        };
        new Thread(task).start();
    }

    private void loadCandidateData() {
        Task<List<Candidate>> task = new Task<>() {
            @Override protected List<Candidate> call() throws IOException, InterruptedException {
                List<Candidate> list = dataService.getAllCandidates(authToken);
                list.forEach(c -> c.setApplyPositionName(positionIdToNameMap.getOrDefault(c.getApplyPositionId(), "未知")));
                return list;
            }
            @Override protected void succeeded() { candidateList.setAll(getValue()); }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleAddCandidate() {
        if (allPositions == null) return;
        Dialog<Candidate> dialog = new Dialog<>();
        dialog.setTitle("新增候选人");
        ButtonType okBtn = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField nameF = new TextField();

        // 🌟 新增性别的下拉选择
        ComboBox<String> genderF = new ComboBox<>(FXCollections.observableArrayList("男", "女"));
        genderF.setValue("男");

        TextField phoneF = new TextField();
        TextField emailF = new TextField();
        ComboBox<Position> posF = new ComboBox<>(FXCollections.observableArrayList(allPositions));
        posF.setConverter(new StringConverter<>() {
            public String toString(Position p) { return p != null ? p.getPosName() : ""; }
            public Position fromString(String s) { return null; }
        });

        grid.add(new Label("姓名:"), 0, 0); grid.add(nameF, 1, 0);
        grid.add(new Label("性别:"), 0, 1); grid.add(genderF, 1, 1);
        grid.add(new Label("电话:"), 0, 2); grid.add(phoneF, 1, 2);
        grid.add(new Label("邮箱:"), 0, 3); grid.add(emailF, 1, 3);
        grid.add(new Label("职位:"), 0, 4); grid.add(posF, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == okBtn) {
                Candidate c = new Candidate();
                c.setName(nameF.getText());
                c.setGender(genderF.getValue()); // 🌟 收集性别
                c.setPhone(phoneF.getText());
                c.setEmail(emailF.getText());
                c.setApplyPositionId(posF.getValue().getPosId());
                c.setInterviewDate(LocalDate.now());
                c.setResult("待定");
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            new Thread(() -> {
                if(dataService.addCandidate(c, authToken)) Platform.runLater(this::loadCandidateData);
            }).start();
        });
    }

    @FXML
    private void handleUpdateResult() {
        Candidate selected = candidateTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        List<String> choices = List.of("录用", "淘汰", "待定");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getResult(), choices);
        dialog.setTitle("处理结果");
        dialog.setHeaderText("更改状态: " + selected.getName());
        dialog.showAndWait().ifPresent(res -> {
            if ("录用".equals(res)) {
                handleHireProcess(selected);
            } else {
                updateStatusOnly(selected, res);
            }
        });
    }

    private void handleHireProcess(Candidate candidate) {
        System.out.println("[DEBUG] 启动录用流程，候选人性别为: " + candidate.getGender());
        try {
            List<Department> depts = dataService.getAllDepartments(authToken);
            List<Employee> emps = dataService.getAllEmployees(authToken);

            Dialog<Map<String, Object>> hireDialog = new Dialog<>();
            hireDialog.setTitle("录用入职详情");
            ButtonType confirmBtn = new ButtonType("完成录用", ButtonBar.ButtonData.OK_DONE);
            hireDialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            TextField userF = new TextField(candidate.getName());
            PasswordField passF = new PasswordField();
            TextField roleF = new TextField("3");
            ComboBox<Department> deptCombo = new ComboBox<>(FXCollections.observableArrayList(depts));
            ComboBox<Employee> managerCombo = new ComboBox<>(FXCollections.observableArrayList(emps));
            managerCombo.setConverter(new StringConverter<>() {
                public String toString(Employee e) { return e != null ? e.getEmpName() : "无上级"; }
                public Employee fromString(String s) { return null; }
            });

            grid.add(new Label("系统用户名:"), 0, 0); grid.add(userF, 1, 0);
            grid.add(new Label("初始密码:"), 0, 1); grid.add(passF, 1, 1);
            grid.add(new Label("角色ID:"), 0, 2); grid.add(roleF, 1, 2);
            grid.add(new Label("分配部门:"), 0, 3); grid.add(deptCombo, 1, 3);
            grid.add(new Label("直属上级:"), 0, 4); grid.add(managerCombo, 1, 4);

            hireDialog.getDialogPane().setContent(grid);
            hireDialog.setResultConverter(btn -> {
                if (btn == confirmBtn) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("u", userF.getText()); m.put("p", passF.getText());
                    m.put("r", Integer.parseInt(roleF.getText()));
                    m.put("d", deptCombo.getValue() != null ? deptCombo.getValue().getDeptId() : null);
                    m.put("m", managerCombo.getValue() != null ? managerCombo.getValue().getEmpId() : null);
                    return m;
                }
                return null;
            });

            hireDialog.showAndWait().ifPresent(info -> {
                Task<Void> task = new Task<>() {
                    @Override protected Void call() throws Exception {
                        dataService.updateCandidateResult(candidate.getCandID(), "录用", authToken);

                        Employee e = new Employee();
                        e.setEmpName(candidate.getName());
                        e.setGender(candidate.getGender()); // 🌟 关键：从候选人对象获取性别并传给员工
                        e.setPhone(candidate.getPhone());
                        e.setEmail(candidate.getEmail());
                        e.setPosId(candidate.getApplyPositionId());
                        e.setDeptId((Integer)info.get("d"));
                        e.setManagerId((Integer)info.get("m"));
                        e.setJoinDate(LocalDate.now());
                        e.setStatus("在职");

                        Employee saved = dataService.createEmployee(e, authToken).orElseThrow(() -> new Exception("员工创建失败"));

                        User u = new User();
                        u.setUsername((String)info.get("u")); u.setRawPassword((String)info.get("p"));
                        u.setRoleId((Integer)info.get("r")); u.setEmpId(saved.getEmpId());
                        if(!dataService.createUser(u, authToken)) throw new Exception("账号创建失败");
                        return null;
                    }
                    @Override protected void succeeded() {
                        showAlert("成功", "录用流程完成，员工及账号已生成！");
                        loadCandidateData();
                    }
                    @Override protected void failed() {
                        showAlert("错误", getException().getMessage());
                    }
                };
                new Thread(task).start();
            });
        } catch (Exception ex) {
            showAlert("数据加载错误", "无法获取部门或员工列表，请重试。");
        }
    }

    private void updateStatusOnly(Candidate c, String res) {
        new Thread(() -> {
            if(dataService.updateCandidateResult(c.getCandID(), res, authToken)) {
                Platform.runLater(this::loadCandidateData);
            }
        }).start();
    }

    @FXML private void handleRefresh() { loadCandidateData(); }

    private void showAlert(String t, String c) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(t);
            a.setHeaderText(null);
            a.setContentText(c);
            a.show();
        });
    }
}