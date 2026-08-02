package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        fromCard = Card.builder()
                .id(1L)
                .user(user)
                .status(CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusMonths(6))
                .balance(new BigDecimal("1000.00"))
                .build();

        toCard = Card.builder()
                .id(2L)
                .user(user)
                .status(CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusMonths(6))
                .balance(new BigDecimal("500.00"))
                .build();
    }

    @Test
    void transferMoney_Success() {
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("200.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        cardService.transferMoney(request);

        assertEquals(new BigDecimal("800.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());

        verify(cardRepository).save(fromCard);
        verify(cardRepository).save(toCard);
    }

    @Test
    void transferMoney_InsufficientBalance_ThrowsException() {
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("1500.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Недостаточно средств на балансе", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_SourceCardBlocked_ThrowsException() {
        fromCard.setStatus(CardStatus.BLOCKED);
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Карта отправителя не активна", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_ToCardBlocked_ThrowsException() {
        toCard.setStatus(CardStatus.BLOCKED);
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Карта получателя не активна", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_ToDifferentUser_ThrowsException() {
        User differentUser = User.builder().id(2L).username("otheruser").build();
        toCard.setUser(differentUser);

        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Обе карты должны принадлежать вам", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_CardExpired_ThrowsException() {
        fromCard.setExpiryDate(LocalDate.now().minusMonths(1));
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Срок действия карты отправителя истек", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_SourceCardNotFound_ThrowsException() {
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("100.00"));

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Карта отправителя не найдена", exception.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferMoney_NegativeAmount_ThrowsException() {
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(new BigDecimal("-100.00"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Сумма должна быть положительной", exception.getMessage());
        verify(cardRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    void transferMoney_ZeroAmount_ThrowsException() {
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.ZERO);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Сумма должна быть положительной", exception.getMessage());
    }

    @Test
    void transferMoney_SameCard_ThrowsException() {
        TransferRequestDto request = new TransferRequestDto();
        request.setFromCardId(1L);
        request.setToCardId(1L);
        request.setAmount(new BigDecimal("100.00"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> cardService.transferMoney(request)
        );
        assertEquals("Невозможно перевести на ту же самую карту", exception.getMessage());
    }
}