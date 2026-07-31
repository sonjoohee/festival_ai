package com.inspire.lgcnsaminspire_5_be.festival.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "festival")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FestivalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long festivalId;

    @Column(name = "content_id", nullable = false, unique = true, length = 50)
    private String contentId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 20)
    private String region;

    private String addr;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiInfo;

    @Column(name = "image_url")
    private String imageUrl;
}
