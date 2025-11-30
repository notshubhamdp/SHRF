package com.SRHF.SRHF.controller;

import com.SRHF.SRHF.entity.Property;
import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.PropertyRepository;
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
import java.util.*;

@Controller
@RequestMapping("/landlord")
public class LandlordPropertyController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/properties";

    public LandlordPropertyController(PropertyRepository propertyRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Show property listing form for landlord to add a new property
     */
    @GetMapping("/add-property")
    public String showAddPropertyForm(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!"LANDLORD".equals(user.getRole())) {
            return "redirect:/role-selection";
        }

        model.addAttribute("user", user);
        model.addAttribute("property", new Property());
        return "landlord-property-form";
    }

    /**
     * Save property details (name, owner, address, city, state, pincode, price, description, location)
     */
    @PostMapping("/add-property")
    public String saveProperty(
            @RequestParam("name") String name,
            @RequestParam("ownerName") String ownerName,
            @RequestParam("address") String address,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("pincode") String pincode,
            @RequestParam("price") Double price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Property property = new Property();
        property.setName(name);
        property.setOwnerName(ownerName);
        property.setAddress(address);
        property.setCity(city);
        property.setState(state);
        property.setPincode(pincode);
        property.setPrice(price);
        property.setDescription(description);
        property.setLatitude(latitude);
        property.setLongitude(longitude);
        property.setLandlordId(user.getId());
        property.setVerificationStatus("PENDING");
        property.setCreatedAt(System.currentTimeMillis());

        Property savedProperty = propertyRepository.save(property);
        redirectAttributes.addFlashAttribute("message", "Property details saved! Now upload images and documents.");

        return "redirect:/landlord/upload-images/" + savedProperty.getId();
    }

    /**
     * Show image upload page
     */
    @GetMapping("/upload-images/{propertyId}")
    public String showImageUploadForm(@PathVariable Long propertyId, Model model) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        model.addAttribute("property", property);
        return "landlord-upload-images";
    }

    /**
     * Handle multiple image uploads
     */
    @PostMapping("/upload-images/{propertyId}")
    public String uploadImages(
            @PathVariable Long propertyId,
            @RequestParam("images") MultipartFile[] files,
            RedirectAttributes redirectAttributes) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (files.length == 0) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one image");
            return "redirect:/landlord/upload-images/" + propertyId;
        }

        try {
            List<String> imagePaths = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                // Validate file type (image only)
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "Only image files are allowed");
                    return "redirect:/landlord/upload-images/" + propertyId;
                }

                // Create directory
                Path uploadPath = Paths.get(UPLOAD_DIR, "property-" + propertyId);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Save file
                String fileName = "image_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, file.getBytes());
                imagePaths.add(filePath.toString());
            }

            property.setImagesPath(String.join(",", imagePaths));
            propertyRepository.save(property);

            redirectAttributes.addFlashAttribute("message", "Images uploaded successfully! Now upload documents.");
            return "redirect:/landlord/upload-documents/" + propertyId;

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload images: " + e.getMessage());
            return "redirect:/landlord/upload-images/" + propertyId;
        }
    }

    /**
     * Show document upload page
     */
    @GetMapping("/upload-documents/{propertyId}")
    public String showDocumentUploadForm(@PathVariable Long propertyId, Model model) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        model.addAttribute("property", property);
        return "landlord-upload-documents";
    }

    /**
     * Handle document uploads (ownership proof, etc.)
     */
    @PostMapping("/upload-documents/{propertyId}")
    public String uploadDocuments(
            @PathVariable Long propertyId,
            @RequestParam("documents") MultipartFile[] files,
            RedirectAttributes redirectAttributes) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        if (files.length == 0) {
            redirectAttributes.addFlashAttribute("error", "Please upload at least one ownership document");
            return "redirect:/landlord/upload-documents/" + propertyId;
        }

        try {
            List<String> docPaths = new ArrayList<>();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                // Validate file type (PDF, images only)
                String contentType = file.getContentType();
                if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                    redirectAttributes.addFlashAttribute("error", "Only PDF and image files are allowed");
                    return "redirect:/landlord/upload-documents/" + propertyId;
                }

                // Create directory
                Path uploadPath = Paths.get(UPLOAD_DIR, "property-" + propertyId, "documents");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Save file
                String fileName = "doc_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.write(filePath, file.getBytes());
                docPaths.add(filePath.toString());
            }

            property.setDocumentsPath(String.join(",", docPaths));
            propertyRepository.save(property);

            redirectAttributes.addFlashAttribute("message", 
                    "Documents uploaded successfully! Your property is now pending admin verification.");
            return "redirect:/landlord/my-properties";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload documents: " + e.getMessage());
            return "redirect:/landlord/upload-documents/" + propertyId;
        }
    }

    /**
     * Show all properties listed by the landlord
     */
    @GetMapping("/my-properties")
    public String myProperties(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByemail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Property> properties = propertyRepository.findByLandlordId(user.getId());
        model.addAttribute("properties", properties);
        model.addAttribute("user", user);

        return "landlord-my-properties";
    }
}
