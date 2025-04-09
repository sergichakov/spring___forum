package com.forum.eureka.eurekaserver.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Workaround for issue gh-4145 in Spring Cloud 2022.0.1
// See https://github.com/spring-cloud/spring-cloud-netflix/issues/4145
@RestController
class CustomErrorController implements ErrorController {

    private static final String ERROR_MAPPING = "/error";

    @RequestMapping(ERROR_MAPPING)
    public ResponseEntity<Void> error() {
        return ResponseEntity.notFound().build();
    }
}