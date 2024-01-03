package com.eric.domain;

import lombok.Data;

import java.util.Date;

@Data
public class SyncResult {

    private int tweSymbolCnt;

    private int tweSymbolSize;

    private int usSymbolCnt;

    private int usSymbolSize;

    private Date queryDate = new Date();

    private String msg;

    public SyncResult() {
    }

//    public SyncResult(int tweSsymbolCnt, int tweSymbolSize, String msg) {
//        this.tweSymbolCnt = tweSymbolCnt;
//        this.tweSymbolSize = tweSymbolSize;
//        this.msg = msg;
//    }


    public SyncResult(int tweSymbolCnt, int tweSymbolSize, int usSymbolCnt, int usSymbolSize, String msg) {
        this.tweSymbolCnt = tweSymbolCnt;
        this.tweSymbolSize = tweSymbolSize;
        this.usSymbolCnt = usSymbolCnt;
        this.usSymbolSize = usSymbolSize;
        this.msg = msg;
    }
}
