package com.simple.bc.trx;

import com.alibaba.fastjson.JSONObject;

import java.sql.Timestamp;
import java.util.List;

public class DataStructIA {

    private Integer id;
    private String Item_ID;  //地址位
    private String Data_Type;  //数据类型
    private String VariableName;
    private String Value;
    private String Quality; //是否正常
    private Integer Update_Count; //更新次数
    private String Eqt;
    private String SubEqt;
    private Timestamp TimeStamp;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getItem_ID() {
        return Item_ID;
    }

    public void setItem_ID(String item_ID) {
        Item_ID = item_ID;
    }

    public String getData_Type() {
        return Data_Type;
    }

    public void setData_Type(String data_Type) {
        Data_Type = data_Type;
    }

    public String getVariableName() {
        return VariableName;
    }

    public void setVariableName(String variableName) {
        VariableName = variableName;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getQuality() {
        return Quality;
    }

    public void setQuality(String quality) {
        Quality = quality;
    }

    public Integer getUpdate_Count() {
        return Update_Count;
    }

    public void setUpdate_Count(Integer update_Count) {
        Update_Count = update_Count;
    }

    public String getEqt() {
        return Eqt;
    }

    public void setEqt(String eqt) {
        Eqt = eqt;
    }

    public String getSubEqt() {
        return SubEqt;
    }

    public void setSubEqt(String subEqt) {
        SubEqt = subEqt;
    }

    public Timestamp getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        TimeStamp = timeStamp;
    }
}


