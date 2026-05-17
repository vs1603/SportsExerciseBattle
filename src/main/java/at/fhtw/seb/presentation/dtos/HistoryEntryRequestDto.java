package at.fhtw.seb.presentation.dtos;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoryEntryRequestDto {
    private String name;
    private int count;
    private int duration;
}
