package com.ecommerce.shop.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Shop API",
                version = "v1",
                description = "E-commerce backend APIs for auth, catalog, cart, checkout, and orders.",
                contact = @Contact(name = "Shop API Team", email = "api@shop.local"),
                license = @License(name = "Proprietary")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/auth/**", "/api/products/**", "/api/categories/**")
                .build();
    }

    @Bean
    public GroupedOpenApi shoppingApi() {
        return GroupedOpenApi.builder()
                .group("shopping")
                .pathsToMatch(
                        "/api/cart", "/api/cart/**",
                        "/api/checkout", "/api/checkout/**",
                        "/api/orders", "/api/orders/**"
                )
                .build();
    }
}
