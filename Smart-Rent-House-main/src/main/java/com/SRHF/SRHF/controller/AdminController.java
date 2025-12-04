package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.Property;
import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.PropertyRepository;
import com.SRHF.SRHF.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public AdminController(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Main admin dashboard at /admin-dashboard
     */
    @GetMapping("/admin-dashboard")
    public String mainAdminDashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User admin = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/home";
        }

        long totalUsers = userRepository.count();
        long totalProperties = propertyRepository.count();
        List<Property> pendingProperties = propertyRepository.findByVerificationStatus("PENDING");
        List<Property> approvedProperties = propertyRepository.findByVerificationStatus("APPROVED");

        model.addAttribute("admin", admin);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProperties", totalProperties);
        model.addAttribute("pendingCount", pendingProperties.size());
        model.addAttribute("approvedCount", approvedProperties.size());
        model.addAttribute("pendingProperties", pendingProperties.stream().limit(5).toList());
        
        return "admin-dashboard";
    }

    /**
     * List all pending properties for admin verification
     */
    @GetMapping("/admin/pending-properties")
    public String pendingProperties(Authentication authentication, Model model) {
        List<Property> properties = propertyRepository.findByVerificationStatusOrderByCreatedAtDesc("PENDING");
        model.addAttribute("properties", properties);
        model.addAttribute("status", "PENDING");
        return "admin-pending-properties";
    }

    /**
     * List approved properties
     */
    @GetMapping("/admin/approved-properties")
    public String approvedProperties(Authentication authentication, Model model) {
        List<Property> properties = propertyRepository.findByVerificationStatusOrderByCreatedAtDesc("APPROVED");
        model.addAttribute("properties", properties);
        model.addAttribute("status", "APPROVED");
        return "admin-pending-properties";
    }

    /**
     * List rejected properties
     */
    @GetMapping("/admin/rejected-properties")
    public String rejectedProperties(Authentication authentication, Model model) {
        List<Property> properties = propertyRepository.findByVerificationStatusOrderByCreatedAtDesc("REJECTED");
        model.addAttribute("properties", properties);
        model.addAttribute("status", "REJECTED");
        return "admin-pending-properties";
    }

    /**
     * View detailed property information for verification
     */
    @GetMapping("/admin/property/{id}")
    public String propertyDetail(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Property> propertyOpt = propertyRepository.findById(id);

        if (propertyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Property not found");
            return "redirect:/admin/pending-properties";
        }

        Property property = propertyOpt.get();
        Optional<User> landlordOpt = userRepository.findById(property.getLandlordId());

        model.addAttribute("property", property);
        if (landlordOpt.isPresent()) {
            model.addAttribute("landlord", landlordOpt.get());
        }

        // Split comma-separated paths for image and document display
        String[] imagePaths = property.getImagesPath() != null ? property.getImagesPath().split(",") : new String[]{};
        String[] documentPaths = property.getDocumentsPath() != null ? property.getDocumentsPath().split(",") : new String[]{};

        model.addAttribute("imagePaths", imagePaths);
        model.addAttribute("documentPaths", documentPaths);

        return "admin-property-detail";
    }

    /**
     * Approve property listing (set status to APPROVED)
     */
    @PostMapping("/admin/property/{id}/approve")
    public String approveProperty(
            @PathVariable Long id,
            @RequestParam(value = "notes", required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Optional<Property> propertyOpt = propertyRepository.findById(id);

        if (propertyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Property not found");
            return "redirect:/admin/pending-properties";
        }

        Property property = propertyOpt.get();
        property.setVerificationStatus("APPROVED");
        if (notes != null && !notes.isEmpty()) {
            property.setAdminNotes("Approved: " + notes);
        } else {
            property.setAdminNotes("Property approved for listing");
        }

        propertyRepository.save(property);

        redirectAttributes.addFlashAttribute("message", 
                "Property '" + property.getName() + "' has been approved and is now listed for rent!");

        // TODO: Send email notification to landlord about approval

        return "redirect:/admin/pending-properties";
    }

    /**
     * Reject property listing (set status to REJECTED)
     */
    @PostMapping("/admin/property/{id}/reject")
    public String rejectProperty(
            @PathVariable Long id,
            @RequestParam(value = "rejectionReason", required = true) String rejectionReason,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please provide a rejection reason");
            return "redirect:/admin/property/" + id;
        }

        Optional<Property> propertyOpt = propertyRepository.findById(id);

        if (propertyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Property not found");
            return "redirect:/admin/pending-properties";
        }

        Property property = propertyOpt.get();
        property.setVerificationStatus("REJECTED");
        property.setAdminNotes("Rejected: " + rejectionReason);

        propertyRepository.save(property);

        redirectAttributes.addFlashAttribute("message", 
                "Property '" + property.getName() + "' has been rejected. Landlord will be notified.");

        // TODO: Send email notification to landlord about rejection with reason

        return "redirect:/admin/pending-properties";
    }

    /**
     * Re-request verification for rejected properties (reset to PENDING)
     */
    @PostMapping("/admin/property/{id}/re-request")
    public String reRequestVerification(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Optional<Property> propertyOpt = propertyRepository.findById(id);

        if (propertyOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Property not found");
            return "redirect:/admin/rejected-properties";
        }

        Property property = propertyOpt.get();
        property.setVerificationStatus("PENDING");
        property.setAdminNotes(null);

        propertyRepository.save(property);

        redirectAttributes.addFlashAttribute("message", 
                "Property '" + property.getName() + "' has been moved back to pending for re-verification.");

        return "redirect:/admin/pending-properties";
    }
}
