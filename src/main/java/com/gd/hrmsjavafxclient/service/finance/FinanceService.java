package com.gd.hrmsjavafxclient.service.finance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.util.List;
import java.util.Optional;

public class FinanceService {

    /**
     * 获取所有员工的工资记录
     */
    public List<SalaryRecord> getAllSalaryRecords(String token) throws Exception {
        Optional<List<SalaryRecord>> result = ServiceUtil.sendGet(
                "/salary/all",
                token,
                new TypeReference<List<SalaryRecord>>() {}
        );
        return result.orElseThrow(() -> new RuntimeException("未能获取到工资记录数据"));
    }
}