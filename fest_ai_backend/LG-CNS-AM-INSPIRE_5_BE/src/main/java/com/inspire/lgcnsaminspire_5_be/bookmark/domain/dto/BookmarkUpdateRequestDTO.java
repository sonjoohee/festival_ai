package com.inspire.lgcnsaminspire_5_be.bookmark.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkUpdateRequestDTO {

    @Size(max = 255, message = "메모는 255자를 초과할 수 없습니다.")
    private String userMemo;
}
