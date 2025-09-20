

package tmtd.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService = username -> null;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // FE localhost:3000 hoặc domain thật của bạn
        cfg.setAllowedOrigins(java.util.List.of("http://localhost:3000"));
        cfg.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // PHẢI cho phép Authorization để Bearer token qua được
        cfg.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));
        // Nếu bạn không dùng cookie thì để false là an toàn
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh.authenticationEntryPoint((req, resp, ex) -> {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write("{\"error\":\"unauthorized\",\"message\":\""
                            + ex.getMessage() + "\",\"path\":\"" + req.getRequestURI() + "\"}");
                }))
                .authorizeHttpRequests(auth -> auth
                        // Preflight
                        .requestMatchers("/error", "/error/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC API
                        .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/feedback/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/review/**").permitAll()
                        // ✅ mở endpoint test mail (GET hoặc POST đều được vì bạn đã disable CSRF)
                        .requestMatchers("/test-mail").permitAll()
                        .requestMatchers("/ws/**", "/topic/**", "/app/**").permitAll()

                        // WS & Broker (mở công khai, có thể thêm interceptor nếu cần auth)
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/topic/**", "/app/**").permitAll()

                        // QR
                        .requestMatchers(HttpMethod.POST, "/api/qr/issue")
                        .hasAnyRole(Roles.USER, Roles.ORGANIZER, Roles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/qr/redeem")
                        .hasAnyRole(Roles.ORGANIZER, Roles.ADMIN)

                        // Admin zone
                        .requestMatchers("/api/admin/**").hasRole(Roles.ADMIN)

                        // Organizer zone
                        .requestMatchers("/api/organizer/**")
                        .hasAnyRole(Roles.ADMIN, Roles.ORGANIZER)

                        // Chặn POST /api/events
                        .requestMatchers(HttpMethod.POST, "/api/events").denyAll()

                        // Đăng ký
                        .requestMatchers(HttpMethod.POST, "/api/registrations").hasRole(Roles.USER)
                        .requestMatchers("/api/registrations/**").authenticated()

                        // Còn lại yêu cầu JWT
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setPasswordEncoder(encoder);
        p.setUserDetailsService(userDetailsService);
        return new ProviderManager(p);
    }

}
