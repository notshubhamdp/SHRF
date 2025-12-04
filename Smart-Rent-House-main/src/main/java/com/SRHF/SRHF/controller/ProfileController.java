package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        try {
            if (authentication == null) {
                logger.warn("No authentication found");
                return "redirect:/login";
            }

            String email = authentication.getName();
            logger.info("Loading profile for: {}", email);

            User user = userRepository.findByemail(email).orElse(null);
            
            if (user == null) {
                logger.warn("User not found: {}", email);
                user = new User();
                user.setEmail(email);
            }

            model.addAttribute("tenant", user);
            model.addAttribute("profilePicture", user.getProfilePicturePath());
            logger.info("Profile page loaded successfully");
            return "profile";
        } catch (Exception e) {
            logger.error("Error loading profile page", e);
            return "redirect:/login";
        }
    }

    @PostMapping("/update")
    public String updateProfile(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "zip", required = false) String zip,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "bio", required = false) String bio,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String email = authentication.getName();
            logger.info("Profile update request received for: {}", email);
            
            User user = userRepository.findByemail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Update fields if provided
            if (firstName != null && !firstName.trim().isEmpty()) {
                user.setFirstName(firstName);
                logger.debug("Updated firstName: {}", firstName);
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                user.setLastName(lastName);
                logger.debug("Updated lastName: {}", lastName);
            }
            if (phone != null && !phone.trim().isEmpty()) {
                user.setPhone(phone);
                logger.debug("Updated phone: {}", phone);
            }
            if (address != null && !address.trim().isEmpty()) {
                user.setAddress(address);
                logger.debug("Updated address: {}", address);
            }
            if (city != null && !city.trim().isEmpty()) {
                user.setCity(city);
                logger.debug("Updated city: {}", city);
            }
            if (state != null && !state.trim().isEmpty()) {
                user.setState(state);
                logger.debug("Updated state: {}", state);
            }
            if (zip != null && !zip.trim().isEmpty()) {
                user.setZip(zip);
                logger.debug("Updated zip: {}", zip);
            }
            if (gender != null && !gender.trim().isEmpty()) {
                user.setGender(gender);
                logger.debug("Updated gender: {}", gender);
            }
            if (bio != null && !bio.trim().isEmpty()) {
                user.setBio(bio);
                logger.debug("Updated bio: {}", bio);
            }
            
            // Handle date of birth
            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                try {
                    user.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                    logger.debug("Updated dateOfBirth: {}", dateOfBirth);
                } catch (Exception e) {
                    logger.warn("Invalid date format: {}", dateOfBirth);
                }
            }

            userRepository.save(user);
            logger.info("Profile saved successfully for: {}", email);
            redirectAttributes.addFlashAttribute("message", "✓ Profile updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}
