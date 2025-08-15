package au.id.ohare.ushort.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Require authentication for admin endpoints only
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                // Allow public access to everything else
                .anyRequest().permitAll()
            )
            .httpBasic(httpBasic -> {}) // Enable HTTP Basic authentication
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API usage
            .formLogin(form -> form.disable()); // Disable form login

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Log warning if using default credentials
        if ("admin".equals(adminUsername) && "admin123".equals(adminPassword)) {
            log.warn("Using default admin credentials (admin/admin123). Please set ADMIN_USERNAME and ADMIN_PASSWORD environment variables for production.");
        }

        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build();

        log.info("Configured admin user: {}", adminUsername);
        
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}