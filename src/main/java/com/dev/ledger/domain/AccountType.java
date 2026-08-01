package com.dev.ledger.domain;

public enum AccountType {
    ASSET(Side.DEBIT),
    EXPENSE(Side.DEBIT),
    LIABILITY(Side.CREDIT),
    EQUITY(Side.CREDIT),
    REVENUE(Side.CREDIT),
    FX_CLEARING(Side.DEBIT);

    private final Side normalSide;

    AccountType(Side normalSide){
        this.normalSide = normalSide;
    }

    public Side normalSide(){
        return normalSide;
    }
}

