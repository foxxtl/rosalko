package ru.seventech.rosalko.dto.ivis;

import java.time.LocalDate;

public record IvisCacheKey(String number, LocalDate dateStart) {
}
