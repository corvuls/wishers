package com.wishers.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class I18nConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames("classpath:messages", "classpath:i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setUseCodeAsDefaultMessage(false);
        return ms;
    }
}
