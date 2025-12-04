package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.entity.Property;
import java.util.List;
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
        
        // Load landlord properties and compute stats
        List<Property> properties = propertyRepository.findByLandlordId(user.getId());

        long totalProperties = properties.size();
        long pendingCount = properties.stream().filter(p -> "PENDING".equals(p.getVerificationStatus())).count();
        long approvedCount = properties.stream().filter(p -> "APPROVED".equals(p.getVerificationStatus())).count();
        long rejectedCount = properties.stream().filter(p -> "REJECTED".equals(p.getVerificationStatus())).count();

        // recent properties (most recent 6)
        List<Property> recentProperties = properties.stream()
                .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                .limit(6)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("totalProperties", totalProperties);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("recentProperties", recentProperties);

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

    @GetMapping("/favorites")
    public String myFavorites(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!"TENANT".equals(user.getRole())) {
            return "redirect:/home";
        }

        // Add user and their favorite properties
        model.addAttribute("user", user);
        model.addAttribute("favoriteProperties", user.getFavoriteProperties());
        return "Favorite-Properties-tenant";
    }
}