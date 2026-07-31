package com.inspire.lgcnsaminspire_5_be.bookmark.domain.entity;

import com.inspire.lgcnsaminspire_5_be.auth.domain.entity.UserEntity;
import com.inspire.lgcnsaminspire_5_be.festival.domain.entity.FestivalEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookmark")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private FestivalEntity festival;

    private String userMemo;

    public void updateUserMemo(String userMemo) {
        this.userMemo = userMemo;
    }
}
