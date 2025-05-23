package com.epam.spring_reactive.etl;

import com.epam.spring_reactive.model.Sport;
import com.epam.spring_reactive.repository.SportReactiveRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.epam.spring_reactive.util.ApplicationConstants.*;

@Component
public class SportHandler {
    private final SportReactiveRepository repository;

    public SportHandler(SportReactiveRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> createSport(ServerRequest request) {
        String sportName = request.pathVariable(SPORT_NAME_PARAMETER);
        return repository.findByNameContains(sportName)
                .hasElements()
                .flatMap(exists -> {
                    if (exists) {
                        return ServerResponse.badRequest().bodyValue("Sport already exists!");
                    } else {
                        Sport sport = Sport.builder()
                                .name(sportName)
                                .build();
                        return repository.save(sport)
                                .flatMap(savedSport -> ServerResponse.ok().bodyValue(savedSport));
                    }
                });
    }

    public Mono<ServerResponse> searchSportsByName(ServerRequest request) {
        String query = request.queryParam("q").orElse("");
        return ServerResponse.ok()
                .body(repository.findByNameContains(query), Sport.class);
    }
}
