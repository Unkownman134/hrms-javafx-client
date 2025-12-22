package com.gd.hrmsjavafxclient.service.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.Schedule;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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

    public boolean addSchedule(Schedule schedule, String authToken) throws IOException, InterruptedException {
        Optional<Schedule> result = ServiceUtil.sendRequest(
                "POST",
                ENDPOINT,
                schedule,
                authToken,
                new TypeReference<Schedule>() {}
        );
        return result.isPresent();
    }
}