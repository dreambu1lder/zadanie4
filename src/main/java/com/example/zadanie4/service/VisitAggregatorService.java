package com.example.zadanie4.service;

import com.example.zadanie4.domain.dto.VisitDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Service
public class VisitAggregatorService {

    private final Map<String, LongAdder> aggregatedVisits = new ConcurrentHashMap<>();

    public void aggregateVisit(VisitDTO visit) {
        String key = visit.site() + "|" + visit.date();
        aggregatedVisits.computeIfAbsent(key, k -> new LongAdder()).increment();
    }

    public Map<String, Integer> getAggregatedVisits() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        aggregatedVisits.forEach((key, value) -> result.put(key, value.intValue()));
        return result;
    }

    public int getAggregatedVisitCount() {
        return aggregatedVisits.values().stream().mapToInt(LongAdder::intValue).sum();
    }

    public void clearAggregatedVisits() {
        aggregatedVisits.clear();
    }
}
