package com.example.resourceserver.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)

//@SecurityRequirement(name = "JWT")
@SecurityScheme(
        name = "JWT",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer"
)
@OpenAPIDefinition(
        info = @Info(
                title = "REST API documentation",
                version = "1.0",
                description = """
                        <a href='http://localhost/'>JavaRush Jira application</a><br>
                        <p><b>Тестовые креденшелы:</b><br>
                        - user@gmail.com / password<br>
                        - admin@gmail.com / admin<br>
                        - guest@gmail.com / guest
                        или JWT 
                        /auth/sign-up - вход с новым пользователем
                        /auth/sign-in - вход
                        </p>""",
                contact = @Contact(url = "https://javarush.com/about/contacts", email = "support@javarush.com")
        ),
        servers = {
                @Server(url = "${app.host-url}")
        },
        security = {
//              https://stackoverflow.com/questions/61477056/why-is-the-authorization-header-missing-in-requests-sent-from-swagger-ui
                @SecurityRequirement(name = "JWT"),
                @SecurityRequirement(name = "basicAuth")
        }//"basicAuth")
)
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("REST API")
                .pathsToMatch("/**")//все этдпоинты .pathsToMatch("/api/**","/**")
                .build();
    }

   /* private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme(){type(SecuritySchemeType.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }}

  */
}
