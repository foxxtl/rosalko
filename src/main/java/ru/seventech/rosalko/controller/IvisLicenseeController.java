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
import ru.seventech.rosalko.service.ivis.IvisLicenseeService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/ivis-licences")
public class IvisLicenseeController {

    private final IvisLicenseeService ivisLicenseeService;

    @GetMapping("/refresh")
    @Operation(summary = "Добавляет лицензии Россельхознадзора в кеш приложения")
    public ResponseEntity<Object> refresh(@RequestParam Integer sphere) {
        ivisLicenseeService.refresh(sphere);
        return ResponseEntity.ok("Задача на обновление лицензий Россельхознадзора успешно запущена");
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск лицензии Россельхознадзора")
    public IvisResponseDto search(@RequestBody @Valid IvisRequestDto requestDto) {
        return ivisLicenseeService.search(requestDto);
    }
}
