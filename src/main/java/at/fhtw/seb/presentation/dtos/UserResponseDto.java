package at.fhtw.seb.presentation.dtos;

import lombok.Data;

@Data
public class UserResponseDto {
    public final String username;
    public final String name;
    public final String bio;
    public final String image;
    public final int elo;
}
