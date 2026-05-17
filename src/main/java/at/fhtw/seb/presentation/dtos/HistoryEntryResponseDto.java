package at.fhtw.seb.presentation.dtos;

import lombok.Data;

@Data
public class HistoryEntryResponseDto {
    public final int id;
    public final String name;
    public final int count;
    public final int duration;
    public final String recordedAt;
    public final Integer tournamentId;
}