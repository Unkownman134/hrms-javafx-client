package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import java.util.Collections;
import java.util.List;


public class SalaryEmpService {


    public List<SalaryRecord> getSalaryRecords(int empId, int year, String authToken) {
        String path = String.format("/salary/history/%d?year=%d", empId, year);

        System.out.println("SalaryEmpService: 正在获取员工工资条记录 (年份: " + year + ")...");
        try {
            return ServiceUtil.sendGet(
                    path,
                    authToken,
                    new TypeReference<List<SalaryRecord>>() {}
            ).orElse(Collections.emptyList());
        } catch (Exception e) {
            System.err.println("API调用失败：无法获取员工工资条记录。");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}