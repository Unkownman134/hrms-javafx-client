package com.gd.hrmsjavafxclient.service.manager;

import com.gd.hrmsjavafxclient.model.ShiftRule;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 部门经理班次规则服务 (ShiftRuleManagerService)
 * 负责获取所有定义的班次规则。
 */
public class ShiftRuleManagerService {

    private static final String ENDPOINT = "/shift/rules";

    /**
     * 获取所有班次规则列表。
     * @param authToken 认证 Token
     * @return 班次规则列表 (ShiftRule Model)
     * @throws IOException 如果 HTTP 请求失败
     */
    public List<ShiftRule> getAllShiftRules(String authToken) throws IOException, InterruptedException {
        Optional<List<ShiftRule>> result = ServiceUtil.sendGet(
                ENDPOINT,
                authToken,
                new TypeReference<List<ShiftRule>>() {}
        );

        return result.orElse(Collections.emptyList());
    }
}