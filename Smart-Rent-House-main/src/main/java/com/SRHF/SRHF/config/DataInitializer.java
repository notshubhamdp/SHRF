package com.SRHF.SRHF.config;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin user already exists
            if (userRepository.findByemail("admin@gmail.com").isEmpty()) {
                // Create default admin user
                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                
                userRepository.save(admin);
                System.out.println("Default admin user created successfully!");
                System.out.println("Email: admin@gmail.com");
                System.out.println("Password: admin");
            } else {
                System.out.println("Admin user already exists");
            }
        };
    }
}
