package com.eric.wessiorfinance.util;

import lombok.Data;

@Data
public class TLPosition {

    private WessiorFintechTL tl;

    private TLStatus status = TLStatus.UNKNOWN;

    private boolean isRising = false;

    public TLPosition(WessiorFintechTL tl) {
        this.tl = tl;
        if (tl.getClose() > tl.getDoublePositiveSTD()) {
            status = TLStatus.UP_TO_2SD;
        } else if (tl.getClose() < tl.getDoublePositiveSTD() &&
                tl.getClose() > tl.getPositiveSTD()) {
            status = TLStatus.BETWEEN_SD_TO_2SD;
        } else if (tl.getClose() < tl.getPositiveSTD() &&
                tl.getClose() > tl.getTl()) {
            status = TLStatus.BETWEEN_TL_TO_SD;
        } else if (tl.getClose() < tl.getTl() &&
                tl.getClose() > tl.getNegtiveSTD()) {
            status = TLStatus.BETWEEN_N_SD_TO_TL;
        } else if (tl.getClose() < tl.getNegtiveSTD() &&
                tl.getClose() > tl.getDoubleNegtiveSTD()) {
            status = TLStatus.BETWEEN_N_2SD_TO_N_SD;
        } else if (tl.getClose() < tl.getDoubleNegtiveSTD()) {
            status = TLStatus.LOW_TO_N_2SD;
        }
    }
}
