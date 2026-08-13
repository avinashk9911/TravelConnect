package com.travelconnect.traveler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Traveler Service.
 *
 * @SpringBootApplication combines three annotations:
 *   @Configuration       - this class can define Spring beans
 *   @EnableAutoConfiguration - Spring Boot auto-configures based on classpath
 *   @ComponentScan       - scans this package and sub-packages for Spring components
 */
@SpringBootApplication
public class TravelerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelerServiceApplication.class, args);
    }
}
