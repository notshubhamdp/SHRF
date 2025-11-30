package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/role-selection")
public class RoleSelectionController {

    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/documents";

    public RoleSelectionController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Show role selection page (Tenant or Landlord)
     */
    @GetMapping
    public String showRoleSelection(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // If role already set, redirect to home or dashboard
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            return "redirect:/home";
        }

        model.addAttribute("user", user);
        return "role-selection";
    }

    /**
     * Set user role (TENANT or LANDLORD)
     */
    @PostMapping
    public String selectRole(
            @RequestParam("role") String role,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (!role.equals("TENANT") && !role.equals("LANDLORD")) {
            redirectAttributes.addFlashAttribute("error", "Invalid role selection");
            return "redirect:/role-selection";
        }

        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setRole(role);
        userRepository.save(user);

        // If LANDLORD, go to property listing form
        if ("LANDLORD".equals(role)) {
            redirectAttributes.addFlashAttribute("message", "Welcome, Landlord! Let's add your first property.");
            return "redirect:/landlord/add-property";
        }

        // If TENANT, go to tenant-type selection
        return "redirect:/role-selection/tenant-type";
    }

    /**
     * Show tenant sub-type selection (Student or Family)
     */
    @GetMapping("/tenant-type")
    public String showTenantType(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"TENANT".equals(user.getRole())) {
            return "redirect:/role-selection";
        }

        model.addAttribute("user", user);
        return "tenant-type";
    }

    /**
     * Set tenant sub-type (STUDENT or FAMILY)
     */
    @PostMapping("/tenant-type")
    public String selectTenantType(
            @RequestParam("tenantType") String tenantType,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (!tenantType.equals("STUDENT") && !tenantType.equals("FAMILY")) {
            redirectAttributes.addFlashAttribute("error", "Invalid tenant type selection");
            return "redirect:/role-selection/tenant-type";
        }

        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setTenantType(tenantType);
        userRepository.save(user);

        // If STUDENT, go to document upload
        if ("STUDENT".equals(tenantType)) {
            return "redirect:/role-selection/student-upload";
        }

        // If FAMILY, go to home/dashboard
        redirectAttributes.addFlashAttribute("message", "Welcome, Family!");
        return "redirect:/home";
    }

    /**
     * Show student document upload page
     */
    @GetMapping("/student-upload")
    public String showStudentUpload(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"STUDENT".equals(user.getTenantType())) {
            return "redirect:/role-selection";
        }

        model.addAttribute("user", user);
        return "student-upload";
    }

    /**
     * Handle student ID document upload
     */
    @PostMapping("/student-upload")
    public String uploadStudentDocument(
            @RequestParam("document") MultipartFile file,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/role-selection/student-upload";
        }

        try {
            // Create uploads directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename: userId_timestamp_originalName
            String fileName = user.getId() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save file
            Files.write(filePath, file.getBytes());

            // Update user with document path
            user.setDocumentPath(filePath.toString());
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("message", 
                    "Document uploaded successfully! Your ID will be verified by our team.");
            return "redirect:/home";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", 
                    "Failed to upload document: " + e.getMessage());
            return "redirect:/role-selection/student-upload";
        }
    }
}
