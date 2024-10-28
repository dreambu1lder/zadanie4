package com.example.zadanie4;

import com.example.zadanie4.domain.dto.VisitDTO;
import com.example.zadanie4.service.VisitProcessingService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class Zadanie4ApplicationTests {

    @Autowired
    private KafkaTemplate<String, VisitDTO> kafkaTemplate;

    @Autowired
    private VisitProcessingService visitProcessingService;

    private final String testFilePath = "visits.txt";

    @Test
    void testProcessingWith10_000Records() throws Exception {
        LocalDate date = LocalDate.of(2024, 10, 18);
        List<String> sites = Arrays.asList("example.com", "other-site.com", "third-site.com");

        for (int i = 0; i < 10_000; i++) {
            String site = sites.get(ThreadLocalRandom.current().nextInt(sites.size()));
            String user = "user" + i;

            VisitDTO visit = new VisitDTO(site, randomDateWithin3Days(date), user);
            kafkaTemplate.send("site-visits-topic", visit);
        }

        boolean completed = visitProcessingService.getLatch().await(30, TimeUnit.SECONDS);
        assertTrue(completed, "Обработка не завершилась в установленный срок");

        Path path = Path.of(testFilePath);
        assertTrue(Files.exists(path), "Файл с посещениями не был создан");

        String content = new String(Files.readAllBytes(path));
        System.out.println(content);

        visitProcessingService.stopProcessing();
    }

    LocalDate randomDateWithin3Days(LocalDate baseDate) {
        int randomDays = ThreadLocalRandom.current().nextInt(3);
        return baseDate.plusDays(randomDays);
    }
}
