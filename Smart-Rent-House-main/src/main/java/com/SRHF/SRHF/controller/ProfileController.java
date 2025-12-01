package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/avatars";
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
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String email = authentication.getName();
            User user = userRepository.findByemail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Update fields if provided
            if (firstName != null && !firstName.trim().isEmpty()) user.setFirstName(firstName);
            if (lastName != null && !lastName.trim().isEmpty()) user.setLastName(lastName);
            if (phone != null && !phone.trim().isEmpty()) user.setPhone(phone);
            if (address != null && !address.trim().isEmpty()) user.setAddress(address);
            if (city != null && !city.trim().isEmpty()) user.setCity(city);
            if (state != null && !state.trim().isEmpty()) user.setState(state);
            if (zip != null && !zip.trim().isEmpty()) user.setZip(zip);
            if (gender != null && !gender.trim().isEmpty()) user.setGender(gender);
            if (bio != null && !bio.trim().isEmpty()) user.setBio(bio);
            
            // Handle date of birth
            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                try {
                    user.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                } catch (Exception e) {
                    logger.warn("Invalid date format: {}", dateOfBirth);
                }
            }

            // Handle avatar upload
            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    String fileName = user.getId() + "_" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.write(filePath, avatarFile.getBytes());

                    user.setAvatarPath(filePath.toString());
                    logger.info("Avatar uploaded: {}", filePath);

                } catch (IOException e) {
                    logger.error("Failed to upload avatar", e);
                    redirectAttributes.addFlashAttribute("error", "Failed to upload avatar: " + e.getMessage());
                    return "redirect:/profile";
                }
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
            logger.info("Profile updated for: {}", email);
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}
