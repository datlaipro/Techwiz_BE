// package tmtd.event.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.ProviderManager;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.config.Customizer;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// import lombok.RequiredArgsConstructor;

// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final tmtd.event.config.JwtFilter jwtFilter;
//     private final UserDetailsService userDetailsService = username -> null;

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         return http
//                 .csrf(csrf -> csrf.disable())
//                 .cors(Customizer.withDefaults()) // dùng CorsConfigurationSource từ CorsConfig
//                 .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight
//                         // Public
//                         .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()     // ✅ THÊM DÒNG NÀY
//                         .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/feedback/**").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/review/**").permitAll()
//                         // Role-based
//                         .requestMatchers("/api/admin/**").hasRole(Roles.ADMIN)
//                         .requestMatchers(HttpMethod.POST, "/api/events").hasRole(Roles.ADMIN)

//                         .requestMatchers("/api/organizer/**").hasRole(Roles.ORGANIZER)
//                         .requestMatchers("/api/registrations/**").hasRole(Roles.USER)
//                         // Còn lại yêu cầu JWT
//                         .anyRequest().authenticated())
//                 .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//                 .build();
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public AuthenticationManager authenticationManager(PasswordEncoder encoder) {
//         DaoAuthenticationProvider p = new DaoAuthenticationProvider();
//         p.setPasswordEncoder(encoder);
//         p.setUserDetailsService(userDetailsService);
//         return new ProviderManager(p);
//     }
// }







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

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    // Không dùng form-login, chỉ để thỏa bean phụ thuộc
    private final UserDetailsService userDetailsService = username -> null;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults()) // dùng CorsConfigurationSource từ CorsConfig
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh.authenticationEntryPoint((req, resp, ex) -> {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"error\":\"unauthorized\",\"message\":\""
                        + ex.getMessage() + "\",\"path\":\"" + req.getRequestURI() + "\"}");
            }))
            .authorizeHttpRequests(auth -> auth
                // Preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // PUBLIC
                .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/events/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/feedback/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/review/**").permitAll()

                // QR: issue cần USER/ORGANIZER/ADMIN; redeem khóa cho ORGANIZER/ADMIN
                .requestMatchers(HttpMethod.POST, "/api/qr/issue")
                    .hasAnyRole(Roles.USER, Roles.ORGANIZER, Roles.ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/qr/redeem")
                    .hasAnyRole(Roles.ORGANIZER, Roles.ADMIN)

                // Admin zone
                .requestMatchers("/api/admin/**").hasRole(Roles.ADMIN)

                // Organizer zone (cho cả ADMIN)
                .requestMatchers("/api/organizer/**")
                    .hasAnyRole(Roles.ADMIN, Roles.ORGANIZER)

                // Events tạo mới: tuỳ policy của bạn, ở đây để ADMIN tạo
                .requestMatchers(HttpMethod.POST, "/api/events").hasRole(Roles.ADMIN)

                // Registrations: POST phải là USER; các API còn lại dưới /api/registrations/** chỉ cần authenticated
                .requestMatchers(HttpMethod.POST, "/api/registrations").hasRole(Roles.USER)
                .requestMatchers("/api/registrations/**").authenticated()

                // Các route còn lại yêu cầu JWT
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager “no-op” (không dùng form-login), để thỏa phụ thuộc
    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setPasswordEncoder(encoder);
        p.setUserDetailsService(userDetailsService);
        return new ProviderManager(p);
    }
}
