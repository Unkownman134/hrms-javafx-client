package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;
import com.gd.hrmsjavafxclient.util.ServiceUtil; // 🌟 导入 ServiceUtil
import java.util.Optional;

/**
 * 员工申请提交服务 (R7)
 */
public class ApplicationEmpService {

    // 假设这是 EmployeeService 接口中 submitApplication 的实现逻辑
    // 返回 boolean 只需要判断 ServiceUtil.sendRequest 是否成功即可
    public boolean submitApplication(ApprovalRequest request, String authToken) throws Exception {
        String path = "/approval-requests";

        // 🌟 使用 ServiceUtil 的通用 POST 方法
        Optional<ApprovalRequest> response = ServiceUtil.sendRequest(
                path,
                authToken,
                request, // 请求体
                "POST",
                // 期望返回 ApprovalRequest 类型，即使没有响应体也会成功处理
                new TypeReference<ApprovalRequest>() {}
        );

        // 如果 ServiceUtil.sendRequest 成功，则不会抛出异常，返回 true
        return true;
    }
}