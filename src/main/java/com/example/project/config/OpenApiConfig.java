package com.example.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI teachingFeedbackOpenAPI() {
        String bearerName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("教学反馈智能分析平台 API")
                        .version("1.0")
                        .description("TeachingFeedback — 教师端与学生端 REST 接口"))
                .addSecurityItem(new SecurityRequirement().addList(bearerName))
                .components(new Components()
                        .addSecuritySchemes(bearerName, new SecurityScheme()
                                .name(bearerName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
