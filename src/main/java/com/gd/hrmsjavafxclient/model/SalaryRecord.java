package com.gd.hrmsjavafxclient.model;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 工资条记录 Model (对应 GET /api/salary/history/{empId})
 * 使用 Property 类支持 JavaFX TableView 绑定。
 */
public class SalaryRecord {
    private final StringProperty month = new SimpleStringProperty(); // 发放月份 (如: 2023年08月)
    private final ObjectProperty<LocalDate> payDate = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> grossPay = new SimpleObjectProperty<>(); // 应发总额
    private final ObjectProperty<BigDecimal> taxDeduction = new SimpleObjectProperty<>(); // 扣税额
    private final ObjectProperty<BigDecimal> netPay = new SimpleObjectProperty<>(); // 实发净额

    public SalaryRecord() {}

    // --- Getters and Setters for Jackson (API deserialization) ---
    public String getMonth() { return month.get(); }
    public void setMonth(String month) { this.month.set(month); }

    public LocalDate getPayDate() { return payDate.get(); }
    public void setPayDate(LocalDate payDate) { this.payDate.set(payDate); }

    public BigDecimal getGrossPay() { return grossPay.get(); }
    public void setGrossPay(BigDecimal grossPay) { this.grossPay.set(grossPay); }

    public BigDecimal getTaxDeduction() { return taxDeduction.get(); }
    public void setTaxDeduction(BigDecimal taxDeduction) { this.taxDeduction.set(taxDeduction); }

    public BigDecimal getNetPay() { return netPay.get(); }
    public void setNetPay(BigDecimal netPay) { this.netPay.set(netPay); }

    // --- Property Getters for JavaFX TableView ---
    public StringProperty monthProperty() { return month; }
    public ObjectProperty<LocalDate> payDateProperty() { return payDate; }
    public ObjectProperty<BigDecimal> grossPayProperty() { return grossPay; }
    public ObjectProperty<BigDecimal> taxDeductionProperty() { return taxDeduction; }
    public ObjectProperty<BigDecimal> netPayProperty() { return netPay; }
}