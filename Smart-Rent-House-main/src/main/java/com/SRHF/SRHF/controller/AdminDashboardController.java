package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import com.SRHF.SRHF.repository.PropertyRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public AdminDashboardController(UserRepository userRepository, PropertyRepository propertyRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!"ADMIN".equals(user.getRole())) {
            return "redirect:/home";
        }

        // Add admin user and statistics
        model.addAttribute("admin", user);
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalProperties", propertyRepository.count());
        
        return "admin-dashboard-main";
    }
}
