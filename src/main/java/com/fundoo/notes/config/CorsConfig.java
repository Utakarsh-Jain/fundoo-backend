
package com.fundoo.notes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * Class Name  : CorsConfig
 * Description : Configures Cross-Origin Resource Sharing
 *               to allow requests from the React frontend.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

@Configuration
public class CorsConfig {

    // Configures allowed frontend origins and HTTP methods.
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS"
                        )
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization");
            }
        };
    }
}
