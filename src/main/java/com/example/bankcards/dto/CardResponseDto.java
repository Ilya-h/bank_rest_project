package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CardResponseDto {
    private Long id;
    private String maskedNumber; // **** **** **** 1234
    private String cardNumber;
    private String ownerName;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private Long userId;
}
