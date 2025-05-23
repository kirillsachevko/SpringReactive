package com.epam.spring_reactive.config;

import com.epam.spring_reactive.etl.SportHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ApiRouter {

    @Bean
    public RouterFunction<ServerResponse> apiRoutes(SportHandler sportHandler, ApplicationProperties properties) {
        return route()
                .POST(properties.getPostMapping(), sportHandler::createSport)
                .GET(properties.getGetMapping(), sportHandler::searchSportsByName)
                .build();
    }
}
