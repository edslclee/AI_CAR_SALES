
package com.aicarsales.app.admin;

import java.math.BigDecimal;

public record CarCsvRecord(
        String oemCode,
        String modelName,
        String trim,
        Integer price,
        String bodyType,
        String fuelType,
        BigDecimal efficiency,
        Short seats,
        String drivetrain,
        Short releaseYear,
        String featuresJson,
        String mediaAssetsJson
) {
}
