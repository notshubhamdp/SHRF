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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/tenant")
public class TenantController {

    private final UserRepository userRepository;
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

    @GetMapping("/test")
    public String test() {
        logger.info("TenantController is working!");
        return "Tenant controller is loaded and working!";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("tenant") User formTenant,
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

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("message", "Profile updated successfully");
        return "redirect:/tenant/profile";
    }
}
