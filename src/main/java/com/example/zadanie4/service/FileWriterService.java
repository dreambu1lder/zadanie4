package com.example.zadanie4.service;

import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileWriterService {

    private final ExecutorService fileWriterExecutor = Executors.newSingleThreadExecutor();

    public void writeToFileAsync(Map<String, Integer> aggregatedVisits) {
        fileWriterExecutor.submit(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("visits.txt", true))) {
                for (Map.Entry<String, Integer> entry : aggregatedVisits.entrySet()) {
                    writer.write(entry.getKey() + "," + entry.getValue() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

