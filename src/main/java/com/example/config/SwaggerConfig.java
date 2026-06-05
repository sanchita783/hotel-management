package com.example.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Management System API")
                        .description("""
                                ## Grand Hotel Management System
                                
                                A comprehensive REST API for managing hotel operations including:
                                - **User Management** – Registration, Login, Profile
                                - **Room Management** – CRUD, Availability Search
                                - **Booking Management** – Create, Cancel, History
                                - **Payment Management** – Payment Records & Status
                                - **Hotel Services** – Laundry, Room Service, Restaurant
                                - **Enquiry System** – Submit & Manage Enquiries
                                - **Email Notifications** – Automated booking & payment emails
                                
                                ### Authentication
                                Use the `/auth/login` endpoint to obtain a JWT token, then click **Authorize** and enter: `Bearer <token>`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hotel Management System")
                                .email("support@grandhotel.com")
                                .url("https://grandhotel.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort + "/api").description("Local Development"),
                        new Server().url("https://api.grandhotel.com").description("Production")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /auth/login")));
    }
}
