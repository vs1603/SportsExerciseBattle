package at.fhtw.seb.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStats {
    private String username;
    private int elo;
    private int totalPushUps;
    private int totalEntries;
}