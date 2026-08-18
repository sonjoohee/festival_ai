package com.inspire.lgcnsaminspire_5_be.festival.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(formatter);
    }

    // 축제 목록 조회
    public List<FestivalResponseDTO> getFestivalsByRegion(String region) {
        System.out.println(">>>> debug festival service connection");
        System.out.println(">>>> debug configured endPoint : " + endPoint);

        String requestUrl = endPoint
                + "searchFestival2"
                + "?serviceKey=" + key
                + "&MobileOS=WEB"
                + "&MobileApp=InspireApp"
                + "&_type=" + type
                + "&arrange=C"
                + "&numOfRows=200"
                + "&eventStartDate=" + getCurrentDateString();

        if (region != null && !region.isBlank() && !region.equals("all")) {
            requestUrl += "&lDongRegnCd=" + region;
        }

        System.out.println(">>>> debug festival service requestUrl : " + requestUrl);

        List<FestivalResponseDTO> festivalList = new ArrayList<>();

        try {
            String response = restTemplate.getForObject(requestUrl, String.class);
            System.out.println(">>>> debug tourAPI raw response: \n" + response);

            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode itemsNode = rootNode.findValue("item");

                if (itemsNode != null && itemsNode.isArray()) {
                    for (JsonNode item : itemsNode) {
                        String contentId = item.path("contentid").asText();
                        String title = item.path("title").asText();
                        String imageUrl = item.path("firstimage").asText();
                        String addr1 = item.path("addr1").asText();
                        String startDate = item.path("eventstartdate").asText();
                        String endDate = item.path("eventenddate").asText();
                        String areaCode = item.path("lDongRegnCd").asText();

                        if (areaCode == null || areaCode.isBlank()) {
                            areaCode = item.path("areacode").asText();
                        }

                        if (areaCode == null || areaCode.isBlank()) {
                            areaCode = region;
                        }

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

    // 축제 상세 조회 (DB 조회 후 없으면 findOrCreateFestival 호출하여 자동 저장 및 DTO 반환)
    @Transactional
    public FestivalResponseDTO getFestivalDetail(String contentId, Long userId) {
        log.info(">>>> debug festival service getFestivalDetail - contentId: {}", contentId);

        FestivalEntity entity;
        try {
            entity = findOrCreateFestival(contentId);
        } catch (Exception e) {
            log.error(">>>> [ERROR] 축제 정보 조회/생성 실패: {}", e.getMessage());
            return null;
        }

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
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }

    /**
     * DB에 축제 정보가 없으면 외부 TourAPI를 호출해 상세 정보를 가져와서 저장 후 반환합니다.
     */
    @Transactional
    public FestivalEntity findOrCreateFestival(String contentId) {
        log.info(">>>> FestivalService findOrCreateFestival - contentId: {}", contentId);

        return festivalRepository.findByContentId(contentId)
                .orElseGet(() -> {
                    // 에러를 유발하던 Y/N 옵션 파라미터를 모두 제거하고 깨끗하게 호출
                    String requestUrl = endPoint
                            + "detailCommon2"
                            + "?serviceKey=" + key
                            + "&MobileOS=WEB"
                            + "&MobileApp=InspireApp"
                            + "&_type=" + type
                            + "&contentId=" + contentId;

                    try {
                        String response = restTemplate.getForObject(requestUrl, String.class);
                        log.info(">>>> TourAPI response in findOrCreateFestival: {}", response);

                        if (response != null) {
                            JsonNode rootNode = objectMapper.readTree(response);
                            JsonNode itemNode = rootNode.findValue("item");

                            if (itemNode != null && itemNode.isContainerNode()) {
                                JsonNode item = itemNode.isArray() ? itemNode.get(0) : itemNode;

                                String title = item.path("title").asText("축제 정보");
                                String region = item.path("lDongRegnCd").asText("");
                                if (region.isBlank()) {
                                    region = item.path("areacode").asText("");
                                }
                                String imageUrl = item.path("firstimage").asText("");
                                String addr1 = item.path("addr1").asText("");
                                String addr2 = item.path("addr2").asText("");
                                String fullAddr = (addr2.isBlank()) ? addr1 : String.format("%s %s", addr1, addr2);
                                String overview = item.path("overview").asText("");
                                String startDate = item.path("eventstartdate").asText("");
                                String endDate = item.path("eventenddate").asText("");

                                String aiInfo = "실시간 AI 가이드 정보를 불러올 수 없습니다.";
                                try {
                                    if (!title.isBlank()) {
                                        aiInfo = openAiService.generateFestivalTips(title, overview);
                                    }
                                } catch (Exception e) {
                                    log.warn(">>>> AI 가공 중 에러 발생: {}", e.getMessage());
                                }

                                FestivalEntity newFestival = FestivalEntity.builder()
                                        .contentId(contentId)
                                        .title(title.isBlank() ? "축제 정보" : title)
                                        .region(region)
                                        .imageUrl(imageUrl)
                                        .addr(fullAddr)
                                        .content(overview)
                                        .aiInfo(aiInfo)
                                        .startDate(startDate)
                                        .endDate(endDate)
                                        .build();

                                return festivalRepository.save(newFestival);
                            }
                        }
                    } catch (Exception e) {
                        log.error(">>>> [ERROR] 외부 API를 통한 축제 정보 생성 실패: {}", e.getMessage());
                        e.printStackTrace();
                    }

                    throw new RuntimeException("축제 정보를 찾을 수 없거나 외부 API 연동에 실패했습니다. contentId: " + contentId);
                });
    }
}