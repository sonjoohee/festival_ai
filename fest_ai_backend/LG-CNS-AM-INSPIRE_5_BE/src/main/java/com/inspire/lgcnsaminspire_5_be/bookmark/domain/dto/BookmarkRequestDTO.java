package com.inspire.lgcnsaminspire_5_be.bookmark.domain.dto;

import com.inspire.lgcnsaminspire_5_be.festival.domain.entity.FestivalEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkRequestDTO {

    @NotBlank(message = "축제 ID는 필수입니다.")
    private String contentId;

    @NotBlank(message = "축제명은 필수입니다.")
    private String title;

    @NotBlank(message = "지역 정보는 필수입니다.")
    private String region;

    @NotBlank(message = "주소는 필수입니다.")
    private String addr;

    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;

    @NotBlank(message = "축제 설명은 필수입니다.")
    private String content;

    @NotBlank(message = "AI 요약 정보는 필수입니다.")
    private String aiInfo;

    private String userMemo;

    public FestivalEntity toFestivalEntity() {
        return FestivalEntity.builder()
                .contentId(this.contentId)
                .title(this.title)
                .region(this.region)
                .addr(this.addr)
                .imageUrl(this.imageUrl)
                .content(this.content)
                .aiInfo(this.aiInfo)
                .build();
    }
}
