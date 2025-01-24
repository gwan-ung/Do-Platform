package com.metamate.metamate_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file-upload.path.windows}")
    private String windowsUploadPath;

    @Value("${file-upload.path.linux}")
    private String linuxUploadPath;

//    @PostConstruct
//    public void init() {
//        System.out.println("Windows Upload Path: " + windowsUploadPath);
//        System.out.println("Linux Upload Path: " + linuxUploadPath);
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        String uploadPath = System.getProperty("os.name").toLowerCase().contains("win") ? windowsUploadPath : linuxUploadPath;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadPath);  // OS에 맞는 업로드 경로
    }
}
