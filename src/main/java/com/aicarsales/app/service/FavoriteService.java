package com.aicarsales.app.service;

import com.aicarsales.app.domain.Favorite;
import com.aicarsales.app.domain.User;
import java.util.List;

public interface FavoriteService {

    Favorite addFavorite(User user, Long carId, String note);

    List<Favorite> listFavorites(User user);
}
