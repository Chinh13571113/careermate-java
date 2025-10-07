package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PackageCreationRequest {
    @NotBlank(message = "Name cannot be null")
    String name;

    @Min(value = 50000, message = "Minimum price must be 50,000 VND")
    @Max(value = 300000, message = ("Maximum price cannot exceed 300,000 VND"))
    long price;

    @Min(value = 7, message = "Minimum duration days must be 7")
    @Max(value = 365, message = ("Maximum duration days cannot exceed 365"))
    int durationDays;
}
