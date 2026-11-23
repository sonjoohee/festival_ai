package com.inspire.lgcnsaminspire_5_be.rag.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RagAskRequestDTO {
    @NotBlank(message = "질문을 입력해 주세요.")
    @Size(max = 500, message = "질문은 500자까지 입력할 수 있습니다.")
    private String question;

    // 상세 페이지에서 보낼 때만 넣는다. 있으면 그 축제 조각과 FAQ 안에서만 찾는다.
    @Size(max = 50, message = "contentId가 너무 깁니다.")
    private String contentId;
}
