package at.fhtw.seb.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryEntry {
    private Integer id;
    private Integer userId;
    private int count;
    private int duration;
    private Integer tournamentId;
}