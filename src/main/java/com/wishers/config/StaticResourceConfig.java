package com.wishers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

  @Value("${wishers.upload-dir:./data/uploads}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    Path dir = Path.of(uploadDir).toAbsolutePath().normalize();
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(dir.toUri().toString())
        .setCachePeriod(3600);
  }
}
