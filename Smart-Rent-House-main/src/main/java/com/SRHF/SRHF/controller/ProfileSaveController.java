package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/profile")
public class ProfileSaveController {

    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/avatars";
    private static final Logger logger = LoggerFactory.getLogger(ProfileSaveController.class);

    public ProfileSaveController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/save")
    public String saveProfile(
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
            if (authentication == null) {
                logger.warn("Attempt to save profile without authentication");
                return "redirect:/login";
            }

            String email = authentication.getName();
            User user = userRepository.findByemail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (firstName != null && !firstName.trim().isEmpty()) user.setFirstName(firstName.trim());
            if (lastName != null && !lastName.trim().isEmpty()) user.setLastName(lastName.trim());
            if (phone != null) user.setPhone(phone.trim());
            if (address != null) user.setAddress(address.trim());
            if (city != null) user.setCity(city.trim());
            if (state != null) user.setState(state.trim());
            if (zip != null) user.setZip(zip.trim());
            if (gender != null) user.setGender(gender.trim());
            if (bio != null) user.setBio(bio.trim());

            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                try {
                    user.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
                } catch (Exception ex) {
                    logger.warn("Invalid dateOfBirth provided: {}", dateOfBirth);
                }
            }

            if (avatarFile != null && !avatarFile.isEmpty()) {
                try {
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                    String fileName = user.getId() + "_" + System.currentTimeMillis() + "_" + avatarFile.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.write(filePath, avatarFile.getBytes());

                    user.setAvatarPath(filePath.toString());
                    logger.info("Saved avatar for {} at {}", email, filePath);
                } catch (IOException ioe) {
                    logger.error("Failed to save avatar", ioe);
                    redirectAttributes.addFlashAttribute("error", "Failed to upload avatar: " + ioe.getMessage());
                    return "redirect:/profile";
                }
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
            logger.info("Profile saved for {}", email);
            return "redirect:/profile";
        } catch (Exception e) {
            logger.error("Error saving profile", e);
            redirectAttributes.addFlashAttribute("error", "Error saving profile: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}
