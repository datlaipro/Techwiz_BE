
package tmtd.event.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tmtd.event.user.EntityUser;
import tmtd.event.user.ServiceUser;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private ServiceUser serviceUser;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.debug("🔍 Đang xử lý đăng nhập...");
        logger.debug("📩 Email đăng nhập: {}", loginRequest.getEmail());
        // Không ghi log mật khẩu để tránh rủi ro bảo mật

        Optional<EntityUser> user = serviceUser.findByEmail(loginRequest.getEmail());

        if (user.isPresent()) {
            logger.debug("✅ User tồn tại: {}", user.get().getEmail());

            if (serviceUser.checkPassword(loginRequest.getPassword(), user.get().getPassword())) {
                String token = jwtUtil.generateToken(user.get().getEmail(), user.get().getRoles(), user.get().getId()); // Truyền String roles và userId
                logger.debug("🔑 Token đã tạo: {}", token);
                return ResponseEntity.ok(new AuthResponse(token, user.get().getId(), user.get().getEmail()));
            } else {
                logger.warn("❌ Mật khẩu không khớp!");
            }
        } else {
            logger.warn("❌ Không tìm thấy user với email: {}", loginRequest.getEmail());
        }

        return ResponseEntity.status(401).body("Email hoặc mật khẩu không đúng!");
    }

}

// Không cần định nghĩa LoginRequest ở đây nữa!
// Đã tách ra file riêng: src/main/java/lee/engback/auth/LoginRequest.java

class AuthResponse {
    private String token;
    private Long userId;
    private String email;

    public AuthResponse(String token, Long userId, String email) {
        this.token = token;
        this.userId = userId;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}

// Cách hoạt động:

// Người dùng gửi email & password.
// Nếu đúng → Hệ thống trả về JWT Token, userId, email.
// Nếu sai → Báo lỗi 401 Unauthorized.