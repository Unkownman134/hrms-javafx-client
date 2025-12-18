package com.gd.hrmsjavafxclient.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SalaryRecord {
    private final IntegerProperty recordId = new SimpleIntegerProperty();
    private final StringProperty salaryMonth = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> grossPay = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> taxDeduction = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> netPay = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> payDate = new SimpleObjectProperty<>();
    private final IntegerProperty empId = new SimpleIntegerProperty();

    public SalaryRecord() {}

    // Getters and Setters
    public int getRecordId() { return recordId.get(); }
    public void setRecordId(int recordId) { this.recordId.set(recordId); }

    public String getSalaryMonth() { return salaryMonth.get(); }
    public void setSalaryMonth(String salaryMonth) { this.salaryMonth.set(salaryMonth); }

    public BigDecimal getGrossPay() { return grossPay.get(); }
    public void setGrossPay(BigDecimal grossPay) { this.grossPay.set(grossPay); }

    public BigDecimal getTaxDeduction() { return taxDeduction.get(); }
    public void setTaxDeduction(BigDecimal taxDeduction) { this.taxDeduction.set(taxDeduction); }

    public BigDecimal getNetPay() { return netPay.get(); }
    public void setNetPay(BigDecimal netPay) { this.netPay.set(netPay); }

    public LocalDate getPayDate() { return payDate.get(); }
    public void setPayDate(LocalDate payDate) { this.payDate.set(payDate); }

    public int getEmpId() { return empId.get(); }
    public void setEmpId(int empId) { this.empId.set(empId); }

    // Property methods for TableView
    public IntegerProperty recordIdProperty() { return recordId; }
    public StringProperty salaryMonthProperty() { return salaryMonth; }
    public ObjectProperty<BigDecimal> grossPayProperty() { return grossPay; }
    public ObjectProperty<BigDecimal> taxDeductionProperty() { return taxDeduction; }
    public ObjectProperty<BigDecimal> netPayProperty() { return netPay; }
    public ObjectProperty<LocalDate> payDateProperty() { return payDate; }
    public IntegerProperty empIdProperty() { return empId; }
}