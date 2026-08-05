package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на аутентификацию")
public class AuthRequest {

    @NotBlank(message = "Требуется имя пользователя")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;

    @NotBlank(message = "Требуется пароль")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    @Schema(description = "Пароль", example = "securePass123")
    private String password;

    @Schema(description = "Роль пользователя", example = "USER", allowableValues = {"USER", "ADMIN"})
    private Role role = Role.USER;
}