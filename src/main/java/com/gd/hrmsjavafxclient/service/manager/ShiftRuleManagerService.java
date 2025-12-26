package com.gd.hrmsjavafxclient.service.manager;

import com.gd.hrmsjavafxclient.model.ShiftRule;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class ShiftRuleManagerService {

    private static final String ENDPOINT = "/shift/rules";


    public List<ShiftRule> getAllShiftRules(String authToken) throws IOException, InterruptedException {
        Optional<List<ShiftRule>> result = ServiceUtil.sendGet(
                ENDPOINT,
                authToken,
                new TypeReference<List<ShiftRule>>() {}
        );

        return result.orElse(Collections.emptyList());
    }
}