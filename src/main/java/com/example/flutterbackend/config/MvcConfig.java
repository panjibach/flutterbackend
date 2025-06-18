package com.example.flutterbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Baris ini membuat semua file di dalam direktori ./uploads/
        // bisa diakses melalui URL /uploads/**
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
} {
    
}
