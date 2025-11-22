package com.example.Backend.service;

import com.example.Backend.entity.Order;
import com.example.Backend.entity.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Send email verification code to user
     */
    public void sendVerificationEmail(String toEmail, String username, String verificationCode) {
        try {
            String htmlContent = generateVerificationEmailHtml(username, verificationCode);

            sendHtmlEmail(
                    toEmail,
                    "Verify Your Email - Car Parts Store",
                    htmlContent);

            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send order confirmation email with professional template
     */
    public void sendOrderConfirmationEmail(Order order) {
        try {
            log.info("Starting order confirmation email generation for order: {}", order.getId());
            
            // Validate order has required data
            if (order.getUser() == null) {
                log.error("Order {} has no user associated", order.getId());
                return;
            }
            
            if (order.getUser().getEmail() == null || order.getUser().getEmail().trim().isEmpty()) {
                log.error("User {} has no email address", order.getUser().getId());
                return;
            }
            
            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                log.warn("Order {} has no order items, email will be sent with empty items list", order.getId());
            }
            
            String customerName = order.getUser().getFullName();
            if (customerName == null || customerName.trim().isEmpty()) {
                customerName = order.getUser().getUsername();
            }
            
            log.info("Generating HTML content for order: {}", order.getId());
            String subject = "Order Confirmation #" + order.getId() + " - Car Parts Store";
            
            String htmlContent;
            try {
                htmlContent = generateOrderConfirmationEmailHtml(order, customerName);
                log.info("HTML content generated successfully, length: {} characters", htmlContent.length());
            } catch (Exception e) {
                log.error("Error generating HTML content: {}", e.getMessage(), e);
                // Fallback to simple email if template generation fails
                htmlContent = generateSimpleOrderConfirmationHtml(order, customerName);
            }
            
            log.info("Sending email to: {} for order: {}", order.getUser().getEmail(), order.getId());
            sendHtmlEmail(order.getUser().getEmail(), subject, htmlContent);
            log.info("Order confirmation email sent successfully to: {} for order: {}", order.getUser().getEmail(), order.getId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", order.getId(), e.getMessage(), e);
            // Re-throw to see the error in logs, but don't fail the order creation
        }
    }

    /**
     * Send password reset code to user
     */
    public void sendPasswordResetEmail(String toEmail, String username, String resetCode) {
        try {
            String htmlContent = generatePasswordResetEmailHtml(username, resetCode);

            sendHtmlEmail(
                    toEmail,
                    "Reset Your Password - Car Parts Store",
                    htmlContent);

            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Generate modern HTML template for email verification
     */
    private String generateVerificationEmailHtml(String username, String verificationCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Email Verification</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fa;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td align="center" style="padding: 40px 0;">
                                <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 20px 40px; text-align: center; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); border-radius: 8px 8px 0 0;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;">
                                                🚗 Car Parts Store
                                            </h1>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 20px 0; color: #333333; font-size: 24px; font-weight: 600;">
                                                Welcome, %s! 👋
                                            </h2>
                                            <p style="margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                Thank you for signing up! To complete your registration and start shopping for quality car parts, please verify your email address.
                                            </p>

                                            <!-- Verification Code Box -->
                                            <div style="background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 30px 0; border-radius: 4px;">
                                                <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px; text-transform: uppercase; letter-spacing: 1px;">
                                                    Your Verification Code
                                                </p>
                                                <p style="margin: 0; color: #333333; font-size: 32px; font-weight: 700; letter-spacing: 4px; font-family: 'Courier New', monospace;">
                                                    %s
                                                </p>
                                            </div>

                                            <p style="margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                Enter this code in the verification page to activate your account.
                                            </p>

                                            <p style="margin: 0 0 20px 0; color: #999999; font-size: 14px; line-height: 1.6;">
                                                ⏱️ This code will expire in <strong>15 minutes</strong>.
                                            </p>

                                            <!-- CTA Button -->
                                            <div style="text-align: center; margin: 30px 0;">
                                                <a href="%s/verify-email" style="display: inline-block; padding: 14px 40px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; text-decoration: none; border-radius: 6px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 6px rgba(102, 126, 234, 0.4);">
                                                    Verify Email Address
                                                </a>
                                            </div>

                                            <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">

                                            <p style="margin: 0; color: #999999; font-size: 14px; line-height: 1.6;">
                                                If you didn't create an account with us, please ignore this email or contact our support team.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 8px 8px; text-align: center;">
                                            <p style="margin: 0 0 10px 0; color: #999999; font-size: 14px;">
                                                © 2025 Car Parts Store. All rights reserved.
                                            </p>
                                            <p style="margin: 0; color: #999999; font-size: 12px;">
                                                This is an automated email. Please do not reply.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(username, verificationCode, frontendUrl);
    }

    /**
     * Generate modern HTML template for password reset
     */
    private String generatePasswordResetEmailHtml(String username, String resetCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7fa;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                        <tr>
                            <td align="center" style="padding: 40px 0;">
                                <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 40px 40px 20px 40px; text-align: center; background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); border-radius: 8px 8px 0 0;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;">
                                                🔐 Password Reset Request
                                            </h1>
                                        </td>
                                    </tr>

                                    <!-- Content -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="margin: 0 0 20px 0; color: #333333; font-size: 24px; font-weight: 600;">
                                                Hello, %s
                                            </h2>
                                            <p style="margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                We received a request to reset your password. Use the code below to create a new password for your account.
                                            </p>

                                            <!-- Reset Code Box -->
                                            <div style="background-color: #fff5f5; border-left: 4px solid #f5576c; padding: 20px; margin: 30px 0; border-radius: 4px;">
                                                <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px; text-transform: uppercase; letter-spacing: 1px;">
                                                    Your Reset Code
                                                </p>
                                                <p style="margin: 0; color: #333333; font-size: 32px; font-weight: 700; letter-spacing: 4px; font-family: 'Courier New', monospace;">
                                                    %s
                                                </p>
                                            </div>

                                            <p style="margin: 0 0 20px 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                                Enter this code on the password reset page to set your new password.
                                            </p>

                                            <p style="margin: 0 0 20px 0; color: #999999; font-size: 14px; line-height: 1.6;">
                                                ⏱️ This code will expire in <strong>15 minutes</strong>.
                                            </p>

                                            <!-- CTA Button -->
                                            <div style="text-align: center; margin: 30px 0;">
                                                <a href="%s/reset-password" style="display: inline-block; padding: 14px 40px; background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: #ffffff; text-decoration: none; border-radius: 6px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 6px rgba(245, 87, 108, 0.4);">
                                                    Reset Password
                                                </a>
                                            </div>

                                            <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">

                                            <div style="background-color: #fff8e1; border-left: 4px solid #ffa726; padding: 15px; margin: 20px 0; border-radius: 4px;">
                                                <p style="margin: 0; color: #e65100; font-size: 14px; line-height: 1.6;">
                                                    <strong>⚠️ Security Notice:</strong> If you didn't request this password reset, please ignore this email and your password will remain unchanged. Consider changing your password if you think your account may be compromised.
                                                </p>
                                            </div>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f8f9fa; border-radius: 0 0 8px 8px; text-align: center;">
                                            <p style="margin: 0 0 10px 0; color: #999999; font-size: 14px;">
                                                © 2025 Car Parts Store. All rights reserved.
                                            </p>
                                            <p style="margin: 0; color: #999999; font-size: 12px;">
                                                This is an automated email. Please do not reply.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(username, resetCode, frontendUrl);
    }

    /**
     * Generate professional HTML template for order confirmation email
     */
    private String generateOrderConfirmationEmailHtml(Order order, String customerName) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH);
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            String orderDate = order.getCreatedAt() != null ? order.getCreatedAt().format(dateFormatter) : "N/A";
            String orderTime = order.getCreatedAt() != null ? order.getCreatedAt().format(timeFormatter) : "N/A";
            
            // Calculate order totals - handle null/empty order items
            BigDecimal subtotal = BigDecimal.ZERO;
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                subtotal = order.getOrderItems().stream()
                        .filter(item -> item != null && item.getPrice() != null && item.getQuantity() != null)
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            
            BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.20)); // 20% VAT
            BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(500)) > 0 
                    ? BigDecimal.ZERO 
                    : BigDecimal.valueOf(30);
            BigDecimal total = order.getTotalPrice() != null ? order.getTotalPrice() : subtotal.add(tax).add(shipping);
            
            // Build order items HTML
            StringBuilder orderItemsHtml = new StringBuilder();
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item == null || item.getProduct() == null) {
                        continue; // Skip invalid items
                    }
                    String productName = item.getProduct().getName();
                    String productImage = item.getProduct().getImageUrl() != null && !item.getProduct().getImageUrl().isEmpty()
                            ? item.getProduct().getImageUrl()
                            : "https://via.placeholder.com/80x80?text=No+Image";
                    String itemPrice = String.format("%.2f", item.getPrice().setScale(2, RoundingMode.HALF_UP));
                    String itemSubtotal = String.format("%.2f", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP));
                    int quantity = item.getQuantity();
                    
                    orderItemsHtml.append(String.format("""
                    <tr>
                        <td style="padding: 20px; border-bottom: 1px solid #eeeeee;">
                            <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                <tr>
                                    <td style="width: 80px; padding-right: 15px; vertical-align: top;">
                                        <img src="%s" alt="%s" style="width: 80px; height: 80px; object-fit: cover; border-radius: 8px; border: 1px solid #eeeeee;">
                                    </td>
                                    <td style="vertical-align: top;">
                                        <h3 style="margin: 0 0 8px 0; color: #333333; font-size: 16px; font-weight: 600;">
                                            %s
                                        </h3>
                                        <p style="margin: 0 0 8px 0; color: #666666; font-size: 14px;">
                                            Quantity: <strong>%d</strong>
                                        </p>
                                        <p style="margin: 0; color: #999999; font-size: 13px;">
                                            Unit Price: %s MAD
                                        </p>
                                    </td>
                                    <td style="text-align: right; vertical-align: top;">
                                        <p style="margin: 0; color: #2196f3; font-size: 18px; font-weight: 700;">
                                            %s MAD
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    """, productImage, productName, productName, quantity, itemPrice, itemSubtotal));
                }
            } else {
                // Show message if no items
                orderItemsHtml.append("""
                    <tr>
                        <td style="padding: 20px; text-align: center; color: #999999;">
                            No items in this order.
                        </td>
                        </tr>
                        """);
            }
            
            // Payment method display
            String paymentMethodDisplay = order.getPaymentMethod() != null && "CASH_ON_DELIVERY".equals(order.getPaymentMethod()) 
                    ? "Cash on Delivery" 
                    : "Credit Card (Stripe)";
            String paymentStatusDisplay = order.getPaymentStatus() != null && "COMPLETED".equals(order.getPaymentStatus()) 
                    ? "Paid" 
                    : "Pending";
            
            String deliveryAddress = order.getDeliveryAddress() != null 
                    ? order.getDeliveryAddress().replace(",", "<br>") 
                    : "Not specified";
            
            return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Order Confirmation</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #f5f5f5;">
                        <tr>
                            <td align="center" style="padding: 40px 20px;">
                                <table role="presentation" style="max-width: 600px; width: 100%%; border-collapse: collapse; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); overflow: hidden;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 40px 30px 40px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700; letter-spacing: -0.5px;">
                                                🚗 Car Parts Store
                                            </h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Success Icon -->
                                    <tr>
                                        <td style="padding: 40px 40px 20px 40px; text-align: center;">
                                            <div style="width: 80px; height: 80px; background: linear-gradient(135deg, #4caf50 0%%, #45a049 100%%); border-radius: 50%%; margin: 0 auto; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);">
                                                <span style="font-size: 40px;">✓</span>
                                            </div>
                                            <h2 style="margin: 20px 0 10px 0; color: #333333; font-size: 24px; font-weight: 700;">
                                                Order Confirmed!
                                            </h2>
                                            <p style="margin: 0; color: #666666; font-size: 16px;">
                                                Thank you for your purchase, %s!
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Order Details Card -->
                                    <tr>
                                        <td style="padding: 0 40px 30px 40px;">
                                            <div style="background: linear-gradient(135deg, #f8f9fa 0%%, #e9ecef 100%%); border-radius: 8px; padding: 25px; border-left: 4px solid #667eea;">
                                                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 5px 0;">
                                                            <p style="margin: 0; color: #666666; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px;">Order Number</p>
                                                            <p style="margin: 5px 0 0 0; color: #333333; font-size: 20px; font-weight: 700; font-family: 'Courier New', monospace;">#%s</p>
                                                        </td>
                                                        <td style="text-align: right; padding: 5px 0;">
                                                            <p style="margin: 0; color: #666666; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px;">Order Date</p>
                                                            <p style="margin: 5px 0 0 0; color: #333333; font-size: 16px; font-weight: 600;">%s</p>
                                                            <p style="margin: 2px 0 0 0; color: #999999; font-size: 13px;">%s</p>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </td>
                                    </tr>
                                    
                                    <!-- Order Items -->
                                    <tr>
                                        <td style="padding: 0 40px 30px 40px;">
                                            <h3 style="margin: 0 0 20px 0; color: #333333; font-size: 18px; font-weight: 600; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px;">
                                                Order Items
                                            </h3>
                                            <table role="presentation" style="width: 100%%; border-collapse: collapse; background-color: #ffffff;">
                                                %s
                                            </table>
                                        </td>
                                    </tr>
                                    
                                    <!-- Order Summary -->
                                    <tr>
                                        <td style="padding: 0 40px 30px 40px;">
                                            <div style="background-color: #f8f9fa; border-radius: 8px; padding: 25px;">
                                                <h3 style="margin: 0 0 20px 0; color: #333333; font-size: 18px; font-weight: 600;">
                                                    Order Summary
                                                </h3>
                                                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                                    <tr>
                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px;">Subtotal</td>
                                                        <td style="text-align: right; padding: 8px 0; color: #333333; font-size: 14px; font-weight: 500;">%s MAD</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px;">VAT (20%%)</td>
                                                        <td style="text-align: right; padding: 8px 0; color: #333333; font-size: 14px; font-weight: 500;">%s MAD</td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 8px 0; color: #666666; font-size: 14px;">Shipping</td>
                                                        <td style="text-align: right; padding: 8px 0; color: #333333; font-size: 14px; font-weight: 500;">
                                                            %s
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td colspan="2" style="padding: 15px 0 0 0; border-top: 2px solid #e0e0e0;"></td>
                                                    </tr>
                                                    <tr>
                                                        <td style="padding: 15px 0 0 0; color: #333333; font-size: 18px; font-weight: 700;">Total</td>
                                                        <td style="text-align: right; padding: 15px 0 0 0; color: #2196f3; font-size: 24px; font-weight: 700;">%s MAD</td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </td>
                                    </tr>
                                    
                                    <!-- Shipping & Payment Info -->
                                    <tr>
                                        <td style="padding: 0 40px 30px 40px;">
                                            <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                                                <tr>
                                                    <td style="width: 50%%; padding-right: 15px; vertical-align: top;">
                                                        <div style="background-color: #f8f9fa; border-radius: 8px; padding: 20px;">
                                                            <h4 style="margin: 0 0 12px 0; color: #333333; font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Shipping Address</h4>
                                                            <p style="margin: 0; color: #666666; font-size: 14px; line-height: 1.6;">
                                                                %s
                                                            </p>
                                                        </div>
                                                    </td>
                                                    <td style="width: 50%%; padding-left: 15px; vertical-align: top;">
                                                        <div style="background-color: #f8f9fa; border-radius: 8px; padding: 20px;">
                                                            <h4 style="margin: 0 0 12px 0; color: #333333; font-size: 14px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">Payment Method</h4>
                                                            <p style="margin: 0 0 8px 0; color: #666666; font-size: 14px; font-weight: 500;">
                                                                %s
                                                            </p>
                                                            <p style="margin: 0; color: %s; font-size: 13px; font-weight: 500;">
                                                                Status: %s
                                                            </p>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    
                                    <!-- Next Steps -->
                                    <tr>
                                        <td style="padding: 0 40px 30px 40px;">
                                            <div style="background: linear-gradient(135deg, #e3f2fd 0%%, #bbdefb 100%%); border-radius: 8px; padding: 20px; border-left: 4px solid #2196f3;">
                                                <h4 style="margin: 0 0 12px 0; color: #1976d2; font-size: 16px; font-weight: 600;">
                                                    📦 What's Next?
                                                </h4>
                                                <ul style="margin: 0; padding-left: 20px; color: #666666; font-size: 14px; line-height: 1.8;">
                                                    <li>We'll prepare your order with care</li>
                                                    <li>You'll receive a shipping notification once your order is dispatched</li>
                                                    <li>Estimated delivery: 3-5 business days</li>
                                                </ul>
                                            </div>
                                        </td>
                                    </tr>
                                    
                                    <!-- CTA Button -->
                                    <tr>
                                        <td style="padding: 0 40px 30px 40px; text-align: center;">
                                            <a href="%s/profile/orders/%s" style="display: inline-block; padding: 14px 32px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; text-decoration: none; border-radius: 6px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);">
                                                View Order Details
                                            </a>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 30px 40px; background-color: #f8f9fa; border-top: 1px solid #eeeeee;">
                                            <p style="margin: 0 0 10px 0; color: #999999; font-size: 14px; text-align: center;">
                                                Questions about your order? Contact us at <a href="mailto:support@carpartsstore.com" style="color: #667eea; text-decoration: none;">support@carpartsstore.com</a>
                                            </p>
                                            <p style="margin: 0 0 10px 0; color: #999999; font-size: 12px; text-align: center;">
                                                © 2025 Car Parts Store. All rights reserved.
                                            </p>
                                            <p style="margin: 0; color: #999999; font-size: 11px; text-align: center;">
                                                This is an automated email. Please do not reply.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, 
                customerName,
                order.getId().toString(),
                orderDate,
                orderTime,
                orderItemsHtml.toString(),
                String.format("%.2f", subtotal.setScale(2, RoundingMode.HALF_UP)),
                String.format("%.2f", tax.setScale(2, RoundingMode.HALF_UP)),
                shipping.compareTo(BigDecimal.ZERO) == 0 ? "<span style='color: #4caf50; font-weight: 600;'>FREE</span>" : String.format("%.2f", shipping.setScale(2, RoundingMode.HALF_UP)) + " MAD",
                String.format("%.2f", total.setScale(2, RoundingMode.HALF_UP)),
                deliveryAddress,
                paymentMethodDisplay,
                "COMPLETED".equals(order.getPaymentStatus()) ? "#4caf50" : "#ff9800",
                paymentStatusDisplay,
                frontendUrl,
                order.getId().toString()
            );
        } catch (Exception e) {
            log.error("Error in generateOrderConfirmationEmailHtml: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger fallback
        }
    }

    /**
     * Generate simple fallback HTML template for order confirmation
     */
    private String generateSimpleOrderConfirmationHtml(Order order, String customerName) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Order Confirmation</title>
                </head>
                <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                        <h1 style="color: #333; border-bottom: 2px solid #667eea; padding-bottom: 10px;">Order Confirmed!</h1>
                        <p style="font-size: 16px; color: #666;">Thank you for your purchase, <strong>%s</strong>!</p>
                        
                        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 4px; margin: 20px 0;">
                            <h2 style="color: #333; margin-top: 0;">Order Details</h2>
                            <p><strong>Order ID:</strong> %s</p>
                            <p><strong>Total:</strong> %s MAD</p>
                            <p><strong>Delivery Address:</strong> %s</p>
                            <p><strong>Payment Method:</strong> %s</p>
                            <p><strong>Order Date:</strong> %s</p>
                        </div>
                        
                        <p style="color: #666; font-size: 14px;">We'll notify you when your order ships.</p>
                        
                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #999; font-size: 12px;">
                            <p>© 2025 Car Parts Store. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                customerName,
                order.getId().toString(),
                order.getTotalPrice() != null ? String.format("%.2f", order.getTotalPrice()) : "0.00",
                order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "Not specified",
                order.getPaymentMethod() != null ? order.getPaymentMethod() : "Not specified",
                order.getCreatedAt() != null ? order.getCreatedAt().toString() : "N/A"
        );
    }
}
