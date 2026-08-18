package com.inspire.lgcnsaminspire_5_be.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;

@Configuration
public class OpenAiConfig {
    @Bean
    public ObjectMapper objectMapper() {
        System.out.println(">>>> debug OpenAi config objectMapper");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper; // 👈 new ObjectMapper()에서 objectMapper로 수정!
    }

    @Bean
    public OkHttpClient okHttpClient() {
        System.out.println(">>>> debug OpenAi config okHttpClient");
        return new OkHttpClient();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}