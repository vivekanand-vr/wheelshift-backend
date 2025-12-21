package com.wheelshiftpro.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for Swagger/OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wheelShiftOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setName("WheelShift Support");
        contact.setEmail("support@wheelshift.com");

        License license = new License()
                .name("Proprietary")
                .url("https://wheelshift.com/license");

        Info info = new Info()
                .title("WheelShift Pro API")
                .version("1.0.0")
                .description("RESTful API for WheelShift - Used Car Trading Management System. " +
                        "This API provides comprehensive endpoints for managing inventory, inspections, " +
                        "clients, employees, sales, reservations, and financial transactions.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}
