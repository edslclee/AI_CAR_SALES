package com.aicarsales.app.service;

import com.aicarsales.app.domain.Car;
import com.aicarsales.app.repository.CarRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarCatalogServiceImpl implements CarCatalogService {

    private final CarRepository carRepository;

    public CarCatalogServiceImpl(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> listAll() {
        return carRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> findByBodyType(String bodyType) {
        if (bodyType == null || bodyType.isBlank()) {
            return List.of();
        }
        return carRepository.findByBodyTypeIgnoreCase(bodyType);
    }
}
