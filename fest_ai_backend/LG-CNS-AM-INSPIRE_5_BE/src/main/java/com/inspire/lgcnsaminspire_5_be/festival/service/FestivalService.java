package com.inspire.lgcnsaminspire_5_be.festival.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspire.lgcnsaminspire_5_be.bookmark.domain.entity.BookmarkEntity;
import com.inspire.lgcnsaminspire_5_be.bookmark.repository.BookmarkRepository;
import com.inspire.lgcnsaminspire_5_be.festival.domain.dto.FestivalResponseDTO;
import com.inspire.lgcnsaminspire_5_be.festival.domain.entity.FestivalEntity;
import com.inspire.lgcnsaminspire_5_be.festival.repository.FestivalRepository;
import com.inspire.lgcnsaminspire_5_be.openai.service.OpenAiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FestivalService {
    private final OpenAiService openAiService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FestivalRepository festivalRepository;
    private final BookmarkRepository bookmarkRepository;

    @Value("${openapi.serviceKey}")
    private String key;

    @Value("${openapi.callbackUrl}")
    private String endPoint;

    @Value("${openapi.dataType}")
    private String type;

    private String getCurrentDateString() {
        LocalDate now = LocalDate.now(); // 현재 날짜 가져오기
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(formatter); //
    }

    // 축제 목록 조회
    public List<FestivalResponseDTO> getFestivalsByRegion(String region) {
        System.out.println(">>>> debug festival service connection");
        System.out.println(">>>> debug configured endPoint : " + endPoint);

        // 1. UriComponentsBuilder로 TourAPI 요청 URL 빌드
        String requestUrl = endPoint
                + "searchFestival2"
                + "?serviceKey=" + key
                + "&MobileOS=WEB"
                + "&MobileApp=InspireApp"
                + "&_type=" + type // env에 등록된 대문자 'JSON'
                + "&arrange=C" // 최신순(수정일순) 정렬
                + "&numOfRows=200" // 한 번에 최대 200개의 결과를 가져오도록 설정
                + "&eventStartDate=" + getCurrentDateString(); // 오늘 날짜 반영

        // 특정 지역(region) 값이 들어왔을 때만 파라미터를 동적으로 추가
        if (region != null && !region.isBlank() && !region.equals("all")) {
            requestUrl += "&lDongRegnCd=" + region; // 매뉴얼 v4.4 공식 지역 코드 항목명
        }

        System.out.println(">>>> debug festival service requestUrl : " + requestUrl);

        List<FestivalResponseDTO> festivalList = new ArrayList<>();

        try {
            // RestTemplate으로 JSON 결과를 통 문자열(String)로 받아오기
            String response = restTemplate.getForObject(requestUrl, String.class);
            System.out.println(">>>> debug tourAPI raw response: \n" + response);

            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode itemsNode = rootNode.findValue("item");

                if (itemsNode != null && itemsNode.isArray()) {
                    for (JsonNode item : itemsNode) {
                        // 공공데이터 JSON 필드명에 맞춰서 가공
                        String contentId = item.path("contentid").asText();
                        String title = item.path("title").asText();
                        String imageUrl = item.path("firstimage").asText(); // TourAPI의 썸네일
                        String addr1 = item.path("addr1").asText();
                        String startDate = item.path("eventstartdate").asText();
                        String endDate = item.path("eventenddate").asText();
                        String areaCode = item.path("lDongRegnCd").asText();

                        // 법정동 시도 코드(lDongRegnCd)로 먼저 꺼내오기
                        if (areaCode == null || areaCode.isBlank()) {
                            areaCode = item.path("areacode").asText(); // 구형 데이터 대응용 백업
                        }

                        // 만약 빈 값이라면, 넘겨주었던 검색 조건(region)을 역으로 채워줌
                        if (areaCode == null || areaCode.isBlank()) {
                            areaCode = region;
                        }

                        // DTO 규격에 맞춰 조립
                        festivalList.add(FestivalResponseDTO.builder()
                                .contentId(contentId)
                                .title(title)
                                .region(areaCode)
                                .imageUrl(imageUrl)
                                .addr(addr1)
                                .startDate(startDate)
                                .endDate(endDate)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return festivalList;
    }

    // 축제 상세 조회
    public FestivalResponseDTO getFestivalDetail(String contentId, Long userId) {
        System.out.println(">>>> debug festival service getFestivalDetail");

        // DB에 이미 저장된 축제(찜된 적 있는 축제)라면 TourAPI/OpenAI 호출 없이 DB 값을 그대로 재사용
        Optional<FestivalEntity> festivalEntity = festivalRepository.findByContentId(contentId);
        if (festivalEntity.isPresent()) {
            System.out.println(">>>> debug festival service DB cache hit - TourAPI/AI 호출 생략");
            FestivalEntity entity = festivalEntity.get();

            boolean isBookmarked = false;
            Long bookmarkId = null;
            if (userId != null) {
                Optional<BookmarkEntity> bookmark = bookmarkRepository
                        .findByUser_UserIdAndFestival_FestivalId(userId, entity.getFestivalId());
                if (bookmark.isPresent()) {
                    isBookmarked = true;
                    bookmarkId = bookmark.get().getBookmarkId();
                }
            }

            return FestivalResponseDTO.builder()
                    .contentId(entity.getContentId())
                    .title(entity.getTitle())
                    .region(entity.getRegion())
                    .imageUrl(entity.getImageUrl())
                    .addr(entity.getAddr())
                    .content(entity.getContent())
                    .aiInfo(entity.getAiInfo())
                    .isBookmarked(isBookmarked)
                    .bookmarkId(bookmarkId)
                    .build();
        }

        System.out.println(">>>> debug configured endPoint : " + endPoint);

        String requestUrl = endPoint
                + "detailCommon2"
                + "?serviceKey=" + key
                + "&MobileOS=WEB"
                + "&MobileApp=InspireApp"
                + "&_type=" + type
                + "&contentId=" + contentId;

        System.out.println(">>>> debug festival detail requestUrl : " + requestUrl);

        try {
            // TourAPI 서버 통신
            String response = restTemplate.getForObject(requestUrl, String.class);
            System.out.println(">>>> debug tourAPI raw response: \n" + response);

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemNode = rootNode.findValue("item");

            if (itemNode != null && itemNode.isContainerNode()) {
                // 배열로 들어오므로 첫 번째 아이템 추출
                JsonNode item = itemNode.isArray() ? itemNode.get(0) : itemNode;

                String title = item.path("title").asText();
                String region = item.path("lDongRegnCd").asText();
                String imageUrl = item.path("firstimage").asText();
                String addr1 = item.path("addr1").asText();
                String addr2 = item.path("addr2").asText();

                String fullAddr = (addr2.isBlank()) ? addr1 : String.format("%s %s", addr1, addr2);

                // 축제 소개/개요 본문
                String overview = item.path("overview").asText();

                // AI 추천 정보(aiInfo) 가공 구역
                String aiInfo = "실시간 AI 가이드 정보를 불러올 수 없습니다. 잠시 후 다시 시도해 주세요.";
                try {
                    // OpenAI Service 또는 ChatGPT Component 연동 시 아래 주석 해제하여 조립
                    aiInfo = openAiService.generateFestivalTips(title, overview);
                } catch (Exception e) {
                    System.out.println(">>>> AI 가공 중 에러 발생 (기본값 대체): " + e.getMessage());
                }

                // 4. 최종 명세서 DTO 포맷에 맞게 빌더 조립 후 반환
                // DB에 FestivalEntity가 없어서 이 분기에 온 것이므로 북마크될 수 없음
                return FestivalResponseDTO.builder()
                        .contentId(contentId)
                        .title(title)
                        .region(region)
                        .imageUrl(imageUrl)
                        .addr(fullAddr)
                        .content(overview) // 개요 데이터 매핑
                        .aiInfo(aiInfo) // AI 가공 데이터 매핑
                        .isBookmarked(false)
                        .bookmarkId(null)
                        .build();
            }

        } catch (Exception e) {
            System.out.println(">>>> [ERROR] Festival 상세 조회 실패: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
