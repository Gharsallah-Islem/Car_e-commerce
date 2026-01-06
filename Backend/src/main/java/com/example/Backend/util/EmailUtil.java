package com.example.Backend.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailUtil {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(fromEmail); // Use configured email

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail); // Use configured email

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmation(String to, String orderNumber, String totalAmount) {
        String subject = "Order Confirmation - " + orderNumber;
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Order Confirmation</h2>
                    <p>Thank you for your order!</p>
                    <p><strong>Order Number:</strong> %s</p>
                    <p><strong>Total Amount:</strong> %s TND</p>
                    <p>We'll notify you when your order ships.</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Spare Parts Team</p>
                </body>
                </html>
                """, orderNumber, totalAmount);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdate(String to, String orderNumber, String status) {
        String subject = "Order Update - " + orderNumber;
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Order Status Update</h2>
                    <p>Your order status has been updated:</p>
                    <p><strong>Order Number:</strong> %s</p>
                    <p><strong>New Status:</strong> %s</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Spare Parts Team</p>
                </body>
                </html>
                """, orderNumber, status);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send delivery tracking email
     */
    public void sendDeliveryTracking(String to, String orderNumber, String trackingNumber) {
        String subject = "Your Order Has Shipped - " + orderNumber;
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Your Order Has Shipped!</h2>
                    <p><strong>Order Number:</strong> %s</p>
                    <p><strong>Tracking Number:</strong> %s</p>
                    <p>Your order is on its way! Track your delivery using the tracking number above.</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Spare Parts Team</p>
                </body>
                </html>
                """, orderNumber, trackingNumber);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Password Reset Request";
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Password Reset Request</h2>
                    <p>You have requested to reset your password.</p>
                    <p>Click the link below to reset your password:</p>
                    <a href="http://localhost:3000/reset-password?token=%s">Reset Password</a>
                    <p>If you didn't request this, please ignore this email.</p>
                    <p>This link will expire in 24 hours.</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Spare Parts Team</p>
                </body>
                </html>
                """, resetToken);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Send welcome email
     */
    public void sendWelcomeEmail(String to, String username) {
        String subject = "Welcome to E-Commerce Spare Parts!";
        String htmlContent = String.format("""
                <html>
                <body>
                    <h2>Welcome, %s!</h2>
                    <p>Thank you for registering with E-Commerce Spare Parts.</p>
                    <p>You can now start shopping for quality spare parts for your vehicle.</p>
                    <p>If you have any questions, feel free to contact our support team.</p>
                    <br>
                    <p>Best regards,<br>E-Commerce Spare Parts Team</p>
                </body>
                </html>
                """, username);

        sendHtmlEmail(to, subject, htmlContent);
    }
}
