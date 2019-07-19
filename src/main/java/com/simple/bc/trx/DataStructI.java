package com.simple.bc.trx;

import java.sql.Timestamp;
import java.util.List;

public class DataStructI {
    private String fab;
    private String line;
    private String operation;
    private Timestamp rpt_time;
    private List<DataStructIA> dataList;

    public String getFab() {
        return fab;
    }

    public void setFab(String fab) {
        this.fab = fab;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Timestamp getRpt_time() {
        return rpt_time;
    }

    public void setRpt_time(Timestamp rpt_time) {
        this.rpt_time = rpt_time;
    }

    public List<DataStructIA> getDataList() {
        return dataList;
    }

    public void setDataList(List<DataStructIA> dataList) {
        this.dataList = dataList;
    }
}
