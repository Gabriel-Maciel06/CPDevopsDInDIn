package br.com.fiap.dimdim.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DimDim Cloud Financial - Web App & Azure SQL API")
                        .version("2.0.0")
                        .description("API e Aplicação Web PaaS do Projeto DimDim para o 2º Checkpoint de DevOps Tools & Cloud Computing (FIAP).\n" +
                                "Arquitetura em nuvem utilizando Azure App Service (Web App Linux), Azure SQL Database e Application Insights.")
                        .contact(new Contact()
                                .name("Grupo DimDim - FIAP 2TDSR")
                                .email("RM562795@fiap.com.br"))
                        .license(new License()
                                .name("FIAP 2026 - Prof. João Menk")
                                .url("https://www.fiap.com.br")));
    }
}
