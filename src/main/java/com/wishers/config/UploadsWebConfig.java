package com.wishers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadsWebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // URL: /uploads/...
    // Файлы: ./uploads/...
    String uploadDir = Path.of("uploads").toAbsolutePath().toUri().toString();

    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(uploadDir)
        .setCachePeriod(3600);

    // если вдруг у тебя в коде уже используется другой префикс
    registry.addResourceHandler("/files/**")
        .addResourceLocations(uploadDir)
        .setCachePeriod(3600);

    registry.addResourceHandler("/images/**")
        .addResourceLocations(uploadDir)
        .setCachePeriod(3600);
  }
}
