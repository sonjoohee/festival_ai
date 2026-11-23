package com.inspire.lgcnsaminspire_5_be.rag.chunk;

import java.util.ArrayList;
import java.util.List;

/**
 * 긴 글을 검색하기 좋은 크기의 조각으로 자른다.
 *
 * 1) 문단(빈 줄, 줄바꿈) 단위로 나눈 뒤 maxChars를 넘지 않을 때까지 이어 붙인다.
 * 2) 한 문단이 maxChars보다 길면 문장 끝(. ! ? 다.)에서 자르고, 그래도 길면 글자 수로 자른다.
 * 3) 조각 경계에서 문맥이 끊기지 않도록 앞 조각의 끝 overlapChars 글자를 다음 조각 앞에 붙인다.
 */
public class TextChunker {

    private final int maxChars;
    private final int overlapChars;

    public TextChunker(int maxChars, int overlapChars) {
        if (maxChars <= 0) {
            throw new IllegalArgumentException("maxChars는 1 이상이어야 합니다.");
        }
        if (overlapChars < 0 || overlapChars >= maxChars) {
            throw new IllegalArgumentException("overlapChars는 0 이상, maxChars 미만이어야 합니다.");
        }
        this.maxChars = maxChars;
        this.overlapChars = overlapChars;
    }

    public List<String> split(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return result;
        }

        // 1. 문단을 maxChars 이하 조각으로 만든다
        List<String> pieces = new ArrayList<>();
        for (String paragraph : text.split("\\n+")) {
            String p = paragraph.strip();
            if (p.isEmpty()) {
                continue;
            }
            if (p.length() <= maxChars) {
                pieces.add(p);
            } else {
                pieces.addAll(splitLongParagraph(p));
            }
        }

        // 2. 조각을 maxChars 안에서 이어 붙인다
        StringBuilder current = new StringBuilder();
        for (String piece : pieces) {
            if (current.length() > 0 && current.length() + 1 + piece.length() > maxChars) {
                result.add(current.toString());
                current = new StringBuilder();
            }
            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(piece);
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }

        // 3. 앞 조각 끝부분을 다음 조각 앞에 붙인다
        if (overlapChars == 0 || result.size() < 2) {
            return result;
        }
        List<String> overlapped = new ArrayList<>();
        overlapped.add(result.get(0));
        for (int i = 1; i < result.size(); i++) {
            String prev = result.get(i - 1);
            String tail = prev.substring(Math.max(0, prev.length() - overlapChars));
            overlapped.add(tail + " " + result.get(i));
        }
        return overlapped;
    }

    private List<String> splitLongParagraph(String paragraph) {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        // 문장 끝 기호 뒤의 공백에서 자른다
        for (String sentence : paragraph.split("(?<=[.!?。])\\s+")) {
            if (sentence.length() > maxChars) {
                if (current.length() > 0) {
                    out.add(current.toString());
                    current = new StringBuilder();
                }
                for (int i = 0; i < sentence.length(); i += maxChars) {
                    out.add(sentence.substring(i, Math.min(sentence.length(), i + maxChars)));
                }
                continue;
            }
            if (current.length() > 0 && current.length() + 1 + sentence.length() > maxChars) {
                out.add(current.toString());
                current = new StringBuilder();
            }
            if (current.length() > 0) {
                current.append(' ');
            }
            current.append(sentence);
        }
        if (current.length() > 0) {
            out.add(current.toString());
        }
        return out;
    }
}
