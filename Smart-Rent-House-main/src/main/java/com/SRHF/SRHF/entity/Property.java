package com.SRHF.SRHF.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // House name/title

    @Column(name = "owner_name", nullable = false)
    private String ownerName; // Landlord's name

    @Column(name = "description")
    private String description; // House description

    @Column(name = "address", nullable = false)
    private String address; // Full address

    @Column(name = "city")
    private String city; // City

    @Column(name = "state")
    private String state; // State/Province

    @Column(name = "pincode")
    private String pincode; // Postal/Zip code

    @Column(name = "latitude")
    private Double latitude; // House location latitude

    @Column(name = "longitude")
    private Double longitude; // House location longitude

    @Column(name = "price", nullable = false)
    private Double price; // Monthly rent price

    @Column(name = "landlord_id", nullable = false)
    private Long landlordId; // Reference to User who owns this property

    @Column(name = "images_path")
    private String imagesPath; // Comma-separated paths to house images

    @Column(name = "documents_path")
    private String documentsPath; // Comma-separated paths to ownership documents

    @Column(name = "verification_status")
    private String verificationStatus; // "PENDING", "APPROVED", "REJECTED"

    @Column(name = "admin_notes")
    private String adminNotes; // Admin's notes on verification

    @Column(name = "created_at")
    private Long createdAt; // Timestamp when property was created

    @ManyToMany(mappedBy = "favoriteProperties")
    private Set<User> favoredBy = new HashSet<>();

    public Property() {
    }

    public Property(String name, String ownerName, String address, Double price, Long landlordId) {
        this.name = name;
        this.ownerName = ownerName;
        this.address = address;
        this.price = price;
        this.landlordId = landlordId;
        this.verificationStatus = "PENDING";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(Long landlordId) {
        this.landlordId = landlordId;
    }

    public String getImagesPath() {
        return imagesPath;
    }

    public void setImagesPath(String imagesPath) {
        this.imagesPath = imagesPath;
    }

    public String getDocumentsPath() {
        return documentsPath;
    }

    public void setDocumentsPath(String documentsPath) {
        this.documentsPath = documentsPath;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Set<User> getFavoredBy() {
        return favoredBy;
    }

    public void setFavoredBy(Set<User> favoredBy) {
        this.favoredBy = favoredBy;
    }
}
