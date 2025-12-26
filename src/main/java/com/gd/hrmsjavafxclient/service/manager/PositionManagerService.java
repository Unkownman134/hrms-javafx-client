package com.gd.hrmsjavafxclient.service.manager;

import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 部门经理/员工职位信息服务 (PositionManagerService)
 * 负责获取所有职位列表，用于在员工列表中将 PosID 映射为职位名称。
 */
public class PositionManagerService {

    private static final String ENDPOINT = "/positions";

    /**
     * 获取所有职位信息列表。
     * @param authToken 认证 Token
     * @return 所有职位列表 (Position Model)
     * @throws IOException 如果 HTTP 请求失败
     * @throws InterruptedException 线程中断
     */
    public List<Position> getAllPositions(String authToken) throws IOException, InterruptedException {

        Optional<List<Position>> result = ServiceUtil.sendGet(
                ENDPOINT,
                authToken,
                new TypeReference<List<Position>>() {}
        );

        return result.orElse(Collections.emptyList());
    }
}