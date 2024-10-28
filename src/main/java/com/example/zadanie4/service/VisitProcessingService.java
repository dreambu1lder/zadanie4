package com.example.zadanie4.service;

import com.example.zadanie4.domain.dto.VisitDTO;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class VisitProcessingService {

    private static final int MAX_QUEUE_SIZE = 10_000;
    private static final long TIMEOUT = 30 * 60 * 1000;

    private final BlockingQueue<VisitDTO> visitQueue;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final VisitAggregatorService aggregatorService;
    private final FileWriterService fileWriterService;
    @Getter
    private final CountDownLatch latch;

    private volatile boolean isRunning = true;

    public VisitProcessingService(BlockingQueue<VisitDTO> visitQueue,
                                  ExecutorService executorService,
                                  ScheduledExecutorService scheduler,
                                  VisitAggregatorService aggregatorService,
                                  FileWriterService fileWriterService) {
        this.visitQueue = visitQueue;
        this.executorService = executorService;
        this.scheduler = scheduler;
        this.aggregatorService = aggregatorService;
        this.fileWriterService = fileWriterService;
        this.latch = new CountDownLatch(1);
    }

    @PostConstruct
    public void startProcessing() {
        scheduler.scheduleAtFixedRate(this::processQueue, TIMEOUT, TIMEOUT, TimeUnit.MILLISECONDS);

        executorService.submit(() -> {
            try {
                while (isRunning) {
                    if (visitQueue.size() >= MAX_QUEUE_SIZE) {
                        processQueue();
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void stopProcessing() {
        isRunning = false;
        executorService.shutdown();
        scheduler.shutdown();
    }

    private void processQueue() {
        List<VisitDTO> batch = new ArrayList<>();
        visitQueue.drainTo(batch, MAX_QUEUE_SIZE);

        if (!batch.isEmpty()) {
            batch.forEach(aggregatorService::aggregateVisit);

            fileWriterService.writeToFile(aggregatorService.getAggregatedVisits(), latch);
            aggregatorService.clearAggregatedVisits();
        }
    }
}
