package com.yushan.analytics_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration for Engagement Service
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI engagementServiceOpenAPI() {
        // Server configuration
        Server localServer = new Server();
        localServer.setUrl("/");
        localServer.setDescription("Engagement Service API");

        // Contact information
        Contact contact = new Contact();
        contact.setName("Yushan Platform Team");
        contact.setEmail("support@yushan.com");

        // License
        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        // API Info
        Info info = new Info()
                .title("Yushan Engagement Service API")
                .version("1.0.0")
                .description("API for managing comments, likes, reports and user engagement interactions")
                .contact(contact)
                .license(license);

        // Security scheme for JWT
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .schemaRequirement(SECURITY_SCHEME_NAME, securityScheme)
                .security(List.of(securityRequirement));
    }
}
