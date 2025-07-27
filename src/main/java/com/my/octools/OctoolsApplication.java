package com.my.octools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for OC Tools homework.
 * @author Jonathan Chang (chang.je@gmail.com)
 */
@SpringBootApplication(scanBasePackages = {"com.my.octools"})
@EnableScheduling
public class OctoolsApplication {
    /**
     * Application entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OctoolsApplication.class, args);
    }
}