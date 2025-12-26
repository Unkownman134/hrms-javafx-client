package com.gd.hrmsjavafxclient.service.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApprovalManagerService {

    /**
     * 获取待办审批
     */
    public List<ApprovalRequest> getMyPendingApprovals(String authToken, Integer myEmpId) throws IOException, InterruptedException {
        Optional<List<ApprovalRequest>> result = ServiceUtil.sendGet(
                "/approval-requests",
                authToken,
                new TypeReference<List<ApprovalRequest>>() {}
        );

        return result.map(list -> list.stream()
                .filter(req -> myEmpId.equals(req.getCurrentApproverId()))
                .filter(req -> "待审批".equals(req.getStatus()))
                .collect(Collectors.toList())
        ).orElse(Collections.emptyList());
    }

    /**
     * 执行审批动作
     * 修正：路径改为 /approvals/{id}/action
     */
    public void handleApprovalAction(Integer requestId, String action, String comments, Integer approverId, String authToken)
            throws IOException, InterruptedException {

        String endpoint = "/approvals/" + requestId + "/action";

        Map<String, Object> body = Map.of(
                "approverId", approverId,
                "action", action,
                "comments", comments
        );

        ServiceUtil.sendRequest(
                endpoint,
                authToken,
                body,
                "POST",
                new TypeReference<Object>() {}
        );
    }
}