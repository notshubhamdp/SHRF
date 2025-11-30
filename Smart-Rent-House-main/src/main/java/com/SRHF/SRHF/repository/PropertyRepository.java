package com.SRHF.SRHF.repository;

import com.SRHF.SRHF.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // Find all properties by landlord ID
    List<Property> findByLandlordId(Long landlordId);

    // Find all properties by verification status
    List<Property> findByVerificationStatus(String verificationStatus);

    // Find all approved properties for tenant listing
    List<Property> findByVerificationStatusOrderByCreatedAtDesc(String verificationStatus);

    // Find a specific property by ID
    Optional<Property> findById(Long id);
}
