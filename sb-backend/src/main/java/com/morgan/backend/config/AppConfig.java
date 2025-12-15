package com.morgan.backend.config;

import com.morgan.backend.entities.Employee;
import com.morgan.backend.entities.UserAccount;
import com.morgan.backend.repositories.EmployeeRepository;
import com.morgan.backend.repositories.UserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Configuration
public class AppConfig {

    // Runs after the Spring context is fully loaded
    @Bean
    public CommandLineRunner initDatabase(EmployeeRepository employeeRepository,
                                          UserAccountRepository userAccountRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            if (employeeRepository.count() == 0) {
                employeeRepository.save(new Employee("Morgan", "SHIRLEY", "Senior fullstack developer"));
                employeeRepository.save(new Employee("Alex", "Martin", "Backend developer"));
                employeeRepository.save(new Employee("Emily", "Carter", "Frontend developer"));
                employeeRepository.save(new Employee("Sarah", "Thompson", "Business analyst"));
                employeeRepository.save(new Employee("Michael", "Reed", "Product owner"));
                employeeRepository.save(new Employee("Laura", "Bennett", "Project manager"));
                employeeRepository.findAll()
                    .forEach(employee -> log.info("Preloaded employee {}", employee));
            }

            if (userAccountRepository.count() == 0) {
                String rawPassword1 = "pwd1"; // for demo only
                String hash1 = passwordEncoder.encode(rawPassword1);
                UserAccount user1 = new UserAccount("Morgan", "morgan@email.com", hash1);

                String rawPassword2 = "pwd2"; // for demo only
                String hash2 = passwordEncoder.encode(rawPassword2);
                UserAccount user2 = new UserAccount("Mark", "mark@email.com", hash2);

                userAccountRepository.saveAll(List.of(user1, user2));
            }
        };
    }

    @Bean
    public Supplier<String> uuidSupplier() {
        return () -> UUID.randomUUID().toString();
    }
}
