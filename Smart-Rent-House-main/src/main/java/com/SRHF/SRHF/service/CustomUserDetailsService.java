package com.SRHF.SRHF.service;

import com.SRHF.SRHF.entity.Token;
import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;

    public CustomUserDetailsService(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    TokenService tokenService,
                                    EmailService emailService) {

        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    /**
     * Initiate a password reset by generating a 6-digit OTP, saving it as a Token
     * and sending it by email using the configured mail sender.
     */
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));

        Token resetToken = new Token(
                otp,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5),
                user
        );
        tokenService.save(resetToken);
        emailService.sendPasswordReset(user.getEmail(), otp);
    }

    public void verifyPasswordResetOtp(String email, String otp) {
        Token token = tokenService.findByToken(otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));
        if (!token.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("OTP does not match the given email");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("OTP expired");
        }
        if (token.getConfirmedAt() != null) {
            // already used once
            throw new IllegalStateException("OTP already used");
        }
        token.setConfirmedAt(LocalDateTime.now());
        tokenService.save(token);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        Token token = tokenService.findByToken(otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));
        if (!token.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("OTP does not match the given email");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("OTP expired");
        }
        if (token.getConfirmedAt() == null) {
            throw new IllegalStateException("OTP not verified");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        //send password change confirmation email
        emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName(), user.getLastName());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByemail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public void registerUser(User user) {
        // check if user with the same email already exists
        userRepository.findByemail(user.getEmail())
                .ifPresent(
                        existingUser -> {
                            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists.");
                        }
                );

        // if user does not exist, encode the password and save the new user
        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        userRepository.save(user);


        //send an email with validation link
        //user click the link for confirmation

        Token confirmationToken = new Token(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5),
                user
        );
        tokenService.save(confirmationToken);
       emailService.send(user.getEmail(), confirmationToken.getToken());
    }

    public void confirmationToken(String token) {
        //check if token is exists
        Token confirmationToken = tokenService.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));
        //check user is already verified
        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Email already confirmed");
        }
        //check if token is expired
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired");
        }

        //if everything is ok then update the confirmation time
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        tokenService.save(confirmationToken);
        //and enable the user
        User user = confirmationToken.getUser();
        enableUser(user);
        //send verification success email
        emailService.sendVerificationSuccessEmail(user.getEmail(), user.getFirstName(), user.getLastName());
    }

    private void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }
}
