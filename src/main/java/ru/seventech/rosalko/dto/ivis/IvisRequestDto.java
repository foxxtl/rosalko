package ru.seventech.rosalko.dto.ivis;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class IvisRequestDto {

    @NotBlank
    @Schema(name = "Номер документа")
    private String number;

    @NotNull
    @Schema(name = "Дата выдачи")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateStart;

    @NotNull
    @Schema(name = "Тип документа. Фармацевтический - 1. Лекарственный - 2")
    private Integer sphere;
}
