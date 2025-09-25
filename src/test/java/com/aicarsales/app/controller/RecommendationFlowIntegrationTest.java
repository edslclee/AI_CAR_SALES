package com.aicarsales.app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aicarsales.app.domain.Car;
import com.aicarsales.app.domain.User;
import com.aicarsales.app.repository.CarRepository;
import com.aicarsales.app.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecommendationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    private User user;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setEmail("integration@test.com");
        user.setPasswordHash("hashed");
        user.setRole("USER");
        userRepository.save(user);

        Car suv = new Car();
        suv.setOemCode("KIA-SUV");
        suv.setModelName("Sorento");
        suv.setBodyType("SUV");
        suv.setFuelType("Gasoline");
        suv.setPrice(35000000);
        carRepository.save(suv);

        Car sedan = new Car();
        sedan.setOemCode("HYUNDAI-SEDAN");
        sedan.setModelName("Sonata");
        sedan.setBodyType("Sedan");
        sedan.setFuelType("Hybrid");
        sedan.setPrice(28000000);
        carRepository.save(sedan);
    }

    @Test
    void surveyToRecommendationAndFavoritesFlow() throws Exception {
        JsonNode surveyPayload = objectMapper.readTree("""
                {
                  "userId": %d,
                  "budgetMin": 25000000,
                  "budgetMax": 36000000,
                  "usage": "Family",
                  "passengers": 5,
                  "preferredBodyTypes": ["SUV"],
                  "preferredBrands": ["KIA"],
                  "options": {"mustHave": ["sunroof"]}
                }
                """.formatted(user.getId()));

        String surveyResponse = mockMvc.perform(post("/api/v1/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(surveyPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long recommendationId = objectMapper.readTree(surveyResponse).get("recommendationId").asLong();
        assertThat(recommendationId).isPositive();

        String recommendationResponse = mockMvc.perform(get("/api/v1/recommendations/" + recommendationId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode items = objectMapper.readTree(recommendationResponse).get("items");
        assertThat(items).isNotNull();
        assertThat(items.size()).isGreaterThan(0);

        Long carId = items.get(0).get("car").get("id").asLong();

        JsonNode favoritePayload = objectMapper.readTree("""
                {
                  "userId": %d,
                  "carId": %d,
                  "note": "Looks promising"
                }
                """.formatted(user.getId(), carId));

        mockMvc.perform(post("/api/v1/favorites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(favoritePayload)))
                .andExpect(status().isCreated());

        String favoritesResponse = mockMvc.perform(get("/api/v1/favorites").param("userId", String.valueOf(user.getId())))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode favorites = objectMapper.readTree(favoritesResponse);
        assertThat(favorites.isArray()).isTrue();
        assertThat(favorites.size()).isEqualTo(1);
        assertThat(favorites.get(0).get("carId").asLong()).isEqualTo(carId);
    }
}
