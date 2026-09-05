package com.callxpeconomy.storage;

public record AdjustmentResult(boolean successful, long balance) {
    public static AdjustmentResult insufficientFunds(long balance) {
        return new AdjustmentResult(false, balance);
    }
}
