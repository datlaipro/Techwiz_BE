

package tmtd.event.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tmtd.event.user.dto.UserCreateRequest;
import tmtd.event.user.dto.UserResponse;
import tmtd.event.user.dto.ChangePasswordRequest; // ✅ import DTO mới
import tmtd.event.config.AuthFacade;              // ✅ để lấy ID user hiện tại

import java.net.URI;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ControllerUserAPI {

    private final ServiceUser serviceUser;
    private final AuthFacade auth; // ✅ thêm vào

    /**
     * PUBLIC REGISTER -> luôn ROLE_USER
     */
    @PostMapping(
        value = "/register",
        consumes = "application/json",
        produces = "application/json"
    )
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest req) {

        final String email = req.email() == null ? null : req.email().trim().toLowerCase();
        final String fullName = req.fullName() == null ? null : req.fullName().trim();
        final String rawPassword = req.password();

        EntityUser u = new EntityUser();
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPassword(rawPassword);
        u.setRoles(null);

        var saved = serviceUser.saveEntityUser(u);

        Long uid = (saved.getUser_id() == null) ? null : Long.valueOf(saved.getUser_id());

        var body = new UserResponse(uid, saved.getEmail(), saved.getFullName(), saved.getRoles());
        return ResponseEntity
                .created(URI.create("/api/user/" + uid))
                .body(body);
    }

    /**
     * CHANGE PASSWORD cho user hiện tại
     */
    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(@Valid @RequestBody ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().build();
        }
        Long uid = auth.currentUserId();
        serviceUser.changePassword(uid, req.getOldPassword(), req.getNewPassword());
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
