
package com.aicarsales.app.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aicarsales.app.repository.CarRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import com.aicarsales.app.support.AbstractIntegrationTest;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCsvUploadControllerTest extends AbstractIntegrationTest {

    private static final String VALID_CSV = """
            oem_code,model_name,trim,price,body_type,fuel_type,efficiency,seats,drivetrain,release_year,features,media_assets
            HYUNDAI-IONIQ5,Ioniq 5,Long Range AWD,58500000,SUV,EV,5.2,5,AWD,2024,"{""safety"":{""adas"":true}}","{}"
            KIA-EV6,EV6,GT-Line,63300000,Crossover,EV,4.8,5,AWD,2024,"{}","{}"
            """.stripIndent();

    private static final String INVALID_CSV = """
            oem_code,model_name
            HYUNDAI-IONIQ5,
            """.stripIndent();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CsvUploadJobRepository jobRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
        carRepository.deleteAll();
    }

    @Test
    void uploadValidCsvShouldProcessSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cars.csv",
                "text/csv",
                VALID_CSV.getBytes(StandardCharsets.UTF_8));

        String response = mockMvc.perform(multipart("/api/v1/admin/cars/upload").file(file))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        String jobId = objectMapper.readTree(response).get("jobId").asText();

        mockMvc.perform(get("/api/v1/admin/cars/upload/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.message").value("created=2, updated=0"));

        assertThat(carRepository.findAll()).hasSize(2);
    }

    @Test
    void uploadInvalidCsvShouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cars.csv",
                "text/csv",
                INVALID_CSV.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/admin/cars/upload").file(file))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public Executor csvUploadTaskExecutor() {
            return Runnable::run; // synchronous for tests
        }
    }
}
