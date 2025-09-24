package com.aicarsales.app.repository;

import com.aicarsales.app.domain.Car;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByBodyTypeIgnoreCase(String bodyType);
    List<Car> findByReleaseYearBetween(Short startYear, Short endYear);
}
