package com.inspire.lgcnsaminspire_5_be.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.inspire.lgcnsaminspire_5_be.rag.chunk.TextChunker;

class TextChunkerTest {

    @Test
    void 빈_글은_조각이_없다() {
        TextChunker chunker = new TextChunker(100, 10);
        assertThat(chunker.split(null)).isEmpty();
        assertThat(chunker.split("  \n\n ")).isEmpty();
    }

    @Test
    void 짧은_글은_한_조각이다() {
        TextChunker chunker = new TextChunker(100, 10);
        assertThat(chunker.split("보령 머드축제는 7월에 열린다.")).containsExactly("보령 머드축제는 7월에 열린다.");
    }

    @Test
    void 문단을_최대_길이_안에서_이어_붙인다() {
        TextChunker chunker = new TextChunker(20, 0);
        List<String> chunks = chunker.split("가나다라마\n\n바사아자차\n\n카타파하가나다라마바사아자차");

        assertThat(chunks).containsExactly("가나다라마\n바사아자차", "카타파하가나다라마바사아자차");
        assertThat(chunks).allSatisfy(c -> assertThat(c.length()).isLessThanOrEqualTo(20));
    }

    @Test
    void 긴_문단은_문장_끝에서_자른다() {
        TextChunker chunker = new TextChunker(30, 0);
        String text = "첫 번째 문장은 열다섯 글자다. 두 번째 문장도 열다섯 글자다. 세 번째 문장입니다.";

        List<String> chunks = chunker.split(text);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allSatisfy(c -> assertThat(c.length()).isLessThanOrEqualTo(30));
        assertThat(String.join(" ", chunks)).isEqualTo(text);
    }

    @Test
    void 문장_끝이_없으면_글자_수로_자른다() {
        TextChunker chunker = new TextChunker(10, 0);
        List<String> chunks = chunker.split("가".repeat(25));

        assertThat(chunks).extracting(String::length).containsExactly(10, 10, 5);
    }

    @Test
    void 다음_조각_앞에_이전_조각_끝을_겹쳐_붙인다() {
        TextChunker chunker = new TextChunker(10, 3);
        List<String> chunks = chunker.split("가나다라마바사아자차\n카타파하");

        assertThat(chunks).containsExactly("가나다라마바사아자차", "아자차 카타파하");
    }

    @Test
    void 잘못된_설정은_막는다() {
        assertThatThrownBy(() -> new TextChunker(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TextChunker(10, 10)).isInstanceOf(IllegalArgumentException.class);
    }
}
