package com.inspire.lgcnsaminspire_5_be.openai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAiService {
    private final ChatClient chatClient;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    /**
     * 축제 명칭과 개요를 기반으로 ChatGPT를 활용하여 맞춤형 핵심 방문 꿀팁을 생성합니다.
     * 프론트엔드에서 렌더링하기 편하도록 친절한 마크다운(Markdown) 문법 형태로 응답을 요구합니다.
     */
    public String generateFestivalTips(String title, String overview) {
        System.out.println(">>>> debug openai service generateFestivalTips");
        System.out.println(">>>> debug openai service model : " + model);
        System.out.println(">>>> debug openai service target festival : " + title);

        // Spring AI ChatClient 플루언트 API 호출로 비즈니스 프롬프트 구성
        String aiResponse = chatClient.prompt()
                .system("""
                        너는 한국관광공사의 축제 공공데이터를 정밀 분석하는 전문 AI 가이드야.
                        사용자가 축제 이름과 상세 개요글을 제공하면, 방문객들에게 실질적으로 도움되는 핵심 팁을 추출해야 해.
                        반드시 아래 제시된 [출력 양식] 규격에 맞춰 친절한 한국어 마크다운(Markdown) 문법으로만 응답해줘.
                        본문에 명시되지 않은 정보라도 해당 축제의 지리적 특성이나 성격상 유의해야 할 상식적인 안내 사항을 유연하게 포함시켜줘.
                        """)
                .user("""
                        [대상 축제 정보]
                        - 축제 이름: %s
                        - 축제 상세 개요: %s

                        [반드시 준수해야 할 생성 규칙]
                        1. "아래는 분석 결과입니다" 같은 서론이나 "즐거운 축제 되세요" 같은 결론 문구는 절대 추가하지 마세요. 오직 [출력 양식]의 내용만 출력하기.
                        2. 상세 개요에 주차나 휠체어 정보가 전혀 언급되어 있지 않더라도, 해당 축제의 이름(예: 바다축제 -> 모래사장 지형, 산속 축제 -> 경사로 등)과 개최 장소의 특성을 논리적으로 고려하여 '유모차/휠체어 이동성 및 접근성 측면'에서 유용한 상식적인 가이드를 제공하기.
                        3. 정보를 지어내지 말고, "주차 공간이 협소할 것으로 예상되므로 대중교통 이용을 권장합니다" 또는 "야외 지형 특성상 휠체어 이동에 제약이 있을 수 있습니다"와 같이 정중하고 객관적인 가이드조로 작성하기.

                        [출력 양식]
                        ## 주차 및 교통 팁
                        - (주차장 혼잡도, 인근 지하철/버스 정류장 연계 안내 등 2~3줄 요약)

                        ## 교통약자 편의 정보
                        - (유모차 및 휠체어 대여: 대여 가능 여부나 꿀팁 기술)
                        - (접근성: 휠체어나 유모차가 이동하기 수월한 지형인지 데크 산책로 유무 기술)
                        """
                        .formatted(title, overview))
                .call()
                .content(); // DTO 맵핑 대신 마크다운 텍스트 원본을 그대로 획득

        System.out.println(">>>> debug gpt response string : \n" + aiResponse);

        return aiResponse;
    }
}
