package com.backend.ripple.auth.config

import com.backend.ripple.auth.filter.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity , jwtAuthFilter : JwtFilter): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
    return http.build();
    }
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}