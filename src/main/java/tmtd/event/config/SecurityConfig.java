

// package tmtd.event.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.ProviderManager;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// import lombok.RequiredArgsConstructor;

// @Configuration
// @EnableMethodSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final tmtd.event.config.JwtFilter jwtFilter; // dùng lại JwtFilter của bạn (đã cập nhật phía dưới)
//     private final UserDetailsService userDetailsService = username -> null; // không dùng form-login

//     @Bean
//     SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(csrf -> csrf.disable())
//             .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//             .authorizeHttpRequests(auth -> auth
//                 // PUBLIC – khách xem nội dung
//                 .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
//                 .requestMatchers(HttpMethod.POST, "/api/user").permitAll() // đăng ký -> luôn ROLE_USER
//                 .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
//                 .requestMatchers(HttpMethod.GET, "/api/feedback/**").permitAll()
//                 .requestMatchers(HttpMethod.GET, "/api/review/**").permitAll()

//                 // Admin zone
//                 .requestMatchers("/api/admin/**").hasRole(Roles.ADMIN)

//                 // Organizer zone
//                 .requestMatchers("/api/organizer/**").hasRole(Roles.ORGANIZER)

//                 // Chỉ USER mới được đăng ký sự kiện
//                 .requestMatchers("/api/registrations/**").hasRole(Roles.USER)

//                 // Các route còn lại đòi JWT
//                 .anyRequest().authenticated()
//             );

//         http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//         return http.build();
//     }

//     @Bean PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     // Chặn Spring cố dùng auth truyền thống (không cần thiết, nhưng để đủ bean)
//     @Bean AuthenticationManager authenticationManager(PasswordEncoder encoder) {
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter; // dùng lại JwtFilter của bạn
    private final UserDetailsService userDetailsService = username -> null; // không dùng form-login

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // .authorizeHttpRequests(auth -> auth
            //     // PUBLIC
            //     .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
            //     .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
            //     .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
            //     .requestMatchers(HttpMethod.GET, "/api/feedback/**").permitAll()
            //     .requestMatchers(HttpMethod.GET, "/api/review/**").permitAll()

            //     // Admin zone
            //     .requestMatchers("/api/admin/**").hasRole(Roles.ADMIN)

            //     // Organizer zone
            //     .requestMatchers("/api/organizer/**").hasRole(Roles.ORGANIZER)

            //     // Đăng ký: chỉ POST yêu cầu ROLE_USER; các API khác của /api/registrations/** chỉ cần authenticated
            //     .requestMatchers(HttpMethod.POST, "/api/registrations").hasRole(Roles.USER)
            //     .requestMatchers("/api/registrations/**").authenticated()
.authorizeHttpRequests(auth -> auth
    // PUBLIC
    .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/user").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/feedback/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/review/**").permitAll()

    // Admin zone
    .requestMatchers("/api/admin/**").hasRole(Roles.ADMIN)

    // Organizer zone (cho cả ADMIN)
    .requestMatchers("/api/organizer/**").hasAnyRole(Roles.ADMIN, Roles.ORGANIZER)

    // Registrations
    .requestMatchers(HttpMethod.POST, "/api/registrations").hasRole(Roles.USER)
    .requestMatchers("/api/registrations/**").authenticated()
                // Các route còn lại đòi JWT
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean AuthenticationManager “no-op” để thỏa phụ thuộc, không dùng form-login
    @Bean
    AuthenticationManager authenticationManager(PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setPasswordEncoder(encoder);
        p.setUserDetailsService(userDetailsService);
        return new ProviderManager(p);
    }
}
