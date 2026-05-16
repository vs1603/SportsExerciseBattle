package at.fhtw.seb.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String passwordHash;
    private int elo;

    // Profile fields (editable)
    private String name;
    private String bio;
    private String image;
}