package com.aicarsales.app.controller;

import com.aicarsales.app.controller.dto.CreateFavoriteRequest;
import com.aicarsales.app.controller.dto.FavoriteResponse;
import com.aicarsales.app.domain.Favorite;
import com.aicarsales.app.domain.User;
import com.aicarsales.app.service.FavoriteService;
import com.aicarsales.app.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoritesController {

    private final FavoriteService favoriteService;
    private final UserService userService;

    public FavoritesController(FavoriteService favoriteService, UserService userService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<FavoriteResponse> addFavorite(@Valid @RequestBody CreateFavoriteRequest request) {
        User user = userService.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Favorite favorite;
        try {
            favorite = favoriteService.addFavorite(user, request.carId(), request.note());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(favorite));
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> listFavorites(@RequestParam Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<FavoriteResponse> responses = favoriteService.listFavorites(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getCar().getId(),
                favorite.getCar().getModelName(),
                favorite.getCar().getTrim(),
                favorite.getCar().getPrice(),
                favorite.getNote(),
                favorite.getCreatedAt()
        );
    }
}
