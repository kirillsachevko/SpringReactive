package com.epam.spring_reactive.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "api.*")
public class ApplicationProperties {
    private String host;
    private String path;
    private String format;
    private String genreId;
    private String applicationId;
    private String elements;
    private String getMapping;
    private String postMapping;
}
