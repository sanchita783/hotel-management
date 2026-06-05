package com.example.repository;

import com.example.entity.Enquiry;
import com.example.entity.EnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

    List<Enquiry> findByUserId(Long userId);

    List<Enquiry> findByEnquiryStatus(EnquiryStatus status);

    List<Enquiry> findByGuestEmail(String email);

    @Query("""
        SELECT COUNT(e) FROM Enquiry e
        WHERE e.enquiryStatus = com.example.entity.EnquiryStatus.OPEN
    """)
    Long countOpenEnquiries();

    List<Enquiry> findAllByOrderByCreatedAtDesc();
}
