package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import java.util.Collections;
import java.util.List;


public class AttendanceEmpService {


    public List<AttendanceRecord> getAttendanceRecords(int empId, String yearMonth, String authToken) {
        String path = String.format("/attendance/%d", empId);

        System.out.println("AttendanceEmpService: 正在获取员工考勤记录...");
        try {
            return ServiceUtil.sendGet(
                    path,
                    authToken,
                    new TypeReference<List<AttendanceRecord>>() {}
            ).orElse(Collections.emptyList());
        } catch (Exception e) {
            System.err.println("API调用失败：无法获取员工考勤记录。");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}