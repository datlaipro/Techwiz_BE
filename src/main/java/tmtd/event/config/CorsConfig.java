// package tmtd.event.config;

// import java.time.Duration;
// import java.util.List;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// @Configuration
// public class CorsConfig {

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration cfg = new CorsConfiguration();
//         cfg.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
//         cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
//         cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Accept"));
//         cfg.setAllowCredentials(true);
//         cfg.setMaxAge(Duration.ofHours(1));

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/api/**", cfg);
//         return source;
//     }
// }

package tmtd.event.config;

import java.time.Duration;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(Duration.ofHours(1));
        cfg.setAllowedHeaders(java.util.List.of("Authorization", "authorization", "Content-Type", "Accept", "Origin"));// cho
                                                                                                                       // phép
                                                                                                                       // lowercase:

        cfg.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        source.registerCorsConfiguration("/ws/**", cfg); // ✅ Mở CORS cho WebSocket
        return source;
    }
}
