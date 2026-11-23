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
}
