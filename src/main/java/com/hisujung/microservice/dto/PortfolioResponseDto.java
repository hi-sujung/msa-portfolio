package com.hisujung.microservice.dto;

import com.hisujung.microservice.entity.Portfolio;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PortfolioResponseDto {

    private Long id;
    private String title;
    private String urlLink;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public PortfolioResponseDto(Portfolio entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.urlLink = entity.getUrlLink();
        this.description = entity.getDescription();
        this.createdDate = entity.getCreatedDate();
        this.modifiedDate = entity.getModifiedDate();
    }
}
