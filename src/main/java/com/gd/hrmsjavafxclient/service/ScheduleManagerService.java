package com.gd.hrmsjavafxclient.service;

import com.gd.hrmsjavafxclient.model.Schedule;
import com.gd.hrmsjavafxclient.model.ShiftRule;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门经理排班记录服务 (ScheduleManagerService)
 * 负责获取指定员工和时间范围内的排班记录，并进行数据补全。
 * 🚨 核心修正：修正 ShiftRule API 路径，并新增数据补全逻辑。
 */
public class ScheduleManagerService {

    private static final String ENDPOINT = "/schedules/filter";
    // 🚨 路径修正：根据用户提供的文档，从 /shiftRules 改为 /shift/rules
    private static final String SHIFT_RULES_ENDPOINT = "/shift/rules";

    // 定义时间格式，通常后端 TIME 类型返回 "HH:mm:ss"
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // 用于缓存已查询的班次规则，避免重复请求
    private Map<Integer, ShiftRule> shiftRuleCache = Collections.emptyMap();

    /**
     * 根据员工ID列表和日期范围获取排班记录列表，并补全班次信息。
     */
    public List<Schedule> getSchedulesByEmpIdsAndDateRange(List<Integer> empIds, String startDate, String endDate, String authToken) throws IOException, InterruptedException {
        if (empIds == null || empIds.isEmpty() || startDate == null || endDate == null) {
            return Collections.emptyList();
        }

        // 1. 获取所有员工的排班记录
        List<Schedule> allSchedules = new ArrayList<>();
        for (Integer empId : empIds) {
            String pathWithQuery = String.format("%s?empId=%d&startDate=%s&endDate=%s",
                    ENDPOINT, empId, startDate, endDate);

            try {
                Optional<List<Schedule>> result = ServiceUtil.sendGet(
                        pathWithQuery,
                        authToken,
                        new TypeReference<List<Schedule>>() {}
                );

                result.ifPresent(allSchedules::addAll);

            } catch (Exception e) {
                System.err.println("⚠️ 警告：查询员工ID " + empId + " 的排班记录失败: " + e.getMessage());
            }
        }

        // 2. 如果没有排班记录，直接返回
        if (allSchedules.isEmpty()) {
            return allSchedules;
        }

        // 3. 核心步骤：补全 ShiftRule 数据 (班次名称、上下班时间)
        return enrichSchedulesWithShiftRules(allSchedules, authToken);
    }

    /**
     * 新增私有方法：加载并缓存所有班次规则 (ShiftRule)。
     */
    private synchronized void loadShiftRuleCache(String authToken) throws IOException, InterruptedException {
        // 只有当缓存为空时才加载
        if (shiftRuleCache.isEmpty()) {
            try {
                System.out.println("⏳ 首次加载班次规则 (Shift Rules)...");
                // 调用 API 获取所有班次规则
                Optional<List<ShiftRule>> result = ServiceUtil.sendGet(
                        SHIFT_RULES_ENDPOINT, // 👈 路径已修正
                        authToken,
                        new TypeReference<List<ShiftRule>>() {}
                );

                if (result.isPresent()) {
                    // 将 List 转换为 Map<shiftRuleId, ShiftRule> 方便查找
                    shiftRuleCache = result.get().stream()
                            // 使用你 ShiftRule.java 里的 getRuleId()
                            .filter(rule -> rule.getRuleId() != null)
                            .collect(Collectors.toMap(ShiftRule::getRuleId, rule -> rule));
                    System.out.println("✅ 班次规则加载成功，共 " + shiftRuleCache.size() + " 条记录。");
                } else {
                    System.out.println("⚠️ 警告：加载班次规则 API 返回空数据。");
                }
            } catch (Exception e) {
                // 打印错误信息，确保用户能看到 403 错误
                System.err.println("❌ 错误：加载班次规则失败: " + e.getMessage());
                shiftRuleCache = Collections.emptyMap();
                // 必须重新抛出异常，否则调用方会继续执行，可能导致 TableView 线程阻塞
                throw e;
            }
        }
    }

    /**
     * 新增私有方法：利用 ShiftRule 缓存补全 Schedule 数据。
     */
    private List<Schedule> enrichSchedulesWithShiftRules(List<Schedule> schedules, String authToken) throws IOException, InterruptedException {

        // 确保班次规则已加载 (如果加载失败，这里会抛出异常，阻止后续执行)
        loadShiftRuleCache(authToken);

        for (Schedule s : schedules) {
            Integer ruleId = s.getShiftRuleId();

            if (ruleId != null && shiftRuleCache.containsKey(ruleId)) {
                ShiftRule rule = shiftRuleCache.get(ruleId);

                // 1. 补全班次名称
                s.setShiftName(rule.getRuleName()); // 使用你 ShiftRule.java 里的 getRuleName()

                // 2. 补全上下班时间 (从 String 转换到 LocalTime)
                try {
                    String startTimeStr = rule.getWorkStartTime();
                    if (startTimeStr != null && !startTimeStr.isEmpty()) {
                        s.setClockInTime(LocalTime.parse(startTimeStr, TIME_FORMATTER));
                    }
                } catch (Exception e) {
                    System.err.println("时间解析错误: " + rule.getWorkStartTime() + "，请检查 ShiftRule.workStartTime 格式。");
                }

                try {
                    String endTimeStr = rule.getWorkEndTime();
                    if (endTimeStr != null && !endTimeStr.isEmpty()) {
                        s.setClockOutTime(LocalTime.parse(endTimeStr, TIME_FORMATTER));
                    }
                } catch (Exception e) {
                    System.err.println("时间解析错误: " + rule.getWorkEndTime() + "，请检查 ShiftRule.workEndTime 格式。");
                }

                // 3. 补全备注
                s.setNote("规则ID: " + ruleId);
            } else {
                // 如果找不到班次规则
                s.setShiftName("未知/删除班次");
                s.setNote("班次规则 (ID:" + ruleId + ") 缺失。");
            }
        }

        return schedules;
    }
}