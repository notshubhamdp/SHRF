package com.SRHF.SRHF.config;

import com.SRHF.SRHF.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;


    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(req -> req
                    .requestMatchers("/register/**", "/login", "/forgot-password/**", "/css/**", "/js/**").permitAll()
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // our login form will submit the email field; treat it as the username
                        .usernameParameter("email")

                        .permitAll()
                )
                .logout(logout -> logout.permitAll())
                .userDetailsService(userDetailsService);

        return http.build();
    }

}
