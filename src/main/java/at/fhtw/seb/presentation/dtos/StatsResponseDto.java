package at.fhtw.seb.presentation.dtos;

import lombok.Data;

@Data
public class StatsResponseDto {
    public final String username;
    public final int elo;
    public final int totalPushUps;
    public final int totalEntries;
}
