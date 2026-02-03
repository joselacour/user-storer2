package model.converter;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;

public class InstantAttributeConverter implements AttributeConverter<Instant> {

    @Override
    public AttributeValue transformFrom(Instant input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        return AttributeValue.builder().n(Long.toString(input.toEpochMilli())).build();
    }

    @Override
    public Instant transformTo(AttributeValue input) {
        if (input == null || input.n() == null) {
            return null;
        }
        return Instant.ofEpochMilli(Long.parseLong(input.n()));
    }

    @Override
    public EnhancedType<Instant> type() {
        return EnhancedType.of(Instant.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.N;
    }
}