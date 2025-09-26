package com.hospital.smartpms.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ChatRequest {

    private String model;
    private List<ChatMessage> messages;
    private boolean stream = false;
    private double temperature = 0.6;

    @JsonProperty("max_tokens")
    private int maxTokens = 1024;

    // Constructors
    public ChatRequest() {
    }

    public ChatRequest(String model, List<ChatMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    // Getters and Setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
}