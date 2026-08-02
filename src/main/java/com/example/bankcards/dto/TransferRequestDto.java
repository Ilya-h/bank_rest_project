package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequestDto {
    @NotNull(message = "Требуется id карты")
    private Long fromCardId;

    @NotNull(message = "Требуется id карты")
    private Long toCardId;

    @NotNull(message = "Требуется сумма")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    private String description;
}
