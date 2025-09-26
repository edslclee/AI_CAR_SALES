
package com.aicarsales.app.admin;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class CsvUploadConfig {

    @Bean
    public Executor csvUploadTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("csv-upload-");
        executor.setConcurrencyLimit(5);
        return executor;
    }
}
