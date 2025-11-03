package com.example.Backend.security;

import com.example.Backend.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user info from Google OAuth2 response
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Generate JWT token
        String accessToken = jwtTokenProvider.generateToken(email);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        // Create response
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .email(email)
                .name(name)
                .build();

        // Redirect to frontend with tokens
        String redirectUrl = String.format(
                "http://localhost:4200/oauth2/redirect?token=%s&refreshToken=%s&email=%s&name=%s",
                accessToken, refreshToken, email, name);

        // For API response (if frontend makes direct request)
        if (request.getParameter("mode") != null && request.getParameter("mode").equals("json")) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
        } else {
            // Redirect to frontend
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        }
    }
}
