package com.example.zadanie4.service;

import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Service
public class FileWriterService {

    public void writeToFile(Map<String, Integer> aggregatedVisits, CountDownLatch latch) {
        try (FileWriter writer = new FileWriter("visits.txt", true)) {
            for (Map.Entry<String, Integer> entry : aggregatedVisits.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }
}
