package com.dev.ledger.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SideConverter implements AttributeConverter<Side, String> {

    @Override
    public String convertToDatabaseColumn(Side side) {
        return side == null ? null : side.code();
    }

    @Override
    public Side convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : Side.fromCode(dbValue.trim());
    }
}
