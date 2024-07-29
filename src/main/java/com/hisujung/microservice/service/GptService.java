package com.hisujung.microservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hisujung.microservice.dto.ParticipatedNoticeDto;

public interface GptService {
    String getAssistantMsg(String activities, String careerField, ParticipatedNoticeDto participatedNoticeDto) throws JsonProcessingException;
}
