package ru.seventech.rosalko.dto.docstore;

import lombok.Data;

import java.util.UUID;

@Data
public class DocStoreResponseDTO {
    private UUID file_uuid;
    private String link;
    private String name;
}
