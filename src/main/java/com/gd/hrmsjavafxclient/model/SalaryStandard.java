package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * R8: 薪酬标准 Model
 * 对应后端 T_SalaryStandard 表结构：StdID, StandardName, BasicSalary, MealAllowance, Allowances, TotalAmount
 * 注意：字段名称已修正为与后端实体一致。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalaryStandard {

    // 🌟 修正字段名：StandardID -> stdId
    private Integer stdId;

    private String standardName;     // 薪酬标准名称 (如: P5-标准薪酬)

    // 🌟 修正字段名：baseSalary -> basicSalary
    private Double basicSalary;      // 基本工资

    // 🌟 修正字段名：allowance -> mealAllowance
    private Double mealAllowance;    // 餐补/津贴

    // 🌟 修正字段名：bonus -> allowances
    private Double allowances;       // 其他补贴总额

    private Double totalAmount;      // 总金额 (客户端计算字段)

    public SalaryStandard() {
        // 确保 TotalAmount 字段在创建时能够正确计算（即使其他为 null）
        this.totalAmount = 0.0;
    }

    // --- Getter 和 Setter (方法名也需要与字段名对应) ---

    public Integer getStdId() {
        return stdId;
    }

    public void setStdId(Integer stdId) {
        this.stdId = stdId;
        // 自动计算总额的逻辑，我们放在 setter 中，确保在反序列化后也有效
        calculateTotalAmount();
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public Double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = basicSalary;
        calculateTotalAmount();
    }

    public Double getMealAllowance() {
        return mealAllowance;
    }

    public void setMealAllowance(Double mealAllowance) {
        this.mealAllowance = mealAllowance;
        calculateTotalAmount();
    }

    public Double getAllowances() {
        return allowances;
    }

    public void setAllowances(Double allowances) {
        this.allowances = allowances;
        calculateTotalAmount();
    }

    public Double getTotalAmount() {
        // 确保总是在需要时重新计算或返回最新的值
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        // 虽然这个字段主要由客户端计算，但为了完整性保留 setter
        this.totalAmount = totalAmount;
    }

    // 辅助计算方法
    private void calculateTotalAmount() {
        double basic = this.basicSalary != null ? this.basicSalary : 0.0;
        double meal = this.mealAllowance != null ? this.mealAllowance : 0.0;
        double other = this.allowances != null ? this.allowances : 0.0;
        this.totalAmount = basic + meal + other;
    }
}