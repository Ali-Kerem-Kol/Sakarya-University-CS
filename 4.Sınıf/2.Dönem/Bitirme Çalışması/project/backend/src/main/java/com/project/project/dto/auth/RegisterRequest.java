package com.project.project.dto.auth;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.project.project.dto.auth.deserializer.LenientBigDecimalDeserializer;
import com.project.project.dto.auth.deserializer.LenientIntegerDeserializer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Carries registration data for creating a new user account.
 */
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotNull(message = "classYear is required")
        @Min(value = 1, message = "classYear must be between 1 and 8")
        @Max(value = 8, message = "classYear must be between 1 and 8")
        @JsonDeserialize(using = LenientIntegerDeserializer.class)
        Integer classYear,
        @NotBlank @Size(max = 120) String department,
        @NotBlank @Size(max = 40) String englishLevel,
        @NotNull(message = "gpa is required")
        @DecimalMin(value = "0.00", message = "gpa must be between 0.00 and 4.00")
        @DecimalMax(value = "4.00", message = "gpa must be between 0.00 and 4.00")
        @JsonDeserialize(using = LenientBigDecimalDeserializer.class)
        java.math.BigDecimal gpa
) {
}
