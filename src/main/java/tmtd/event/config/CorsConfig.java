package tmtd.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // registry.addMapping("/**") // Allow CORS for all endpoints
                registry.addMapping("/api/**") // Bật CORS cho tất cả API
                        .allowedOrigins("http://localhost:3000") // Cho phép frontend tại địa chỉ này truy cập
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Các phương thức HTTP được phép
                        .allowedHeaders("*") // Cho phép tất cả các headers
                        // .allowedHeaders("Authorization", "Content-Type") // Cho phép header Authorization (chứa token JWT)
                        .allowCredentials(true); // Cho phép gửi cookie hoặc token (nếu có)
            }
        };
    }
}

//BẬT CROSS ORIGIN RESOURCE SHARING (CORS) TRONG SPRING BOOT
//Cross Origin Resource Sharing (CORS) là một cơ chế cho phép tài nguyên từ một trang web
//hoặc ứng dụng web được yêu cầu từ một tên miền khác nơi mà tài nguyên đó được tải.
//CORS cho phép các trình duyệt web yêu cầu web từ một tên miền khác ngoài tên miền mà trang web đó được tải.
// Ở ĐÂY ĐANG BẬT CHO TẤT CẢ CÁC ĐỊA CHỈ
