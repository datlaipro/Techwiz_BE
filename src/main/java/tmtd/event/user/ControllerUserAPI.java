// package tmtd.event.user;

// import java.net.URI;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import tmtd.event.user.dto.UserCreateRequest;
// import tmtd.event.user.dto.UserResponse;

// @RestController
// @RequestMapping("/api/user")
// @RequiredArgsConstructor
// public class ControllerUserAPI {

//     private final ServiceUser serviceUser;

//     /**
//      * PUBLIC REGISTER -> luôn ROLE_USER
//      * Yêu cầu:
//      *  - email: trim + lowercase
//      *  - roles: luôn để service set (không nhận từ client)
//      *  - password: service tự encode
//      *  - duplicate email: service ném EmailAlreadyUsedException -> GlobalExceptionHandler trả 409
//      */
//     @PostMapping(
//         value = "/register",
//         consumes = "application/json",
//         produces = "application/json"
//     )
//     public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest req) {

//         // Chuẩn hóa input
//         final String email = req.email() == null ? null : req.email().trim().toLowerCase();
//         final String fullName = req.fullName() == null ? null : req.fullName().trim();
//         final String rawPassword = req.password(); // không log, service sẽ encode

//         // Map DTO -> Entity (KHÔNG set roles từ client)
//         EntityUser u = new EntityUser();
//         u.setEmail(email);
//         u.setFullName(fullName);
//         u.setPassword(rawPassword);
//         u.setRoles(null); // để ServiceUser tự set "ROLE_USER"

//         // Lưu (service phải: existsByEmailIgnoreCase + bắt DataIntegrityViolationException)
//         var saved = serviceUser.saveEntityUser(u);

//         // Entity id hiện là Integer -> convert sang Long cho DTO
//         Long uid = (saved.getUser_id() == null) ? null : Long.valueOf(saved.getUser_id());

//         // Trả 201 Created + Location header
//         var body = new UserResponse(uid, saved.getEmail(), saved.getFullName(), saved.getRoles());
//         return ResponseEntity
//                 .created(URI.create("/api/user/" + uid))
//                 .body(body);
//     }
// }





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
