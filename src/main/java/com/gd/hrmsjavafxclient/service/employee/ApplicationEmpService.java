package com.gd.hrmsjavafxclient.service.employee;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ApplicationEmpService {

    public boolean submitApplication(ApprovalRequest request, String authToken) throws Exception {
        String path = "/approval-requests";
        ServiceUtil.sendRequest(path, authToken, request, "POST", new TypeReference<ApprovalRequest>() {});
        return true;
    }

    public List<ApprovalRequest> getMyApplications(int empId, String authToken) throws Exception {
        String path = "/approval-requests/my/" + empId;
        Optional<List<ApprovalRequest>> response = ServiceUtil.sendGet(
                path,
                authToken,
                new TypeReference<List<ApprovalRequest>>() {}
        );
        return response.orElse(Collections.emptyList());
    }

    public boolean withdrawApplication(int requestId, int empId, String authToken) throws Exception {
        String path = "/approval-requests/" + requestId + "/withdraw";
        ApprovalRequest body = new ApprovalRequest();
        body.setEmpId(empId);

        ServiceUtil.sendRequest(path, authToken, body, "PUT", new TypeReference<String>() {});
        return true;
    }
}