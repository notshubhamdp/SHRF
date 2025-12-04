package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.entity.Property;
import com.SRHF.SRHF.repository.UserRepository;
import com.SRHF.SRHF.repository.PropertyRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/favorites")
public class FavoritesController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private static final Logger logger = LoggerFactory.getLogger(FavoritesController.class);

    public FavoritesController(UserRepository userRepository, PropertyRepository propertyRepository) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    @PostMapping("/add")
    public String addFavorite(
            @RequestParam Long propertyId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByemail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found"));

            user.addFavoriteProperty(property);
            userRepository.save(user);
            logger.info("Property {} added to favorites for user {}", propertyId, email);
            redirectAttributes.addFlashAttribute("message", "✓ Property added to favorites!");
        } catch (Exception e) {
            logger.error("Error adding favorite", e);
            redirectAttributes.addFlashAttribute("error", "Failed to add to favorites");
        }
        
        return "redirect:/tenant-dashboard";
    }

    @PostMapping("/remove")
    public String removeFavorite(
            @RequestParam Long propertyId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByemail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found"));

            user.removeFavoriteProperty(property);
            userRepository.save(user);
            logger.info("Property {} removed from favorites for user {}", propertyId, email);
            redirectAttributes.addFlashAttribute("message", "✓ Property removed from favorites!");
        } catch (Exception e) {
            logger.error("Error removing favorite", e);
            redirectAttributes.addFlashAttribute("error", "Failed to remove from favorites");
        }
        
        return "redirect:/tenant-dashboard";
    }
}
