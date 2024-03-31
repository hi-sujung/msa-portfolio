package com.hisujung.microservice.dto;

import com.hisujung.microservice.entity.Portfolio;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PortfolioListResponseDto {
    private Long id;
    private String title;
    private String urlLink;
    //private Member member;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public PortfolioListResponseDto(Portfolio entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.urlLink = entity.getUrlLink();
        //this.member = entity.getMember();
        this.description = entity.getDescription();
        this.createdDate = entity.getCreatedDate();
        this.modifiedDate = entity.getModifiedDate();
    }
}
