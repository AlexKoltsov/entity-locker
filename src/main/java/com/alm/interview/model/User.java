package com.alm.interview.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private Long userScore;
}
