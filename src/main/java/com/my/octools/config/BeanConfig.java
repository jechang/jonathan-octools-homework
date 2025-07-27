package com.my.octools.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableScheduling
public class BeanConfig {

    /**
     * Configures WebClient with authentication for API calls
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://oct-backend-homework.us-east-1.elasticbeanstalk.com:8080")
                .defaultHeaders(headers -> headers.setBasicAuth("b2N0QXBwbGljYW50OmIwZTg1YWE4LWQ2YWUtNGQzYi1iODA5LTA0ZDIwN2VkZTNmNQ=="))
                .build();
    }

    /**
     * Primary executor for general async tasks (100 threads)
     */
    @Bean(name = "apiTaskExecutor", destroyMethod = "shutdown")
    public ScheduledExecutorService apiTaskExecutor() {
        return Executors.newScheduledThreadPool(100);
    }
}