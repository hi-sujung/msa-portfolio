package com.hisujung.microservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

public interface GptService {
    ResponseEntity<?> getAssistantMsg(String activities, String careerField) throws JsonProcessingException;
}
