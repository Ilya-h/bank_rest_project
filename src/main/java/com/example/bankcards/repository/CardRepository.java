package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.status = :status")
    Page<Card> findByUserIdAndStatus(@Param("userId") @NonNull Long userId,
                                     @Param("status") @NonNull CardStatus status,
                                     Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdWithLock(@Param("id")Long id);

    List<Card> findByUserId(Long userId);

    // Метод поиска карты по номеру карты
    Optional<Card> findByCardNumber(String cardNumber);

    // Метод проверки пользователя на существование и номер его карты
    boolean existsByUserIdAndCardNumber(Long userId, String cardNumber);
}
