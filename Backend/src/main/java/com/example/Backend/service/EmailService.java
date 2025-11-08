package com.example.Backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
}
