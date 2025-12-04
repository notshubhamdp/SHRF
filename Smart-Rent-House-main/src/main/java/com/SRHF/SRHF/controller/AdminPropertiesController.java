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

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminPropertiesController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public AdminPropertiesController(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * View properties by status (PENDING, APPROVED, REJECTED)
     */
    @GetMapping("/pending-properties")
    public String viewPendingProperties(Model model) {
        List<Property> properties = propertyRepository.findByVerificationStatus("PENDING");
        model.addAttribute("properties", properties);
        model.addAttribute("status", "PENDING");
        return "admin-pending-properties";
    }

    @GetMapping("/approved-properties")
    public String viewApprovedProperties(Model model) {
        List<Property> properties = propertyRepository.findByVerificationStatus("APPROVED");
        model.addAttribute("properties", properties);
        model.addAttribute("status", "APPROVED");
        return "admin-pending-properties";
    }

    @GetMapping("/rejected-properties")
    public String viewRejectedProperties(Model model) {
        List<Property> properties = propertyRepository.findByVerificationStatus("REJECTED");
        model.addAttribute("properties", properties);
        model.addAttribute("status", "REJECTED");
        return "admin-pending-properties";
    }

    /**
     * View detailed property information for approval/rejection
     */
    @GetMapping("/property/{id}")
    public String viewPropertyDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        User landlord = userRepository.findById(property.getLandlordId())
                .orElseThrow(() -> new IllegalArgumentException("Landlord not found"));

        // Parse images and documents paths
        String[] imagePaths = property.getImagesPath() != null && !property.getImagesPath().isEmpty()
                ? property.getImagesPath().split(",")
                : new String[0];
        String[] documentPaths = property.getDocumentsPath() != null && !property.getDocumentsPath().isEmpty()
                ? property.getDocumentsPath().split(",")
                : new String[0];

        model.addAttribute("property", property);
        model.addAttribute("landlord", landlord);
        model.addAttribute("imagePaths", imagePaths);
        model.addAttribute("documentPaths", documentPaths);

        return "admin-property-detail";
    }

    /**
     * Approve a property
     */
    @PostMapping("/property/{id}/approve")
    public String approveProperty(@PathVariable Long id,
                                   @RequestParam(value = "notes", required = false) String notes,
                                   RedirectAttributes redirectAttributes) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        property.setVerificationStatus("APPROVED");
        if (notes != null && !notes.isEmpty()) {
            property.setAdminNotes(notes);
        } else {
            property.setAdminNotes("Approved by admin");
        }
        propertyRepository.save(property);

        redirectAttributes.addFlashAttribute("message", "Property approved successfully!");
        return "redirect:/admin/pending-properties";
    }

    /**
     * Reject a property
     */
    @PostMapping("/property/{id}/reject")
    public String rejectProperty(@PathVariable Long id,
                                  @RequestParam("rejectionReason") String rejectionReason,
                                  RedirectAttributes redirectAttributes) {
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Rejection reason is required");
            return "redirect:/admin/property/" + id;
        }

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        property.setVerificationStatus("REJECTED");
        property.setAdminNotes("Rejection reason: " + rejectionReason);
        propertyRepository.save(property);

        redirectAttributes.addFlashAttribute("message", "Property rejected successfully!");
        return "redirect:/admin/pending-properties";
    }
}
