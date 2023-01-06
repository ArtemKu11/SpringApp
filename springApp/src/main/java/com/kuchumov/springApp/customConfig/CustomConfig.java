package com.kuchumov.springApp.customConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class CustomConfig {
    @Bean
    public Docket swaggerConfig() { // http://localhost:8080/swagger-ui/
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.kuchumov.springApp.controller"))
                .build()
                .useDefaultResponseMessages(false);
    }
}
