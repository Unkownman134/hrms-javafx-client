package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * R8: 薪酬标准 Model
 * 对应后端 T_SalaryStandard 表结构：StdID, StandardName, BasicSalary, MealAllowance, Allowances, TotalAmount
 * 注意：字段名称已修正为与后端实体一致。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalaryStandard {

    private Integer stdId;

    private String standardName;

    private Double basicSalary;

    private Double mealAllowance;

    private Double allowances;

    private Double totalAmount;

    public SalaryStandard() {
        this.totalAmount = 0.0;
    }


    public Integer getStdId() {
        return stdId;
    }

    public void setStdId(Integer stdId) {
        this.stdId = stdId;
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
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    private void calculateTotalAmount() {
        double basic = this.basicSalary != null ? this.basicSalary : 0.0;
        double meal = this.mealAllowance != null ? this.mealAllowance : 0.0;
        double other = this.allowances != null ? this.allowances : 0.0;
        this.totalAmount = basic + meal + other;
    }
}