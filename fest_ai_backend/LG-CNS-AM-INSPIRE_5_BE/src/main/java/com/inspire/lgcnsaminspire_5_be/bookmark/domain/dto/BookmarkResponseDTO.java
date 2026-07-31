package com.inspire.lgcnsaminspire_5_be.bookmark.domain.dto;

import com.inspire.lgcnsaminspire_5_be.bookmark.domain.entity.BookmarkEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponseDTO {

    private Long bookmarkId;
    private String title;
    private String contentId;
    private String imageUrl;
    private String userMemo;

    public static BookmarkResponseDTO fromEntity(BookmarkEntity entity) {
        return BookmarkResponseDTO.builder()
                .bookmarkId(entity.getBookmarkId())
                .title(entity.getFestival().getTitle())
                .contentId(entity.getFestival().getContentId())
                .imageUrl(entity.getFestival().getImageUrl())
                .userMemo(entity.getUserMemo())
                .build();
    }
}
