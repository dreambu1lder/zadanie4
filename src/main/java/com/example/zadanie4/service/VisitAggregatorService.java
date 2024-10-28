package com.example.zadanie4.service;

import com.example.zadanie4.domain.dto.VisitDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VisitAggregatorService {

    private final Map<String, Integer> aggregatedVisits = new ConcurrentHashMap<>();

    public void aggregateVisit(VisitDTO visit) {
        String key = visit.site() + "|" + visit.date();
        aggregatedVisits.merge(key, 1, Integer::sum);
    }

    public Map<String, Integer> getAggregatedVisits() {
        return new ConcurrentHashMap<>(aggregatedVisits);
    }

    public void clearAggregatedVisits() {
        aggregatedVisits.clear();
    }
}
