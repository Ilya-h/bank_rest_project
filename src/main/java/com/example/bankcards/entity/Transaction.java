package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions",
        indexes = {
            @Index(name = "idx_transaction_date", columnList = "transaction_date"),
            @Index(name = "idx_from_card_id", columnList = "from_card_id"),
            @Index(name = "idx_to_card_id", columnList = "to_card_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id")
    private Card fromCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id")
    private Card toCard;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @Column
    private String description;

    @PrePersist
    protected  void onCreate() {
        transactionDate = LocalDateTime.now();
        if (status == null){
            status = TransactionStatus.PENDING;
        }

    }

    // Метод для завершения транзакции
    public void complete() {
        this.status = TransactionStatus.COMPLETED;
    }

    // Метод для неудачной транзакции
    public void fail() {
        this.status = TransactionStatus.FAILED;
    }
}
enum TransactionStatus {
    PENDING, COMPLETED, FAILED
}
