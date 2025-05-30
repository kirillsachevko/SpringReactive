package com.epam.spring_reactive.repository;

import com.epam.spring_reactive.model.Sport;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SportReactiveRepository extends ReactiveCrudRepository<Sport, Integer> {
    Flux<Sport> findByNameContains(String name);
}
