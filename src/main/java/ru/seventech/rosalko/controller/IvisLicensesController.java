package ru.seventech.rosalko.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.seventech.rosalko.dto.ivis.IvisRequestDto;
import ru.seventech.rosalko.dto.ivis.IvisResponseDto;
import ru.seventech.rosalko.service.ivis.IvisLicensesService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/ivis-licences")
public class IvisLicensesController {

    private final IvisLicensesService ivisLicensesService;

    @GetMapping("/refresh")
    @Operation(summary = "Добавляет лицензии Россельхознадзора в кеш приложения")
    public ResponseEntity<Object> refresh(@RequestParam Integer sphere) {
        ivisLicensesService.refresh(sphere);
        return ResponseEntity.ok("Задача на обновление лицензий Россельхознадзора успешно запущена");
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск лицензии Россельхознадзора")
    public IvisResponseDto search(@RequestBody @Valid IvisRequestDto requestDto) {
        return ivisLicensesService.search(requestDto);
    }
}
