package com.canvara.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${canvara.upload.dir}")
    private String uploadDir;

    @Value("${canvara.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Serve uploaded images as static resources.
     * e.g. GET /uploads/artworks/abc123.jpg → file on disk
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/artworks/**")
                .addResourceLocations(absolutePath);
    }

    /** Allow React dev server (port 3000) to call the API. */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
