package com.SRHF.SRHF.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void send(String to, String token) {
        // Implementation for sending email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("smartrenthouseotp@gmail.com");
            message.setSubject("Confirm Your Email");
            String messageBody = """
                    Thank you for registering with Smart Rent House.
                    We’re glad to welcome you to our community.
                    
                    To complete your registration, please confirm your email address by clicking the link below:
                    
                    Confirm your account:
                    http://localhost:8085/register/tokenConfirmed?token=%s
                    
                    For security reasons, this link will remain active for 5 minutes.
                    If it expires, you can request a new confirmation link anytime from the login page.
                    
                    If you did not create this account, please ignore this email — no action is required and your information will remain safe.
                    
                    Wishing you a smooth and peaceful experience ahead.
                    
                    Warm regards,
                    Smart Rent House Team
                    Helping you find the right place peacefully
                    
                    """.formatted(token);
            message.setText(messageBody);
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to {}", to, e);
        }
    }

    @Async
    public void sendPasswordReset(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("smartrenthouseotp@gmail.com");
            message.setSubject("Password Reset OTP - Smart Rent House");
            String messageBody = "Use the following One-Time Password (OTP) to reset your password:\n\n" +
                    otp +
                    "\n\nThis OTP is valid for 5 minutes. If you did not request a password reset, please ignore this email.";
            message.setText(messageBody);
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}", to, e);
        }
    }

    @Async
    public void sendVerificationSuccessEmail(String to, String firstName, String lastName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("smartrenthouseotp@gmail.com");
            message.setSubject("Account Successfully Verified - Smart Rent House");
            String fullName = firstName + " " + lastName;
            String messageBody = """
                    Dear %s,
                    
                    We're happy to let you know that your account has been successfully verified.
                    Thank you for confirming your email and joining the Smart Rent House community.
                    
                    You can now log in and start exploring rental homes that best suit your lifestyle, preferences, and comfort.
                    
                    We wish you a smooth, peaceful, and satisfying experience ahead.
                    If you ever need assistance, our support team is always here to help.
                    
                    Warm regards,
                    Team Smart Rent House
                    Find your place. Live your peace.
                    """.formatted(fullName);
            message.setText(messageBody);
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send verification success email to {}", to, e);
        }
    }

    @Async
    public void sendPasswordChangeEmail(String to, String firstName, String lastName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("smartrenthouseotp@gmail.com");
            message.setSubject("Password Successfully Updated - Smart Rent House");
            String fullName = firstName + " " + lastName;
            String messageBody = """
                    Dear %s,
                    
                    This is a quick confirmation to let you know that your password has been successfully updated.
                    
                    If you made this change, no further action is required.
                    If you did not request this password update, please contact our support team immediately to secure your account.
                    
                    Your safety and peace of mind are important to us.
                    Thank you for being a valued part of the Smart Rent House community.
                    
                    Warm regards,
                    Team Smart Rent House
                    Find your place. Live your peace.
                    """.formatted(fullName);
            message.setText(messageBody);
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send password change email to {}", to, e);
        }
    }
}
