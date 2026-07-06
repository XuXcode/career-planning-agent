package com.mssj.config;

import com.mssj.filter.JwtAuthenticationFilter;
import com.mssj.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(
                    "/api/**"
                )
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/check-username",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/api/auth/info",
                    "/auth/login",
                    "/auth/register",
                    "/auth/check-username",
                    "/auth/refresh",
                    "/auth/logout",
                    "/auth/info",
                    "/login.html",
                    "/register.html",
                    "/index.html",
                    "/job-profile/**",
                    "/job-relation/**",
                    "/job-graph/**",
                    "/hybridaction/**"
                );
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthenticationFilter);
        // 过滤所有请求，但公开路径在过滤器内部跳过
        registration.addUrlPatterns("/*");
        // 设置过滤器顺序，确保在其他过滤器之前执行
        registration.setOrder(1);
        return registration;
    }
}