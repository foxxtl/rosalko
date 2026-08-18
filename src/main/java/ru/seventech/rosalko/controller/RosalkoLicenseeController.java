package ru.seventech.rosalko.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.seventech.rosalko.service.RosalkoLicenseeService;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/licensee")
public class RosalkoLicenseeController {

    private final RosalkoLicenseeService rosalkoLicenseeService;

    @GetMapping("/refresh")
    @Operation(summary = "Скачивает zip архив со страницы Росалкогольрегулирования и отправляет в transformer-service для обновления данных")
    public ResponseEntity<Object> refresh() {
        rosalkoLicenseeService.refresh();
        return ResponseEntity.ok("Задача на обновление данных успешно запущена");
    }

}
