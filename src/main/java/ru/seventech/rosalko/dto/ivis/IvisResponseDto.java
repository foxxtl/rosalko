package ru.seventech.rosalko.dto.ivis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class IvisResponseDto {

    private UUID id;

    @JsonProperty("fz99_id")
    private String fz99Id;

    private License license;

    private Applicant applicant;

    private List<String> addresses = new ArrayList<>();

}
