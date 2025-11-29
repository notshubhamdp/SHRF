package com.SRHF.SRHF.config;

import com.SRHF.SRHF.entity.User;
import com.SRHF.SRHF.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        User user = userRepository.findByemail(email).orElse(null);

        if (user != null && (user.getRole() == null || user.getRole().isEmpty())) {
            // Redirect to role selection if role not set
            response.sendRedirect("/role-selection");
        } else {
            // Redirect to home if role is already set
            response.sendRedirect("/home");
        }
    }
}
