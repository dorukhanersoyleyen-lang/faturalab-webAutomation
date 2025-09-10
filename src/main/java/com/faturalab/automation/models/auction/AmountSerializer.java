package com.faturalab.automation.models.auction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AmountSerializer extends JsonSerializer<Double> {
    @Override
    public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        // If the value is integral (like 75000.0), write as integer to avoid trailing .0
        if (Math.floor(value) == value.doubleValue()) {
            gen.writeNumber(value.longValue());
        } else {
            // Write as BigDecimal to avoid scientific notation and keep precision
            java.math.BigDecimal bd = java.math.BigDecimal.valueOf(value);
            gen.writeNumber(bd);
        }
    }
} 