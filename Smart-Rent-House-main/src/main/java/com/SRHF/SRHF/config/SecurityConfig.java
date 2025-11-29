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
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService, 
                         CustomAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(req -> req
                    .requestMatchers("/register/**", "/login", "/forgot-password/**", "/css/**", "/js/**", "/images/**").permitAll()
                    .requestMatchers("/role-selection/**").authenticated()
                    .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // our login form will submit the email field; treat it as the username
                        .usernameParameter("email")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll())
                .userDetailsService(userDetailsService);

        return http.build();
    }

}
