package com.example.zadanie4.service;

import com.example.zadanie4.domain.dto.VisitDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final BlockingQueue<VisitDTO> visitQueues;

    @KafkaListener(topics = "site-visits-topic", groupId = "siteVisitsConsumer")
    public void consume(VisitDTO visitDTO) throws InterruptedException {
        visitQueues.put(visitDTO);
    }
}
