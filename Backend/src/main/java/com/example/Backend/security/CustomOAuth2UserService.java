package com.example.Backend.security;

import com.example.Backend.entity.Role;
import com.example.Backend.entity.User;
import com.example.Backend.repository.RoleRepository;
import com.example.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Extract user info from OAuth2 provider (Google)
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Check if user exists, if not create new user
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            // Get CLIENT role
            Role clientRole = roleRepository.findByName("CLIENT")
                    .orElseThrow(() -> new RuntimeException("CLIENT role not found"));

            // Create new user from OAuth2 data
            User newUser = new User();
            newUser.setUsername(email.split("@")[0]); // Use email prefix as username
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password
            newUser.setRole(clientRole);

            userRepository.save(newUser);
        } else {
            // Update existing user info if needed
            User existingUser = userOptional.get();
            if (existingUser.getFullName() == null || existingUser.getFullName().isEmpty()) {
                existingUser.setFullName(name);
                userRepository.save(existingUser);
            }
        }

        return oAuth2User;
    }
}
