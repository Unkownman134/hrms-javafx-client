package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.gd.hrmsjavafxclient.model.Schedule;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class ScheduleEmpService {


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


    public JsonNode getShiftRuleFullNode(int ruleId, String authToken) throws Exception {
        String path = "/shift/rules/" + ruleId;
        Optional<JsonNode> response = ServiceUtil.sendGet(
                path,
                authToken,
                new TypeReference<JsonNode>() {}
        );
        return response.orElse(null);
    }


    public List<JsonNode> getMyApplications(int empId, String authToken) throws Exception {
        String path = "/approval-requests/my/" + empId;
        Optional<List<JsonNode>> response = ServiceUtil.sendGet(
                path,
                authToken,
                new TypeReference<List<JsonNode>>() {}
        );
        return response.orElse(Collections.emptyList());
    }
}