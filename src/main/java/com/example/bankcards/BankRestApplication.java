package com.example.bankcards;


import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class BankRestApplication {
    public static void main(String[] args){

        SpringApplication.run(BankRestApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)  // ← ВАЖНО: ADMIN, а не USER
                        .active(true)
                        .build();
                userRepository.save(admin);
                System.out.println("✅ Администратор создан: admin/admin123");
            }
        };
    }
}

