
package com.aicarsales.app.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class CsvValidator {

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "oem_code",
            "model_name",
            "price",
            "body_type",
            "fuel_type",
            "release_year"
    );

    private final ObjectMapper objectMapper;

    public CsvValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<CarCsvRecord> parse(byte[] data, String filename) {
        if (data == null || data.length == 0) {
            throw new CsvValidationException("CSV 파일이 비어 있습니다.");
        }
        try (CSVParser parser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build()
                .parse(new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8))) {

            validateHeaders(parser.getHeaderNames());
            List<CarCsvRecord> records = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            int rowNumber = 1; // header counted separately
            for (CSVRecord record : parser) {
                rowNumber++;
                try {
                    records.add(toCarRecord(record));
                } catch (IllegalArgumentException e) {
                    errors.add("Row " + rowNumber + ": " + e.getMessage());
                }
            }

            if (!errors.isEmpty()) {
                throw new CsvValidationException(String.join("; ", errors));
            }

            return records;
        } catch (IOException e) {
            throw new CsvValidationException("CSV 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void validateHeaders(List<String> headers) {
        Set<String> normalized = new HashSet<>();
        for (String header : headers) {
            if (header != null) {
                normalized.add(header.trim().toLowerCase());
            }
        }
        if (!normalized.containsAll(REQUIRED_HEADERS)) {
            Set<String> missing = new HashSet<>(REQUIRED_HEADERS);
            missing.removeAll(normalized);
            throw new CsvValidationException("필수 컬럼이 누락되었습니다: " + String.join(", ", missing));
        }
    }

    private CarCsvRecord toCarRecord(CSVRecord record) {
        String oemCode = required(record, "oem_code");
        String modelName = required(record, "model_name");
        String trim = optional(record, "trim");
        Integer price = parseInteger(required(record, "price"), "price");
        String bodyType = required(record, "body_type");
        String fuelType = required(record, "fuel_type");
        BigDecimal efficiency = parseBigDecimal(optional(record, "efficiency"), "efficiency");
        Short seats = parseShort(optional(record, "seats"), "seats");
        String drivetrain = optional(record, "drivetrain");
        Short releaseYear = parseShort(required(record, "release_year"), "release_year");
        String features = normalizeJson(optional(record, "features"), "features");
        String mediaAssets = normalizeJson(optional(record, "media_assets"), "media_assets");

        return new CarCsvRecord(oemCode, modelName, trim, price, bodyType, fuelType,
                efficiency, seats, drivetrain, releaseYear, features, mediaAssets);
    }

    private String required(CSVRecord record, String name) {
        String value = record.get(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " 값이 필요합니다.");
        }
        return value.trim();
    }

    private String optional(CSVRecord record, String name) {
        String value = null;
        try {
            value = record.get(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private Integer parseInteger(String value, String field) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 값이 정수가 아닙니다.");
        }
    }

    private Short parseShort(String value, String field) {
        if (value == null) {
            return null;
        }
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 값이 정수가 아닙니다.");
        }
    }

    private BigDecimal parseBigDecimal(String value, String field) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 값이 숫자가 아닙니다.");
        }
    }

    private String normalizeJson(String value, String field) {
        if (value == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(value);
            return objectMapper.writeValueAsString(node);
        } catch (IOException e) {
            throw new IllegalArgumentException(field + " 컬럼이 올바른 JSON 형식이 아닙니다.");
        }
    }
}
