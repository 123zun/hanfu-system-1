package com.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到实际目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")  // 相对路径
                .setCachePeriod(0); // 开发环境禁用缓存
    }
}
