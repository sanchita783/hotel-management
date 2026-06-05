package com.example.service;

import com.example.entity.Booking;
import com.example.entity.Payment;
import com.example.entity.User;

public interface EmailService {

    void sendWelcomeEmail(User user);

    void sendEmailVerificationEmail(User user, String token);

    void sendPasswordResetEmail(User user, String token);

    void sendBookingConfirmationEmail(Booking booking);

    void sendBookingCancellationEmail(Booking booking);

    void sendCheckInEmail(Booking booking);

    void sendCheckOutEmail(Booking booking);

    void sendPaymentConfirmationEmail(Payment payment);

    void sendEnquiryAcknowledgementEmail(String email, String name, String subject);

    void sendEnquiryResponseEmail(String email, String name, String subject, String response);

    void sendSimpleEmail(String to, String subject, String body);
}
