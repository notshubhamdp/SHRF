package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import com.SRHF.SRHF.repository.PropertyRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public HomeController(UserRepository userRepository, PropertyRepository propertyRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/landlord-dashboard")
    public String landlordDashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!"LANDLORD".equals(user.getRole())) {
            return "redirect:/home";
        }

        model.addAttribute("user", user);
        return "landlord-dashboard";
    }

    @GetMapping("/tenant-dashboard")
    public String tenantDashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!"TENANT".equals(user.getRole())) {
            return "redirect:/home";
        }

        // Add user and a list of approved properties for tenants to browse
        model.addAttribute("user", user);
        model.addAttribute("availableProperties", propertyRepository.findByVerificationStatusOrderByCreatedAtDesc("APPROVED"));
        model.addAttribute("favoriteProperties", user.getFavoriteProperties());
        return "tenant-dashboard";
    }
}