package com.eric.domain;

import lombok.Data;

import java.util.Date;

@Data
public class SyncResult {

    private int symbolCnt;

    private int symbolSize;

    private String msg;

    public SyncResult() {
    }

    public SyncResult(int symbolCnt, int symbolSize, String msg) {
        this.symbolCnt = symbolCnt;
        this.symbolSize = symbolSize;
        this.msg = msg;
    }
}
