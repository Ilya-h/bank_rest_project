package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CardRequestDto {

    @NotBlank(message = "Требуется указать номер карты")
    @Size(min = 16, max = 16, message = "Номер карты должен состоять из 16 цифр")
    @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать только цифры")
    private String cardNumber;

    @NotBlank(message = "Требуется указать имя владельца")
    @Size(max = 100, message = "Имя владельца не должно превышать 100 символов")
    @Pattern(regexp = "^[A-Za-zА-Яа-я\\s-]+$", message = "Имя должно содержать только буквы, пробелы и дефисы")
    private String ownerName;

    @NotNull(message = "Требуется указать срок годности")
    private LocalDate expiryDate;

    // Геттеры и сеттеры

    public String getCardNumber() {
        return cardNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}