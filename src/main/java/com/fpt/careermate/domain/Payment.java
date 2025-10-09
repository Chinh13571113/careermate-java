package com.fpt.careermate.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String txnRef; // vnp_TxnRef
    String transactionNo; // vnp_TransactionNo
    String responseCode; // vnp_ResponseCode
    Long amount;
    String bankCode;
    LocalDate payDate;
    String status;

    @Column(columnDefinition = "TEXT")
    String rawResponse;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Order order;
}
