package at.fhtw.seb.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tournament {
    private Integer id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TournamentState state;
}