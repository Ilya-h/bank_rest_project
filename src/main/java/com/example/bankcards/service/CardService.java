package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BusinessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    private static final String ENCRYPTION_KEY = "0123456789abcdef0123456789abcdef";

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CardResponseDto createCard(CardRequestDto request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Пользователь не найден"));

        String encryptedNumber = encrypt(request.getCardNumber());
        if (cardRepository.findByCardNumber(encryptedNumber).isPresent()) {
            throw new BusinessException("Номер карты уже существует");
        }

        Card card = Card.builder()
                .cardNumber(encryptedNumber)
                .ownerName(request.getOwnerName())
                .expiryDate(request.getExpiryDate())
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .user(user)
                .build();

        Card savedCard = cardRepository.save(card);
        log.info("Карточка создана: {} для пользователя {}", maskCardNumber(request.getCardNumber()), userId);

        return mapToDto(savedCard);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponseDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @PreAuthorize("hasRole('USER')")
    public Page<CardResponseDto> getMyCards(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        return cardRepository.findByUserId(userId, pageable)
                .map(this::mapToDto);
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void blockCard(Long cardId) {
        Long userId = securityUtils.getCurrentUserId();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Карта не найдена"));

        if (!card.getUser().getId().equals(userId)) {
            throw new BusinessException("Вы можете заблокировать только свои собственные карты");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new BusinessException("Невозможно заблокировать карту с истекшим сроком действия");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new BusinessException("Карта уже заблокирована");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        log.info("Карта {} заблокирована пользователем {}", cardId, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Карта не найдена"));

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new BusinessException("Не удается активировать карту с истекшим сроком действия");
        }

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new BusinessException("Карта уже активна");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        log.info("Карта {} активирована", cardId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Карта не найдена"));

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Невозможно удалить карту с положительным балансом");
        }

        cardRepository.delete(card);
        log.info("Карточка {} удалена", cardId);
    }

    // Метод для получения карты по ID (для тестов)
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException("Карта не найдена"));
    }

    private String encrypt(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Сбой шифрования", e);
        }
    }

    private String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8),
                    "AES"
            );
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось расшифровать", e);
        }
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "**** **** **** ****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    private CardResponseDto mapToDto(Card card) {
        String decryptedNumber = card.getCardNumber() != null ? decrypt(card.getCardNumber()) : "";
        return CardResponseDto.builder()
                .id(card.getId())
                .maskedNumber(maskCardNumber(decryptedNumber))
                .cardNumber(decryptedNumber)
                .ownerName(card.getOwnerName())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .userId(card.getUser().getId())
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void transferMoney(TransferRequestDto request) {
        // Проверка суммы
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Сумма должна быть положительной");
        }

        // Проверка на перевод на ту же карту
        if (request.getFromCardId().equals(request.getToCardId())) {
            throw new BusinessException("Невозможно перевести на ту же самую карту");
        }

        Long userId = securityUtils.getCurrentUserId();

        Card fromCard = cardRepository.findByIdWithLock(request.getFromCardId())
                .orElseThrow(() -> new BusinessException("Карта отправителя не найдена"));

        Card toCard = cardRepository.findByIdWithLock(request.getToCardId())
                .orElseThrow(() -> new BusinessException("Карта получателя не найдена"));

        // Проверка, что обе карты принадлежат текущему пользователю
        if (!fromCard.getUser().getId().equals(userId) || !toCard.getUser().getId().equals(userId)) {
            throw new BusinessException("Обе карты должны принадлежать вам");
        }

        // Проверка, что карты активны
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException("Карта отправителя не активна");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException("Карта получателя не активна");
        }

        // Проверка на истечение срока
        if (fromCard.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Срок действия карты отправителя истек");
        }
        if (toCard.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Срок действия карты получателя истек");
        }

        // Проверка достаточности средств
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("Недостаточно средств на балансе");
        }

        // Выполняем перевод
        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Перевод {} с карты {} на карту {} пользователем {}",
                request.getAmount(), fromCard.getId(), toCard.getId(), userId);
    }
}