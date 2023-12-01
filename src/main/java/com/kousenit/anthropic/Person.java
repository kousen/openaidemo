package com.kousenit.anthropic;

import java.time.LocalDate;

public record Person(
        String firstName,
        String lastName,
        LocalDate dob
) {
}
