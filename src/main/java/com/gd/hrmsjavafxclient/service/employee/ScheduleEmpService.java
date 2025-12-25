package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.gd.hrmsjavafxclient.model.Schedule;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 员工排班服务类 📅
 */
public class ScheduleEmpService {

    /**
     * 根据员工ID和日期范围获取排班记录
     * API: /schedules?empId=xxx&startDate=yyyy-MM-dd&endDate=yyyy-MM-dd
     */
    public List<Schedule> getMySchedules(int empId, LocalDate startDate, LocalDate endDate, String authToken) throws Exception {
        String path = String.format("/schedules?empId=%d&startDate=%s&endDate=%s",
                empId, startDate.toString(), endDate.toString());

        Optional<List<Schedule>> response = ServiceUtil.sendGet(
                path,
                authToken,
                new TypeReference<List<Schedule>>() {}
        );

        return response.orElse(Collections.emptyList());
    }

    /**
     * 🌟 新增：根据班次 ID 获取班次名称
     * API: /shift/rules/{RuleID}
     */
    public String getShiftRuleName(int ruleId, String authToken) throws Exception {
        String path = "/shift/rules/" + ruleId;
        // 使用 JsonNode 通用解析，避免修改 Model
        Optional<JsonNode> response = ServiceUtil.sendGet(
                path,
                authToken,
                new TypeReference<JsonNode>() {}
        );

        if (response.isPresent()) {
            JsonNode node = response.get();
            // 根据 API.txt 文档，班次名称字段是 ruleName
            if (node.has("ruleName")) {
                return node.get("ruleName").asText();
            }
        }
        return "未知班次(" + ruleId + ")";
    }
}