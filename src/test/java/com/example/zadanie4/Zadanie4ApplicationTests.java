package com.example.zadanie4;

import com.example.zadanie4.domain.dto.VisitDTO;
import com.example.zadanie4.service.VisitProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class Zadanie4ApplicationTests {

    @Autowired
    private KafkaTemplate<String, VisitDTO> kafkaTemplate;

    @Autowired
    private VisitProcessingService visitProcessingService;

    private final String testFilePath = "visits.txt";
    private final int THREAD_COUNT = 8;
    private final int RECORDS_COUNT = 200_000;

    @Test
    void testProcessingWith10_000Records_MultiThreaded() throws Exception {
        LocalDate date = LocalDate.of(2024, 10, 18);
        List<String> sites = Arrays.asList("example.com", "other-site.com", "third-site.com");

        long startTimeMultiThreaded = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Void>> tasks = createTasks(date, sites);

        executorService.invokeAll(tasks);
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        waitForProcessingToComplete();

        long endTimeMultiThreaded = System.currentTimeMillis();
        System.out.println("Многопоточная обработка заняла: " + (endTimeMultiThreaded - startTimeMultiThreaded) + " мс");

        verifyFileContent();
    }

    @Test
    void testProcessingWith10_000Records_SingleThreaded() throws Exception {
        LocalDate date = LocalDate.of(2024, 10, 18);
        List<String> sites = Arrays.asList("example.com", "other-site.com", "third-site.com");

        long startTimeSingleThreaded = System.currentTimeMillis();

        for (int i = 0; i < RECORDS_COUNT; i++) {
            String site = sites.get(ThreadLocalRandom.current().nextInt(sites.size()));
            String user = "user" + i;
            VisitDTO visit = new VisitDTO(site, randomDateWithin3Days(date), user);
            kafkaTemplate.send("site-visits-topic", visit);
        }

        waitForProcessingToComplete();

        long endTimeSingleThreaded = System.currentTimeMillis();
        System.out.println("Однопоточная обработка заняла: " + (endTimeSingleThreaded - startTimeSingleThreaded) + " мс");

        verifyFileContent();
    }

    private List<Callable<Void>> createTasks(LocalDate date, List<String> sites) {
        AtomicInteger counter = new AtomicInteger(0);

        return IntStream.range(0, THREAD_COUNT)
                .mapToObj(i -> (Callable<Void>) () -> {
                    for (int j = 0; j < RECORDS_COUNT / THREAD_COUNT; j++) {
                        String site = sites.get(ThreadLocalRandom.current().nextInt(sites.size()));
                        String user = "user" + counter.incrementAndGet();
                        VisitDTO visit = new VisitDTO(site, randomDateWithin3Days(date), user);
                        kafkaTemplate.send("site-visits-topic", visit);
                    }
                    return null;
                }).collect(Collectors.toList());
    }

    private void verifyFileContent() throws Exception {
        Path path = Path.of(testFilePath);
        assertTrue(Files.exists(path), "Файл с посещениями не был создан");

        String content = Files.readString(path);
        System.out.println(content);
    }

    private void waitForProcessingToComplete() throws InterruptedException {
        while (visitProcessingService.isProcessing()) {
            Thread.sleep(100);
        }
    }

    private LocalDate randomDateWithin3Days(LocalDate baseDate) {
        return baseDate.plusDays(ThreadLocalRandom.current().nextInt(-3, 4));
    }
}
