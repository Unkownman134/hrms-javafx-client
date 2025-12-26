package com.gd.hrmsjavafxclient.service.manager;

import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class AttendanceManagerService {

    private static final String BASE_ENDPOINT = "/attendance";


    public List<AttendanceRecord> getAttendanceRecordsByEmpId(Integer empId, String authToken) throws IOException, InterruptedException {
        if (empId == null) {
            throw new IllegalArgumentException("员工ID不能为空！");
        }

        String endpoint = String.format("%s/%d", BASE_ENDPOINT, empId);

        Optional<List<AttendanceRecord>> result = ServiceUtil.sendGet(
                endpoint,
                authToken,
                new TypeReference<List<AttendanceRecord>>() {}
        );

        return result.orElse(Collections.emptyList());
    }
}