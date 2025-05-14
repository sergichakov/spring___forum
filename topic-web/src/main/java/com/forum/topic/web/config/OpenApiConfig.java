package com.forum.topic.web.config;

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
                        <a href='http://localhost/'>Forum application</a><br>
                        <p><b>Тестовые креденшелы:</b><br>
                        - 1@gmail.com / user password<br>
                        - 2@gmail.com / admin password<br>
                        и обязательно с использованием JWT <br>
                        Из Response заголовка code получите authorization_code <br>
                        <a href='http://localhost:9000/oauth2/authorize?response_type=code&client_id=gateway'>Authorization</a>.<br>
                         вставте в ссылку с соотвествующем полем code=<br>
                        curl --location --request POST 'http://127.0.0.1:9000/oauth2/token?grant_type=authorization_code&code='<br>
                        'PWi0bOo0Ne7MgPFlPY0VxfglG3RnUHO-d5M4RbNLBz4gG3ujiRDF8uZvjXHhRJaMxjY5WPUrZS7gh1jdFOPQUO55COo9Vx55aCV-6MRjHJeKPj0dcwrnPumRNc_9gWDd'<br>
                        '&redirect_uri=http://localhost:8080/swagger-ui/index.html' <br>
                        --header<br> 'Authorization: Basic Z2F0ZXdheTpzZWNyZXQ='<br>
                         Затем вставте полученный код в поле code= в ссылке <br> 
                        http://127.0.0.1:9000/oauth2/token?grant_type=authorization_code&code=<br>
                          Из возвращенного JSON скопируйте access token и вставте в Authorize -> JWT token</p><br>
                        Topic - микросервис тем - первых постов, ожидающих модерации Admin'ом. Admin регистрирует пост из Topic в Directories.
                          Пользователи и Admin'ы видят только свои Topic. Чтобы увидеть все Topics требутся в GET/topicsweb?allusers=true
                        """,
                contact = @Contact(url = "https://localhost:8080/", email = "sergejsmelchakov@gmail.com")
        ),
        servers = {
                @Server(url = "${app.host-url}")
        },
        security = {

                @SecurityRequirement(name = "JWT"),
                @SecurityRequirement(name = "basicAuth")
        }
)
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("REST API")
                .pathsToMatch("/**")
                .build();
    }
}
