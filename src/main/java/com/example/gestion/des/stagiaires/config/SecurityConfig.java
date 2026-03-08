package com.example.gestion.des.stagiaires.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**").permitAll()
                                                // Endpoints publics pour le portail candidats
                                                .requestMatchers("/api/public/**").permitAll()
                                                .requestMatchers("/api/universites/**").permitAll()
                                                .requestMatchers("/api/candidatures/**").permitAll()
                                                // Lecture seule autorisée pour AGENT_RH
                                                .requestMatchers(HttpMethod.GET, "/api/admin/departements",
                                                                "/api/admin/departements/**")
                                                .hasAnyRole("ADMIN", "AGENT_RH")
                                                .requestMatchers(HttpMethod.GET, "/api/admin/specialites",
                                                                "/api/admin/specialites/**")
                                                .hasAnyRole("ADMIN", "AGENT_RH")
                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/admin/specialites-universitaires",
                                                                "/api/admin/specialites-universitaires/**")
                                                .hasAnyRole("ADMIN", "AGENT_RH")
                                                .requestMatchers(HttpMethod.GET, "/api/admin/competences",
                                                                "/api/admin/competences/**")
                                                .hasAnyRole("ADMIN", "AGENT_RH")
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/agent-rh/**").hasAnyRole("ADMIN", "AGENT_RH")
                                                .requestMatchers("/api/encadrant/**").hasAnyRole("ADMIN", "ENCADRANT")
                                                .requestMatchers("/api/stagiaire/**").hasAnyRole("ADMIN", "STAGIAIRE")
                                                .requestMatchers("/api/sujets-pfe/**").authenticated()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(List.of("http://localhost:*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
                configuration.setExposedHeaders(List.of("Authorization"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
