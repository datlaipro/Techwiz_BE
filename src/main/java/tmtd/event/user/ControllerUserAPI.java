package tmtd.event.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmtd.event.user.dto.UserCreateRequest;
import tmtd.event.user.dto.UserResponse;

import java.net.URI;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ControllerUserAPI {

    private final ServiceUser serviceUser;

    /**
     * PUBLIC REGISTER -> luôn ROLE_USER
     * Yêu cầu:
     *  - email: trim + lowercase
     *  - roles: luôn để service set (không nhận từ client)
     *  - password: service tự encode
     *  - duplicate email: service ném EmailAlreadyUsedException -> GlobalExceptionHandler trả 409
     */
    @PostMapping(
        value = "/register",
        consumes = "application/json",
        produces = "application/json"
    )
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest req) {

        // Chuẩn hóa input
        final String email = req.email() == null ? null : req.email().trim().toLowerCase();
        final String fullName = req.fullName() == null ? null : req.fullName().trim();
        final String rawPassword = req.password(); // không log, service sẽ encode

        // Map DTO -> Entity (KHÔNG set roles từ client)
        EntityUser u = new EntityUser();
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPassword(rawPassword);
        u.setRoles(null); // để ServiceUser tự set "ROLE_USER"

        // Lưu (service phải: existsByEmailIgnoreCase + bắt DataIntegrityViolationException)
        var saved = serviceUser.saveEntityUser(u);

        // Entity id hiện là Integer -> convert sang Long cho DTO
        Long uid = (saved.getUser_id() == null) ? null : Long.valueOf(saved.getUser_id());

        // Trả 201 Created + Location header
        var body = new UserResponse(uid, saved.getEmail(), saved.getFullName(), saved.getRoles());
        return ResponseEntity
                .created(URI.create("/api/user/" + uid))
                .body(body);
    }
}
