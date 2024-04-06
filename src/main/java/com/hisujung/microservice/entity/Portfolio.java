package com.hisujung.microservice.entity;

import com.hisujung.microservice.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Portfolio extends BaseTimeEntity {

    @Id
    @Column(name = "portfolio_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
//    private Member member;

    @Column(nullable = false)
    private String title;

    private String urlLink;

    private String description;

    @Builder
    public Portfolio(String title, String urlLink, String description) {
        //this.member = member;
        this.title = title;
        this.urlLink = urlLink;
        this.description = description;
    }

    public void update(String title, String urlLink, String description) {
        this.title = title;
        this.urlLink = urlLink;
        this.description = description;
    }
}
