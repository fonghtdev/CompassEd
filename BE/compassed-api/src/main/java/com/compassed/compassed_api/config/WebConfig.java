package com.compassed.compassed_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final RoleInterceptor roleInterceptor;

    public WebConfig(RoleInterceptor roleInterceptor) {
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://127.0.0.1:5500",
                        "http://localhost:5500",
                        "http://127.0.0.1:5000",
                        "http://localhost:5000",
                        "http://127.0.0.1:3000",
                        "http://localhost:3000",
                        "https://compassed.io.vn",
                        "https://www.compassed.io.vn",
                        "http://compassed.io.vn",
                        "http://www.compassed.io.vn")
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**"); // Không check role cho auth endpoints
    }
}
