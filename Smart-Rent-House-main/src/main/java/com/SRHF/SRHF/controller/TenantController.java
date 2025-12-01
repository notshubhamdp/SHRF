package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/tenant")
public class TenantController {

    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/avatars";
    private static final Logger logger = LoggerFactory.getLogger(TenantController.class);

    public TenantController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String showProfile(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            logger.info("Loading profile for user: {}", email);
            User user = userRepository.findByemail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            model.addAttribute("tenant", user);
            logger.info("Profile loaded successfully for user: {}", email);
            return "profile";
        } catch (Exception e) {
            logger.error("Error loading profile", e);
            throw e;
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("tenant") User formTenant,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update allowed fields
        user.setFirstName(formTenant.getFirstName());
        user.setLastName(formTenant.getLastName());
        user.setPhone(formTenant.getPhone());
        user.setAddress(formTenant.getAddress());
        user.setCity(formTenant.getCity());
        user.setState(formTenant.getState());
        user.setZip(formTenant.getZip());
        user.setDateOfBirth(formTenant.getDateOfBirth());
        user.setGender(formTenant.getGender());
        user.setBio(formTenant.getBio());

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

                // Save relative path so it can be used in <img src="...">
                user.setAvatarPath(filePath.toString());

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload avatar: " + e.getMessage());
                return "redirect:/tenant/profile";
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
        return "redirect:/tenant/profile";
    }
}
