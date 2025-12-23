package com.gd.hrmsjavafxclient.service.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.Schedule;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ScheduleManagerService {

    private static final String ENDPOINT = "/schedules";

    /**
     * 严格按照 API 文档：?empId={id}&startDate={s}&endDate={e}
     */
    public List<Schedule> getSchedulesByRange(Integer empId, String startDate, String endDate, String authToken)
            throws IOException, InterruptedException {

        String url = String.format("%s?empId=%d&startDate=%s&endDate=%s",
                ENDPOINT, empId, startDate, endDate);

        Optional<List<Schedule>> result = ServiceUtil.sendGet(
                url,
                authToken,
                new TypeReference<List<Schedule>>() {}
        );
        return result.orElse(Collections.emptyList());
    }

    /**
     * 🌟 关键修正：
     * 1. 修正了 ServiceUtil.sendRequest 的参数顺序，防止 Token 被解析为 HTTP 方法。
     * 2. 在不修改 Model 的前提下，通过 Map 仅提取后端需要的 3 个字段。
     */
    public boolean addSchedule(Schedule schedule, String authToken) throws IOException, InterruptedException {

        // 既然不改 Model，我们就手动挑出后端需要的 3 个字段组装成 Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("empId", schedule.getEmpId());
        payload.put("scheduleDate", schedule.getScheduleDate().toString()); // 转成 "yyyy-MM-dd"
        payload.put("shiftRuleId", schedule.getShiftRuleId());

        // ServiceUtil.sendRequest 签名顺序: (endpoint, authToken, body, method, responseTypeRef)
        Optional<Schedule> result = ServiceUtil.sendRequest(
                ENDPOINT,               // 1. endpoint
                authToken,              // 2. authToken
                payload,                // 3. body (只包含 3 个字段的 Map)
                "POST",                 // 4. method (必须是字符串 "POST")
                new TypeReference<Schedule>() {}
        );

        return result.isPresent();
    }
}