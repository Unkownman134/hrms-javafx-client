package com.gd.hrmsjavafxclient.service.hr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.model.Candidate;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.util.ServiceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 招聘管理专用服务类 🚀
 * 严格匹配 ServiceUtil 定义的接口
 */
public class RecruitmentService {

    private static final String CAND_API = "/candidates";
    private static final String POS_API = "/positions";

    /**
     * 获取所有候选人
     */
    public List<Candidate> getAllCandidates(String token) {
        try {
            // ServiceUtil.sendGet 返回的是 Optional<T>
            Optional<List<Candidate>> result = ServiceUtil.sendGet(CAND_API, token, new TypeReference<List<Candidate>>() {});
            return result.orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * 获取所有职位 (用于前端 ID 到 名称 的转换)
     */
    public List<Position> getAllPositions(String token) {
        try {
            Optional<List<Position>> result = ServiceUtil.sendGet(POS_API, token, new TypeReference<List<Position>>() {});
            return result.orElse(List.of());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * 新增候选人
     */
    public boolean createCandidate(Candidate c, String token) {
        try {
            // ServiceUtil.sendRequest 也会返回 Optional，这里我们只关注是否抛异常
            ServiceUtil.sendRequest(CAND_API, token, c, "POST", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新面试结果
     */
    public boolean updateCandidateResult(int id, String res, String token) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("result", res);
            ServiceUtil.sendRequest(CAND_API + "/" + id + "/result", token, body, "PUT", new TypeReference<Void>() {});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}