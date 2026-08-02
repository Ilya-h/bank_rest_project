package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequestDto;
import com.example.bankcards.dto.CardResponseDto;
import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Управление картами", description = "Эндпоинты для управления банковскими картами")
public class CardController {

    private final CardService cardService;

    @PostMapping
    @Operation(summary = "Создать новую карту (Только для АДМИНИСТРАТОРА)")
    public ResponseEntity<CardResponseDto> createCard(
            @Valid @RequestBody CardRequestDto request,
            @RequestParam Long userId) {
        return ResponseEntity.ok(cardService.createCard(request, userId));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "Получить все карты (Только для АДМИНИСТРАТОРА)")
    public ResponseEntity<Page<CardResponseDto>> getAllCards(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }

    @GetMapping("/my")
    @Operation(summary = "Получить карты текущего пользователя")
    public ResponseEntity<Page<CardResponseDto>> getMyCards(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(cardService.getMyCards(pageable));
    }

    @PatchMapping("/{cardId}/block")
    @Operation(summary = "Заблокировать карту")
    public ResponseEntity<Void> blockCard(@PathVariable Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/{cardId}/activate")
    @Operation(summary = "Активировать карту (Только для АДМИНИСТРАТОРА)")
    public ResponseEntity<Void> activateCard(@PathVariable Long cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/{cardId}")
    @Operation(summary = "Удалить карту (Только для АДМИНИСТРАТОРА)")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод денег между своими картами")
    public ResponseEntity<Void> transfer(
            @Valid @RequestBody TransferRequestDto request) {
        cardService.transferMoney(request);
        return ResponseEntity.ok().build();
    }
}
