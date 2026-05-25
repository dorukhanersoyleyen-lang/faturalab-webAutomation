package com.faturalab.automation.models.auction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmountSerializer extends JsonSerializer<Double> {
    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isNaN() || value.isInfinite()) {
            gen.writeNull();
            return;
        }

        if (Math.floor(value) == value) {
            gen.writeNumber(value.longValue());
            return;
        }

        BigDecimal decimal = BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros();

        if (decimal.scale() <= 0) {
            gen.writeNumber(decimal.longValue());
        } else {
            gen.writeNumber(decimal);
        }
    }
} 