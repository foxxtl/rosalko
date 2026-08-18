package ru.seventech.rosalko.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import ru.seventech.rosalko.service.RosalkoLicenseeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ActiveProfiles("dev")
@Disabled("dev tests only")
@ExtendWith(MockitoExtension.class)
class RosalkoLicenseeControllerTest {

    @Mock
    private RosalkoLicenseeService rosalkoLicenseeService;

    @InjectMocks
    private RosalkoLicenseeController controller;

    @Test
    void refresh_shouldCallServiceAndReturnOk() {
        ResponseEntity<Object> response = controller.refresh();

        verify(rosalkoLicenseeService).refresh();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "Задача на обновление данных успешно запущена",
                response.getBody()
        );
    }
}

