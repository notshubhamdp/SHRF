package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import com.SRHF.SRHF.service.CustomUserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("register")
public class AuthenticationController {

    private final CustomUserDetailsService userDetailsService;

    public AuthenticationController(CustomUserDetailsService userDetailsService, UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        // userRepository param kept for compatibility with other constructors/routes, but not stored
    }

    @GetMapping
    public String register(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "register";

    }

    @PostMapping
    public String postUser(@ModelAttribute("user") User user,
                           @RequestParam("confirmPassword") String confirmPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            // validate password confirmation
            if (user.getPassword() == null || !user.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
                redirectAttributes.addFlashAttribute("user", user);
                return "redirect:/register";
            }

            //save the user to the database
            //send a confirmation email
            userDetailsService.registerUser(user);
            redirectAttributes.addFlashAttribute(
                    "message",
                    "Registration successful! Please check your email to confirm your account."
            );
            return "redirect:/register";
        } catch (IllegalArgumentException ex) {
            // email already exists or validation failed
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            // preserve the entered user data (optional)
            redirectAttributes.addFlashAttribute("user", user);
            return "redirect:/register";
        }

    }

    @GetMapping("/confirmToken")
    public String confirmToken(@RequestParam("token") String token, Model model) {
        try {
            userDetailsService.confirmationToken(token);
            model.addAttribute("success", true);
            model.addAttribute("message", "Email confirmed successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("success", false);
            model.addAttribute("message", ex.getMessage());
        } catch (Exception ex) {
            model.addAttribute("success", false);
            model.addAttribute("message", "An unexpected error occurred: " + ex.getMessage());
        }
        return "tokenConfirmed";
    }

    // Added: support the URL /register/tokenConfirmed?token=... which the email link uses
    @GetMapping("/tokenConfirmed")
    public String tokenConfirmed(@RequestParam("token") String token, Model model) {
        try {
            userDetailsService.confirmationToken(token);
            model.addAttribute("success", true);
            model.addAttribute("message", "Email confirmed successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("success", false);
            model.addAttribute("message", ex.getMessage());
        } catch (Exception ex) {
            model.addAttribute("success", false);
            model.addAttribute("message", "An unexpected error occurred: " + ex.getMessage());
        }
        return "tokenConfirmed";
    }
}
