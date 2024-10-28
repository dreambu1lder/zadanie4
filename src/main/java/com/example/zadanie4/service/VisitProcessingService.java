package com.example.zadanie4.service;

import com.example.zadanie4.domain.dto.VisitDTO;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class VisitProcessingService {

    private static final int MAX_RECORDS = 10_000;
    private static final long PROCESSING_INTERVAL = 30;

    @Getter
    private final BlockingQueue<VisitDTO> visitQueue = new LinkedBlockingQueue<>();
    private final ExecutorService processingExecutor;
    private final ScheduledExecutorService scheduler;
    private final VisitAggregatorService aggregatorService;
    private final FileWriterService fileWriterService;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    public VisitProcessingService(VisitAggregatorService aggregatorService,
                                  FileWriterService fileWriterService) {
        this.aggregatorService = aggregatorService;
        this.fileWriterService = fileWriterService;
        this.processingExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        this.scheduler = Executors.newScheduledThreadPool(1);
        startProcessing();
    }

    public void enqueueVisit(VisitDTO visit) {
        if (isRunning.get()) {
            visitQueue.offer(visit);
            System.out.println("Элемент добавлен в очередь: " + visit);
        }
    }

    public boolean isProcessing() {
        return !visitQueue.isEmpty();
    }

    public void stopProcessing() {
        isRunning.set(false);
        scheduler.shutdown();

        try {
            processingExecutor.shutdown();
            if (!processingExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                processingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            processingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void checkAndProcessQueue() {
        if (aggregatorService.getAggregatedVisitCount() >= MAX_RECORDS) {
            processQueue();
        }
    }

    private void forceProcess() {
        processQueue();
    }

    private void processQueue() {
        List<VisitDTO> batch = new ArrayList<>(MAX_RECORDS);
        visitQueue.drainTo(batch, MAX_RECORDS);

        if (!batch.isEmpty()) {
            processingExecutor.submit(() -> {
                batch.forEach(aggregatorService::aggregateVisit);
                Map<String, Integer> aggregatedVisits = aggregatorService.getAggregatedVisits();
                fileWriterService.writeToFileAsync(aggregatedVisits); // Асинхронная запись
                aggregatorService.clearAggregatedVisits();
            });
        }
    }

    private void startProcessing() {
        scheduler.scheduleAtFixedRate(this::checkAndProcessQueue, 0, 500, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(this::forceProcess, PROCESSING_INTERVAL, PROCESSING_INTERVAL, TimeUnit.MINUTES);
    }
}
