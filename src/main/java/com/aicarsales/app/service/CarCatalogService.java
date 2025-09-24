package com.aicarsales.app.service;

import com.aicarsales.app.domain.Car;
import java.util.List;

public interface CarCatalogService {
    List<Car> listAll();
    List<Car> findByBodyType(String bodyType);
}
