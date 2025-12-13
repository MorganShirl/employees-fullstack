package com.morgan.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record EmployeeDto(
    Long id,

    @NotBlank(message = "First name is required")
    String firstName,

    @NotBlank(message = "Last name is required")
    String lastName,

    @NotBlank(message = "Role is required")
    String role
) {
}
