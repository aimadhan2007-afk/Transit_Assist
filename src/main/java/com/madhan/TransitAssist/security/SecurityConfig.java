package com.madhan.TransitAssist.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler, 
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.successHandler = successHandler;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/api/**"),
                    new AntPathRequestMatcher("/h2-console/**")
                )
            )
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .authorizeHttpRequests(auth -> auth
                // Static resources & public pages
                .requestMatchers(
                    "/", "/index.html", "/login", "/register", "/access-denied", "/error",
                    "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico",
                    "/h2-console/**"
                ).permitAll()

                // Public Auth API
                .requestMatchers("/api/auth/**").permitAll()

                // Admin-only endpoints
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                // Staff & Admin endpoints
                .requestMatchers("/staff/**", "/api/staff/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/helpers/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/helpers/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/helpers/**").hasAnyRole("STAFF", "ADMIN")

                // Authenticated user pages & APIs
                .requestMatchers(
                    "/dashboard/**", "/requests/**", "/profile/**", 
                    "/api/requests/**", "/api/helpers/**"
                ).authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .httpBasic(Customizer.withDefaults())
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
            );

        return http.build();
    }
}
