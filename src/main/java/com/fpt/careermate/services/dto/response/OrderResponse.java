package com.fpt.careermate.services.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
     int id;
     Long amount;
     String status;

    LocalDate createAt;
    LocalDate startDate;
    LocalDate endDate;

    int candidateId;
    int packageId;
}
