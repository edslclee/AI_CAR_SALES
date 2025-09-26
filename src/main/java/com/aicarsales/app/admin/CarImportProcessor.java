
package com.aicarsales.app.admin;

import com.aicarsales.app.domain.Car;
import com.aicarsales.app.repository.CarRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CarImportProcessor {

    private final CarRepository carRepository;

    public CarImportProcessor(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Transactional
    public ImportResult importRecords(List<CarCsvRecord> records) {
        int created = 0;
        int updated = 0;

        for (CarCsvRecord record : records) {
            Optional<Car> existing = carRepository.findByOemCodeAndModelNameAndTrim(record.oemCode(), record.modelName(), record.trim());
            Car car = existing.orElseGet(Car::new);
            if (existing.isEmpty()) {
                created++;
            } else {
                updated++;
            }
            car.setOemCode(record.oemCode());
            car.setModelName(record.modelName());
            car.setTrim(record.trim());
            car.setPrice(record.price());
            car.setBodyType(record.bodyType());
            car.setFuelType(record.fuelType());
            car.setEfficiency(record.efficiency());
            car.setSeats(record.seats());
            car.setDrivetrain(record.drivetrain());
            car.setReleaseYear(record.releaseYear());
            car.setFeatures(record.featuresJson());
            car.setMediaAssets(record.mediaAssetsJson());
            carRepository.save(car);
        }

        return new ImportResult(created, updated);
    }

    public record ImportResult(int created, int updated) {
        public String asMessage() {
            return "created=" + created + ", updated=" + updated;
        }
    }
}
