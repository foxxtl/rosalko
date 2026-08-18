package ru.seventech.rosalko.dto.mdm;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class PageResponse<T> implements Serializable {

    private List<T> content;
    private long number;
    private long size;
    private long totalElements;
    private long totalPages;

    public PageResponse(List<T> content, PageResponse<T> response) {
        this.content = content;
        this.size = content.size();
        this.number = response.getNumber();
        this.totalPages = response.getTotalPages();
        this.totalElements = response.getTotalElements();
    }

}
