package com.example.zadanie4.config;

import com.example.zadanie4.domain.dto.VisitDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Configuration
public class QueueConfig {

    @Bean
    public BlockingQueue<VisitDTO> visitQueue() {
        return new LinkedBlockingDeque<>(10_000);
    }
}
