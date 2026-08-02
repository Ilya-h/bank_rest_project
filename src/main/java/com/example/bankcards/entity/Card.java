package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards",
        indexes = {
                @Index(name = "idx_card_user", columnList = "user_id"),
                @Index(name = "idx_card_status", columnList = "status")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber; // Хранится в зашифрованном виде

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = LocalDateTime.now();
        if (status == null) status = CardStatus.ACTIVE;
        if (balance == null) balance = BigDecimal.ZERO;
    }

    // конструктор с бизнес-логикой
    public Card(String cardNumber, String ownerName, LocalDate expiryDate, User user) {
        this.cardNumber = cardNumber;
        this.ownerName = ownerName;
        this.expiryDate = expiryDate;
        this.user = user;
        this.status = CardStatus.ACTIVE;
        this.balance = BigDecimal.ZERO;
    }

}
