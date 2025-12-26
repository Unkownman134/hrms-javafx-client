package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * R9: 职位信息 Model
 * 对应后端 T_Position 表结构：PosID, PosName, PosLevel, BaseSalaryLevel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Position {
    private Integer posId;
    private String posName;
    private String posLevel;
    private Integer baseSalaryLevel;

    public Position() {}


    public Integer getPosId() {
        return posId;
    }

    public void setPosId(Integer posId) {
        this.posId = posId;
    }

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public String getPosLevel() {
        return posLevel;
    }

    public void setPosLevel(String posLevel) {
        this.posLevel = posLevel;
    }

    public Integer getBaseSalaryLevel() {
        return baseSalaryLevel;
    }

    public void setBaseSalaryLevel(Integer baseSalaryLevel) {
        this.baseSalaryLevel = baseSalaryLevel;
    }
}