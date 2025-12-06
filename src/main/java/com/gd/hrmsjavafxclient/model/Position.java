package com.gd.hrmsjavafxclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Position {
    private Integer posId;
    private String posName; // 🌟 需要获取

    public Position() {}

    // Getter and Setter
    public Integer getPosId() { return posId; }
    public void setPosId(Integer posId) { this.posId = posId; }
    public String getPosName() { return posName; }
    public void setPosName(String posName) { this.posName = posName; }
}