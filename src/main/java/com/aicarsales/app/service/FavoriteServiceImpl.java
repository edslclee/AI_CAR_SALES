package com.aicarsales.app.service;

import com.aicarsales.app.domain.Car;
import com.aicarsales.app.domain.Favorite;
import com.aicarsales.app.domain.User;
import com.aicarsales.app.repository.CarRepository;
import com.aicarsales.app.repository.FavoriteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final CarRepository carRepository;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository, CarRepository carRepository) {
        this.favoriteRepository = favoriteRepository;
        this.carRepository = carRepository;
    }

    @Override
    @Transactional
    public Favorite addFavorite(User user, Long carId, String note) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new IllegalArgumentException("Car not found: " + carId));
        return favoriteRepository.findByUserIdAndCarId(user.getId(), carId).orElseGet(() -> {
            Favorite favorite = new Favorite();
            favorite.setUser(user);
            favorite.setCar(car);
            favorite.setNote(note);
            return favoriteRepository.save(favorite);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Favorite> listFavorites(User user) {
        return favoriteRepository.findByUserId(user.getId());
    }
}
