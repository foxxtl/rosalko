package ru.seventech.rosalko.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.seventech.basetemplate.error.CustomMessageException;
import ru.seventech.rosalko.dto.docstore.DocStoreResponseDTO;
import ru.seventech.rosalko.rabbit.RabbitProducer;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RosalkoLicenseeServiceTest {


    @Mock
    private RosalkoTransferService rosalkoTransferService;

    @Mock
    private RabbitProducer rabbitProducer;

    @InjectMocks
    private RosalkoLicenseeService service;

    @Test
    void refresh_shouldDownloadSaveAndSendFile() {

    }



}
