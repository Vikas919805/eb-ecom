package com.ecommerce.project.security.oauth;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = oAuth2User.getAttribute("email");

            System.out.println("OAuth2 email: " + email);

            if (email == null || email.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found from OAuth2 provider");
                return;
            }

            User user = userRepository.findByEmail(email).orElse(null);

            System.out.println("User from DB: " + user);

            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setUserName(email);
                user.setPassword("OAUTH2_USER");

                Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

                user.setRoles(Collections.singleton(userRole));
                userRepository.save(user);

                System.out.println("New OAuth user saved");
            }

            ResponseCookie jwtCookie = jwtUtils.generateJwtCookieFromUsername(user.getUserName());

            System.out.println("JWT cookie created");

            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
            response.setContentType("text/plain");
            response.getWriter().write("Google login success: " + user.getUserName());
            response.getWriter().flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/plain");
            response.getWriter().write("OAuth2 error: " + e.getMessage());
            response.getWriter().flush();
        }
    }
}