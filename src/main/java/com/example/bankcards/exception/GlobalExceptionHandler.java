package com.example.bankcards.exception;
import com.example.bankcards.exception.BusinessException;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<LocalErrorResponse> handleBusinessException(BusinessException ex) {
        LocalErrorResponse error = new LocalErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Бизнес-ошибка",
                ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<LocalErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        LocalErrorResponse error = new LocalErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации",
                "Переданы некорректные данные",
                errors
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<LocalErrorResponse> handleBadCredentials() {
        LocalErrorResponse error = new LocalErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Ошибка аутентификации",
                "Неверное имя пользователя или пароль"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<LocalErrorResponse> handleAccessDenied() {
        LocalErrorResponse error = new LocalErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Доступ запрещен",
                "У вас нет прав для доступа к этому ресурсу"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<LocalErrorResponse> handleGenericException(Exception ex) {
        LocalErrorResponse error = new LocalErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Внутренняя ошибка сервера",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Модель ответа об ошибке
    public record LocalErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            Map<String, String> details
    ) {
        public LocalErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
            this(timestamp, status, error, message, null);
        }
    }
}
