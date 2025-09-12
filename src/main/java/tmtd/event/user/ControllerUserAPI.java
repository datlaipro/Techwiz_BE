package tmtd.event.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmtd.event.user.dto.UserCreateRequest;
import tmtd.event.user.dto.UserResponse;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ControllerUserAPI {

    private final ServiceUser serviceUser;

    // PUBLIC REGISTER -> luôn ROLE_USER
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest req) {

        // map DTO -> Entity
        EntityUser u = new EntityUser();
        u.setEmail(req.email());
        u.setFullName(req.fullName());
        u.setPassword(req.password());       // ServiceUser sẽ encode + set ROLE_USER nếu trống
        u.setRoles(null);

        var saved = serviceUser.saveEntityUser(u);

        // Entity id của bạn đang là Integer -> convert sang Long cho DTO nếu cần
        Long uid = (saved.getUser_id() == null) ? null : Long.valueOf(saved.getUser_id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(uid, saved.getEmail(), saved.getFullName(), saved.getRoles()));
    }
}
