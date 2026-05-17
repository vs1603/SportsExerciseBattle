package at.fhtw.seb.presentation.dtos;

import lombok.Data;

@Data
public class TokenResponseDto {
    public final String token;
    public final String message;
}
