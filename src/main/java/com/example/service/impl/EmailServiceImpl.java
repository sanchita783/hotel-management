//package com.example.service.impl;
//
//import com.example.entity.Booking;
//import com.example.entity.Payment;
//import com.example.entity.User;
//import com.example.service.EmailService;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailServiceImpl implements EmailService {
//
//    private final JavaMailSender mailSender;
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    @Value("${app.name}")
//    private String appName;
//
//    @Value("${app.frontend.url}")
//    private String frontendUrl;
//
//    @Override
//    @Async("emailExecutor")
//    public void sendWelcomeEmail(User user) {
//        String subject = "Welcome to " + appName + "!";
//        String body = buildWelcomeEmailBody(user);
//        sendHtmlEmail(user.getEmail(), subject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendEmailVerificationEmail(User user, String token) {
//        String subject = "Verify Your Email - " + appName;
//        String verifyLink = frontendUrl + "/verify-email?token=" + token;
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <h2 style="color: #2c3e50;">Email Verification</h2>
//                    <p>Hello <strong>%s</strong>,</p>
//                    <p>Please verify your email address by clicking the button below:</p>
//                    <a href="%s" style="background-color: #3498db; color: white; padding: 12px 24px;
//                       text-decoration: none; border-radius: 4px; display: inline-block; margin: 16px 0;">
//                       Verify Email
//                    </a>
//                    <p>This link expires in 24 hours.</p>
//                    <p>If you didn't create an account, please ignore this email.</p>
//                    <hr/>
//                    <p style="color: #7f8c8d; font-size: 12px;">%s | support@grandhotel.com</p>
//                </div>
//                """.formatted(user.getFullName(), verifyLink, appName);
//        sendHtmlEmail(user.getEmail(), subject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendPasswordResetEmail(User user, String token) {
//        String subject = "Password Reset Request - " + appName;
//        String resetLink = frontendUrl + "/reset-password.html?token=" + token;
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <h2 style="color: #e74c3c;">Password Reset</h2>
//                    <p>Hello <strong>%s</strong>,</p>
//                    <p>We received a request to reset your password. Click below to proceed:</p>
//                    <a href="%s" style="background-color: #e74c3c; color: white; padding: 12px 24px;
//                       text-decoration: none; border-radius: 4px; display: inline-block; margin: 16px 0;">
//                       Reset Password
//                    </a>
//                    <p>This link expires in <strong>1 hour</strong>.</p>
//                    <p>If you did not request a password reset, please ignore this email or contact support immediately.</p>
//                    <hr/>
//                    <p style="color: #7f8c8d; font-size: 12px;">%s | support@grandhotel.com</p>
//                </div>
//                """.formatted(user.getFullName(), resetLink, appName);
//        sendHtmlEmail(user.getEmail(), subject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendBookingConfirmationEmail(Booking booking) {
//        String subject = "Booking Confirmed - " + booking.getBookingReference() + " | " + appName;
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <div style="background-color: #27ae60; padding: 20px; text-align: center;">
//                        <h1 style="color: white; margin: 0;">Booking Confirmed!</h1>
//                    </div>
//                    <div style="padding: 24px; background: #f9f9f9;">
//                        <p>Hello <strong>%s</strong>,</p>
//                        <p>Your booking has been confirmed. Here are your details:</p>
//                        <table style="width: 100%%; border-collapse: collapse; background: white; border-radius: 8px;">
//                            <tr style="background: #ecf0f1;">
//                                <td style="padding: 12px; font-weight: bold;">Booking Reference</td>
//                                <td style="padding: 12px; color: #27ae60; font-weight: bold;">%s</td>
//                            </tr>
//                            <tr>
//                                <td style="padding: 12px; font-weight: bold;">Room</td>
//                                <td style="padding: 12px;">%s (%s)</td>
//                            </tr>
//                            <tr style="background: #ecf0f1;">
//                                <td style="padding: 12px; font-weight: bold;">Check-In</td>
//                                <td style="padding: 12px;">%s</td>
//                            </tr>
//                            <tr>
//                                <td style="padding: 12px; font-weight: bold;">Check-Out</td>
//                                <td style="padding: 12px;">%s</td>
//                            </tr>
//                            <tr style="background: #ecf0f1;">
//                                <td style="padding: 12px; font-weight: bold;">Nights</td>
//                                <td style="padding: 12px;">%d nights</td>
//                            </tr>
//                            <tr>
//                                <td style="padding: 12px; font-weight: bold;">Guests</td>
//                                <td style="padding: 12px;">%d</td>
//                            </tr>
//                            <tr style="background: #ecf0f1;">
//                                <td style="padding: 12px; font-weight: bold;">Total Amount</td>
//                                <td style="padding: 12px; font-size: 18px; color: #27ae60;">
//                                    ₹%s
//                                </td>
//                            </tr>
//                            <tr>
//                                <td style="padding: 12px; font-weight: bold;">Balance Due</td>
//                                <td style="padding: 12px; color: #e74c3c;">₹%s</td>
//                            </tr>
//                        </table>
//                        %s
//                        <p style="margin-top: 20px;">We look forward to welcoming you!</p>
//                    </div>
//                    <div style="background: #2c3e50; padding: 16px; text-align: center;">
//                        <p style="color: white; margin: 0;">%s | support@grandhotel.com</p>
//                    </div>
//                </div>
//                """.formatted(
//                booking.getUser().getFullName(),
//                booking.getBookingReference(),
//                booking.getRoom().getRoomNumber(),
//                booking.getRoom().getRoomType(),
//                booking.getCheckInDate(),
//                booking.getCheckOutDate(),
//                booking.getNumberOfNights(),
//                booking.getNumberOfGuests(),
//                booking.getTotalAmount(),
//                booking.getBalanceAmount(),
//                booking.getSpecialRequests() != null ?
//                        "<p><strong>Special Requests:</strong> " + booking.getSpecialRequests() + "</p>" : "",
//                appName
//        );
//        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendBookingCancellationEmail(Booking booking) {
//        String subject = "Booking Cancelled - " + booking.getBookingReference() + " | " + appName;
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <div style="background-color: #e74c3c; padding: 20px; text-align: center;">
//                        <h1 style="color: white; margin: 0;">Booking Cancelled</h1>
//                    </div>
//                    <div style="padding: 24px;">
//                        <p>Hello <strong>%s</strong>,</p>
//                        <p>Your booking <strong>%s</strong> has been cancelled.</p>
//                        %s
//                        <p>If you have any questions or this was a mistake, please contact our support team.</p>
//                        <p>Email: support@grandhotel.com</p>
//                    </div>
//                    <div style="background: #2c3e50; padding: 16px; text-align: center;">
//                        <p style="color: white; margin: 0;">%s</p>
//                    </div>
//                </div>
//                """.formatted(
//                booking.getUser().getFullName(),
//                booking.getBookingReference(),
//                booking.getCancellationReason() != null ?
//                        "<p><strong>Reason:</strong> " + booking.getCancellationReason() + "</p>" : "",
//                appName
//        );
//        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendCheckInEmail(Booking booking) {
//        String subject = "Welcome! Check-In Successful - " + booking.getBookingReference();
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <div style="background-color: #3498db; padding: 20px; text-align: center;">
//                        <h1 style="color: white; margin: 0;">Welcome to %s!</h1>
//                    </div>
//                    <div style="padding: 24px;">
//                        <p>Hello <strong>%s</strong>,</p>
//                        <p>You have successfully checked into Room <strong>%s</strong>.</p>
//                        <p><strong>Check-out Date:</strong> %s</p>
//                        <p>Enjoy your stay! For any assistance, dial <strong>0</strong> from your room phone.</p>
//                    </div>
//                </div>
//                """.formatted(appName, booking.getUser().getFullName(),
//                booking.getRoom().getRoomNumber(), booking.getCheckOutDate());
//        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendCheckOutEmail(Booking booking) {
//        String subject = "Thank You! Check-Out Complete - " + booking.getBookingReference();
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <div style="background-color: #9b59b6; padding: 20px; text-align: center;">
//                        <h1 style="color: white; margin: 0;">Thank You for Your Stay!</h1>
//                    </div>
//                    <div style="padding: 24px;">
//                        <p>Hello <strong>%s</strong>,</p>
//                        <p>You have successfully checked out from <strong>%s</strong>.</p>
//                        <p>We hope you had a wonderful stay. We'd love to see you again!</p>
//                        <p>Please share your experience with us by leaving a review.</p>
//                    </div>
//                </div>
//                """.formatted(booking.getUser().getFullName(), appName);
//        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendPaymentConfirmationEmail(Payment payment) {
//        String subject = "Payment Confirmed - " + payment.getTransactionId() + " | " + appName;
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <div style="background-color: #27ae60; padding: 20px; text-align: center;">
//                        <h1 style="color: white; margin: 0;">Payment Successful</h1>
//                    </div>
//                    <div style="padding: 24px;">
//                        <p>Hello,</p>
//                        <p>Your payment has been processed successfully.</p>
//                        <table style="width: 100%%; border-collapse: collapse;">
//                            <tr style="background: #ecf0f1;">
//                                <td style="padding: 12px; font-weight: bold;">Transaction ID</td>
//                                <td style="padding: 12px;">%s</td>
//                            </tr>
//                            <tr>
//                                <td style="padding: 12px; font-weight: bold;">Booking Reference</td>
//                                <td style="padding: 12px;">%s</td>
//                            </tr>
//                            <tr style="background: #ecf0f1;">
//                                <td style="padding: 12px; font-weight: bold;">Amount</td>
//                                <td style="padding: 12px; color: #27ae60; font-size: 18px;">₹%s</td>
//                            </tr>
//                            <tr>
//                                <td style="padding: 12px; font-weight: bold;">Payment Method</td>
//                                <td style="padding: 12px;">%s</td>
//                            </tr>
//                            <tr style="background: #ecf0f1;">
//                                <td style="padding: 12px; font-weight: bold;">Date</td>
//                                <td style="padding: 12px;">%s</td>
//                            </tr>
//                        </table>
//                    </div>
//                    <div style="background: #2c3e50; padding: 16px; text-align: center;">
//                        <p style="color: white; margin: 0;">%s | support@grandhotel.com</p>
//                    </div>
//                </div>
//                """.formatted(
//                payment.getTransactionId(),
//                payment.getBooking().getBookingReference(),
//                payment.getAmount(),
//                payment.getPaymentMethod(),
//                payment.getPaymentDate(),
//                appName
//        );
//        sendHtmlEmail(payment.getBooking().getUser().getEmail(), subject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendEnquiryAcknowledgementEmail(String email, String name, String subject) {
//        String emailSubject = "Enquiry Received - " + appName;
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <h2 style="color: #2c3e50;">Thank you for contacting us!</h2>
//                    <p>Hello <strong>%s</strong>,</p>
//                    <p>We have received your enquiry regarding: <strong>%s</strong></p>
//                    <p>Our team will respond within <strong>24–48 hours</strong>.</p>
//                    <p>For urgent matters, call us at <strong>+91 1800 000 0000</strong>.</p>
//                    <hr/>
//                    <p style="color: #7f8c8d; font-size: 12px;">%s</p>
//                </div>
//                """.formatted(name, subject, appName);
//        sendHtmlEmail(email, emailSubject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendEnquiryResponseEmail(String email, String name, String subject, String response) {
//        String emailSubject = "Response to Your Enquiry - " + appName;
//        String body = """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <h2 style="color: #2c3e50;">Enquiry Response</h2>
//                    <p>Hello <strong>%s</strong>,</p>
//                    <p>Regarding your enquiry: <strong>%s</strong></p>
//                    <div style="background: #f8f9fa; padding: 16px; border-left: 4px solid #3498db;
//                                border-radius: 4px; margin: 16px 0;">
//                        <p style="margin: 0;">%s</p>
//                    </div>
//                    <p>If you have further questions, don't hesitate to reach out.</p>
//                    <hr/>
//                    <p style="color: #7f8c8d; font-size: 12px;">%s | support@grandhotel.com</p>
//                </div>
//                """.formatted(name, subject, response, appName);
//        sendHtmlEmail(email, emailSubject, body);
//    }
//
//    @Override
//    @Async("emailExecutor")
//    public void sendSimpleEmail(String to, String subject, String body) {
//        sendHtmlEmail(to, subject, "<pre>" + body + "</pre>");
//    }
//
//    private void sendHtmlEmail(String to, String subject, String htmlBody) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//            helper.setFrom(fromEmail, appName);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlBody, true);
//            mailSender.send(message);
//            log.info("Email sent to: {} | Subject: {}", to, subject);
//        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
//            log.error("Failed to send email to {}: {}", to, e.getMessage());
//        }
//    }
//
//    private String buildWelcomeEmailBody(User user) {
//        return """
//                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
//                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
//                                padding: 40px 20px; text-align: center;">
//                        <h1 style="color: white; margin: 0;">Welcome to %s!</h1>
//                        <p style="color: rgba(255,255,255,0.9);">Your luxury stay begins here</p>
//                    </div>
//                    <div style="padding: 32px 24px; background: #ffffff;">
//                        <h2 style="color: #2c3e50;">Hello, %s! 🎉</h2>
//                        <p>Your account has been created successfully.</p>
//                        <div style="background: #f8f9fa; border-radius: 8px; padding: 20px; margin: 20px 0;">
//                            <p><strong>Account Details:</strong></p>
//                            <p>📧 Email: %s</p>
//                            <p>👤 Role: Customer</p>
//                        </div>
//                        <p>With your account you can:</p>
//                        <ul>
//                            <li>Browse and book rooms</li>
//                            <li>Manage your reservations</li>
//                            <li>Request hotel services</li>
//                            <li>View payment history</li>
//                            <li>Submit enquiries</li>
//                        </ul>
//                        <a href="%s/login" style="background-color: #667eea; color: white; padding: 14px 28px;
//                           text-decoration: none; border-radius: 6px; display: inline-block; margin: 16px 0;
//                           font-weight: bold;">
//                           Start Exploring
//                        </a>
//                    </div>
//                    <div style="background: #2c3e50; padding: 16px; text-align: center;">
//                        <p style="color: #bdc3c7; margin: 0; font-size: 12px;">
//                            © 2024 %s. All rights reserved.
//                        </p>
//                    </div>
//                </div>
//                """.formatted(appName, user.getFullName(), user.getEmail(), frontendUrl, appName);
//    }
//}
package com.example.service.impl;

import com.example.entity.Booking;
import com.example.entity.Payment;
import com.example.entity.User;
import com.example.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name}")
    private String appName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Async("emailExecutor")
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to " + appName + "!";
        String body = buildWelcomeEmailBody(user);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendEmailVerificationEmail(User user, String token) {
        String subject = "Verify Your Email - " + appName;
        // बदल: इथे .html जोडले आहे जेणेकरून static फोल्डरमधील फाईल मॅच होईल
        String verifyLink = frontendUrl + "/verify-email.html?token=" + token;
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Email Verification</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Please verify your email address by clicking the button below:</p>
                    <a href="%s" style="background-color: #3498db; color: white; padding: 12px 24px;
                       text-decoration: none; border-radius: 4px; display: inline-block; margin: 16px 0;">
                       Verify Email
                    </a>
                    <p>This link expires in 24 hours.</p>
                    <p>If you didn't create an account, please ignore this email.</p>
                    <hr/>
                    <p style="color: #7f8c8d; font-size: 12px;">%s | support@grandhotel.com</p>
                </div>
                """.formatted(user.getFullName(), verifyLink, appName);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Password Reset Request - " + appName;
        String resetLink = frontendUrl + "/reset-password.html?token=" + token;
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #e74c3c;">Password Reset</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>We received a request to reset your password. Click below to proceed:</p>
                    <a href="%s" style="background-color: #e74c3c; color: white; padding: 12px 24px;
                       text-decoration: none; border-radius: 4px; display: inline-block; margin: 16px 0;">
                       Reset Password
                    </a>
                    <p>This link expires in <strong>1 hour</strong>.</p>
                    <p>If you did not request a password reset, please ignore this email or contact support immediately.</p>
                    <hr/>
                    <p style="color: #7f8c8d; font-size: 12px;">%s | support@grandhotel.com</p>
                </div>
                """.formatted(user.getFullName(), resetLink, appName);
        sendHtmlEmail(user.getEmail(), subject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendBookingConfirmationEmail(Booking booking) {
        String subject = "Booking Confirmed - " + booking.getBookingReference() + " | " + appName;
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #27ae60; padding: 20px; text-align: center;">
                        <h1 style="color: white; margin: 0;">Booking Confirmed!</h1>
                    </div>
                    <div style="padding: 24px; background: #f9f9f9;">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Your booking has been confirmed. Here are your details:</p>
                        <table style="width: 100%%; border-collapse: collapse; background: white; border-radius: 8px;">
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 12px; font-weight: bold;">Booking Reference</td>
                                <td style="padding: 12px; color: #27ae60; font-weight: bold;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; font-weight: bold;">Room</td>
                                <td style="padding: 12px;">%s (%s)</td>
                            </tr>
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 12px; font-weight: bold;">Check-In</td>
                                <td style="padding: 12px;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; font-weight: bold;">Check-Out</td>
                                <td style="padding: 12px;">%s</td>
                            </tr>
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 12px; font-weight: bold;">Nights</td>
                                <td style="padding: 12px;">%d nights</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; font-weight: bold;">Guests</td>
                                <td style="padding: 12px;">%d</td>
                            </tr>
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 12px; font-weight: bold;">Total Amount</td>
                                <td style="padding: 12px; font-size: 18px; color: #27ae60;">
                                    ₹%s
                                </td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; font-weight: bold;">Balance Due</td>
                                <td style="padding: 12px; color: #e74c3c;">₹%s</td>
                            </tr>
                        </table>
                        %s
                        <p style="margin-top: 20px;">We look forward to welcoming you!</p>
                    </div>
                    <div style="background: #2c3e50; padding: 16px; text-align: center;">
                        <p style="color: white; margin: 0;">%s | support@grandhotel.com</p>
                    </div>
                </div>
                """.formatted(
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getRoom().getRoomNumber(),
                booking.getRoom().getRoomType(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfNights(),
                booking.getNumberOfGuests(),
                booking.getTotalAmount(),
                booking.getBalanceAmount(),
                booking.getSpecialRequests() != null ?
                        "<p><strong>Special Requests:</strong> " + booking.getSpecialRequests() + "</p>" : "",
                appName
        );
        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendBookingCancellationEmail(Booking booking) {
        String subject = "Booking Cancelled - " + booking.getBookingReference() + " | " + appName;
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #e74c3c; padding: 20px; text-align: center;">
                        <h1 style="color: white; margin: 0;">Booking Cancelled</h1>
                    </div>
                    <div style="padding: 24px;">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Your booking <strong>%s</strong> has been cancelled.</p>
                        %s
                        <p>If you have any questions or this was a mistake, please contact our support team.</p>
                        <p>Email: support@grandhotel.com</p>
                    </div>
                    <div style="background: #2c3e50; padding: 16px; text-align: center;">
                        <p style="color: white; margin: 0;">%s</p>
                    </div>
                </div>
                """.formatted(
                booking.getUser().getFullName(),
                booking.getBookingReference(),
                booking.getCancellationReason() != null ?
                        "<p><strong>Reason:</strong> " + booking.getCancellationReason() + "</p>" : "",
                appName
        );
        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendCheckInEmail(Booking booking) {
        String subject = "Welcome! Check-In Successful - " + booking.getBookingReference();
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #3498db; padding: 20px; text-align: center;">
                        <h1 style="color: white; margin: 0;">Welcome to %s!</h1>
                    </div>
                    <div style="padding: 24px;">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>You have successfully checked into Room <strong>%s</strong>.</p>
                        <p><strong>Check-out Date:</strong> %s</p>
                        <p>Enjoy your stay! For any assistance, dial <strong>0</strong> from your room phone.</p>
                    </div>
                </div>
                """.formatted(appName, booking.getUser().getFullName(),
                booking.getRoom().getRoomNumber(), booking.getCheckOutDate());
        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendCheckOutEmail(Booking booking) {
        String subject = "Thank You! Check-Out Complete - " + booking.getBookingReference();
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #9b59b6; padding: 20px; text-align: center;">
                        <h1 style="color: white; margin: 0;">Thank You for Your Stay!</h1>
                    </div>
                    <div style="padding: 24px;">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>You have successfully checked out from <strong>%s</strong>.</p>
                        <p>We hope you had a wonderful stay. We'd love to see you again!</p>
                        <p>Please share your experience with us by leaving a review.</p>
                    </div>
                </div>
                """.formatted(booking.getUser().getFullName(), appName);
        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendPaymentConfirmationEmail(Payment payment) {
        Booking booking = payment.getBooking();
        String guestName = booking.getUser().getFullName();
        String subject = "Payment Confirmed — Booking " + booking.getBookingReference() + " | " + appName;

        // Balance after this payment
        String balanceStr = booking.getBalanceAmount() != null
                ? "₹" + booking.getBalanceAmount()
                : "₹0";
        String balanceColor = (booking.getBalanceAmount() != null
                && booking.getBalanceAmount().compareTo(java.math.BigDecimal.ZERO) > 0)
                ? "#e74c3c" : "#27ae60";

        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 620px; margin: 0 auto; border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden;">

                    <!-- Header -->
                    <div style="background-color: #27ae60; padding: 24px 20px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 22px;">✅ Payment Successful</h1>
                        <p style="color: rgba(255,255,255,0.9); margin: 6px 0 0;">Thank you, <strong>%s</strong>!</p>
                    </div>

                    <!-- Payment Details -->
                    <div style="padding: 24px; background: #f9fdf9;">
                        <h3 style="color: #1a1a1a; margin: 0 0 12px; font-size: 15px; border-bottom: 2px solid #27ae60; padding-bottom: 8px;">
                            💳 Payment Details
                        </h3>
                        <table style="width: 100%%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,.06);">
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Transaction ID</td>
                                <td style="padding: 11px 14px; font-size: 13px; color: #1a1a1a; font-family: monospace;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Amount Paid</td>
                                <td style="padding: 11px 14px; font-size: 18px; font-weight: 700; color: #27ae60;">₹%s</td>
                            </tr>
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Payment Method</td>
                                <td style="padding: 11px 14px; font-size: 13px;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Payment Date</td>
                                <td style="padding: 11px 14px; font-size: 13px;">%s</td>
                            </tr>
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Balance Remaining</td>
                                <td style="padding: 11px 14px; font-size: 14px; font-weight: 700; color: %s;">%s</td>
                            </tr>
                        </table>
                    </div>

                    <!-- Booking Details -->
                    <div style="padding: 0 24px 24px; background: #fff;">
                        <h3 style="color: #1a1a1a; margin: 0 0 12px; font-size: 15px; border-bottom: 2px solid #3498db; padding-bottom: 8px;">
                            🏨 Booking Details
                        </h3>
                        <table style="width: 100%%; border-collapse: collapse; background: #f8fafc; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,.06);">
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Booking Reference</td>
                                <td style="padding: 11px 14px; font-size: 13px; font-weight: 700; color: #27ae60;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Room</td>
                                <td style="padding: 11px 14px; font-size: 13px;">Room %s — %s</td>
                            </tr>
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Check-In</td>
                                <td style="padding: 11px 14px; font-size: 13px;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Check-Out</td>
                                <td style="padding: 11px 14px; font-size: 13px;">%s</td>
                            </tr>
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Duration</td>
                                <td style="padding: 11px 14px; font-size: 13px;">%d night%s · %d guest%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Total Booking Amount</td>
                                <td style="padding: 11px 14px; font-size: 15px; font-weight: 700; color: #1a1a1a;">₹%s</td>
                            </tr>
                            <tr style="background: #ecf0f1;">
                                <td style="padding: 11px 14px; font-weight: 600; font-size: 13px; color: #555;">Booking Status</td>
                                <td style="padding: 11px 14px; font-size: 13px; font-weight: 600; color: #27ae60;">%s</td>
                            </tr>
                        </table>
                    </div>

                    <!-- Footer -->
                    <div style="background: #2c3e50; padding: 16px; text-align: center;">
                        <p style="color: #bdc3c7; margin: 0; font-size: 12px;">%s | support@grandhotel.com</p>
                    </div>
                </div>
                """.formatted(
                // Header
                guestName,
                // Payment section
                payment.getTransactionId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentDate(),
                balanceColor, balanceStr,
                // Booking section
                booking.getBookingReference(),
                booking.getRoom().getRoomNumber(),
                booking.getRoom().getRoomType(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfNights(),
                booking.getNumberOfNights() == 1 ? "" : "s",
                booking.getNumberOfGuests(),
                booking.getNumberOfGuests() == 1 ? "" : "s",
                booking.getTotalAmount(),
                booking.getBookingStatus(),
                // Footer
                appName
        );
        sendHtmlEmail(booking.getUser().getEmail(), subject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendEnquiryAcknowledgementEmail(String email, String name, String subject) {
        String emailSubject = "Enquiry Received - " + appName;
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Thank you for contacting us!</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>We have received your enquiry regarding: <strong>%s</strong></p>
                    <p>Our team will respond within <strong>24–48 hours</strong>.</p>
                    <p>For urgent matters, call us at <strong>+91 1800 000 0000</strong>.</p>
                    <hr/>
                    <p style="color: #7f8c8d; font-size: 12px;">%s</p>
                </div>
                """.formatted(name, subject, appName);
        sendHtmlEmail(email, emailSubject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendEnquiryResponseEmail(String email, String name, String subject, String response) {
        String emailSubject = "Response to Your Enquiry - " + appName;
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Enquiry Response</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Regarding your enquiry: <strong>%s</strong></p>
                    <div style="background: #f8f9fa; padding: 16px; border-left: 4px solid #3498db;
                                border-radius: 4px; margin: 16px 0;">
                        <p style="margin: 0;">%s</p>
                    </div>
                    <p>If you have further questions, don't hesitate to reach out.</p>
                    <hr/>
                    <p style="color: #7f8c8d; font-size: 12px;">%s | support@grandhotel.com</p>
                </div>
                """.formatted(name, subject, response, appName);
        sendHtmlEmail(email, emailSubject, body);
    }

    @Override
    @Async("emailExecutor")
    public void sendSimpleEmail(String to, String subject, String body) {
        sendHtmlEmail(to, subject, "<pre>" + body + "</pre>");
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, appName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to: {} | Subject: {}", to, subject);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildWelcomeEmailBody(User user) {
        // बदल: इथे /login बदलून /login.html केले आहे
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                                padding: 40px 20px; text-align: center;">
                        <h1 style="color: white; margin: 0;">Welcome to %s!</h1>
                        <p style="color: rgba(255,255,255,0.9);">Your luxury stay begins here</p>
                    </div>
                    <div style="padding: 32px 24px; background: #ffffff;">
                        <h2 style="color: #2c3e50;">Hello, %s! 🎉</h2>
                        <p>Your account has been created successfully.</p>
                        <div style="background: #f8f9fa; border-radius: 8px; padding: 20px; margin: 20px 0;">
                            <p><strong>Account Details:</strong></p>
                            <p>📧 Email: %s</p>
                            <p>👤 Role: Customer</p>
                        </div>
                        <p>With your account you can:</p>
                        <ul>
                            <li>Browse and book rooms</li>
                            <li>Manage your reservations</li>
                            <li>Request hotel services</li>
                            <li>View payment history</li>
                            <li>Submit enquiries</li>
                        </ul>
                        <a href="%s/login.html" style="background-color: #667eea; color: white; padding: 14px 28px;
                           text-decoration: none; border-radius: 6px; display: inline-block; margin: 16px 0;
                           font-weight: bold;">
                            Start Exploring
                        </a>
                    </div>
                    <div style="background: #2c3e50; padding: 16px; text-align: center;">
                        <p style="color: #bdc3c7; margin: 0; font-size: 12px;">
                            © 2024 %s. All rights reserved.
                        </p>
                    </div>
                </div>
                """.formatted(appName, user.getFullName(), user.getEmail(), frontendUrl, appName);
    }
}