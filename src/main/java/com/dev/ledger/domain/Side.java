package com.dev.ledger.domain;

public enum Side {
    DEBIT("DR"),
    CREDIT("CR");

    private final String code;

    Side(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public Side opposite() {
        return this == DEBIT ? CREDIT : DEBIT;
    }

    public static Side fromCode(String code) {
        return switch (code) {
            case "DR", "DEBIT" -> DEBIT;
            case "CR", "CREDIT" -> CREDIT;
            default -> throw new IllegalArgumentException("Unknown side: " + code);
        };
    }

}
