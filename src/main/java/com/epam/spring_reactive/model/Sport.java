package com.epam.spring_reactive.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@Table("sports")
public class Sport {
    @Id
    private Integer id;
    private String name;
}
