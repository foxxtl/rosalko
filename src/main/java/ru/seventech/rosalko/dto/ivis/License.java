package ru.seventech.rosalko.dto.ivis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
public class License {

    private String number;

    @JsonProperty("number_end")
    private String numberEnd;

    private boolean status;

    @JsonProperty("date_start")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateStart;

    @JsonProperty("is_indefinitely")
    private boolean isIndefinitely;

    public boolean isValidLicense() {
        return Objects.nonNull(dateStart) && StringUtils.isNotBlank(number);
    }
}
