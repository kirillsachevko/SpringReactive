package com.epam.spring_reactive.etl;

import com.epam.spring_reactive.config.ApplicationProperties;
import com.epam.spring_reactive.model.Sport;
import com.epam.spring_reactive.repository.SportReactiveRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;

import static com.epam.spring_reactive.util.ApplicationConstants.*;

@Slf4j
@Component
public class SportETL {
    private static final Gson GSON = new Gson();
    private final SportReactiveRepository repository;
    private final WebClient webClient;
    private final ApplicationProperties properties;

    public SportETL(SportReactiveRepository repository, ApplicationProperties properties) {
        this.repository = repository;
        this.properties = properties;
        this.webClient = WebClient.create();
    }

    @PostConstruct
    void init() {
        executeETLProcess();
    }

    public void executeETLProcess() {
        webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(URL_SCHEME)
                        .host(properties.getHost())
                        .path(properties.getPath())
                        .queryParams(populateRequestParams())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMapMany(this::mapToSport)
                .onBackpressureBuffer()
                .limitRate(20)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(repository::save)
                .doOnError(sport -> log.error(String.format("Error with retrieving %s", sport)))
                .subscribe();
    }

    private Flux<Sport> mapToSport(String jsonResponse) {
        JsonObject jsonNode = GSON.fromJson(jsonResponse, JsonObject.class);
        JsonArray arrayNode = extractArray(jsonNode, ITEM_ARRAY_PROPERTY);

        return Flux.fromIterable(arrayNode)
                .map(item -> {
                    if (!item.isJsonNull() && item.getAsJsonObject().has(ITEM_PROPERTY)) {
                        JsonObject itemObject = item.getAsJsonObject().get(ITEM_PROPERTY).getAsJsonObject();
                        if (!itemObject.isJsonNull() && itemObject.getAsJsonObject().has(ITEM_NAME_PROPERTY)) {
                            String name = itemObject.get(ITEM_NAME_PROPERTY).getAsString();
                            log.info(name);
                            return Sport.builder()
                                    .name(name)
                                    .build();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull);
    }

    private MultiValueMap<String, String> populateRequestParams() {
        return MultiValueMap.fromSingleValue(Map.of(FORMAT_PARAMETER, properties.getFormat(), GENRE_ID_PARAMETER, properties.getGenreId(),
                APPLICATION_ID_PARAMETER, properties.getApplicationId(), ELEMENTS_PARAMETER, properties.getElements()));
    }

    private JsonArray extractArray(JsonObject json, String property) {
        JsonArray arrayNode = json.get(property).getAsJsonArray();
        if (arrayNode == null || !arrayNode.isJsonArray()) {
            return new JsonArray();
        }
        return arrayNode;
    }
}
