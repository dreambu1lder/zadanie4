package com.example.zadanie4.domain.dto;

import java.time.LocalDate;

public record VisitDTO(
        String site,
        LocalDate date,
        String user
) {
}
