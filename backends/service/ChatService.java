package com.hospital.smartpms.service;

import com.hospital.smartpms.config.BigModelConfig;
import com.hospital.smartpms.dto.chat.ChatMessage;
import com.hospital.smartpms.dto.chat.ChatRequest;
import com.hospital.smartpms.dto.chat.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private BigModelConfig bigModelConfig;

    private final RestTemplate restTemplate;

    public ChatService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get AI response for mental health support
     */
    public String getMentalHealthResponse(String userMessage) {
        try {
            List<ChatMessage> messages = new ArrayList<>();

            // System message to set the context for mental health support
            messages.add(ChatMessage.system(
                    "You are a compassionate and professional mental health AI assistant working in a hospital setting. "
                            +
                            "Your role is to:\n" +
                            "1. Provide supportive and empathetic responses\n" +
                            "2. Offer general mental health information and coping strategies\n" +
                            "3. Encourage professional help when appropriate\n" +
                            "4. Maintain a warm, understanding tone\n" +
                            "5. IMPORTANT: Always remind users that you are an AI assistant and cannot replace professional medical advice\n"
                            +
                            "6. For urgent situations, always recommend immediate professional help\n\n" +
                            "Keep responses helpful but brief (2-3 paragraphs max). Focus on emotional support and general guidance."));

            // User message
            messages.add(ChatMessage.user(userMessage));

            return getChatResponse(messages);

        } catch (Exception e) {
            logger.error("Error getting mental health response", e);
            return "I'm sorry, I'm experiencing technical difficulties right now. " +
                    "Please consider speaking with one of our professional counselors for immediate support.";
        }
    }

    /**
     * Get general chat response from BigModel API
     */
    public String getChatResponse(List<ChatMessage> messages) {
        try {
            // Create request
            ChatRequest request = new ChatRequest(bigModelConfig.getModel(), messages);
            request.setTemperature(0.7); // Slightly more creative for conversational responses
            request.setMaxTokens(512); // Reasonable limit for chat responses

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(bigModelConfig.getKey());

            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

            // Make API call
            String url = bigModelConfig.getBaseUrl() + "/chat/completions";
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(url, entity, ChatResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String content = response.getBody().getContent();
                if (content != null && !content.trim().isEmpty()) {
                    logger.info("Successfully got AI response, tokens used: {}",
                            response.getBody().getUsage() != null ? response.getBody().getUsage().getTotalTokens()
                                    : "unknown");
                    return content.trim();
                }
            }

            logger.warn("Empty or invalid response from BigModel API");
            return getDefaultErrorResponse();

        } catch (Exception e) {
            logger.error("Error calling BigModel API", e);
            return getDefaultErrorResponse();
        }
    }

    /**
     * Test the API connection
     */
    public boolean testConnection() {
        try {
            List<ChatMessage> testMessages = new ArrayList<>();
            testMessages.add(ChatMessage.user("Hello, can you respond with just 'API working'?"));

            String response = getChatResponse(testMessages);
            return response != null && !response.equals(getDefaultErrorResponse());

        } catch (Exception e) {
            logger.error("API connection test failed", e);
            return false;
        }
    }

    private String getDefaultErrorResponse() {
        return "I'm currently unavailable. Please speak with one of our professional counselors who are always here to help.";
    }
}