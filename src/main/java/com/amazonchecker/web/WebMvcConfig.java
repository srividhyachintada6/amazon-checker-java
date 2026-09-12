package com.amazonchecker.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 * Note: Screenshots are now served via the controlled /api/screenshots/{filename}
 * endpoint in DashboardController to prevent arbitrary filesystem traversal.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // Custom controllers handle static resource access with strict security validation
}
