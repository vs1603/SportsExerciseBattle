package at.fhtw.seb.presentation.dtos;

import lombok.Data;

@Data
public class TournamentResponseDto {
    public final int id;
    public final String startTime;
    public final String endTime;
    public final String state;
}
