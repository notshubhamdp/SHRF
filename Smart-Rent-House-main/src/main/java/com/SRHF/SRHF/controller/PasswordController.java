package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.service.CustomUserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class PasswordController {

    private final CustomUserDetailsService userDetailsService;

    public PasswordController(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam("email") String email,
                                       RedirectAttributes redirectAttributes) {
        try {
            userDetailsService.initiatePasswordReset(email);
            redirectAttributes.addFlashAttribute("message", "OTP sent to your email address.");
            String encoded = URLEncoder.encode(email, StandardCharsets.UTF_8);
            return "redirect:/forgot-password/verify?email=" + encoded;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/forgot-password/verify")
    public String verifyOtpPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp";
    }

    @PostMapping("/forgot-password/verify")
    public String handleVerifyOtp(@RequestParam("email") String email,
                                  @RequestParam("otp") String otp,
                                  RedirectAttributes redirectAttributes) {
        try {
            userDetailsService.verifyPasswordResetOtp(email, otp);
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String encodedOtp = URLEncoder.encode(otp, StandardCharsets.UTF_8);
            return "redirect:/forgot-password/reset?email=" + encodedEmail + "&token=" + encodedOtp;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/forgot-password/verify?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/forgot-password/reset")
    public String resetPasswordPage(@RequestParam("email") String email,
                                    @RequestParam("token") String token,
                                    Model model) {
        model.addAttribute("email", email);
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/forgot-password/reset")
    public String handleResetPassword(@RequestParam("email") String email,
                                      @RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        try {
            if (password == null || !password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
                return "redirect:/forgot-password/reset?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
            }

            userDetailsService.resetPassword(email, token, password);
            redirectAttributes.addFlashAttribute("message", "Password reset successful. You can now login.");
            return "redirect:/login";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/forgot-password/reset?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/forgot-password/resend")
    public String handleResendOtp(@RequestParam("email") String email,
                                   RedirectAttributes redirectAttributes) {
        try {
            userDetailsService.initiatePasswordReset(email);
            redirectAttributes.addFlashAttribute("message", "New OTP sent to your email address.");
            String encoded = URLEncoder.encode(email, StandardCharsets.UTF_8);
            return "redirect:/forgot-password/verify?email=" + encoded;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/forgot-password";
        }
    }

}
