package com.example.zadanie4.service;

import com.example.zadanie4.domain.dto.VisitDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final VisitProcessingService visitProcessingService;

    public KafkaConsumerService(VisitProcessingService visitProcessingService) {
        this.visitProcessingService = visitProcessingService;
    }

    @KafkaListener(topics = "site-visits-topic", groupId = "siteVisitsConsumer", concurrency = "3")
    public void listen(VisitDTO visit) {
        visitProcessingService.enqueueVisit(visit);
    }
}